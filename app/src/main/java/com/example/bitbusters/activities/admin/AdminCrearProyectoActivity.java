package com.example.bitbusters.activities.admin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import com.example.bitbusters.R;
import com.example.bitbusters.data.AdminProyectoSessionData;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminCrearProyectoActivity extends AppCompatActivity {

    private TextInputEditText etNombreProyecto, etDescripcionProyecto;
    private TextView tvFechaEntrega;
    private TextInputEditText etDireccion, etDistrito;
    private TextInputEditText etCostoSeparacion, etPrecioTotal;
    private TextInputEditText etNombreComercial, etPrecioPublicado;

    private Button btnEnPlanos, btnPreventa, btnEnVenta;
    private Button btnAgregarTipologia, btnAgregarAsesor;
    private Button btnGuardarProyecto, btnCancelarCrear;
    private ImageButton btnBackCreateProject;
    private LinearLayout btnAddImage;
    private LinearLayout tipologiasContainer, asesoresContainer, imagesContainer;
    private ChipGroup chipGroupAreas;

    private String selectedEstado = "";
    private AdminProyectoSessionData sessionData; // ← tipo correcto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_crear_proyecto);

        sessionData = AdminProyectoSessionData.getInstance(); // ← clase correcta

        initializeViews();
        restoreSessionData();
        setupListeners();
        initializeStateButtonColors();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // No limpiar aquí, solo en cancelar o guardar
    }

    private void initializeViews() {
        etNombreProyecto      = findViewById(R.id.etNombreProyecto);
        etDescripcionProyecto = findViewById(R.id.etDescripcionProyecto);
        tvFechaEntrega        = findViewById(R.id.tvFechaEntrega);
        etDireccion           = findViewById(R.id.etDireccion);
        etDistrito            = findViewById(R.id.etDistrito);
        etCostoSeparacion     = findViewById(R.id.etCostoSeparacion);
        etPrecioTotal         = findViewById(R.id.etPrecioTotal);
        etNombreComercial     = findViewById(R.id.etNombreComercial);
        etPrecioPublicado     = findViewById(R.id.etPrecioPublicado);

        btnEnPlanos           = findViewById(R.id.btnEnPlanos);
        btnPreventa           = findViewById(R.id.btnPreventa);
        btnEnVenta            = findViewById(R.id.btnEnVenta);

        btnAgregarTipologia   = findViewById(R.id.btnAgregarTipologia);
        btnAgregarAsesor      = findViewById(R.id.btnAgregarAsesor);
        btnAddImage           = findViewById(R.id.btnAddImage);
        btnGuardarProyecto    = findViewById(R.id.btnGuardarProyecto);
        btnCancelarCrear      = findViewById(R.id.btnCancelarCrear);
        btnBackCreateProject  = findViewById(R.id.btnBackCreateProject);

        tipologiasContainer   = findViewById(R.id.tipologiasContainer);
        asesoresContainer     = findViewById(R.id.asesoresContainer);
        imagesContainer       = findViewById(R.id.imagesContainer);

        chipGroupAreas        = findViewById(R.id.chipGroupAreas);
    }

    private void setupListeners() {
        btnBackCreateProject.setOnClickListener(v -> {
            sessionData.clear();
            finish();
        });

        tvFechaEntrega.setOnClickListener(v -> showDatePicker());

        btnEnPlanos.setOnClickListener(v -> selectEstado("En planos", btnEnPlanos));
        btnPreventa.setOnClickListener(v -> selectEstado("Preventa",  btnPreventa));
        btnEnVenta.setOnClickListener(v  -> selectEstado("En venta",  btnEnVenta));

        btnAgregarTipologia.setOnClickListener(v -> {
            saveCurrentFormData();
            startActivityForResult(
                    new Intent(this, AdminAgregarTipologiaActivity.class), 100);
        });

        btnAgregarAsesor.setOnClickListener(v -> {
            saveCurrentFormData();
            startActivityForResult(
                    new Intent(this, AdminAsignarAsesoresActivity.class), 101);
        });

        btnAddImage.setOnClickListener(v -> {
            saveCurrentFormData();
            openGallery();
        });

        btnGuardarProyecto.setOnClickListener(v -> {
            saveCurrentFormData();
            // TODO: guardar en base de datos
            sessionData.clear();
            finish();
        });

        btnCancelarCrear.setOnClickListener(v -> {
            sessionData.clear();
            finish();
        });
    }

    private void initializeStateButtonColors() {
        int deepBlue = ContextCompat.getColor(this, R.color.brand_deep_blue);
        btnEnPlanos.setTextColor(deepBlue);
        btnPreventa.setTextColor(deepBlue);
        btnEnVenta.setTextColor(deepBlue);
        btnEnPlanos.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnPreventa.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnEnVenta.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
    }

    private void selectEstado(String estado, Button selectedButton) {
        selectedEstado     = estado;
        sessionData.estado = estado;

        int deepBlue = ContextCompat.getColor(this, R.color.brand_deep_blue);
        btnEnPlanos.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnEnPlanos.setTextColor(deepBlue);
        btnPreventa.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnPreventa.setTextColor(deepBlue);
        btnEnVenta.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnEnVenta.setTextColor(deepBlue);

        selectedButton.setBackgroundColor(deepBlue);
        selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(calendar.getTime());
                    tvFechaEntrega.setText(selectedDate);
                    sessionData.fechaEntrega = selectedDate;
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveCurrentFormData() {
        sessionData.nombreProyecto  = etNombreProyecto.getText().toString();
        sessionData.descripcion     = etDescripcionProyecto.getText().toString();
        sessionData.direccion       = etDireccion.getText().toString();
        sessionData.distrito        = etDistrito.getText().toString();
        sessionData.costoSeparacion = etCostoSeparacion.getText().toString();
        sessionData.precioTotal     = etPrecioTotal.getText().toString();
        sessionData.nombreComercial = etNombreComercial.getText().toString();
        sessionData.precioPublicado = etPrecioPublicado.getText().toString();
    }

    private void restoreSessionData() {
        if (!sessionData.nombreProyecto.isEmpty()) {
            etNombreProyecto.setText(sessionData.nombreProyecto);
            etDescripcionProyecto.setText(sessionData.descripcion);
            tvFechaEntrega.setText(sessionData.fechaEntrega);
            etDireccion.setText(sessionData.direccion);
            etDistrito.setText(sessionData.distrito);
            etCostoSeparacion.setText(sessionData.costoSeparacion);
            etPrecioTotal.setText(sessionData.precioTotal);
            etNombreComercial.setText(sessionData.nombreComercial);
            etPrecioPublicado.setText(sessionData.precioPublicado);
            selectedEstado = sessionData.estado;

            if (selectedEstado.equals("En planos"))  selectEstado("En planos", btnEnPlanos);
            else if (selectedEstado.equals("Preventa")) selectEstado("Preventa", btnPreventa);
            else if (selectedEstado.equals("En venta")) selectEstado("En venta", btnEnVenta);
        }
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreSessionData();
    }
}