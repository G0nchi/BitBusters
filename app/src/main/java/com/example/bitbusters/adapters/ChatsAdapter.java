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

/**
 * Adapter multi-tipo para la lista de mensajes.
 * Soporta dos tipos de item: encabezado de sección y fila de chat.
 *
 * La lista recibe objetos:
 *   - String  → encabezado de sección (ej. "ACTIVOS", "FINALIZADAS")
 *   - Chat    → fila de conversación
 */
public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CHAT   = 1;

    private List<Object> items;
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatsAdapter(List<Object> items, OnChatClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_CHAT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_chat_section_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_chat, parent, false);
            return new ChatViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tvHeader.setText((String) items.get(position));
        } else {
            Chat chat = (Chat) items.get(position);
            ChatViewHolder h = (ChatViewHolder) holder;
            h.tvName.setText(chat.getName());
            h.tvLastMessage.setText(chat.getLastMessage());
            h.tvTime.setText(chat.getTime());
            h.tvInitials.setText(chat.getInitials());
            h.cardInitials.setCardBackgroundColor(Color.parseColor(chat.getColorHex()));

            if (chat.getUnreadCount() > 0) {
                h.cardUnread.setVisibility(View.VISIBLE);
                h.tvUnreadCount.setText(String.valueOf(chat.getUnreadCount()));
            } else {
                h.cardUnread.setVisibility(View.GONE);
            }

            // Chats finalizados con avatar más apagado
            float alpha = chat.isRecent() ? 1f : 0.6f;
            h.tvName.setAlpha(alpha);
            h.tvLastMessage.setAlpha(alpha);

            h.itemView.setOnClickListener(v -> listener.onChatClick(chat));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()
                && items.get(position) instanceof Chat) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ── ViewHolders ────────────────────────────────────────────────────────────

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderVH(View v) {
            super(v);
            tvHeader = (TextView) v;
        }
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime, tvInitials, tvUnreadCount;
        MaterialCardView cardInitials, cardUnread;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName        = itemView.findViewById(R.id.tvChatName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime        = itemView.findViewById(R.id.tvChatTime);
            tvInitials    = itemView.findViewById(R.id.tvInitials);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            cardInitials  = itemView.findViewById(R.id.cardInitials);
            cardUnread    = itemView.findViewById(R.id.cardUnread);
        }
    }
}
