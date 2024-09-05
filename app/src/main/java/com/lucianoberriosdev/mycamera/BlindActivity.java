package com.lucianoberriosdev.mycamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;

public class BlindActivity extends AppCompatActivity {

    private ToggleButton narratorToggleButton;
    private TextView mainTextView;
    private NarratorManager narratorManager;

    private static final String TAG = "BlindActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blind);

        // Initialize NarratorManager
        narratorManager = NarratorManager.getInstance(this);

        // Find UI elements
        narratorToggleButton = findViewById(R.id.narratorToggleButton);
        mainTextView = findViewById(R.id.titleTextView); // Cambiado a titleTextView para que lea el título
        Button colorButton = findViewById(R.id.colorIdentificationButton); // Cambiado para que coincida con el id en XML
        Button currencyButton = findViewById(R.id.identifyBillsButton); // Cambiado para que coincida con el id en XML
        Button historyButton = findViewById(R.id.historyButton);
        Button backButton = findViewById(R.id.backButton); // Añadido para el botón de volver atrás

        // Load narrator state from preferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isNarratorEnabled = prefs.getBoolean("narratorEnabled", false);
        narratorToggleButton.setChecked(isNarratorEnabled);

        // Log the narrator state
        Log.d(TAG, "Estado del narrador: " + (isNarratorEnabled ? "Activado" : "Desactivado"));

        // Enable or disable narrator based on the state
        if (isNarratorEnabled) {
            narratorManager.enableNarrator();
            // Leer el título y las opciones disponibles
            speak("Narrador activado. Opciones disponibles: Identificación de colores, Identificación de billetes, Ver historial, Volver atrás.");
        } else {
            narratorManager.disableNarrator();
            speak("Narrador desactivado");
        }

        // Configure ToggleButton listener
        narratorToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                narratorManager.enableNarrator();
                Log.d(TAG, "Activado Estado del narrador: " + (isChecked ? "Activado" : "Desactivado"));
                speak("Narrador activado. Opciones disponibles: Identificación de colores, Identificación de billetes, Ver historial, Volver atrás.");
            } else {
                narratorManager.disableNarrator();
                Log.d(TAG, "Desactivado Estado del narrador: " + (isChecked ? "Activado" : "Desactivado"));
                speak("Narrador desactivado");
            }
            saveNarratorState(isChecked);
        });

        // Configure button listeners
        colorButton.setOnClickListener(v -> {
            Intent intent = new Intent(BlindActivity.this, MainActivity.class);
            startActivity(intent);
        });

        currencyButton.setOnClickListener(v -> {
            Intent intent = new Intent(BlindActivity.this, MainActivity.class);
            startActivity(intent);
        });

        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(BlindActivity.this, SenseSpeakActivity.class);
            startActivity(intent);
        });

        // Configurar botón Volver Atrás
        backButton.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y vuelve a la anterior
        });
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
            speak("Narrador activado. Opciones disponibles: Identificación de colores, Identificación de billetes, Ver historial, Volver atrás.");
        }
    }

}
