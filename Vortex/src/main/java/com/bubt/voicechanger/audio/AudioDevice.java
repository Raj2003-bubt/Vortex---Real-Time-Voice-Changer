package com.bubt.voicechanger.audio;

import javax.sound.sampled.Mixer;
import java.util.Objects;

public final class AudioDevice {
    private final Mixer.Info mixerInfo;
    private final String displayName;

    private AudioDevice(Mixer.Info mixerInfo, String displayName) {
        this.mixerInfo = mixerInfo;
        this.displayName = Objects.requireNonNull(displayName);
    }

    public static AudioDevice systemDefault() {
        return new AudioDevice(null, "System Default");
    }

    public static AudioDevice of(Mixer.Info mixerInfo) {
        String vendor = mixerInfo.getVendor();
        String suffix = vendor == null || vendor.isBlank() ? "" : " — " + vendor;
        return new AudioDevice(mixerInfo, mixerInfo.getName() + suffix);
    }

    public Mixer.Info mixerInfo() {
        return mixerInfo;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
