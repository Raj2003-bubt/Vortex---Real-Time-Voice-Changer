package com.bubt.voicechanger.model;

public enum VoicePreset {
    DEEP("Deep Voice", "Lower pitch with warm bass emphasis", -4.5),
    BRIGHT("Bright / Female-Style", "Higher pitch with a brighter tone", 4.0),
    CHILD("Child Voice", "High pitch with playful tonal movement", 7.0),
    ROBOT("Robot Voice", "Metallic ring modulation and controlled distortion", 0.0),
    MONSTER("Monster Voice", "Low pitch, rough distortion, echo and reverb", -7.0),
    ALIEN("Alien Voice", "Pitch movement, tremolo, filtering and short delay", 1.5),
    RADIO("Radio Voice", "Band-limited compressed walkie-talkie sound", 0.0),
    ECHO("Echo Voice", "Adjustable repeating reflections", 0.0),
    CAVE("Cave Voice", "Long spacious reverb with low-frequency weight", -1.0),
    DEMON("Demon Voice", "Very low pitch, distortion and layered ambience", -9.0);

    private final String displayName;
    private final String description;
    private final double basePitchSemitones;

    VoicePreset(String displayName, String description, double basePitchSemitones) {
        this.displayName = displayName;
        this.description = description;
        this.basePitchSemitones = basePitchSemitones;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public double basePitchSemitones() {
        return basePitchSemitones;
    }
}
