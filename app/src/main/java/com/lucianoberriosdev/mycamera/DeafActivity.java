package com.lucianoberriosdev.mycamera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class DeafActivity extends AppCompatActivity {

    private NarratorManager narratorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deaf);

        narratorManager = NarratorManager.getInstance(this);

        // Botón Voz a Texto
        Button voiceToTextButton = findViewById(R.id.voiceToTextButton);
        voiceToTextButton.setOnClickListener(v -> {
            Intent intent = new Intent(DeafActivity.this, VoiceActivity.class);
            startActivity(intent);
        });
        Button alarmsButton = findViewById(R.id.alarmsButton);
        alarmsButton.setOnClickListener(v -> {
            // Implementación del comportamiento de Alarmas
        });

        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(v -> {
            // Implementación del comportamiento del Historial
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // Volver a la actividad anterior
        });
    }


}
