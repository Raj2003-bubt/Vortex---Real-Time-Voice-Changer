package com.bubt.voicechanger.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.List;

public final class AudioDeviceService {
    private AudioDeviceService() {
    }

    public static List<AudioDevice> inputDevices(AudioFormat format) {
        return findDevices(new DataLine.Info(TargetDataLine.class, format));
    }

    public static List<AudioDevice> outputDevices(AudioFormat format) {
        return findDevices(new DataLine.Info(SourceDataLine.class, format));
    }

    private static List<AudioDevice> findDevices(DataLine.Info lineInfo) {
        List<AudioDevice> devices = new ArrayList<>();
        devices.add(AudioDevice.systemDefault());

        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            if (mixer.isLineSupported(lineInfo)) {
                devices.add(AudioDevice.of(mixerInfo));
            }
        }
        return devices;
    }
}
