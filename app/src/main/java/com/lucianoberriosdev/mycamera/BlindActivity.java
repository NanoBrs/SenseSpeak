package com.lucianoberriosdev.mycamera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class BlindActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blind);

        Button identifyColorsButton = findViewById(R.id.identifyColorsButton);
        Button identifyBillsButton = findViewById(R.id.identifyBillsButton);
        Button historyButton = findViewById(R.id.historyButton);
        Button backButton = findViewById(R.id.backButton);

        identifyColorsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to MainActivity
                Intent intent = new Intent(BlindActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Implement onClick listeners for other buttons as needed
    }
}
