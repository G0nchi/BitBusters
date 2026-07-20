package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminNotificationsAdapter;
import com.example.bitbusters.data.AdminNotificacionesRepository;
import com.example.bitbusters.models.AdminNotificacion;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

/**
 * Pantalla de historial de notificaciones del Administrador (Parte 5 — Lab 5).
 *
 * Muestra todas las notificaciones lanzadas en esta sesión, ordenadas de más
 * reciente a más antigua. Si no hay ninguna, muestra el estado vacío.
 *
 * Los datos vienen de AdminNotificacionesRepository (en memoria), que es
 * alimentado automáticamente por NotificationHelper.lanzarNotificacionAdmin().
 */
public class AdminNotificacionesActivity extends AppCompatActivity {

    private RecyclerView rvNotificaciones;
    private AdminNotificationsAdapter adapter;
    private TextView tvEstadoVacio;
    private ListenerRegistration notificacionesListener;
    private List<AdminNotificacion> listaActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notificaciones);

        // Botón de retroceso
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvEstadoVacio = findViewById(R.id.tvEstadoVacio);
        inicializarRecyclerView();
    }

    /**
     * Se llama cada vez que la pantalla vuelve al frente.
     * Refresca la lista para mostrar notificaciones lanzadas mientras estuvo en segundo plano.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refrescarLista();
    }

    @Override
    protected void onStart() {
        super.onStart();
        escucharNotificacionesFirestore();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notificacionesListener != null) {
            notificacionesListener.remove();
            notificacionesListener = null;
        }
    }

    // ── Inicialización ───────────────────────────────────────────────────────

    /** Configura el RecyclerView con el adapter y el LayoutManager. */
    private void inicializarRecyclerView() {
        rvNotificaciones = findViewById(R.id.rvNotificaciones);
        if (rvNotificaciones != null) {
            rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));
            listaActual = AdminNotificacionesRepository.getLista();
            adapter = new AdminNotificationsAdapter(
                    listaActual,
                    notificacion -> { /* sin acción adicional al tocar un ítem */ }
            );
            rvNotificaciones.setAdapter(adapter);
        }
        actualizarEstadoVacio();
    }

    // ── Actualización de la UI ───────────────────────────────────────────────

    /**
     * Recarga la lista desde el repositorio y actualiza el adapter.
     * Muestra el estado vacío si no hay notificaciones.
     */
    private void refrescarLista() {
        if (adapter == null) return;
        if (listaActual == null) {
            listaActual = AdminNotificacionesRepository.getLista();
        }
        adapter.setData(listaActual);
        actualizarEstadoVacio();
    }

    private void escucharNotificacionesFirestore() {
        if (notificacionesListener != null) {
            notificacionesListener.remove();
        }

        notificacionesListener = AdminNotificacionesRepository.escucharDesdeFirestore(
                this,
                new AdminNotificacionesRepository.NotificacionesListener() {
                    @Override
                    public void onNotificacionesActualizadas(List<AdminNotificacion> notificaciones) {
                        listaActual = notificaciones;
                        if (adapter != null) {
                            adapter.setData(listaActual);
                        }
                        actualizarEstadoVacio();
                    }

                    @Override
                    public void onError(String mensaje) {
                        listaActual = AdminNotificacionesRepository.getLista();
                        if (adapter != null) {
                            adapter.setData(listaActual);
                        }
                        actualizarEstadoVacio();
                    }
                });
    }

    /**
     * Muestra u oculta el TextView de estado vacío según si hay notificaciones.
     * - Lista vacía → oculta RecyclerView, muestra "No hay notificaciones"
     * - Lista con datos → muestra RecyclerView, oculta el estado vacío
     */
    private void actualizarEstadoVacio() {
        boolean vacia = listaActual == null || listaActual.isEmpty();

        if (tvEstadoVacio != null) {
            tvEstadoVacio.setVisibility(vacia ? View.VISIBLE : View.GONE);
        }
        if (rvNotificaciones != null) {
            rvNotificaciones.setVisibility(vacia ? View.GONE : View.VISIBLE);
        }
    }
}
