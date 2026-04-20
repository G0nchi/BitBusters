package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bitbusters.R;
import android.content.Intent;

public class PaymentMethodActivity extends AppCompatActivity {

    private EditText etNombreTitular, etNumeroTarjeta, etFechaVencimiento, etCVV;
    private TextView tvNumeroTarjeta, tvNombreTarjeta, tvVencimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        etNombreTitular   = findViewById(R.id.etNombreTitular);
        etNumeroTarjeta   = findViewById(R.id.etNumeroTarjeta);
        etFechaVencimiento= findViewById(R.id.etFechaVencimiento);
        etCVV             = findViewById(R.id.etCVV);
        tvNumeroTarjeta   = findViewById(R.id.tvNumeroTarjeta);
        tvNombreTarjeta   = findViewById(R.id.tvNombreTarjeta);
        tvVencimiento     = findViewById(R.id.tvVencimiento);

        // Volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Saltar — ir a Home sin agregar tarjeta
        findViewById(R.id.btnSaltar).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        // Preview en tiempo real: nombre
        etNombreTitular.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                String nombre = s.toString().trim();
                tvNombreTarjeta.setText(nombre.isEmpty() ? "Olivia Johns" : nombre);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Preview en tiempo real: número de tarjeta con formato
        etNumeroTarjeta.addTextChangedListener(new TextWatcher() {
            private boolean editando = false;
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (editando) return;
                editando = true;

                // Formatear con espacios cada 4 dígitos
                String raw = s.toString().replace(" ", "");
                StringBuilder formateado = new StringBuilder();
                for (int i = 0; i < raw.length() && i < 16; i++) {
                    if (i > 0 && i % 4 == 0) formateado.append(" ");
                    formateado.append(raw.charAt(i));
                }
                s.replace(0, s.length(), formateado);

                // Preview en la tarjeta visual
                String preview = formateado.toString();
                if (preview.length() >= 4) {
                    String ultimos = preview.substring(Math.max(0, preview.replace(" ","").length() - 4));
                    tvNumeroTarjeta.setText("**** **** **** " + ultimos);
                } else {
                    tvNumeroTarjeta.setText("**** **** **** 1234");
                }
                editando = false;
            }
        });

        // Preview en tiempo real: vencimiento
        etFechaVencimiento.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                String fecha = s.toString().trim();
                tvVencimiento.setText(fecha.isEmpty() ? "01/22" : fecha);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Guardar tarjeta
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarTarjeta());
    }

    private void guardarTarjeta() {
        String nombre = etNombreTitular.getText().toString().trim();
        String numero = etNumeroTarjeta.getText().toString().trim();
        String fecha  = etFechaVencimiento.getText().toString().trim();
        String cvv    = etCVV.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre del titular", Toast.LENGTH_SHORT).show();
            return;
        }
        if (numero.replace(" ", "").length() < 16) {
            Toast.makeText(this, "Ingresa un número de tarjeta válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fecha.isEmpty()) {
            Toast.makeText(this, "Ingresa la fecha de vencimiento", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cvv.length() < 3) {
            Toast.makeText(this, "Ingresa el CVV", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pop up de confirmación
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Método de pago guardado")
                .setMessage("Tu tarjeta terminada en " +
                        numero.replace(" ", "").substring(12) +
                        " fue agregada correctamente.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}