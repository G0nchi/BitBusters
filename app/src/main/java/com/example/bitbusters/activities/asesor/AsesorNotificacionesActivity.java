package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.os.Bundle;
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
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;

public class AsesorNotificacionesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_notificaciones);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_notificaciones);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rv.setAdapter(new NotifAdapter(buildNotificaciones()));
    }

    private List<Notif> buildNotificaciones() {
        return Arrays.asList(
            new Notif("Cita confirmada", "Carlos Mendoza confirmó su visita para el lun. 7 Abr a las 10:30 AM.", "5 min", R.drawable.ic_nav_calendar, "#DFFBEC", "#186A3B"),
            new Notif("Nueva separación registrada", "Rosa Torres registró una separación en Torres del Sol · Dpto 108.", "20 min", R.drawable.ic_star_filled, "#FFF3DC", "#9A5700"),
            new Notif("Cita pendiente sin confirmar", "Ana López tiene una cita el mar. 8 Abr. Confirma antes del mediodía.", "1 h", R.drawable.ic_nav_calendar, "#FFF3DC", "#9A5700"),
            new Notif("Valoración recibida", "Jorge Castro valoró su visita con 4/5 estrellas.", "3 h", R.drawable.ic_star_filled, "#E8F4FF", "#1A5799"),
            new Notif("Separación vencida", "La separación SEP-2025-0043 (Rosa Torres) venció. Revisión requerida.", "Ayer", R.drawable.ic_bell, "#FFE8E8", "#CC2222"),
            new Notif("Nuevo mensaje", "Marco Paredes envió un mensaje: '¿El departamento tiene estacionamiento?'", "Ayer", R.drawable.ic_nav_chat, "#DFFBEC", "#186A3B"),
            new Notif("Recordatorio de cita", "Mañana tienes 3 citas agendadas. Revisa tu calendario.", "Lun", R.drawable.ic_nav_calendar, "#EAF0F4", "#234F68")
        );
    }

    static class Notif {
        final String titulo, descripcion, tiempo;
        final int iconRes;
        final String bgColor, tintColor;

        Notif(String titulo, String descripcion, String tiempo, int iconRes, String bgColor, String tintColor) {
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.tiempo = tiempo;
            this.iconRes = iconRes;
            this.bgColor = bgColor;
            this.tintColor = tintColor;
        }
    }

    static class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.VH> {
        private final List<Notif> items;

        NotifAdapter(List<Notif> items) { this.items = items; }

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
                tvDesc = v.findViewById(R.id.tv_descripcion);
                tvTiempo = v.findViewById(R.id.tv_tiempo);
                ivIcon = v.findViewById(R.id.iv_icon);
                cvIcon = v.findViewById(R.id.cv_icon);
            }
        }
    }
}
