package com.lucianoberriosdev.mycamera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;

public class SenseSpeakActivity extends AppCompatActivity {

    private ToggleButton narratorToggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sense_speak);

        Button visualDisabilityButton = findViewById(R.id.visualDisabilityButton);
        Button auditoryDisabilityButton = findViewById(R.id.auditoryDisabilityButton);
        narratorToggleButton = findViewById(R.id.narratorToggleButton);

        visualDisabilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to BlindActivity
                Intent intent = new Intent(SenseSpeakActivity.this, BlindActivity.class);
                startActivity(intent);
            }
        });

        auditoryDisabilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle auditory disability case
            }
        });
    }
}

