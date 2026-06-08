package com.example.bitbusters.activities.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.data.AdminProyectoSessionData;
import com.example.bitbusters.models.Tipologia;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Activity para que el admin agregue una tipología al proyecto que está creando/editando.
 *
 * Imagen: al seleccionar una imagen desde la galería, se copia a almacenamiento interno
 * (getFilesDir/tipologia_images/) para garantizar persistencia. La ruta local se guarda
 * en Tipologia.imageUri.
 *
 * TODO-Firebase: en copiarImagenAStorage(), reemplazar la copia local por
 * FirebaseStorage.getInstance().getReference("tipologias/{id}").putFile(uri)
 * y guardar la URL de descarga devuelta por getDownloadUrl().
 */
public class AdminAgregarTipologiaActivity extends AppCompatActivity {

    private TextInputEditText etNombreTipologia, etMetraje, etPrecio, etDescripcion;
    private Button btnDorm1, btnDorm2, btnDorm3;
    private Button btnBano1, btnBano2, btnBano3;
    private Button btnGuardarTipologia, btnCancelarTipologia;
    private ImageButton btnBackTipologia;
    private LinearLayout btnAddImage;
    private ImageView imgTipologiaPreview;
    private TextView tvPreviewTitle, tvPreviewDetails, tvPreviewPrice;

    private int    selectedDormitorios = 0;
    private int    selectedBanos       = 0;
    private String localImagePath      = ""; // ruta en almacenamiento interno

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_agregar_tipologia);

        initializeViews();
        setupListeners();
        updatePreview();
    }

    private void initializeViews() {
        etNombreTipologia    = findViewById(R.id.etNombreTipologia);
        etMetraje            = findViewById(R.id.etMetraje);
        etPrecio             = findViewById(R.id.etPrecio);
        etDescripcion        = findViewById(R.id.etDescripcion);

        btnDorm1             = findViewById(R.id.btnDorm1);
        btnDorm2             = findViewById(R.id.btnDorm2);
        btnDorm3             = findViewById(R.id.btnDorm3);

        btnBano1             = findViewById(R.id.btnBano1);
        btnBano2             = findViewById(R.id.btnBano2);
        btnBano3             = findViewById(R.id.btnBano3);

        btnGuardarTipologia  = findViewById(R.id.btnGuardarTipologia);
        btnCancelarTipologia = findViewById(R.id.btnCancelarTipologia);
        btnBackTipologia     = findViewById(R.id.btnBackTipologia);
        btnAddImage          = findViewById(R.id.btnAddImage);
        imgTipologiaPreview  = findViewById(R.id.imgTipologiaPreview);

        tvPreviewTitle       = findViewById(R.id.tvPreviewTitle);
        tvPreviewDetails     = findViewById(R.id.tvPreviewDetails);
        tvPreviewPrice       = findViewById(R.id.tvPreviewPrice);
    }

    private void setupListeners() {
        etNombreTipologia.addTextChangedListener(previewUpdateWatcher);
        etMetraje.addTextChangedListener(previewUpdateWatcher);
        etPrecio.addTextChangedListener(previewUpdateWatcher);

        btnDorm1.setOnClickListener(v -> selectDormitorios(1, btnDorm1));
        btnDorm2.setOnClickListener(v -> selectDormitorios(2, btnDorm2));
        btnDorm3.setOnClickListener(v -> selectDormitorios(3, btnDorm3));

        btnBano1.setOnClickListener(v -> selectBanos(1, btnBano1));
        btnBano2.setOnClickListener(v -> selectBanos(2, btnBano2));
        btnBano3.setOnClickListener(v -> selectBanos(3, btnBano3));

        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> openGallery());
        }

        btnGuardarTipologia.setOnClickListener(v -> saveTypology());
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
        resetButtonStyle(btnDorm1);
        resetButtonStyle(btnDorm2);
        resetButtonStyle(btnDorm3);
        selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        updatePreview();
    }

    private void selectBanos(int banos, Button selectedButton) {
        selectedBanos = banos;
        resetButtonStyle(btnBano1);
        resetButtonStyle(btnBano2);
        resetButtonStyle(btnBano3);
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

        tvPreviewTitle.setText((nombre.isEmpty() ? "Tipo A" : nombre)
                + " – " + selectedDormitorios + " dorm.");
        tvPreviewDetails.setText((metraje.isEmpty() ? "0" : metraje)
                + " m² · " + selectedBanos + " baño" + (selectedBanos > 1 ? "s" : ""));
        tvPreviewPrice.setText("S/" + (precio.isEmpty() ? "0" : precio));
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri uri = data.getData();

            // TODO-Firebase: aquí llamarías a FirebaseStorage.upload(uri) en lugar de copiarImagenAStorage.
            //                El resultado sería una URL de descarga (String).
            localImagePath = copiarImagenAStorage(uri);

            // Mostrar vista previa
            if (imgTipologiaPreview != null) {
                imgTipologiaPreview.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(localImagePath.isEmpty() ? uri : new File(localImagePath))
                        .centerCrop()
                        .into(imgTipologiaPreview);
            }
        }
    }

    /**
     * Copia la imagen desde la URI de galería a almacenamiento interno del app
     * y devuelve la ruta absoluta del archivo copiado.
     *
     * Ruta destino: getFilesDir()/tipologia_images/{uuid}.jpg
     *
     * TODO-Firebase: reemplazar este método por una subida a FirebaseStorage y
     * devolver la URL de descarga en su lugar.
     *
     * @return ruta local absoluta, o "" si falla la copia.
     */
    private String copiarImagenAStorage(Uri uri) {
        try {
            File dir = new File(getFilesDir(), "tipologia_images");
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, "tip_" + UUID.randomUUID() + ".jpg");
            try (InputStream in  = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(dest)) {
                if (in == null) return "";
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }
            return dest.getAbsolutePath();
        } catch (Exception e) {
            return "";
        }
    }

    private void saveTypology() {
        String nombre     = etNombreTipologia.getText() != null
                ? etNombreTipologia.getText().toString().trim() : "";
        String metrajeStr = etMetraje.getText() != null
                ? etMetraje.getText().toString().trim() : "";
        String precioStr  = etPrecio.getText() != null
                ? etPrecio.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null
                ? etDescripcion.getText().toString().trim() : "";

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
            Toast.makeText(this, "Por favor ingresa el metraje", Toast.LENGTH_SHORT).show();
            etMetraje.requestFocus();
            return;
        }
        if (precioStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el precio total", Toast.LENGTH_SHORT).show();
            etPrecio.requestFocus();
            return;
        }

        double metraje, precio;
        try {
            metraje = Double.parseDouble(metrajeStr);
            precio  = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El metraje y precio deben ser valores numéricos", Toast.LENGTH_SHORT).show();
            return;
        }

        Tipologia tipologia = new Tipologia(
                nombre,
                selectedDormitorios,
                selectedBanos,
                metraje,
                precio,
                descripcion,
                localImagePath   // ruta local (o "" si no se seleccionó imagen)
        );
        AdminProyectoSessionData.getInstance().tipologias.add(tipologia);

        Toast.makeText(this, "Tipología \"" + nombre + "\" agregada", Toast.LENGTH_SHORT).show();
        finish();
    }
}
