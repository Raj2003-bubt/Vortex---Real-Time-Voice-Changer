# Software Architecture

## Presentation layer

`VoiceChangerApp` builds the JavaFX interface, handles device selection, preset selection, controls, status messages and virtual-routing guidance.

## Control layer

The UI creates immutable `EffectSettings` values and sends them to `AudioEngine`. Settings are stored atomically so the audio thread can read them safely without blocking.

## Audio engine

`AudioEngine` uses:

- `TargetDataLine` for microphone capture
- a dedicated high-priority audio thread
- 512-sample processing blocks
- `SourceDataLine` for physical or virtual output
- live peak-level and waveform snapshots for the UI

## Effect layer

`AudioEffectProcessor` provides ten effect paths using reusable DSP components:

- delay-line pitch shifting
- one-pole low-pass and high-pass filtering
- ring modulation
- tremolo and pitch modulation
- compression-style soft clipping
- feedback delay
- multi-tap reverb
- local deterministic noise for the radio preset

## Routing layer

The output may be a speaker/headphone device or a virtual cable. With VB-CABLE, the Java application writes to `CABLE Input`, and the communication application reads from `CABLE Output`.
