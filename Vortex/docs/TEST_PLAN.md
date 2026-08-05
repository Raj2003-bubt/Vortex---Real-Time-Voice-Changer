# Test Plan

## Functional tests

1. Application lists at least one input and output device.
2. Start and stop processing ten times without a crash.
3. Switch through all ten presets while speaking.
4. Verify each preset is audibly distinct.
5. Verify pitch, strength and volume controls update without restarting.
6. Verify bypass sends a near-original signal.
7. Verify mute sends silence.
8. Verify a virtual-cable output can be selected.
9. Verify Discord, Zoom or Meet can receive the processed signal.
10. Verify device refresh works while processing is stopped.

## Quality tests

- Record a sample from every preset.
- Check speech intelligibility and severe clipping.
- Measure approximate microphone-to-output delay.
- Run a 20-minute continuous processing session.
- Repeatedly switch presets and controls during the session.

## Acceptance targets

- Stable processing for an extended demonstration.
- Conversational latency, preferably below approximately 150 ms on suitable hardware.
- No application crash during repeated preset switching.
- Clearly distinguishable output from all ten presets.
