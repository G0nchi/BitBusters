package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.data.AdminProyectoSessionData;
import com.example.bitbusters.models.Tipologia;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity para que el admin agregue una tipología de departamento al proyecto
 * que está creando. Al guardar, agrega la tipología a la sesión
 * (AdminProyectoSessionData.getInstance().tipologias) y regresa al formulario.
 */
public class AdminAgregarTipologiaActivity extends AppCompatActivity {

    private TextInputEditText etNombreTipologia, etMetraje, etPrecio, etDescripcion;
    private Button btnDorm1, btnDorm2, btnDorm3;
    private Button btnBano1, btnBano2, btnBano3;
    private Button btnGuardarTipologia, btnCancelarTipologia;
    private ImageButton btnBackTipologia;
    private LinearLayout btnAddImage;
    private TextView tvPreviewTitle, tvPreviewDetails, tvPreviewPrice;

    private int selectedDormitorios = 0;
    private int selectedBanos       = 0;
    private Uri imageUri             = null; // URI de la imagen seleccionada (opcional)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_agregar_tipologia);

        initializeViews();
        setupListeners();
        updatePreview();
    }

    private void initializeViews() {
        etNombreTipologia = findViewById(R.id.etNombreTipologia);
        etMetraje         = findViewById(R.id.etMetraje);
        etPrecio          = findViewById(R.id.etPrecio);
        etDescripcion     = findViewById(R.id.etDescripcion);

        btnDorm1 = findViewById(R.id.btnDorm1);
        btnDorm2 = findViewById(R.id.btnDorm2);
        btnDorm3 = findViewById(R.id.btnDorm3);

        btnBano1 = findViewById(R.id.btnBano1);
        btnBano2 = findViewById(R.id.btnBano2);
        btnBano3 = findViewById(R.id.btnBano3);

        btnGuardarTipologia  = findViewById(R.id.btnGuardarTipologia);
        btnCancelarTipologia = findViewById(R.id.btnCancelarTipologia);
        btnBackTipologia     = findViewById(R.id.btnBackTipologia);
        btnAddImage          = findViewById(R.id.btnAddImage);

        tvPreviewTitle   = findViewById(R.id.tvPreviewTitle);
        tvPreviewDetails = findViewById(R.id.tvPreviewDetails);
        tvPreviewPrice   = findViewById(R.id.tvPreviewPrice);
    }

    private void setupListeners() {
        // Actualizar vista previa en tiempo real
        etNombreTipologia.addTextChangedListener(previewUpdateWatcher);
        etMetraje.addTextChangedListener(previewUpdateWatcher);
        etPrecio.addTextChangedListener(previewUpdateWatcher);

        // Selección de dormitorios
        btnDorm1.setOnClickListener(v -> selectDormitorios(1, btnDorm1));
        btnDorm2.setOnClickListener(v -> selectDormitorios(2, btnDorm2));
        btnDorm3.setOnClickListener(v -> selectDormitorios(3, btnDorm3));

        // Selección de baños
        btnBano1.setOnClickListener(v -> selectBanos(1, btnBano1));
        btnBano2.setOnClickListener(v -> selectBanos(2, btnBano2));
        btnBano3.setOnClickListener(v -> selectBanos(3, btnBano3));

        // Imagen opcional de la tipología
        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> openGallery());
        }

        // Guardar tipología → agregar a sesión y volver
        btnGuardarTipologia.setOnClickListener(v -> saveTypology());

        // Cancelar → volver sin guardar
        btnCancelarTipologia.setOnClickListener(v -> finish());
        btnBackTipologia.setOnClickListener(v -> finish());
    }

    private final TextWatcher previewUpdateWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updatePreview(); }
        @Override public void afterTextChanged(Editable s) {}
    };

    private void selectDormitorios(int dormi, Button selectedButton) {
        selectedDormitorios = dormi;

        // Resetear todos al estilo outline
        resetButtonStyle(btnDorm1);
        resetButtonStyle(btnDorm2);
        resetButtonStyle(btnDorm3);

        // Marcar el seleccionado
        selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        updatePreview();
    }

    private void selectBanos(int banos, Button selectedButton) {
        selectedBanos = banos;

        // Resetear todos al estilo outline
        resetButtonStyle(btnBano1);
        resetButtonStyle(btnBano2);
        resetButtonStyle(btnBano3);

        // Marcar el seleccionado
        selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        updatePreview();
    }

    private void resetButtonStyle(Button btn) {
        btn.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btn.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
    }

    private void updatePreview() {
        String nombre  = etNombreTipologia.getText().toString().trim();
        String metraje = etMetraje.getText().toString().trim();
        String precio  = etPrecio.getText().toString().trim();

        // Título
        String title = (nombre.isEmpty() ? "Tipo A" : nombre)
                + " – " + selectedDormitorios + " dorm.";
        tvPreviewTitle.setText(title);

        // Detalles
        String details = (metraje.isEmpty() ? "0" : metraje)
                + " m² · " + selectedBanos + " baño" + (selectedBanos > 1 ? "s" : "");
        tvPreviewDetails.setText(details);

        // Precio
        tvPreviewPrice.setText("S/" + (precio.isEmpty() ? "0" : precio));
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }

    // ── Parte 1: Guardar tipología en la sesión ──────────────────────────────

    /**
     * Valida los campos, crea el objeto Tipologia y lo agrega a
     * AdminProyectoSessionData.getInstance().tipologias, luego cierra esta Activity.
     */
    private void saveTypology() {
        String nombre     = etNombreTipologia.getText() != null ? etNombreTipologia.getText().toString().trim() : "";
        String metrajeStr = etMetraje.getText()         != null ? etMetraje.getText().toString().trim()         : "";
        String precioStr  = etPrecio.getText()          != null ? etPrecio.getText().toString().trim()          : "";
        String descripcion = etDescripcion.getText()    != null ? etDescripcion.getText().toString().trim()     : "";

        // Validar campos obligatorios
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el nombre de la tipología", Toast.LENGTH_SHORT).show();
            etNombreTipologia.requestFocus();
            return;
        }
        if (selectedDormitorios == 0) {
            Toast.makeText(this, "Por favor selecciona el número de dormitorios", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedBanos == 0) {
            Toast.makeText(this, "Por favor selecciona el número de baños", Toast.LENGTH_SHORT).show();
            return;
        }
        if (metrajeStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el metraje de la tipología", Toast.LENGTH_SHORT).show();
            etMetraje.requestFocus();
            return;
        }
        if (precioStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el precio total de la tipología", Toast.LENGTH_SHORT).show();
            etPrecio.requestFocus();
            return;
        }

        // Parsear valores numéricos
        double metraje, precio;
        try {
            metraje = Double.parseDouble(metrajeStr);
            precio  = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El metraje y precio deben ser valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear objeto Tipologia y agregar a la sesión compartida
        Tipologia tipologia = new Tipologia(
                nombre,
                selectedDormitorios,
                selectedBanos,
                metraje,
                precio,
                descripcion,
                imageUri != null ? imageUri.toString() : ""
        );
        AdminProyectoSessionData.getInstance().tipologias.add(tipologia);

        Toast.makeText(this, "Tipología \"" + nombre + "\" agregada", Toast.LENGTH_SHORT).show();
        finish(); // volver a AdminCrearProyectoActivity
    }
}
