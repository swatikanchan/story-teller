package com.swati.storyteller;

import android.content.Context;
import android.speech.tts.TextToSpeech;

/**
 * Created by Swati on 27-12-2016.
 */

public class TextToSpeechConverter{
    private static TextToSpeech tts;

    public static void init(final Context contxt) {
        if (tts == null) {
            tts = new TextToSpeech(contxt, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {

                }
            });
        }
    }

    public static void speak(final String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
