package com.bubt.voicechanger.dsp;

import com.bubt.voicechanger.model.VoicePreset;

public record EffectSettings(
        VoicePreset preset,
        double pitchSemitones,
        double strength,
        double echo,
        double reverb,
        double volume,
        boolean bypass,
        boolean mute) {

    public EffectSettings {
        preset = preset == null ? VoicePreset.DEEP : preset;
        pitchSemitones = clamp(pitchSemitones, -12.0, 12.0);
        strength = clamp(strength, 0.0, 1.0);
        echo = clamp(echo, 0.0, 1.0);
        reverb = clamp(reverb, 0.0, 1.0);
        volume = clamp(volume, 0.0, 1.5);
    }

    public static EffectSettings defaults() {
        return new EffectSettings(VoicePreset.DEEP, 0.0, 0.78, 0.25, 0.20, 1.0, false, false);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
