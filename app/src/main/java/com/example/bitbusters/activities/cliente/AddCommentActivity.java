package com.example.bitbusters.activities.cliente;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
public class AddCommentActivity extends AppCompatActivity {

    private ImageButton star1, star2, star3, star4, star5;
    private TextView tvRating;
    private EditText etComentario;
    private int calificacionSeleccionada = 0;

    private ImageButton[] estrellas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);
        tvRating = findViewById(R.id.tvRating);
        etComentario = findViewById(R.id.etComentario);
        estrellas = new ImageButton[]{star1, star2, star3, star4, star5};

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Lógica de estrellas interactivas
        for (int i = 0; i < estrellas.length; i++) {
            final int posicion = i + 1;
            estrellas[i].setOnClickListener(v -> seleccionarEstrellas(posicion));
        }

        // Botón Guardar
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarComentario());
    }

    private void seleccionarEstrellas(int cantidad) {
        calificacionSeleccionada = cantidad;
        tvRating.setText(String.valueOf((float) cantidad));

        for (int i = 0; i < estrellas.length; i++) {
            if (i < cantidad) {
                estrellas[i].setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                estrellas[i].setImageResource(android.R.drawable.btn_star_big_off);
            }
        }
    }

    private void guardarComentario() {
        String comentario = etComentario.getText().toString().trim();

        if (calificacionSeleccionada == 0) {
            Toast.makeText(this, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: en Lab 6 guardar en Firebase
        // DatabaseReference ref = FirebaseDatabase.getInstance()
        //     .getReference("comentarios").child(proyectoId);
        // Comentario nuevo = new Comentario(usuarioId, comentario, calificacionSeleccionada);
        // ref.push().setValue(nuevo);

        Toast.makeText(this, "¡Comentario guardado!", Toast.LENGTH_SHORT).show();
        finish();
    }
}