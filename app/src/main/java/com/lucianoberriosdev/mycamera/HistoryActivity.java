package com.lucianoberriosdev.mycamera;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        tableLayout = findViewById(R.id.tableHistory); // Referencia al TableLayout
        db = FirebaseFirestore.getInstance();

        // Botón de volver atrás
        Button backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> finish()); // Finaliza la actividad y vuelve atrás

        loadHistory(); // Cargar los datos de Firebase
    }

    private void loadHistory() {
        db.collection("historial")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(15)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();

                    for (DocumentSnapshot document : documents) {
                        String tipo = document.getString("Tipo");
                        String fecha = document.getString("fecha");
                        String valor = document.getString("valor");

                        TableRow row = new TableRow(this);

                        // Crear las celdas
                        TextView tipoTextView = new TextView(this);
                        tipoTextView.setText(tipo);
                        tipoTextView.setPadding(8, 8, 8, 8);
                        tipoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22); // Tamaño de fuente

                        TextView fechaTextView = new TextView(this);
                        fechaTextView.setText(fecha);
                        fechaTextView.setPadding(8, 8, 8, 8);
                        fechaTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22); // Tamaño de fuente

                        TextView valorTextView = new TextView(this);
                        valorTextView.setText(valor);
                        valorTextView.setPadding(8, 8, 8, 8);
                        valorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22); // Tamaño de fuente

                        // Agregar las celdas a la fila
                        row.addView(tipoTextView);
                        row.addView(fechaTextView);
                        row.addView(valorTextView);

                        // Agregar la fila a la tabla
                        tableLayout.addView(row);
                    }
                });
    }
}
