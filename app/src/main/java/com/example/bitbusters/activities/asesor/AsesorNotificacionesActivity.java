package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityAsesorNotificacionesBinding;
import com.example.bitbusters.models.AsesorNotif;
import com.example.bitbusters.utils.AsesorStorage;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lee notificaciones del asesor de DOS esquemas que coexisten hoy en el equipo:
 *
 *  - Anidado {@code notifications/{uid}/items} — usado por AsesorHomeActivity
 *    y SuperadminApprovalEvaluationActivity (ya en master).
 *  - Plano {@code notifications/{id}} con {@code targetUid} — contrato oficial
 *    para separaciones descrito en INTEGRACION_FINAL_ADMIN_CLIENTE_ASESOR_SUPERADMIN.md
 *    (separacion_registrada/aprobada/rechazada/pagada).
 *
 * Se combinan y ordenan por fecha antes de mostrarse.
 */
public class AsesorNotificacionesActivity extends AppCompatActivity {

    private static final String TAG = "ASESOR_NOTIF";

    private ActivityAsesorNotificacionesBinding binding;
    private NotifAdapter adapter;

    private static class Entrada {
        final AsesorNotif notif;
        final Date fecha;
        Entrada(AsesorNotif notif, Date fecha) { this.notif = notif; this.fecha = fecha; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAsesorNotificacionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());
        setupRecyclerView();
        loadNotificationsFromFirestore();
    }

    private void setupRecyclerView() {
        binding.rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotificaciones.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new NotifAdapter(new ArrayList<>());
        binding.rvNotificaciones.setAdapter(adapter);
    }

    private void loadNotificationsFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            adapter.setData(new ArrayList<>());
            return;
        }
        String uid = user.getUid();

        List<Entrada> combinadas = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger pendientes = new AtomicInteger(2);

        Runnable alTerminarAmbas = () -> {
            if (pendientes.decrementAndGet() != 0) return;
            combinadas.sort(Comparator.comparing(
                    (Entrada e) -> e.fecha != null ? e.fecha : new Date(0)).reversed());
            List<Notif> notifs = new ArrayList<>();
            for (Entrada e : combinadas) notifs.add(toNotif(e.notif));
            adapter.setData(notifs);
            AsesorStorage.resetNotifCount(this);
        };

        FirebaseFirestore.getInstance()
            .collection("notifications")
            .document(uid)
            .collection("items")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshots -> {
                for (QueryDocumentSnapshot doc : snapshots) {
                    String title = doc.getString("title");
                    if (title == null) continue;
                    String body = doc.getString("body");
                    String type = doc.getString("type");
                    Date timestamp = doc.getDate("timestamp");

                    combinadas.add(new Entrada(new AsesorNotif(
                            title, body != null ? body : "",
                            tiempoRelativo(timestamp), tipoLocalPara(type)), timestamp));

                    if (Boolean.FALSE.equals(doc.getBoolean("read"))) {
                        doc.getReference().update("read", true);
                    }
                }
                alTerminarAmbas.run();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "No se pudieron cargar notificaciones (anidado)", e);
                alTerminarAmbas.run();
            });

        FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("targetUid", uid)
            .get()
            .addOnSuccessListener(snapshots -> {
                for (QueryDocumentSnapshot doc : snapshots) {
                    String title = doc.getString("title");
                    if (title == null) continue;
                    String descripcion = doc.getString("descripcion");
                    String type = doc.getString("type");
                    Date createdAt = doc.getDate("createdAt");

                    combinadas.add(new Entrada(new AsesorNotif(
                            title, descripcion != null ? descripcion : "",
                            tiempoRelativo(createdAt), tipoLocalPara(type)), createdAt));

                    if (Boolean.FALSE.equals(doc.getBoolean("read"))) {
                        doc.getReference().update("read", true);
                    }
                }
                alTerminarAmbas.run();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "No se pudieron cargar notificaciones (plano)", e);
                alTerminarAmbas.run();
            });
    }

    private static String tipoLocalPara(String type) {
        if (type == null) return AsesorNotif.TIPO_ALERTA;
        switch (type) {
            case "nueva_separacion":
            case "separacion_registrada":
            case "separacion_aprobada":
            case "separacion_rechazada":
            case "separacion_pagada":
                return AsesorNotif.TIPO_SEPARACION;
            case "approval_accepted":
            case "approval_rejected": return AsesorNotif.TIPO_VALORACION;
            case "mensaje": return AsesorNotif.TIPO_MENSAJE;
            case "cita": return AsesorNotif.TIPO_CITA;
            default: return AsesorNotif.TIPO_ALERTA;
        }
    }

    private static String tiempoRelativo(Date fecha) {
        if (fecha == null) return "";
        long diffMs = System.currentTimeMillis() - fecha.getTime();
        if (diffMs < 0) diffMs = 0;
        long minutos = TimeUnit.MILLISECONDS.toMinutes(diffMs);
        if (minutos < 1) return "Ahora";
        if (minutos < 60) return minutos + " min";
        long horas = TimeUnit.MILLISECONDS.toHours(diffMs);
        if (horas < 24) return horas + " h";
        long dias = TimeUnit.MILLISECONDS.toDays(diffMs);
        if (dias == 1) return "Ayer";
        return dias + " d";
    }

    private Notif toNotif(AsesorNotif n) {
        switch (n.tipo) {
            case AsesorNotif.TIPO_SEPARACION:
                return new Notif(n.titulo, n.descripcion, n.tiempo,
                    R.drawable.ic_star_filled, "#FFF3DC", "#9A5700");
            case AsesorNotif.TIPO_MENSAJE:
                return new Notif(n.titulo, n.descripcion, n.tiempo,
                    R.drawable.ic_nav_chat, "#DFFBEC", "#186A3B");
            case AsesorNotif.TIPO_VALORACION:
                return new Notif(n.titulo, n.descripcion, n.tiempo,
                    R.drawable.ic_star_filled, "#E8F4FF", "#1A5799");
            case AsesorNotif.TIPO_ALERTA:
                return new Notif(n.titulo, n.descripcion, n.tiempo,
                    R.drawable.ic_bell, "#FFE8E8", "#CC2222");
            case AsesorNotif.TIPO_CITA:
            default:
                return new Notif(n.titulo, n.descripcion, n.tiempo,
                    R.drawable.ic_nav_calendar, "#DFFBEC", "#186A3B");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    // ── Inner classes ───────────────────────────────────────────────────────────

    static class Notif {
        final String titulo, descripcion, tiempo;
        final int iconRes;
        final String bgColor, tintColor;

        Notif(String titulo, String descripcion, String tiempo,
              int iconRes, String bgColor, String tintColor) {
            this.titulo      = titulo;
            this.descripcion = descripcion;
            this.tiempo      = tiempo;
            this.iconRes     = iconRes;
            this.bgColor     = bgColor;
            this.tintColor   = tintColor;
        }
    }

    static class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.VH> {
        private final List<Notif> items;

        NotifAdapter(List<Notif> items) { this.items = items; }

        void setData(List<Notif> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion_asesor, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Notif n = items.get(position);
            h.tvTitulo.setText(n.titulo);
            h.tvDesc.setText(n.descripcion);
            h.tvTiempo.setText(n.tiempo);
            h.ivIcon.setImageResource(n.iconRes);
            h.ivIcon.setColorFilter(Color.parseColor(n.tintColor));
            h.cvIcon.setCardBackgroundColor(Color.parseColor(n.bgColor));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitulo, tvDesc, tvTiempo;
            ImageView ivIcon;
            MaterialCardView cvIcon;

            VH(View v) {
                super(v);
                tvTitulo = v.findViewById(R.id.tv_titulo);
                tvDesc   = v.findViewById(R.id.tv_descripcion);
                tvTiempo = v.findViewById(R.id.tv_tiempo);
                ivIcon   = v.findViewById(R.id.iv_icon);
                cvIcon   = v.findViewById(R.id.cv_icon);
            }
        }
    }
}
