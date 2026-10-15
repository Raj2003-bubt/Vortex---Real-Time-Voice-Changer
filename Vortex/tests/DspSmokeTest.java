import com.bubt.voicechanger.audio.PcmCodec;
import com.bubt.voicechanger.dsp.AudioEffectProcessor;
import com.bubt.voicechanger.dsp.EffectSettings;
import com.bubt.voicechanger.model.VoicePreset;

public class DspSmokeTest {
    public static void main(String[] args) {
        byte[] pcm = new byte[4096];
        float[] src = new float[2048];
        for (int i=0; i<src.length; i++) src[i] = (float)(0.5*Math.sin(2*Math.PI*220*i/48000));
        int count = PcmCodec.encode16BitLittleEndian(src, src.length, pcm);
        if (count != 4096) throw new AssertionError("PCM encode size mismatch");
        float[] decoded = new float[2048];
        if (PcmCodec.decode16BitLittleEndian(pcm, pcm.length, decoded) != 2048)
            throw new AssertionError("PCM decode sample mismatch");
        for (VoicePreset preset : VoicePreset.values()) {
            AudioEffectProcessor processor = new AudioEffectProcessor(48000f);
            EffectSettings settings = new EffectSettings(preset,0,0.8,0.3,0.3,1.0,false,false);
            for(int k=0;k<8;k++) {
                float[] samples = decoded.clone();
                processor.process(samples, samples.length, settings);
                for(float v:samples) if (!Float.isFinite(v) || v < -1.01f || v > 1.01f)
                    throw new AssertionError("Invalid output: " + preset);
            }
        }
        AudioEffectProcessor muted = new AudioEffectProcessor(48000f);
        float[] samples = src.clone();
        muted.process(samples,samples.length,new EffectSettings(VoicePreset.DEEP,0,1,0,0,1,false,true));
        for(float v:samples) if(Math.abs(v)>0.00001f) throw new AssertionError("Mute failed");
        System.out.println("PASS: PCM, all 10 presets, finite output and mute");
    }
}
