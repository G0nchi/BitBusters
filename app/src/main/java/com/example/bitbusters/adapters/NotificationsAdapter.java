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

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;

    public NotificationsAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.tvName.setText(notification.getName());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTime.setText(notification.getTime());
        holder.imgAvatar.setImageResource(notification.getAvatarResId());
        
        if (notification.getPropertyResId() != 0) {
            holder.imgProperty.setVisibility(View.VISIBLE);
            holder.imgProperty.setImageResource(notification.getPropertyResId());
        } else {
            holder.imgProperty.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void removeItem(int position) {
        notificationList.remove(position);
        notifyItemRemoved(position);
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMessage, tvTime;
        ImageView imgAvatar, imgProperty;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgProperty = itemView.findViewById(R.id.imgProperty);
        }
    }
}
