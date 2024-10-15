package com.lucianoberriosdev.mycamera;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Executor executor;
    private TextView colorTextView;
    private NarratorManager narratorManager;
    private FirebaseFirestore db;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        previewView = findViewById(R.id.previewView);
        colorTextView = findViewById(R.id.colorTextView);
        ToggleButton narratorToggleButton = findViewById(R.id.narratorToggleButton);

        executor = Executors.newSingleThreadExecutor();
        narratorManager = NarratorManager.getInstance(this);

        // Load narrator state from preferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isNarratorEnabled = prefs.getBoolean("narratorEnabled", false);
        narratorToggleButton.setChecked(isNarratorEnabled);

        narratorToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                narratorManager.enableNarrator();
                speak("Narrador activado");
            } else {
                narratorManager.disableNarrator();
                speak("Narrador desactivado");
            }
            // Save narrator state to preferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("narratorEnabled", isChecked);
            editor.apply();
        });

        checkPermissions();
        setUpCameraX();

        Button captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> takePhoto());
    }

    private void speak(String text) {
        if (narratorManager.isNarratorEnabled()) {
            narratorManager.speak(text);
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    100);
        }
    }

    private void setUpCameraX() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Failed to set up camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture != null) {
            File photoFile = new File(getExternalFilesDir(null), "photo_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg");

            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            imageCapture.takePicture(outputOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Log.d(TAG, "Photo capture succeeded: " + outputFileResults.getSavedUri());
                    analyzeColor(photoFile);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                }
            });
        }
    }

    private void analyzeColor(File photoFile) {
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

        if (bitmap != null) {
            int color = bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            String hexColor = String.format("#%06X", (0xFFFFFF & color));

            String colorName = identifyColor(hexColor);

            Log.d(TAG, "Dominant color detected: " + hexColor + " (" + colorName + ")");

            runOnUiThread(() -> {
                String textToSpeak = "Color: " + colorName + " (" + hexColor + ")";
                colorTextView.setText(textToSpeak);
                speak(textToSpeak);
                saveColorToFirestore(colorName, hexColor);
            });

        } else {
            Log.e(TAG, "Failed to load bitmap.");
        }
    }
    private String identifyColor(String hexColor) {
        int color = Color.parseColor(hexColor);

        // Definir más colores
        int rojo = Color.parseColor("#FF0000");
        int azul = Color.parseColor("#0000FF");
        int amarillo = Color.parseColor("#FFFF00");
        int negro = Color.parseColor("#000000");
        int blanco = Color.parseColor("#FFFFFF");
        int morado = Color.parseColor("#800080");
        int cafe = Color.parseColor("#8B4513");
        int verde = Color.parseColor("#008000");
        int naranja = Color.parseColor("#FFA500");
        int rosa = Color.parseColor("#FFC0CB");
        int gris = Color.parseColor("#808080");
        int cyan = Color.parseColor("#00FFFF");
        int violeta = Color.parseColor("#EE82EE");
        int plata = Color.parseColor("#C0C0C0");
        int oro = Color.parseColor("#FFD700");
        int marron = Color.parseColor("#A52A2A");
        int lima = Color.parseColor("#00FF00");
        int aqua = Color.parseColor("#00FFFF");
        int fucsia = Color.parseColor("#FF00FF");
        int oliva = Color.parseColor("#808000");
        int lavanda = Color.parseColor("#E6E6FA");
        int turquesa = Color.parseColor("#40E0D0");
        int coral = Color.parseColor("#FF7F50");
        int salmon = Color.parseColor("#FA8072");
        int granate = Color.parseColor("#800000");
        int azulMarino = Color.parseColor("#000080");
        int celeste = Color.parseColor("#87CEEB");

        String closestColorName = "Desconocido";
        double closestDistance = Double.MAX_VALUE;

        // Ampliar el arreglo de colores base
        int[] baseColors = {
                rojo, azul, amarillo, negro, blanco, morado, cafe, verde, naranja, rosa, gris, cyan,
                violeta, plata, oro, marron, lima, aqua, fucsia, oliva, lavanda, turquesa, coral, salmon, granate, azulMarino, celeste
        };
        String[] colorNames = {
                "Rojo", "Azul", "Amarillo", "Negro", "Blanco", "Morado", "Café", "Verde", "Naranja", "Rosa", "Gris", "Cian",
                "Violeta", "Plata", "Oro", "Marrón", "Lima", "Aqua", "Fucsia", "Oliva", "Lavanda", "Turquesa", "Coral", "Salmón", "Granate", "Azul Marino", "Celeste"
        };

        // Buscar el color más cercano
        for (int i = 0; i < baseColors.length; i++) {
            double distance = calculateColorDistance(color, baseColors[i]);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestColorName = colorNames[i];
            }
        }

        return closestColorName;
    }

    private double calculateColorDistance(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);

        return Math.sqrt(Math.pow(r2 - r1, 2) + Math.pow(g2 - g1, 2) + Math.pow(b2 - b1, 2));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verificar el estado del narrador en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isNarratorEnabled = prefs.getBoolean("narratorEnabled", false);

        if (isNarratorEnabled) {
            narratorManager.enableNarrator();
        } else {
            narratorManager.disableNarrator();
        }
    }
    private void saveColorToFirestore(String colorName, String hexColor) {
        // Create a map to hold the color data
        Map<String, Object> colorData = new HashMap<>();
        colorData.put("Tipo", "Color");
        colorData.put("fecha", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        colorData.put("valor", colorName);

        // Add a new document with a generated ID
        db.collection("historial").add(colorData)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Color data saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving color data", e));
    }

}
