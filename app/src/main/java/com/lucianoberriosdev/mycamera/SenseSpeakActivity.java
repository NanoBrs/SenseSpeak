package com.lucianoberriosdev.mycamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // Importa la biblioteca de logging
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;

public class SenseSpeakActivity extends AppCompatActivity {

    private ToggleButton narratorToggleButton;
    private TextView mainTextView;
    private NarratorManager narratorManager;

    private static final String TAG = "SenseSpeakActivity"; // Define TAG como public

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sense_speak);

        // Initialize NarratorManager
        narratorManager = NarratorManager.getInstance(this);

        // Find UI elements
        narratorToggleButton = findViewById(R.id.narratorToggleButton);
        mainTextView = findViewById(R.id.questionTextView);
        Button visualDisabilityButton = findViewById(R.id.visualDisabilityButton);
        Button auditoryDisabilityButton = findViewById(R.id.auditoryDisabilityButton);

        // Load narrator state from preferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isNarratorEnabled = prefs.getBoolean("narratorEnabled", false);
        narratorToggleButton.setChecked(isNarratorEnabled);

        // Log the narrator state
        Log.d(TAG, "Estado inicial del narrador: " + (isNarratorEnabled ? "Activado" : "Desactivado"));

        // Enable or disable narrator based on the state
        if (isNarratorEnabled) {
            narratorManager.enableNarrator();
            speak("Narrador activado, SenseSpeak");
        } else {
            narratorManager.disableNarrator();
            speak("Narrador desactivado");
        }

        // Configure ToggleButton listener
        narratorToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                narratorManager.enableNarrator();
                Log.d(TAG, "Activado Estado del narrador: " + (isChecked ? "Activado" : "Desactivado"));
                speak("Narrador activado, ¿Cuál es tu discapacidad? Uno Discapacidad visual, dos Discapacidad auditiva");
            } else {
                narratorManager.disableNarrator();
                Log.d(TAG, "Desactivado Estado del narrador: " + (isChecked ? "Activado" : "Desactivado"));
                speak("Narrador desactivado");
            }
            saveNarratorState(isChecked);
        });

        // Configure button listeners
        visualDisabilityButton.setOnClickListener(v -> {
            Intent intent = new Intent(SenseSpeakActivity.this, BlindActivity.class);
            startActivity(intent);
        });

        auditoryDisabilityButton.setOnClickListener(v -> {
            Intent intent = new Intent(SenseSpeakActivity.this, DeafActivity.class);
            startActivity(intent);
        });

        // Speak main text if narrator is enabled
        if (narratorManager.isNarratorEnabled()) {
            speak("Discapacidad Auditiva");
        }
    }

    private void saveNarratorState(boolean isEnabled) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("narratorEnabled", isEnabled);
        editor.apply();
    }

    private void speak(String text) {
        if (narratorManager.isNarratorEnabled()) {
            narratorManager.speak(text);
        }
    }
    protected void onResume() {
        super.onResume();

        // Verifica si el narrador está activado
        boolean isNarratorEnabled = narratorManager.isNarratorEnabled();

        // Actualiza el estado del ToggleButton
        narratorToggleButton.setChecked(isNarratorEnabled);

        // Lee el texto si el narrador está activado
        if (isNarratorEnabled) {
            speak("Menu Principal, ¿Cuál es tu discapacidad? Uno Discapacidad visual, dos Discapacidad auditiva");
        }
    }

}
