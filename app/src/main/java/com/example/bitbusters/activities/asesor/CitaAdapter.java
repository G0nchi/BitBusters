package com.example.bitbusters.activities.asesor;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.ViewHolder> {

    public static class Cita {
        public final String initials;
        public final int avatarColor;
        public final String nombre;
        public final String proyecto;
        public final String fecha;
        public final String hora;
        public final String badge;
        public final int badgeBg;
        public final int badgeText;
        public final String btnLeft;
        public final String btnRight;
        public final boolean showSeparacion;
        public final boolean showRating;

        public Cita(String initials, int avatarColor, String nombre, String proyecto,
                    String fecha, String hora, String badge, int badgeBg, int badgeText,
                    String btnLeft, String btnRight, boolean showSeparacion, boolean showRating) {
            this.initials = initials;
            this.avatarColor = avatarColor;
            this.nombre = nombre;
            this.proyecto = proyecto;
            this.fecha = fecha;
            this.hora = hora;
            this.badge = badge;
            this.badgeBg = badgeBg;
            this.badgeText = badgeText;
            this.btnLeft = btnLeft;
            this.btnRight = btnRight;
            this.showSeparacion = showSeparacion;
            this.showRating = showRating;
        }
    }

    public interface OnCitaActionListener {
        void onLeftClick(int position, Cita cita);
        void onRightClick(int position, Cita cita);
    }

    private final List<Cita> citas;
    private final OnCitaActionListener listener;

    public CitaAdapter(List<Cita> citas, OnCitaActionListener listener) {
        this.citas = citas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Cita c = citas.get(position);
        h.tvInitials.setText(c.initials);
        h.cvAvatar.setCardBackgroundColor(c.avatarColor);
        h.tvNombre.setText(c.nombre);
        h.tvProyecto.setText(c.proyecto);
        h.tvFecha.setText(c.fecha);
        h.tvHora.setText(c.hora);
        h.tvBadge.setText(c.badge);

        switch (c.badge) {
            case "Pendiente":
                h.tvBadge.setBackgroundResource(R.drawable.badge_pendiente);
                h.tvBadge.setTextColor(c.badgeText);
                break;
            case "Confirmada":
                h.tvBadge.setBackgroundResource(R.drawable.badge_confirmada);
                h.tvBadge.setTextColor(c.badgeText);
                break;
            case "Realizada":
            case "Cancelada":
                h.tvBadge.setBackgroundResource(R.drawable.badge_pasada);
                h.tvBadge.setTextColor(c.badgeText);
                break;
            case "Valorada":
                h.tvBadge.setBackgroundResource(R.drawable.badge_valorada);
                h.tvBadge.setTextColor(c.badgeText);
                break;
            default:
                h.tvBadge.setBackgroundResource(R.drawable.badge_pasada);
                h.tvBadge.setTextColor(c.badgeText);
        }

        h.llSeparacion.setVisibility(c.showSeparacion ? View.VISIBLE : View.GONE);
        h.llRating.setVisibility(c.showRating ? View.VISIBLE : View.GONE);
        h.btnLeft.setText(c.btnLeft);
        h.btnRight.setText(c.btnRight);

        h.btnLeft.setOnClickListener(v -> listener.onLeftClick(h.getAdapterPosition(), c));
        h.btnRight.setOnClickListener(v -> listener.onRightClick(h.getAdapterPosition(), c));
    }

    @Override
    public int getItemCount() { return citas.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cvAvatar;
        TextView tvInitials, tvNombre, tvProyecto, tvFecha, tvHora, tvBadge;
        LinearLayout llSeparacion, llRating;
        MaterialButton btnLeft, btnRight;

        ViewHolder(View v) {
            super(v);
            cvAvatar = v.findViewById(R.id.cv_avatar);
            tvInitials = v.findViewById(R.id.tv_initials);
            tvNombre = v.findViewById(R.id.tv_nombre);
            tvProyecto = v.findViewById(R.id.tv_proyecto);
            tvFecha = v.findViewById(R.id.tv_fecha);
            tvHora = v.findViewById(R.id.tv_hora);
            tvBadge = v.findViewById(R.id.tv_badge);
            llSeparacion = v.findViewById(R.id.ll_separacion);
            llRating = v.findViewById(R.id.ll_rating);
            btnLeft = v.findViewById(R.id.btn_left);
            btnRight = v.findViewById(R.id.btn_right);
        }
    }
}
