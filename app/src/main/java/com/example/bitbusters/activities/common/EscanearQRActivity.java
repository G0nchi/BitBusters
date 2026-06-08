package com.example.bitbusters.activities.common;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.activities.asesor.ProyectoDetalleActivity;
import com.example.bitbusters.models.QRResult;
import com.example.bitbusters.utils.QRParser;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class EscanearQRActivity extends AppCompatActivity {

    public static final String EXTRA_ROL = "rol";
    public static final String ROL_CLIENTE = "cliente";
    public static final String ROL_ASESOR = "asesor";

    private static final int REQUEST_CAMERA = 200;

    private DecoratedBarcodeView barcodeView;
    private ImageButton btnLinterna;
    private boolean linternaEncendida = false;
    // Previene procesar múltiples resultados simultáneos
    private volatile boolean procesando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escanear_qr);

        barcodeView = findViewById(R.id.barcode_scanner);
        btnLinterna = findViewById(R.id.btnLinterna);

        findViewById(R.id.btnCancelar).setOnClickListener(v -> finish());
        btnLinterna.setOnClickListener(v -> toggleLinterna());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            barcodeView.decodeContinuous(callback);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result == null || result.getText() == null || procesando) return;
            procesando = true;
            Log.d("DeepLink", "QR detectado: " + result.getText());

            QRResult qr = QRParser.parse(result.getText());
            if (qr.valido && "proyecto".equals(qr.tipo)) {
                abrirProyecto(qr.id);
            } else {
                runOnUiThread(() ->
                    Toast.makeText(EscanearQRActivity.this,
                        "QR no reconocido. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                );
                barcodeView.postDelayed(() -> procesando = false, 1500);
            }
        }

        @Override
        public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {}
    };

    private void abrirProyecto(String id) {
        String rol = getIntent().getStringExtra(EXTRA_ROL);

        if (ROL_ASESOR.equals(rol)) {
            // ProyectoDetalleActivity usa índice entero (0–2); navegar directamente
            int index = 0;
            try { index = Integer.parseInt(id); } catch (NumberFormatException ignored) {}
            index = Math.max(0, Math.min(index, 2));
            Intent intent = new Intent(this, ProyectoDetalleActivity.class);
            intent.putExtra(ProyectoDetalleActivity.EXTRA_PROYECTO_INDEX, index);
            startActivity(intent);
        } else {
            // Cliente: usar deep link → el sistema enruta a ProjectDetailActivity
            Uri deepLinkUri = new Uri.Builder()
                    .scheme("inmobiliaria")
                    .authority("proyecto")
                    .appendPath(id)
                    .build();
            Log.d("DeepLink", "Lanzando deep link: " + deepLinkUri);
            Intent intent = new Intent(Intent.ACTION_VIEW, deepLinkUri);
            intent.setPackage(getPackageName());
            startActivity(intent);
        }
        finish();
    }

    private void toggleLinterna() {
        linternaEncendida = !linternaEncendida;
        if (linternaEncendida) {
            barcodeView.setTorchOn();
            btnLinterna.setAlpha(1.0f);
        } else {
            barcodeView.setTorchOff();
            btnLinterna.setAlpha(0.5f);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // La cámara ya está corriendo (onResume fue llamado antes del diálogo)
                barcodeView.decodeContinuous(callback);
            } else {
                Toast.makeText(this,
                    "Se necesita permiso de cámara para escanear QR.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
