package com.bubt.voicechanger.dsp;

import com.bubt.voicechanger.model.VoicePreset;

import java.util.Arrays;

/**
 * Lightweight real-time DSP processor. The effects are intentionally modular and
 * local-only; no voice data is uploaded or sent to a remote service.
 */
public final class AudioEffectProcessor {
    private final float sampleRate;
    private final PitchShifter pitchShifter;
    private final float[] delayBuffer;
    private final float[][] reverbBuffers;
    private final int[] reverbIndices;

    private float[] dry = new float[0];
    private float[] wet = new float[0];
    private VoicePreset activePreset;
    private int delayIndex;
    private float lowPassState;
    private float highPassState;
    private float highPassPreviousInput;
    private double ringPhase;
    private double lfoPhase;
    private int noiseState = 0x13579BDF;

    public AudioEffectProcessor(float sampleRate) {
        this.sampleRate = sampleRate;
        this.pitchShifter = new PitchShifter(8192);
        this.delayBuffer = new float[Math.max(4096, (int) (sampleRate * 2.5f))];
        this.reverbBuffers = new float[][]{
                new float[Math.max(1, (int) (sampleRate * 0.113f))],
                new float[Math.max(1, (int) (sampleRate * 0.173f))],
                new float[Math.max(1, (int) (sampleRate * 0.257f))]
        };
        this.reverbIndices = new int[reverbBuffers.length];
    }

    public void process(float[] samples, int sampleCount, EffectSettings settings) {
        ensureCapacity(sampleCount);
        System.arraycopy(samples, 0, dry, 0, sampleCount);

        if (settings.bypass()) {
            applyOutput(samples, dry, sampleCount, settings.volume(), settings.mute());
            return;
        }

        if (activePreset != settings.preset()) {
            resetState();
            activePreset = settings.preset();
        }

        double pitchFactor = semitonesToFactor(settings.preset().basePitchSemitones()
                + settings.pitchSemitones());

        switch (settings.preset()) {
            case DEEP -> processDeep(dry, wet, sampleCount, pitchFactor);
            case BRIGHT -> processBright(dry, wet, sampleCount, pitchFactor);
            case CHILD -> processChild(dry, wet, sampleCount, pitchFactor);
            case ROBOT -> processRobot(dry, wet, sampleCount);
            case MONSTER -> processMonster(dry, wet, sampleCount, pitchFactor, settings.echo(), settings.reverb());
            case ALIEN -> processAlien(dry, wet, sampleCount, pitchFactor, settings.echo());
            case RADIO -> processRadio(dry, wet, sampleCount);
            case ECHO -> processEcho(dry, wet, sampleCount, settings.echo());
            case CAVE -> processCave(dry, wet, sampleCount, settings.reverb());
            case DEMON -> processDemon(dry, wet, sampleCount, pitchFactor, settings.echo(), settings.reverb());
        }

        float mix = (float) settings.strength();
        for (int i = 0; i < sampleCount; i++) {
            float value = dry[i] * (1.0f - mix) + wet[i] * mix;
            samples[i] = softClip(value);
        }
        applyOutput(samples, samples, sampleCount, settings.volume(), settings.mute());
    }

    private void processDeep(float[] input, float[] output, int count, double factor) {
        for (int i = 0; i < count; i++) {
            float pitched = pitchShifter.process(input[i], factor);
            float low = lowPass(pitched, 0.09f);
            output[i] = pitched * 0.72f + low * 0.52f;
        }
    }

    private void processBright(float[] input, float[] output, int count, double factor) {
        for (int i = 0; i < count; i++) {
            float pitched = pitchShifter.process(input[i], factor);
            float low = lowPass(pitched, 0.18f);
            output[i] = pitched + (pitched - low) * 0.48f;
        }
    }

    private void processChild(float[] input, float[] output, int count, double factor) {
        for (int i = 0; i < count; i++) {
            lfoPhase = wrapPhase(lfoPhase + 2.0 * Math.PI * 5.2 / sampleRate);
            double movingFactor = factor * (1.0 + 0.018 * Math.sin(lfoPhase));
            float pitched = pitchShifter.process(input[i], movingFactor);
            output[i] = pitched * (0.95f + 0.05f * (float) Math.sin(lfoPhase * 0.5));
        }
    }

    private void processRobot(float[] input, float[] output, int count) {
        for (int i = 0; i < count; i++) {
            ringPhase = wrapPhase(ringPhase + 2.0 * Math.PI * 78.0 / sampleRate);
            float metallic = input[i] * (float) Math.sin(ringPhase);
            output[i] = softClip(metallic * 2.1f) * 0.95f;
        }
    }

    private void processMonster(float[] input, float[] output, int count, double factor,
                                double echo, double reverb) {
        for (int i = 0; i < count; i++) {
            float pitched = pitchShifter.process(input[i], factor);
            float rough = softClip(pitched * 2.6f);
            float delayed = delay(rough, 0.19, 0.28 + echo * 0.38, 0.22 + echo * 0.30);
            output[i] = delayed + reverb(delayed, 0.18 + reverb * 0.38) * 0.42f;
        }
    }

    private void processAlien(float[] input, float[] output, int count, double factor, double echo) {
        for (int i = 0; i < count; i++) {
            lfoPhase = wrapPhase(lfoPhase + 2.0 * Math.PI * 2.7 / sampleRate);
            ringPhase = wrapPhase(ringPhase + 2.0 * Math.PI * 31.0 / sampleRate);
            double movingFactor = factor * (1.0 + 0.11 * Math.sin(lfoPhase));
            float pitched = pitchShifter.process(input[i], movingFactor);
            float tremolo = pitched * (0.72f + 0.28f * (float) Math.sin(lfoPhase * 1.7));
            float metallic = tremolo + tremolo * (float) Math.sin(ringPhase) * 0.32f;
            output[i] = delay(metallic, 0.072, 0.18 + echo * 0.18, 0.22 + echo * 0.18);
        }
    }

    private void processRadio(float[] input, float[] output, int count) {
        for (int i = 0; i < count; i++) {
            float highPassed = highPass(input[i], 0.94f);
            float bandLimited = lowPass(highPassed, 0.23f);
            float compressed = (float) Math.tanh(bandLimited * 3.4f) * 0.76f;
            output[i] = compressed + nextNoise() * 0.012f;
        }
    }

    private void processEcho(float[] input, float[] output, int count, double echo) {
        double feedback = 0.22 + echo * 0.58;
        double wetMix = 0.25 + echo * 0.55;
        for (int i = 0; i < count; i++) {
            output[i] = delay(input[i], 0.235, feedback, wetMix);
        }
    }

    private void processCave(float[] input, float[] output, int count, double reverbAmount) {
        double amount = 0.32 + reverbAmount * 0.56;
        for (int i = 0; i < count; i++) {
            float low = lowPass(input[i], 0.12f);
            float spacious = reverb(low, amount);
            output[i] = input[i] * 0.56f + spacious * 0.88f;
        }
    }

    private void processDemon(float[] input, float[] output, int count, double factor,
                              double echo, double reverbAmount) {
        for (int i = 0; i < count; i++) {
            float pitched = pitchShifter.process(input[i], factor);
            float distorted = softClip(pitched * 3.8f);
            float layered = delay(distorted, 0.145, 0.35 + echo * 0.42, 0.34 + echo * 0.34);
            float ambience = reverb(layered, 0.35 + reverbAmount * 0.55);
            output[i] = softClip(layered * 0.86f + ambience * 0.62f);
        }
    }

    private float delay(float input, double seconds, double feedback, double wetMix) {
        int delaySamples = Math.max(1, Math.min(delayBuffer.length - 1, (int) (seconds * sampleRate)));
        int readIndex = delayIndex - delaySamples;
        if (readIndex < 0) {
            readIndex += delayBuffer.length;
        }
        float delayed = delayBuffer[readIndex];
        delayBuffer[delayIndex] = softClip(input + delayed * (float) Math.min(0.88, feedback));
        delayIndex++;
        if (delayIndex == delayBuffer.length) {
            delayIndex = 0;
        }
        return input * (float) (1.0 - Math.min(0.9, wetMix)) + delayed * (float) Math.min(0.9, wetMix);
    }

    private float reverb(float input, double amount) {
        float sum = 0.0f;
        float feedback = (float) Math.min(0.82, 0.32 + amount * 0.52);
        for (int i = 0; i < reverbBuffers.length; i++) {
            float[] buffer = reverbBuffers[i];
            int index = reverbIndices[i];
            float delayed = buffer[index];
            buffer[index] = softClip(input + delayed * feedback * (0.92f - i * 0.08f));
            reverbIndices[i] = (index + 1) % buffer.length;
            sum += delayed;
        }
        return sum / reverbBuffers.length;
    }

    private float lowPass(float input, float coefficient) {
        lowPassState += coefficient * (input - lowPassState);
        return lowPassState;
    }

    private float highPass(float input, float coefficient) {
        highPassState = coefficient * (highPassState + input - highPassPreviousInput);
        highPassPreviousInput = input;
        return highPassState;
    }

    private float nextNoise() {
        noiseState = noiseState * 1664525 + 1013904223;
        return ((noiseState >>> 8) / (float) 0x00FFFFFF) * 2.0f - 1.0f;
    }

    private void applyOutput(float[] destination, float[] source, int count, double volume, boolean mute) {
        float gain = mute ? 0.0f : (float) volume;
        for (int i = 0; i < count; i++) {
            destination[i] = softClip(source[i] * gain);
        }
    }

    private void ensureCapacity(int sampleCount) {
        if (dry.length < sampleCount) {
            dry = new float[sampleCount];
            wet = new float[sampleCount];
        }
    }

    private void resetState() {
        Arrays.fill(delayBuffer, 0.0f);
        for (float[] buffer : reverbBuffers) {
            Arrays.fill(buffer, 0.0f);
        }
        Arrays.fill(reverbIndices, 0);
        delayIndex = 0;
        lowPassState = 0.0f;
        highPassState = 0.0f;
        highPassPreviousInput = 0.0f;
        ringPhase = 0.0;
        lfoPhase = 0.0;
        pitchShifter.reset();
    }

    private static double semitonesToFactor(double semitones) {
        return Math.max(0.48, Math.min(1.95, Math.pow(2.0, semitones / 12.0)));
    }

    private static double wrapPhase(double phase) {
        return phase >= Math.PI * 2.0 ? phase - Math.PI * 2.0 : phase;
    }

    private static float softClip(float value) {
        return (float) Math.tanh(value);
    }

    private static final class PitchShifter {
        private final float[] buffer;
        private int writeIndex;
        private double phase;

        private PitchShifter(int size) {
            this.buffer = new float[size];
        }

        private float process(float input, double factor) {
            buffer[writeIndex] = input;
            if (Math.abs(factor - 1.0) < 0.008) {
                advance();
                return input;
            }

            int window = Math.min(2048, buffer.length / 2);
            phase += Math.abs(factor - 1.0) / window;
            phase -= Math.floor(phase);

            double phaseA = phase;
            double phaseB = (phase + 0.5) % 1.0;
            double delayA = delayForPhase(phaseA, factor, window);
            double delayB = delayForPhase(phaseB, factor, window);
            float sampleA = readInterpolated(writeIndex - delayA);
            float sampleB = readInterpolated(writeIndex - delayB);

            double weightA = triangularWindow(phaseA);
            double weightB = triangularWindow(phaseB);
            double total = Math.max(0.0001, weightA + weightB);
            float result = (float) ((sampleA * weightA + sampleB * weightB) / total);
            advance();
            return result;
        }

        private double delayForPhase(double p, double factor, int window) {
            double baseDelay = 96.0;
            return factor > 1.0
                    ? baseDelay + (1.0 - p) * window
                    : baseDelay + p * window;
        }

        private float readInterpolated(double position) {
            while (position < 0) {
                position += buffer.length;
            }
            while (position >= buffer.length) {
                position -= buffer.length;
            }
            int i0 = (int) position;
            int i1 = (i0 + 1) % buffer.length;
            double fraction = position - i0;
            return (float) (buffer[i0] * (1.0 - fraction) + buffer[i1] * fraction);
        }

        private static double triangularWindow(double p) {
            return 1.0 - Math.abs(2.0 * p - 1.0);
        }

        private void advance() {
            writeIndex = (writeIndex + 1) % buffer.length;
        }

        private void reset() {
            Arrays.fill(buffer, 0.0f);
            writeIndex = 0;
            phase = 0.0;
        }
    }
}
