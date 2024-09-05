package com.lucianoberriosdev.mycamera;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import java.util.Locale;

public class NarratorManager implements OnInitListener {
    private static final String TAG = "NarratorManager";
    private static NarratorManager instance;
    private TextToSpeech tts;
    private boolean isNarratorEnabled = false;
    private Context context;

    private NarratorManager(Context context) {
        this.context = context;
        tts = new TextToSpeech(context, this);
    }

    public static synchronized NarratorManager getInstance(Context context) {
        if (instance == null) {
            instance = new NarratorManager(context);
        }
        return instance;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int langResult = tts.setLanguage(new Locale("es", "ES")); // Configura el idioma a español (España)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not supported or missing data.");
            } else {
                Log.i(TAG, "Language set to Spanish.");
            }
        } else {
            Log.e(TAG, "Initialization failed.");
        }
    }

    public void enableNarrator() {
        isNarratorEnabled = true;
    }

    public void disableNarrator() {
        isNarratorEnabled = false;
    }

    public boolean isNarratorEnabled() {
        return isNarratorEnabled;
    }

    public void speak(String text) {
        if (isNarratorEnabled && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}

