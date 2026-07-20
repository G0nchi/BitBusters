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
import com.example.bitbusters.repository.ClienteSeparacionRepository;
import com.example.bitbusters.workers.ClienteWorkHelper;
import com.example.bitbusters.utils.NotificationHelper;
import com.example.bitbusters.utils.PreferencesManager;
import com.google.firebase.auth.FirebaseAuth;

public class PaymentMethodActivity extends AppCompatActivity {

    public static final String EXTRA_SEPARACION_ID = "extra_separacion_id";
    public static final String EXTRA_PROYECTO_ID = "extra_proyecto_id";
    public static final String EXTRA_PROYECTO_NOMBRE = "extra_proyecto_nombre";
    public static final String EXTRA_UID_ASESOR = "extra_uid_asesor";
    public static final String EXTRA_INMOBILIARIA_ID = "extra_inmobiliaria_id";
    public static final String EXTRA_MONTO_SEPARACION = "extra_monto_separacion";

    private EditText etNombreTitular;
    private EditText etNumeroTarjeta;
    private EditText etFechaVencimiento;
    private EditText etCVV;
    private TextView tvNumeroTarjeta;
    private TextView tvNombreTarjeta;
    private TextView tvVencimiento;

    private final ClienteSeparacionRepository separacionRepository = new ClienteSeparacionRepository();
    private String separacionId;
    private String proyectoId;
    private String proyectoNombre;
    private String uidAsesor;
    private String inmobiliariaId;
    private String montoSeparacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        // Crear el canal de notificaciones (necesario para lanzar la notificación de pago)
        NotificationHelper.crearCanal(this);

        leerExtrasDeSeparacion();
        enlazarVistas();
        configurarAcciones();
        cargarTarjetaGuardada();
        configurarPreviewsTarjeta();

        // Guardar tarjeta
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarTarjeta());
    }

    private void leerExtrasDeSeparacion() {
        separacionId = getIntent().getStringExtra(EXTRA_SEPARACION_ID);
        proyectoId = getIntent().getStringExtra(EXTRA_PROYECTO_ID);
        proyectoNombre = getIntent().getStringExtra(EXTRA_PROYECTO_NOMBRE);
        uidAsesor = getIntent().getStringExtra(EXTRA_UID_ASESOR);
        inmobiliariaId = getIntent().getStringExtra(EXTRA_INMOBILIARIA_ID);
        montoSeparacion = getIntent().getStringExtra(EXTRA_MONTO_SEPARACION);
    }

    private void enlazarVistas() {
        etNombreTitular = findViewById(R.id.etNombreTitular);
        etNumeroTarjeta = findViewById(R.id.etNumeroTarjeta);
        etFechaVencimiento = findViewById(R.id.etFechaVencimiento);
        etCVV = findViewById(R.id.etCVV);
        tvNumeroTarjeta = findViewById(R.id.tvNumeroTarjeta);
        tvNombreTarjeta = findViewById(R.id.tvNombreTarjeta);
        tvVencimiento = findViewById(R.id.tvVencimiento);
    }

    private void configurarAcciones() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSaltar).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    private void configurarPreviewsTarjeta() {
        configurarPreviewNombre();
        configurarPreviewNumero();
        configurarPreviewVencimiento();
    }

    private void configurarPreviewNombre() {
        etNombreTitular.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {
                // Sin trabajo previo requerido para la vista previa.
            }
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                String nombre = s.toString().trim();
                tvNombreTarjeta.setText(nombre.isEmpty() ? "Olivia Johns" : nombre);
            }
            @Override public void afterTextChanged(Editable s) {
                // La actualización ocurre en onTextChanged.
            }
        });
    }

    private void configurarPreviewNumero() {
        etNumeroTarjeta.addTextChangedListener(new TextWatcher() {
            private boolean editando = false;
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {
                // El formato se resuelve en afterTextChanged.
            }
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                // No se usa; el formateo se hace después para evitar ciclos.
            }
            @Override public void afterTextChanged(Editable s) {
                if (editando) return;
                editando = true;
                String formateado = formatearNumeroTarjeta(s.toString());
                s.replace(0, s.length(), formateado);
                tvNumeroTarjeta.setText(resolverPreviewNumero(formateado));
                editando = false;
            }
        });
    }

    private void configurarPreviewVencimiento() {
        etFechaVencimiento.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {
                // No se requiere trabajo previo.
            }
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                String fecha = s.toString().trim();
                tvVencimiento.setText(fecha.isEmpty() ? "01/22" : fecha);
            }
            @Override public void afterTextChanged(Editable s) {
                // La actualización ocurre en onTextChanged.
            }
        });
    }

    private String formatearNumeroTarjeta(String rawValue) {
        String raw = rawValue.replace(" ", "");
        StringBuilder formateado = new StringBuilder();
        for (int i = 0; i < raw.length() && i < 16; i++) {
            if (i > 0 && i % 4 == 0) formateado.append(" ");
            formateado.append(raw.charAt(i));
        }
        return formateado.toString();
    }

    private String resolverPreviewNumero(String preview) {
        if (preview.length() < 4) {
            return "**** **** **** 1234";
        }
        String limpio = preview.replace(" ", "");
        String ultimos = limpio.substring(Math.max(0, limpio.length() - 4));
        return "**** **** **** " + ultimos;
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
        String numLimpio = numero.replace(" ", "");
        String ultimos4  = numLimpio.length() >= 4
                ? numLimpio.substring(numLimpio.length() - 4) : numLimpio;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Método de pago guardado")
                .setMessage("Tu tarjeta terminada en " + ultimos4 + " fue agregada correctamente.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    guardarTarjetaLocal(nombre, ultimos4, fecha);

                    if (hayFlujoSeparacionAprobada()) {
                        registrarPagoSeparacionAprobada(ultimos4);
                        return;
                    } else if (hayFlujoSeparacionProyecto()) {
                        crearSeparacionPendiente(ultimos4);
                    }

                    // Lanzar notificación de método de pago guardado
                    // Al tocarla, abre HomeActivity
                    Intent intentHome = new Intent(this, HomeActivity.class);
                    NotificationHelper.lanzarNotificacion(
                            this,
                            "Método de pago guardado",
                            "Tu tarjeta ha sido registrada exitosamente",
                            NotificationHelper.NOTIF_METODO_PAGO,
                            intentHome
                    );
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private boolean hayFlujoSeparacionProyecto() {
        return proyectoId != null && !proyectoId.isEmpty()
                && proyectoNombre != null && !proyectoNombre.isEmpty();
    }

    private boolean hayFlujoSeparacionAprobada() {
        return separacionId != null && !separacionId.isEmpty()
                && proyectoNombre != null && !proyectoNombre.isEmpty();
    }

    private void registrarPagoSeparacionAprobada(String ultimos4) {
        String uidCliente = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uidCliente == null || uidCliente.isEmpty()) {
            Toast.makeText(this, "Debes iniciar sesión para registrar el pago.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombreCliente = PreferencesManager.obtenerNombre(this);
        double monto = parseMontoSeparacion(montoSeparacion);

        separacionRepository.registrarPagoDeSeparacionAprobada(
            separacionId,
            new ClienteSeparacionRepository.SeparacionPendienteRequest(
                new ClienteSeparacionRepository.ClienteInfo(uidCliente, nombreCliente),
                new ClienteSeparacionRepository.ProyectoInfo(uidAsesor, proyectoId, proyectoNombre, inmobiliariaId),
                new ClienteSeparacionRepository.PagoInfo(monto, "tarjeta", ultimos4)))
                .addOnSuccessListener(v ->
                    finalizarPagoRegistrado())
                .addOnFailureListener(e ->
                    Toast.makeText(this, "No se pudo registrar el pago de la separación.", Toast.LENGTH_SHORT).show());
    }

    private void finalizarPagoRegistrado() {
        Intent intentHome = new Intent(this, HomeActivity.class);
        NotificationHelper.lanzarNotificacion(
                this,
                "Pago registrado",
                "Tu pago de separación fue registrado correctamente",
                NotificationHelper.NOTIF_METODO_PAGO,
                intentHome
        );
        Toast.makeText(this, "Pago registrado para la separación.", Toast.LENGTH_LONG).show();
        startActivity(intentHome);
        finish();
    }

    private void crearSeparacionPendiente(String ultimos4) {
        String uidCliente = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uidCliente == null || uidCliente.isEmpty()) {
            Toast.makeText(this, "Debes iniciar sesión para crear una separación.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombreCliente = PreferencesManager.obtenerNombre(this);
        double monto = parseMontoSeparacion(montoSeparacion);

        separacionRepository.crearSeparacionPendiente(
            new ClienteSeparacionRepository.SeparacionPendienteRequest(
                new ClienteSeparacionRepository.ClienteInfo(uidCliente, nombreCliente),
                new ClienteSeparacionRepository.ProyectoInfo(uidAsesor, proyectoId, proyectoNombre, inmobiliariaId),
                new ClienteSeparacionRepository.PagoInfo(monto, "tarjeta", ultimos4)))
                .addOnSuccessListener(ref -> {
                PreferencesManager.guardarSeparacionActiva(
                    this,
                    ref.getId(),
                    proyectoNombre,
                    System.currentTimeMillis() + 10 * 60 * 1000L);
                    ClienteWorkHelper.scheduleSeparacionDeadline(
                            this,
                            ref.getId(),
                            ref.getId(),
                            proyectoNombre);
                Toast.makeText(this,
                    "Separación creada. Tienes 10 minutos para completar el pago.",
                    Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "No se pudo registrar la separación.", Toast.LENGTH_SHORT).show());
    }

    private void guardarTarjetaLocal(String nombreTitular, String ultimos4, String fechaVencimiento) {
        getSharedPreferences("bitbusters_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("cliente_tarjeta_guardada", true)
                .putString("cliente_tarjeta_nombre", nombreTitular)
                .putString("cliente_tarjeta_ultimos4", ultimos4)
                .putString("cliente_tarjeta_vencimiento", fechaVencimiento)
                .apply();
    }

    private void cargarTarjetaGuardada() {
        android.content.SharedPreferences prefs = getSharedPreferences("bitbusters_prefs", MODE_PRIVATE);
        if (!prefs.getBoolean("cliente_tarjeta_guardada", false)) return;

        String nombre = prefs.getString("cliente_tarjeta_nombre", "");
        String ultimos4 = prefs.getString("cliente_tarjeta_ultimos4", "");
        String vencimiento = prefs.getString("cliente_tarjeta_vencimiento", "");

        if (etNombreTitular != null && !nombre.isEmpty()) etNombreTitular.setText(nombre);
        if (etNumeroTarjeta != null && !ultimos4.isEmpty()) etNumeroTarjeta.setText("**** **** **** " + ultimos4);
        if (etFechaVencimiento != null && !vencimiento.isEmpty()) etFechaVencimiento.setText(vencimiento);
    }

    private double parseMontoSeparacion(String montoStr) {
        if (montoStr == null || montoStr.trim().isEmpty()) return 0d;
        String limpio = montoStr.replaceAll("\\D", "");
        if (limpio.isEmpty()) return 0d;
        try {
            return Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            return 0d;
        }
    }
}
