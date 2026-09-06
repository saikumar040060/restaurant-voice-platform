package com.harborvoice.platform.speech;

public interface TextToSpeechPort {
    AudioSynthesis synthesize(String text, long epoch);

    record AudioSynthesis(String codec, byte[] payload, long epoch) {
        public AudioSynthesis {
            if (codec == null || codec.isBlank() || payload == null || payload.length > 64 * 1024 || epoch < 0) {
                throw new IllegalArgumentException("invalid synthesized audio");
            }
            payload = payload.clone();
        }
        @Override public byte[] payload() { return payload.clone(); }
    }
}
