# Vortex — Real-Time Voice Changer

> Academic team project. Original repository: [Sakib-183/Vortex---Real-Time-Voice-Changer](https://github.com/Sakib-183/Vortex---Real-Time-Voice-Changer). Contributor roles and publication notes: [docs/PORTFOLIO_NOTES.md](docs/PORTFOLIO_NOTES.md). This is DSP, **not an AI voice-cloning system**.

A Java-based real-time microphone processing application with a polished JavaFX interface, selectable input/output devices, live level meters, waveform monitoring, and ten distinct voice presets.

## Included presets

1. Deep Voice
2. Bright / Female-Style Voice
3. Child Voice
4. Robot Voice
5. Monster Voice
6. Alien Voice
7. Radio Voice
8. Echo Voice
9. Cave Voice
10. Demon Voice

The Bright / Female-Style option is an effect-based transformation. It does not claim perfect identity or gender conversion.

## Main features

- Live microphone capture using the Java Sound API
- 48 kHz, 16-bit PCM, mono processing
- Approximately 512-sample application buffer
- Selectable physical microphone and output device
- Real-time switching between ten presets
- Pitch, strength, echo, reverb and volume controls
- Bypass and mute controls
- Input/output level meters and waveform display
- Local-only processing; audio is not uploaded
- VB-CABLE-compatible virtual microphone routing

## Requirements

- Windows 10 or Windows 11
- JDK 17 or JDK 21
- Apache Maven 3.8+
- A microphone and headphones
- Optional: VB-CABLE or an equivalent virtual audio cable

## Run the application

### Easy Windows method

Double-click `run-windows.bat`.

### Command line

```bash
mvn clean javafx:run
```

Maven downloads JavaFX automatically on the first run.

## Build

```bash
mvn clean package
```

Or double-click `build-windows.bat`.

To create a self-contained Windows runtime image on a Windows PC, double-click `package-windows.bat`. The launcher will be created at `target\vortex\bin\vortex.bat`.

## Virtual microphone setup

1. Install VB-CABLE.
2. Open this application.
3. Select your physical microphone as **Input microphone**.
4. Select **CABLE Input** as **Processed output / virtual cable**.
5. Press **Start Processing**.
6. In Discord, Zoom, Google Meet or a game, select **CABLE Output** as the microphone.
7. Use headphones to prevent feedback.

## Project structure

```text
src/main/java/com/bubt/voicechanger/
├── Main.java
├── audio/
│   ├── AudioDevice.java
│   ├── AudioDeviceService.java
│   ├── AudioEngine.java
│   └── PcmCodec.java
├── dsp/
│   ├── AudioEffectProcessor.java
│   └── EffectSettings.java
├── model/
│   └── VoicePreset.java
└── ui/
    ├── VoiceChangerApp.java
    └── WaveformView.java
```

## Important notes

- Real-time latency depends on the selected audio drivers, hardware and other applications using the devices.
- Close applications that exclusively lock the selected microphone or output device.
- The effect algorithms are lightweight DSP implementations designed for an academic demonstration, not studio-grade AI voice conversion.
- If a selected device rejects 48 kHz mono PCM, choose **System Default** or configure the device to 48 kHz in Windows Sound settings.

## Smoke test (pure Java, no audio hardware)

On macOS/Linux with JDK 17+:

```bash
bash tests/run-smoke-test.sh
```

This checks PCM conversion, all ten presets, and mute logic. The JavaFX user interface, Windows scripts and live microphone/virtual audio routing still need testing on a Windows machine.

See the `docs` folder for architecture, testing and team responsibilities.
