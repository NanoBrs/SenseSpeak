package com.lucianoberriosdev.mycamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import org.tensorflow.lite.Interpreter;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BillIdentifierActivity extends AppCompatActivity {

    private static final String TAG = "BillIdentifierActivity";
    private static final int PICK_IMAGE_REQUEST = 1; // Código de solicitud para la galería
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Executor executor;
    private TextView billTextView;
    private Interpreter tflite;
    private FirebaseFirestore db;
    private ToggleButton narratorToggleButton;

    private NarratorManager narratorManager;
    private List<String> labels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_bill);

        db = FirebaseFirestore.getInstance();
        previewView = findViewById(R.id.previewView);
        billTextView = findViewById(R.id.billTextView);
        narratorToggleButton = findViewById(R.id.narratorToggleButton);

        executor = Executors.newSingleThreadExecutor();
        narratorManager = NarratorManager.getInstance(this);
        labels = loadLabels(); // Cargar etiquetas

        // Load TFLite model
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (IOException e) {
            Log.e(TAG, "Error loading model", e);
        }

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

        // Botón para capturar imagen
        Button captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> takePhoto());

        // Botón para seleccionar imagen desde la galería
        Button galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v -> openGallery());
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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    101);
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
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
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
            File photoFile = new File(getExternalFilesDir(null), "bill_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg");

            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            imageCapture.takePicture(outputOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Log.d(TAG, "Photo capture succeeded: " + outputFileResults.getSavedUri());
                    analyzeBill(photoFile); // Cambiado a analizar desde la ruta del archivo
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                }
            });
        }
    }

    // Método para abrir la galería
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            analyzeBillFromGallery(imageUri);
        }
    }

    private void analyzeBillFromGallery(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            analyzeBill(bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Error loading image from gallery", e);
        }
    }

    // Análisis del billete a partir del bitmap
    private void analyzeBill(Bitmap bitmap) {
        if (bitmap != null) {
            String billDetected = identifyBillUsingModel(bitmap);

            Log.d(TAG, "Billete detectado: " + billDetected);

            runOnUiThread(() -> {
                String textToSpeak = "Billete: " + billDetected;
                billTextView.setText(textToSpeak);
                speak(textToSpeak);
                saveBillToFirestore(billDetected);
            });

        } else {
            Log.e(TAG, "Failed to load bitmap.");
        }
    }

    private String identifyBillUsingModel(Bitmap bitmap) {
        // Preprocess the image and run inference using the TFLite model
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        ByteBuffer inputBuffer = preprocessImage(resizedBitmap);
        float[][] output = new float[1][labels.size()]; // Modifica según tus etiquetas

        tflite.run(inputBuffer, output);

        // Identificar el índice de la clase predicha y el nombre correspondiente
        int predictedClassIndex = getPredictedClassIndex(output[0]);
        return labels.get(predictedClassIndex); // Asegúrate de que labels esté cargado correctamente
    }

    private int getPredictedClassIndex(float[] output) {
        int maxIndex = 0;
        float maxValue = output[0];
        for (int i = 1; i < output.length; i++) {
            if (output[i] > maxValue) {
                maxValue = output[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private ByteBuffer preprocessImage(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.rewind();

        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                int pixel = bitmap.getPixel(x, y);
                byteBuffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f);
                byteBuffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);
                byteBuffer.putFloat((pixel & 0xFF) / 255.0f);
            }
        }
        return byteBuffer;
    }

    // Método para analizar el archivo de foto capturado por la cámara
    private void analyzeBill(File photoFile) {
        if (photoFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            analyzeBill(bitmap);
        } else {
            Log.e(TAG, "Photo file doesn't exist.");
        }
    }

    private void saveBillToFirestore(String billDetected) {
        // Crear un mapa para los datos que se van a guardar en Firebase
        Map<String, String> data = new HashMap<>();

        // Agregar los campos necesarios
        data.put("Tipo", "Billete"); // El tipo es Billete
        data.put("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())); // Fecha actual
        data.put("valor", billDetected); // El valor del billete detectado

        // Guardar los datos en la colección 'historial' de Firebase
        db.collection("historial")
                .add(data) // Añadir el mapa de datos
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Billete guardado con ID: " + documentReference.getId());
                    // Mensaje adicional opcional para indicar que los datos se han guardado correctamente
                    runOnUiThread(() -> speak("Billete guardado exitosamente"));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar el billete", e);
                    // Mensaje adicional opcional para indicar el fallo
                    runOnUiThread(() -> speak("Error al guardar el billete"));
                });
    }

    private List<String> loadLabels() {
        List<String> labels = new ArrayList<>();
        try {
            InputStream is = getAssets().open("labels.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Error loading labels", e);
        }
        return labels;
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model_unquant.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }
}
