# User Guide

## Starting

1. Connect a microphone or headset.
2. Launch the application with `run-windows.bat` or `mvn javafx:run`.
3. Select an input microphone.
4. Select an output device.
5. Select a voice preset.
6. Press **Start Processing**.

## Controls

- **Pitch:** Adds an extra pitch adjustment from -12 to +12 semitones.
- **Effect strength:** Mixes the processed signal with the original signal.
- **Echo:** Changes delay feedback and wet mix for echo-based presets.
- **Reverb:** Changes room/cave ambience for reverb-based presets.
- **Output volume:** Adjusts final output gain.
- **Bypass effects:** Sends the original microphone through the selected output.
- **Mute output:** Sends silence without stopping the audio engine.

## Troubleshooting

### No microphone appears

- Close other audio applications.
- Check Windows microphone privacy permissions.
- Press **Refresh Devices**.
- Try **System Default**.

### No audio in Discord/Zoom/Meet

- Confirm this application outputs to `CABLE Input`.
- Confirm the calling application uses `CABLE Output` as its microphone.
- Start processing before joining the call.

### Feedback or echo

Use headphones and do not route processed output to speakers near the active microphone.

### Audio crackles

- Close CPU-heavy applications.
- Try the system-default audio devices.
- Confirm both devices are configured for 48 kHz.
- Avoid Bluetooth headsets when low latency is required.
