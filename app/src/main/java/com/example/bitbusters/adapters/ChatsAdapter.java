package com.example.bitbusters.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitbusters.R;
import com.example.bitbusters.models.Chat;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatsAdapter(List<Chat> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.tvName.setText(chat.getName());
        holder.tvLastMessage.setText(chat.getLastMessage());
        holder.tvTime.setText(chat.getTime());
        holder.tvInitials.setText(chat.getInitials());
        holder.cardInitials.setCardBackgroundColor(Color.parseColor(chat.getColorHex()));
        
        if (chat.getUnreadCount() > 0) {
            holder.cardUnread.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(chat.getUnreadCount()));
        } else {
            holder.cardUnread.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void removeItem(int position) {
        chatList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime, tvInitials, tvUnreadCount;
        MaterialCardView cardInitials, cardUnread;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChatName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvChatTime);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            cardInitials = itemView.findViewById(R.id.cardInitials);
            cardUnread = itemView.findViewById(R.id.cardUnread);
        }
    }
}
