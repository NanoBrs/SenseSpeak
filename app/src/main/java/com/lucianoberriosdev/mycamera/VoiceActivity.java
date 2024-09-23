package com.lucianoberriosdev.mycamera;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private TextView voiceResultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_to_text); // Asegúrate de que el nombre del layout XML sea correcto

        voiceResultTextView = findViewById(R.id.voiceResultTextView);
        Button startVoiceRecognitionButton = findViewById(R.id.startVoiceRecognitionButton);
        Button historyButton = findViewById(R.id.historyButton);
        Button backButton = findViewById(R.id.backButton);

        // Iniciar reconocimiento de voz
        startVoiceRecognitionButton.setOnClickListener(v -> startVoiceRecognition());

        // Ir a la actividad de Historial
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(VoiceActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Volver atrás
        backButton.setOnClickListener(v -> onBackPressed());
    }

    // Método para iniciar el reconocimiento de voz
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX");  // Para español de México
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Reconocimiento de voz no soportado en este dispositivo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            voiceResultTextView.setText(result.get(0));
        }
    }
}
