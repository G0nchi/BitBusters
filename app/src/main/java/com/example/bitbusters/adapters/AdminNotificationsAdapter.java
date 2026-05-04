package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.Notification;

import java.util.List;

public class AdminNotificationsAdapter extends RecyclerView.Adapter<AdminNotificationsAdapter.AdminNotificationViewHolder> {

    private List<Notification> notificationList;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public AdminNotificationsAdapter(List<Notification> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new AdminNotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminNotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.tvName.setText(notification.getName());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTime.setText(notification.getTime());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    public void setData(List<Notification> nuevaLista) {
        this.notificationList = nuevaLista;
        notifyDataSetChanged();
    }

    public static class AdminNotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMessage, tvTime;
        ImageView imgAvatar;

        public AdminNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
