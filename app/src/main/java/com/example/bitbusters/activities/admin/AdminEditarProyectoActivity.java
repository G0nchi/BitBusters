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

import com.example.bitbusters.activities.admin.dialogs.ConfirmDeleteDialogFragment;
import com.example.bitbusters.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminEditarProyectoActivity extends AppCompatActivity implements
        ConfirmDeleteDialogFragment.OnConfirmDeleteListener {

    private ImageButton btnBackEditProject;
    private Button btnSaveChanges, btnCancel;
    private Button btnAddTipologia, btnAddAdvisor;
    private LinearLayout btnAddImage;
    private ImageButton btnDeleteTipologia1, btnDeleteTipologia2;
    private ImageButton btnDeleteImg1, btnDeleteImg2;
    private ImageButton btnDeleteAsesor1, btnDeleteAsesor2, btnDeleteAsesor3;
    private Button btnStateInPlanos, btnStateEnConstruccion, btnStateEnVenta;
    private TextView tvFechaEntrega;

    // Constants for dialog
    private static final String DELETE_TIPOLOGIA = "tipología";
    private static final String DELETE_IMAGE = "imagen";
    private static final String DELETE_ADVISOR = "asesor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_editar_proyecto);

        initializeViews();
        setupNavigationListeners();
        setupDeleteListeners();
        setupAddListeners();
        setupStateButtons();
        initializeStateButtonColors();
    }

    private void initializeViews() {
        btnBackEditProject = findViewById(R.id.btnBackEditProject);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);
        tvFechaEntrega = findViewById(R.id.tvFechaEntrega);

        // Delete buttons
        btnDeleteTipologia1 = findViewById(R.id.btnDeleteTipologia1);
        btnDeleteTipologia2 = findViewById(R.id.btnDeleteTipologia2);
        btnDeleteImg1 = findViewById(R.id.btnDeleteImg1);
        btnDeleteImg2 = findViewById(R.id.btnDeleteImg2);
        btnDeleteAsesor1 = findViewById(R.id.btnDeleteAsesor1);
        btnDeleteAsesor2 = findViewById(R.id.btnDeleteAsesor2);
        btnDeleteAsesor3 = findViewById(R.id.btnDeleteAsesor3);

        // Add buttons
        btnAddTipologia = findViewById(R.id.btnAddTipologia);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnAddAdvisor = findViewById(R.id.btnAddAdvisor);

        // State buttons
        btnStateInPlanos = findViewById(R.id.btnStateInPlanos);
        btnStateEnConstruccion = findViewById(R.id.btnStateEnConstruccion);
        btnStateEnVenta = findViewById(R.id.btnStateEnVenta);
    }

    private void setupNavigationListeners() {
        btnBackEditProject.setOnClickListener(v -> finish());

        btnSaveChanges.setOnClickListener(v -> {
            // TODO: Save changes to database
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupDeleteListeners() {
        // Tipología delete buttons
        btnDeleteTipologia1.setOnClickListener(v -> showDeleteConfirmation(DELETE_TIPOLOGIA, 1));
        btnDeleteTipologia2.setOnClickListener(v -> showDeleteConfirmation(DELETE_TIPOLOGIA, 2));

        // Image delete buttons
        btnDeleteImg1.setOnClickListener(v -> showDeleteConfirmation(DELETE_IMAGE, 1));
        btnDeleteImg2.setOnClickListener(v -> showDeleteConfirmation(DELETE_IMAGE, 2));

        // Advisor delete buttons
        btnDeleteAsesor1.setOnClickListener(v -> showDeleteConfirmation(DELETE_ADVISOR, 1));
        btnDeleteAsesor2.setOnClickListener(v -> showDeleteConfirmation(DELETE_ADVISOR, 2));
        btnDeleteAsesor3.setOnClickListener(v -> showDeleteConfirmation(DELETE_ADVISOR, 3));
    }

    private void setupAddListeners() {
        // Date picker
        tvFechaEntrega.setOnClickListener(v -> showDatePicker());

        // Add tipología
        btnAddTipologia.setOnClickListener(v -> {
            Intent intent = new Intent(AdminEditarProyectoActivity.this,
                    AdminAgregarTipologiaActivity.class);
            startActivityForResult(intent, 100);
        });

        // Add image - open gallery
        btnAddImage.setOnClickListener(v -> openGallery());

        // Add advisor - navigate to assign advisors activity
        btnAddAdvisor.setOnClickListener(v -> {
            Intent intent = new Intent(AdminEditarProyectoActivity.this,
                    AdminAsignarAsesoresActivity.class);
            startActivityForResult(intent, 101);
        });
    }

    private void setupStateButtons() {
        btnStateInPlanos.setOnClickListener(v -> selectStateButton(btnStateInPlanos));
        btnStateEnConstruccion.setOnClickListener(v -> selectStateButton(btnStateEnConstruccion));
        btnStateEnVenta.setOnClickListener(v -> selectStateButton(btnStateEnVenta));
    }

    private void initializeStateButtonColors() {
        // Establecer colores iniciales para que se vean todos los botones
        int deepBlue = ContextCompat.getColor(this, R.color.brand_deep_blue);
        btnStateInPlanos.setTextColor(deepBlue);
        btnStateEnConstruccion.setTextColor(deepBlue);
        btnStateEnVenta.setTextColor(deepBlue);
        
        // Aplicar drawable de outline a todos inicialmente
        btnStateInPlanos.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnStateEnConstruccion.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnStateEnVenta.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
    }

    private void selectStateButton(Button selectedButton) {
        // Reset all buttons to outline style (transparent background with border)
        int deepBlue = ContextCompat.getColor(this, R.color.brand_deep_blue);
        btnStateInPlanos.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnStateInPlanos.setTextColor(deepBlue);
        
        btnStateEnConstruccion.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnStateEnConstruccion.setTextColor(deepBlue);
        
        btnStateEnVenta.setBackground(AppCompatResources.getDrawable(this, R.drawable.button_outline_state_bg));
        btnStateEnVenta.setTextColor(deepBlue);

        // Set selected button to blue filled
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
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH));
        
        datePickerDialog.show();
    }

    private void showDeleteConfirmation(String itemType, int itemIndex) {
        ConfirmDeleteDialogFragment dialog = ConfirmDeleteDialogFragment.newInstance(itemType, itemIndex);
        dialog.setOnConfirmDeleteListener(this);
        dialog.show(getSupportFragmentManager(), "confirm_delete_" + itemType);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), 1001);
    }

    @Override
    public void onConfirmDelete(String itemType, int itemIndex) {
        switch (itemType) {
            case DELETE_TIPOLOGIA:
                deleteTipologia(itemIndex);
                break;
            case DELETE_IMAGE:
                deleteImage(itemIndex);
                break;
            case DELETE_ADVISOR:
                deleteAdvisor(itemIndex);
                break;
        }
    }

    @Override
    public void onCancelDelete() {
        // Dialog dismissed, no action needed
    }

    private void deleteTipologia(int index) {
        // TODO: Remove tipología from list
    }

    private void deleteImage(int index) {
        // TODO: Remove image from gallery
    }

    private void deleteAdvisor(int index) {
        // TODO: Remove advisor from list
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // TODO: Load selected image to gallery
        }
    }
}
