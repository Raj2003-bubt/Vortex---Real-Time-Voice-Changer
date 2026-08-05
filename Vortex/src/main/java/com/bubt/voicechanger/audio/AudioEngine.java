package com.bubt.voicechanger.audio;

import com.bubt.voicechanger.dsp.AudioEffectProcessor;
import com.bubt.voicechanger.dsp.EffectSettings;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class AudioEngine implements AutoCloseable {
    public static final float SAMPLE_RATE = 48_000.0f;
    public static final int BUFFER_SAMPLES = 512;
    public static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private final AtomicReference<EffectSettings> settings = new AtomicReference<>(EffectSettings.defaults());
    private final AudioEffectProcessor processor = new AudioEffectProcessor(SAMPLE_RATE);

    private volatile boolean running;
    private volatile double inputLevel;
    private volatile double outputLevel;
    private volatile float[] waveform = new float[0];
    private volatile Consumer<String> statusListener = ignored -> { };

    private Thread audioThread;
    private TargetDataLine inputLine;
    private SourceDataLine outputLine;

    public synchronized void start(AudioDevice inputDevice, AudioDevice outputDevice) throws LineUnavailableException {
        if (running) {
            return;
        }
        Objects.requireNonNull(inputDevice, "inputDevice");
        Objects.requireNonNull(outputDevice, "outputDevice");

        DataLine.Info inputInfo = new DataLine.Info(TargetDataLine.class, FORMAT);
        DataLine.Info outputInfo = new DataLine.Info(SourceDataLine.class, FORMAT);

        try {
            inputLine = openInput(inputDevice, inputInfo);
            outputLine = openOutput(outputDevice, outputInfo);

            int lineBufferBytes = FORMAT.getFrameSize() * BUFFER_SAMPLES * 8;
            inputLine.open(FORMAT, lineBufferBytes);
            outputLine.open(FORMAT, lineBufferBytes);
            inputLine.start();
            outputLine.start();
        } catch (LineUnavailableException | RuntimeException ex) {
            closeLines();
            throw ex;
        }

        running = true;
        audioThread = new Thread(this::audioLoop, "voice-changer-audio");
        audioThread.setDaemon(true);
        audioThread.setPriority(Thread.MAX_PRIORITY - 1);
        audioThread.start();
        notifyStatus("Audio processing is active");
    }

    private TargetDataLine openInput(AudioDevice device, DataLine.Info info) throws LineUnavailableException {
        if (device.mixerInfo() == null) {
            return (TargetDataLine) AudioSystem.getLine(info);
        }
        Mixer mixer = AudioSystem.getMixer(device.mixerInfo());
        return (TargetDataLine) mixer.getLine(info);
    }

    private SourceDataLine openOutput(AudioDevice device, DataLine.Info info) throws LineUnavailableException {
        if (device.mixerInfo() == null) {
            return (SourceDataLine) AudioSystem.getLine(info);
        }
        Mixer mixer = AudioSystem.getMixer(device.mixerInfo());
        return (SourceDataLine) mixer.getLine(info);
    }

    private void audioLoop() {
        byte[] inputBytes = new byte[BUFFER_SAMPLES * FORMAT.getFrameSize()];
        byte[] outputBytes = new byte[inputBytes.length];
        float[] samples = new float[BUFFER_SAMPLES];
        int waveformCounter = 0;

        try {
            while (running) {
                int bytesRead = inputLine.read(inputBytes, 0, inputBytes.length);
                if (bytesRead <= 0) {
                    continue;
                }

                int sampleCount = PcmCodec.decode16BitLittleEndian(inputBytes, bytesRead, samples);
                inputLevel = smoothLevel(inputLevel, peak(samples, sampleCount));

                processor.process(samples, sampleCount, settings.get());
                outputLevel = smoothLevel(outputLevel, peak(samples, sampleCount));

                int bytesToWrite = PcmCodec.encode16BitLittleEndian(samples, sampleCount, outputBytes);
                outputLine.write(outputBytes, 0, bytesToWrite);

                if (++waveformCounter >= 3) {
                    waveform = Arrays.copyOf(samples, sampleCount);
                    waveformCounter = 0;
                }
            }
        } catch (RuntimeException ex) {
            if (running) {
                notifyStatus("Audio error: " + ex.getMessage());
            }
        } finally {
            closeLines();
            inputLevel = 0.0;
            outputLevel = 0.0;
            waveform = new float[0];
            running = false;
        }
    }

    public synchronized void stop() {
        if (!running && inputLine == null && outputLine == null) {
            return;
        }
        running = false;
        if (inputLine != null) {
            inputLine.stop();
            inputLine.flush();
        }
        closeLines();
        if (audioThread != null && audioThread != Thread.currentThread()) {
            try {
                audioThread.join(600);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        audioThread = null;
        notifyStatus("Audio processing stopped");
    }

    private synchronized void closeLines() {
        if (inputLine != null) {
            inputLine.close();
            inputLine = null;
        }
        if (outputLine != null) {
            if (outputLine.isOpen()) {
                outputLine.flush();
            }
            outputLine.close();
            outputLine = null;
        }
    }

    public void setSettings(EffectSettings newSettings) {
        settings.set(Objects.requireNonNull(newSettings));
    }

    public EffectSettings getSettings() {
        return settings.get();
    }

    public boolean isRunning() {
        return running;
    }

    public double getInputLevel() {
        return inputLevel;
    }

    public double getOutputLevel() {
        return outputLevel;
    }

    public float[] getWaveform() {
        return waveform;
    }

    public void setStatusListener(Consumer<String> listener) {
        statusListener = listener == null ? ignored -> { } : listener;
    }

    private void notifyStatus(String status) {
        statusListener.accept(status);
    }

    private static double peak(float[] samples, int count) {
        double peak = 0.0;
        for (int i = 0; i < count; i++) {
            peak = Math.max(peak, Math.abs(samples[i]));
        }
        return Math.min(1.0, peak);
    }

    private static double smoothLevel(double previous, double current) {
        return current > previous ? current : previous * 0.86 + current * 0.14;
    }

    @Override
    public void close() {
        stop();
    }
}
