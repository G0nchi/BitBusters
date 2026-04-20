package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.example.bitbusters.R;
import com.google.android.material.textfield.TextInputEditText;

public class AdminAgregarTipologiaActivity extends AppCompatActivity {

    private TextInputEditText etNombreTipologia, etMetraje, etPrecio, etDescripcion;
    private Button btnDorm1, btnDorm2, btnDorm3;
    private Button btnBano1, btnBano2, btnBano3;
    private Button btnGuardarTipologia, btnCancelarTipologia;
    private ImageButton btnBackTipologia;
    private LinearLayout btnAddImage;
    private TextView tvPreviewTitle, tvPreviewDetails, tvPreviewPrice;

    private int selectedDormitorios = 0;
    private int selectedBanos = 0;

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
        etMetraje = findViewById(R.id.etMetraje);
        etPrecio = findViewById(R.id.etPrecio);
        etDescripcion = findViewById(R.id.etDescripcion);

        btnDorm1 = findViewById(R.id.btnDorm1);
        btnDorm2 = findViewById(R.id.btnDorm2);
        btnDorm3 = findViewById(R.id.btnDorm3);

        btnBano1 = findViewById(R.id.btnBano1);
        btnBano2 = findViewById(R.id.btnBano2);
        btnBano3 = findViewById(R.id.btnBano3);

        btnGuardarTipologia = findViewById(R.id.btnGuardarTipologia);
        btnCancelarTipologia = findViewById(R.id.btnCancelarTipologia);
        btnBackTipologia = findViewById(R.id.btnBackTipologia);
        btnAddImage = findViewById(R.id.btnAddImage);

        tvPreviewTitle = findViewById(R.id.tvPreviewTitle);
        tvPreviewDetails = findViewById(R.id.tvPreviewDetails);
        tvPreviewPrice = findViewById(R.id.tvPreviewPrice);
    }

    private void setupListeners() {
        // Text change listeners for real-time preview update
        etNombreTipologia.addTextChangedListener(previewUpdateWatcher);
        etMetraje.addTextChangedListener(previewUpdateWatcher);
        etPrecio.addTextChangedListener(previewUpdateWatcher);

        // Dormitorios button listeners
        btnDorm1.setOnClickListener(v -> selectDormitorios(1, btnDorm1));
        btnDorm2.setOnClickListener(v -> selectDormitorios(2, btnDorm2));
        btnDorm3.setOnClickListener(v -> selectDormitorios(3, btnDorm3));

        // Baños button listeners
        btnBano1.setOnClickListener(v -> selectBanos(1, btnBano1));
        btnBano2.setOnClickListener(v -> selectBanos(2, btnBano2));
        btnBano3.setOnClickListener(v -> selectBanos(3, btnBano3));

        // Add image
        btnAddImage.setOnClickListener(v -> openGallery());

        // Action buttons
        btnGuardarTipologia.setOnClickListener(v -> saveTypology());
        btnCancelarTipologia.setOnClickListener(v -> finish());
        btnBackTipologia.setOnClickListener(v -> finish());
    }

    private TextWatcher previewUpdateWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updatePreview();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    };

    private void selectDormitorios(int dormi, Button selectedButton) {
        selectedDormitorios = dormi;

        // Reset all buttons to outline style
        btnDorm1.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnDorm1.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        btnDorm2.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnDorm2.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        btnDorm3.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnDorm3.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));

        // Set selected button to solid blue background
        selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        updatePreview();
    }

    private void selectBanos(int banos, Button selectedButton) {
        selectedBanos = banos;

        // Reset all buttons to outline style
        btnBano1.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnBano1.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        btnBano2.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnBano2.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        btnBano3.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnBano3.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));

        // Set selected button to solid blue background
        selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        updatePreview();
    }

    private void updatePreview() {
        String nombre = etNombreTipologia.getText().toString().trim();
        String metraje = etMetraje.getText().toString().trim();
        String precio = etPrecio.getText().toString().trim();

        // Build title
        String title = (nombre.isEmpty() ? "Tipo A" : nombre) + " – " + selectedDormitorios + " dorm.";
        tvPreviewTitle.setText(title);

        // Build details
        String details = (metraje.isEmpty() ? "0" : metraje) + " m² · " + selectedBanos + " baño" + (selectedBanos > 1 ? "s" : "");
        tvPreviewDetails.setText(details);

        // Build price
        String priceText = "S/" + (precio.isEmpty() ? "0" : precio);
        tvPreviewPrice.setText(priceText);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), 1001);
    }

    private void saveTypology() {
        // TODO: Save typology to database
        finish();
    }
}
