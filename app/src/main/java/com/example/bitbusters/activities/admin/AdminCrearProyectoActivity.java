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
import com.example.bitbusters.data.ProjectSessionData;
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
    private ProjectSessionData sessionData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_crear_proyecto);

        sessionData = ProjectSessionData.getInstance();
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
        // Text fields
        etNombreProyecto = findViewById(R.id.etNombreProyecto);
        etDescripcionProyecto = findViewById(R.id.etDescripcionProyecto);
        tvFechaEntrega = findViewById(R.id.tvFechaEntrega);
        etDireccion = findViewById(R.id.etDireccion);
        etDistrito = findViewById(R.id.etDistrito);
        etCostoSeparacion = findViewById(R.id.etCostoSeparacion);
        etPrecioTotal = findViewById(R.id.etPrecioTotal);
        etNombreComercial = findViewById(R.id.etNombreComercial);
        etPrecioPublicado = findViewById(R.id.etPrecioPublicado);

        // State buttons
        btnEnPlanos = findViewById(R.id.btnEnPlanos);
        btnPreventa = findViewById(R.id.btnPreventa);
        btnEnVenta = findViewById(R.id.btnEnVenta);

        // Action buttons
        btnAgregarTipologia = findViewById(R.id.btnAgregarTipologia);
        btnAgregarAsesor = findViewById(R.id.btnAgregarAsesor);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnGuardarProyecto = findViewById(R.id.btnGuardarProyecto);
        btnCancelarCrear = findViewById(R.id.btnCancelarCrear);
        btnBackCreateProject = findViewById(R.id.btnBackCreateProject);

        // Containers
        tipologiasContainer = findViewById(R.id.tipologiasContainer);
        asesoresContainer = findViewById(R.id.asesoresContainer);
        imagesContainer = findViewById(R.id.imagesContainer);

        // Chips
        chipGroupAreas = findViewById(R.id.chipGroupAreas);
    }

    private void setupListeners() {
        // Back button - limpia datos
        btnBackCreateProject.setOnClickListener(v -> {
            sessionData.clear();
            finish();
        });

        // Fecha - abre DatePicker
        tvFechaEntrega.setOnClickListener(v -> showDatePicker());

        // State buttons
        btnEnPlanos.setOnClickListener(v -> selectEstado("En planos", btnEnPlanos));
        btnPreventa.setOnClickListener(v -> selectEstado("Preventa", btnPreventa));
        btnEnVenta.setOnClickListener(v -> selectEstado("En venta", btnEnVenta));

        // Navigation buttons - guarda datos en sessionData antes de navegar
        btnAgregarTipologia.setOnClickListener(v -> {
            saveCurrentFormData();
            startActivityForResult(
                new Intent(AdminCrearProyectoActivity.this, AdminAgregarTipologiaActivity.class), 100);
        });

        btnAgregarAsesor.setOnClickListener(v -> {
            saveCurrentFormData();
            startActivityForResult(
                new Intent(AdminCrearProyectoActivity.this, AdminAsignarAsesoresActivity.class), 101);
        });

        btnAddImage.setOnClickListener(v -> {
            saveCurrentFormData();
            openGallery();
        });

        // Action buttons
        btnGuardarProyecto.setOnClickListener(v -> {
            saveCurrentFormData();
            // TODO: Save to database
            sessionData.clear();
            finish();
        });
        btnCancelarCrear.setOnClickListener(v -> {
            sessionData.clear();
            finish();
        });
    }

    private void initializeStateButtonColors() {
        // Establecer colores iniciales para que se vean todos los botones
        int deepBlue = ContextCompat.getColor(this, R.color.brand_deep_blue);
        btnEnPlanos.setTextColor(deepBlue);
        btnPreventa.setTextColor(deepBlue);
        btnEnVenta.setTextColor(deepBlue);
        
        // Aplicar drawable de outline a todos inicialmente
        btnEnPlanos.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnPreventa.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnEnVenta.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
    }

    private void selectEstado(String estado, Button selectedButton) {
        selectedEstado = estado;
        sessionData.estado = estado;

        // Reset all buttons to outline style with color visible
        int deepBlue = ContextCompat.getColor(this, R.color.brand_deep_blue);
        btnEnPlanos.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnEnPlanos.setTextColor(deepBlue);
        btnPreventa.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnPreventa.setTextColor(deepBlue);
        btnEnVenta.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnEnVenta.setTextColor(deepBlue);

        // Set selected button to solid blue background
        selectedButton.setBackgroundColor(deepBlue);
        selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String selectedDate = sdf.format(calendar.getTime());
                tvFechaEntrega.setText(selectedDate);
                sessionData.fechaEntrega = selectedDate;
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH));
        
        datePickerDialog.show();
    }

    private void saveCurrentFormData() {
        sessionData.nombreProyecto = etNombreProyecto.getText().toString();
        sessionData.descripcion = etDescripcionProyecto.getText().toString();
        sessionData.direccion = etDireccion.getText().toString();
        sessionData.distrito = etDistrito.getText().toString();
        sessionData.costoSeparacion = etCostoSeparacion.getText().toString();
        sessionData.precioTotal = etPrecioTotal.getText().toString();
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
            
            // Restaurar estado seleccionado
            if (!selectedEstado.isEmpty()) {
                if (selectedEstado.equals("En planos")) {
                    selectEstado("En planos", btnEnPlanos);
                } else if (selectedEstado.equals("Preventa")) {
                    selectEstado("Preventa", btnPreventa);
                } else if (selectedEstado.equals("En venta")) {
                    selectEstado("En venta", btnEnVenta);
                }
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), 1001);
    }

    private void saveProject() {
        // TODO: Save project to database
        // For now, just go back
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // El estado del formulario se restaura automáticamente en onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restaurar datos de la sesión en caso de volver de otras activities
        restoreSessionData();
    }
}

