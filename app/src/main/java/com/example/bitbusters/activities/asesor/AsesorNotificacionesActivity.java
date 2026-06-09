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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AsesorNotificacionesActivity extends AppCompatActivity {

    private static final String TAG = "ASESOR_NOTIF";

    private ActivityAsesorNotificacionesBinding binding;
    private NotifAdapter adapter;

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
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("role", "asesor")
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(snapshots -> {
                List<AsesorNotif> list;
                if (!snapshots.isEmpty()) {
                    list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String titulo     = doc.getString("titulo");
                        String descripcion = doc.getString("descripcion");
                        String tiempo     = doc.getString("tiempo");
                        String tipo       = doc.getString("tipo");
                        if (titulo != null && tipo != null) {
                            list.add(new AsesorNotif(
                                    titulo,
                                    descripcion != null ? descripcion : "",
                                    tiempo      != null ? tiempo      : "",
                                    tipo));
                        }
                    }
                } else {
                    list = defaultNotificaciones();
                }
                AsesorStorage.saveNotificaciones(this, list);
                AsesorStorage.resetNotifCount(this);
                List<Notif> notifs = new ArrayList<>();
                for (AsesorNotif n : list) notifs.add(toNotif(n));
                adapter.setData(notifs);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Firestore fetch failed, using defaults", e);
                List<AsesorNotif> list = defaultNotificaciones();
                AsesorStorage.saveNotificaciones(this, list);
                AsesorStorage.resetNotifCount(this);
                List<Notif> notifs = new ArrayList<>();
                for (AsesorNotif n : list) notifs.add(toNotif(n));
                adapter.setData(notifs);
            });
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

    private List<AsesorNotif> defaultNotificaciones() {
        List<AsesorNotif> list = new ArrayList<>();
        list.add(new AsesorNotif("Cita confirmada",
            "Carlos Mendoza confirmó su visita para el lun. 7 Abr a las 10:30 AM.",
            "5 min", AsesorNotif.TIPO_CITA));
        list.add(new AsesorNotif("Nueva separación registrada",
            "Rosa Torres registró una separación en Torres del Sol · Dpto 108.",
            "20 min", AsesorNotif.TIPO_SEPARACION));
        list.add(new AsesorNotif("Cita pendiente sin confirmar",
            "Ana López tiene una cita el mar. 8 Abr. Confirma antes del mediodía.",
            "1 h", AsesorNotif.TIPO_CITA));
        list.add(new AsesorNotif("Valoración recibida",
            "Jorge Castro valoró su visita con 4/5 estrellas.",
            "3 h", AsesorNotif.TIPO_VALORACION));
        list.add(new AsesorNotif("Separación vencida",
            "La separación SEP-2025-0043 (Rosa Torres) venció. Revisión requerida.",
            "Ayer", AsesorNotif.TIPO_ALERTA));
        list.add(new AsesorNotif("Nuevo mensaje",
            "Marco Paredes envió un mensaje: '¿El departamento tiene estacionamiento?'",
            "Ayer", AsesorNotif.TIPO_MENSAJE));
        list.add(new AsesorNotif("Recordatorio de cita",
            "Mañana tienes 3 citas agendadas. Revisa tu calendario.",
            "Lun", AsesorNotif.TIPO_CITA));
        return list;
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
