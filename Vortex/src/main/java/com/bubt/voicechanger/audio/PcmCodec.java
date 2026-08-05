package com.bubt.voicechanger.audio;

public final class PcmCodec {
    private PcmCodec() {
    }

    public static int decode16BitLittleEndian(byte[] bytes, int byteCount, float[] samples) {
        int sampleCount = Math.min(byteCount / 2, samples.length);
        for (int i = 0; i < sampleCount; i++) {
            int low = bytes[i * 2] & 0xFF;
            int high = bytes[i * 2 + 1];
            short value = (short) ((high << 8) | low);
            samples[i] = value / 32768.0f;
        }
        return sampleCount;
    }

    public static int encode16BitLittleEndian(float[] samples, int sampleCount, byte[] bytes) {
        int count = Math.min(sampleCount, Math.min(samples.length, bytes.length / 2));
        for (int i = 0; i < count; i++) {
            float clipped = Math.max(-1.0f, Math.min(1.0f, samples[i]));
            int value = Math.round(clipped * 32767.0f);
            bytes[i * 2] = (byte) (value & 0xFF);
            bytes[i * 2 + 1] = (byte) ((value >>> 8) & 0xFF);
        }
        return count * 2;
    }
}
