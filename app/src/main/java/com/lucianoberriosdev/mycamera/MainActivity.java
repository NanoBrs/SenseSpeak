package com.lucianoberriosdev.mycamera;

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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Executor executor;
    private TextView colorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        colorTextView = findViewById(R.id.colorTextView);

        executor = Executors.newSingleThreadExecutor();

        // Check and request permissions
        checkPermissions();

        // Set up the CameraX configuration
        setUpCameraX();

        Button captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    private void checkPermissions() {
        // Check if permissions are granted and request if needed
        // This is a simplified check for demonstration
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
                Preview preview = new Preview.Builder()
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK) // Change to FRONT for front camera
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
            // Save photo to the app's specific directory
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
        // Decodificar el bitmap fuera del UI thread
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

        if (bitmap != null) {
            int color = bitmap.getPixel(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
            String hexColor = String.format("#%06X", (0xFFFFFF & color));

            // Identificar el color
            String colorName = identifyColor(hexColor);

            Log.d(TAG, "Dominant color detected: " + hexColor + " (" + colorName + ")");

            // Ahora actualizar la UI en el hilo principal
            runOnUiThread(() -> {
                colorTextView.setText("Color: " + colorName + " (" + hexColor + ")");
            });
        } else {
            Log.e(TAG, "Failed to load bitmap.");
        }
    }


    private String identifyColor(String hexColor) {
        int color = Color.parseColor(hexColor);

        // Colores base
        int rojo = Color.parseColor("#FF0000");
        int azul = Color.parseColor("#0000FF");
        int amarillo = Color.parseColor("#FFFF00");
        int negro = Color.parseColor("#000000");
        int blanco = Color.parseColor("#FFFFFF");
        int morado = Color.parseColor("#800080");
        int cafe = Color.parseColor("#8B4513");

        // Comparar con los colores base
        String closestColorName = "Desconocido";
        double closestDistance = Double.MAX_VALUE;

        int[] baseColors = {rojo, azul, amarillo, negro, blanco, morado, cafe};
        String[] colorNames = {"Rojo", "Azul", "Amarillo", "Negro", "Blanco", "Morado", "Café"};

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
}
