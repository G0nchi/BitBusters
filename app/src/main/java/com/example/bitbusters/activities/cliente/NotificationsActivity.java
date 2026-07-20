package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.NotificationsAdapter;
import com.example.bitbusters.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "CLIENTE_NOTIF";

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private ProgressBar progressNotifications;
    private View layoutNotificationsError;
    private TextView tvNotificationsError;
    private View layoutNotificationsContent;
    private TextView tvNotificationsEmpty;
    private ListenerRegistration notificationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        progressNotifications = findViewById(R.id.progressNotifications);
        layoutNotificationsError = findViewById(R.id.layoutNotificationsError);
        tvNotificationsError = findViewById(R.id.tvNotificationsError);
        layoutNotificationsContent = findViewById(R.id.layoutNotificationsContent);
        tvNotificationsEmpty = findViewById(R.id.tvNotificationsEmpty);
        findViewById(R.id.btnRetryNotifications).setOnClickListener(v -> loadNotificationsFromFirestore());

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationsAdapter(new ArrayList<>(), this::abrirNotificacion);
        recyclerView.setAdapter(adapter);

        setupSwipeToDelete();
        setupTabs();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadNotificationsFromFirestore();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notificationsListener != null) {
            notificationsListener.remove();
            notificationsListener = null;
        }
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        if (tabLayout == null) return;

        TabLayout.Tab tabNotificaciones = tabLayout.getTabAt(0);
        if (tabNotificaciones != null) tabNotificaciones.select();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    startActivity(new Intent(NotificationsActivity.this, MessagesActivity.class));
                    finish();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadNotificationsFromFirestore() {
        mostrarCargando();

        if (notificationsListener != null) {
            notificationsListener.remove();
            notificationsListener = null;
        }

        notificationsListener = FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("role", "cliente")
            .addSnapshotListener((snapshots, e) -> {
                if (e != null) {
                    Log.e(TAG, "Firestore listener failed", e);
                    mostrarError("No se pudieron cargar las notificaciones: " + e.getMessage());
                    return;
                }

                List<Notification> list = new ArrayList<>();
                if (snapshots != null) {
                    List<DocumentSnapshot> docs = new ArrayList<>(snapshots.getDocuments());
                    docs.sort((a, b) -> Long.compare(getOrderValue(b), getOrderValue(a)));
                    String uidClienteActual = FirebaseAuth.getInstance().getCurrentUser() != null
                            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                            : "";
                    for (DocumentSnapshot doc : docs) {
                        if (!perteneceAlClienteActual(doc, uidClienteActual)) continue;
                        Notification notification = mapNotification(doc);
                        if (notification != null) list.add(notification);
                    }
                }

                adapter.setData(list);
                mostrarContenido();
            });
    }

    private Notification mapNotification(DocumentSnapshot doc) {
        String senderName = firstNonEmpty(doc.getString("senderName"), doc.getString("title"), "BitBusters");
        String descripcion = firstNonEmpty(doc.getString("descripcion"), doc.getString("body"), "");
        String tiempo = firstNonEmpty(doc.getString("tiempo"), doc.getString("time"), "Ahora");
        String avatarName = doc.getString("avatarName");
        String propertyName = doc.getString("propertyName");
        Boolean isOld = doc.getBoolean("isOld");
        String tipo = firstNonEmpty(doc.getString("tipo"), doc.getString("type"), "");
        String separacionId = doc.getString("separacionId");
        String proyectoId = firstNonEmpty(doc.getString("proyectoId"), separacionId, "");
        String proyectoNombre = firstNonEmpty(doc.getString("proyectoNombre"), doc.getString("proyecto"), "");
        String uidAsesor = doc.getString("uidAsesor");
        String inmobiliariaId = doc.getString("inmobiliariaId");
        String montoSeparacion = firstNonEmpty(doc.getString("montoSeparacion"), doc.getString("monto"), "");
        Long pagoVenceEnMillis = doc.getLong("pagoVenceEnMillis");

        if (descripcion.isEmpty()) return null;

        return new Notification(
                doc.getId(),
                senderName,
                descripcion,
                tiempo,
                resolveDrawable(avatarName),
                resolveDrawable(propertyName),
                isOld != null && isOld,
                tipo,
                separacionId,
                proyectoId,
                proyectoNombre,
                uidAsesor,
                inmobiliariaId,
                montoSeparacion,
                pagoVenceEnMillis != null ? pagoVenceEnMillis : 0L);
    }

    private void abrirNotificacion(Notification notification) {
        if (notification == null) return;

        if (!"separacion_aprobada".equals(notification.getTipo())) {
            return;
        }

        String proyectoNombre = firstNonEmpty(notification.getProyectoNombre(), "", "");
        if (proyectoNombre.isEmpty()) {
            mostrarError("La notificación no tiene datos suficientes para continuar con el pago.");
            return;
        }

        Intent intent = new Intent(this, PaymentMethodActivity.class);
        intent.putExtra(PaymentMethodActivity.EXTRA_SEPARACION_ID, notification.getSeparacionId());
        intent.putExtra(PaymentMethodActivity.EXTRA_PROYECTO_ID, notification.getProyectoId());
        intent.putExtra(PaymentMethodActivity.EXTRA_PROYECTO_NOMBRE, proyectoNombre);
        intent.putExtra(PaymentMethodActivity.EXTRA_UID_ASESOR, notification.getUidAsesor());
        intent.putExtra(PaymentMethodActivity.EXTRA_INMOBILIARIA_ID, notification.getInmobiliariaId());
        intent.putExtra(PaymentMethodActivity.EXTRA_MONTO_SEPARACION, notification.getMontoSeparacion());
        intent.putExtra(PaymentMethodActivity.EXTRA_PAGO_VENCE_EN_MILLIS, notification.getPagoVenceEnMillis());
        startActivity(intent);
    }

    private boolean perteneceAlClienteActual(DocumentSnapshot doc, String uidClienteActual) {
        if (uidClienteActual == null || uidClienteActual.isEmpty()) {
            return true;
        }
        String targetUid = firstNonEmpty(
                doc.getString("targetUid"),
                doc.getString("clienteUid"),
                doc.getString("uidCliente")
        );
        String tipo = firstNonEmpty(doc.getString("tipo"), doc.getString("type"), "");
        if (targetUid.isEmpty() && tipo.startsWith("separacion_")) {
            return false;
        }
        return targetUid.isEmpty() || uidClienteActual.equals(targetUid);
    }

    private static String firstNonEmpty(String primary, String secondary, String fallback) {
        if (primary != null && !primary.trim().isEmpty()) return primary;
        if (secondary != null && !secondary.trim().isEmpty()) return secondary;
        return fallback;
    }

    private static long getOrderValue(DocumentSnapshot doc) {
        Object order = doc.get("order");
        if (order instanceof Number) return ((Number) order).longValue();
        try {
            return order != null ? Long.parseLong(order.toString()) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void mostrarCargando() {
        if (progressNotifications != null) progressNotifications.setVisibility(View.VISIBLE);
        if (layoutNotificationsError != null) layoutNotificationsError.setVisibility(View.GONE);
        if (layoutNotificationsContent != null) layoutNotificationsContent.setVisibility(View.GONE);
        if (tvNotificationsEmpty != null) tvNotificationsEmpty.setVisibility(View.GONE);
    }

    private void mostrarContenido() {
        if (progressNotifications != null) progressNotifications.setVisibility(View.GONE);
        if (layoutNotificationsError != null) layoutNotificationsError.setVisibility(View.GONE);
        if (layoutNotificationsContent != null) layoutNotificationsContent.setVisibility(View.VISIBLE);
        if (tvNotificationsEmpty != null) {
            tvNotificationsEmpty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void mostrarError(String mensaje) {
        if (progressNotifications != null) progressNotifications.setVisibility(View.GONE);
        if (layoutNotificationsContent != null) layoutNotificationsContent.setVisibility(View.GONE);
        if (layoutNotificationsError != null) layoutNotificationsError.setVisibility(View.VISIBLE);
        if (tvNotificationsError != null) tvNotificationsError.setText(mensaje);
        if (tvNotificationsEmpty != null) tvNotificationsEmpty.setVisibility(View.GONE);
    }

    private int resolveDrawable(String name) {
        if (name == null || name.isEmpty()) return 0;
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                adapter.removeItem(viewHolder.getAdapterPosition());
                if (tvNotificationsEmpty != null && adapter.getItemCount() == 0) {
                    tvNotificationsEmpty.setVisibility(View.VISIBLE);
                }
            }
        }).attachToRecyclerView(recyclerView);
    }
}
