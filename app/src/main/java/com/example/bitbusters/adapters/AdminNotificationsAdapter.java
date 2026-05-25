package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.AdminNotificacion;

import java.util.List;

/**
 * Adapter para el RecyclerView de AdminNotificacionesActivity.
 * Muestra la lista de notificaciones lanzadas por el Administrador en esta sesión.
 * Usa el modelo AdminNotificacion (Parte 5 — Lab 5) en lugar del genérico Notification.
 */
public class AdminNotificationsAdapter
        extends RecyclerView.Adapter<AdminNotificationsAdapter.AdminNotificationViewHolder> {

    private List<AdminNotificacion> notificationList;
    private final OnNotificationClickListener listener;

    /** Interfaz de callback para el click en una notificación. */
    public interface OnNotificationClickListener {
        void onNotificationClick(AdminNotificacion notificacion);
    }

    public AdminNotificationsAdapter(List<AdminNotificacion> notificationList,
                                     OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener         = listener;
    }

    @NonNull
    @Override
    public AdminNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new AdminNotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminNotificationViewHolder holder, int position) {
        AdminNotificacion notif = notificationList.get(position);

        // titulo → campo "nombre" del layout item_notification
        holder.tvName.setText(notif.getTitulo());
        // mensaje → campo "message"
        holder.tvMessage.setText(notif.getMensaje());
        // timestamp → campo "time"
        holder.tvTime.setText(notif.getTimestamp());

        // Ícono genérico de notificación del sistema
        if (holder.imgAvatar != null) {
            holder.imgAvatar.setImageResource(android.R.drawable.ic_dialog_info);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notif);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    /**
     * Reemplaza la lista actual y notifica al RecyclerView.
     * Llamar desde AdminNotificacionesActivity.onResume() para refrescar.
     *
     * @param nuevaLista Lista actualizada de AdminNotificacion.
     */
    public void setData(List<AdminNotificacion> nuevaLista) {
        this.notificationList = nuevaLista;
        notifyDataSetChanged();
    }

    // ── ViewHolder ───────────────────────────────────────────────────────────

    public static class AdminNotificationViewHolder extends RecyclerView.ViewHolder {
        TextView  tvName;     // título de la notificación
        TextView  tvMessage;  // mensaje/cuerpo
        TextView  tvTime;     // timestamp
        ImageView imgAvatar;  // ícono (usa ic_dialog_info)

        public AdminNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime    = itemView.findViewById(R.id.tvTime);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
