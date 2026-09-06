package com.harborvoice.platform.speech;

import com.harborvoice.platform.media.MediaEnvelope;

public interface SpeechToTextPort {
    Transcript accept(MediaEnvelope audio);

    record Transcript(String text, boolean finalText, long sequence, long epoch) {
        public Transcript {
            if (text == null || text.length() > 16000 || sequence < 0 || epoch < 0) {
                throw new IllegalArgumentException("invalid transcript");
            }
        }
    }
}
