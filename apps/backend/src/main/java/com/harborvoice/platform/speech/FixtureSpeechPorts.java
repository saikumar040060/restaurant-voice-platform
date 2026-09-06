package com.harborvoice.platform.speech;

import com.harborvoice.platform.media.MediaEnvelope;

/** Deterministic fixtures for contract tests; no network or provider SDK. */
public final class FixtureSpeechPorts {
    private FixtureSpeechPorts() { }

    public static SpeechToTextPort stt() {
        return audio -> new SpeechToTextPort.Transcript("fixture transcript", true, audio.sequence(), audio.epoch());
    }

    public static TextToSpeechPort tts() {
        return (text, epoch) -> new TextToSpeechPort.AudioSynthesis("fixture/pcm", text.getBytes(java.nio.charset.StandardCharsets.UTF_8), epoch);
    }
}
