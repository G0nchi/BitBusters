package com.example.bitbusters.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.Chat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Adapter para la lista de chats del CLIENTE, usando datos de Firestore (modelo Chat).
 */
public class ClienteChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CHAT   = 1;

    private static final String[] AVATAR_COLORS = {
        "#4ECDC4", "#FF6B9D", "#FF8C42", "#9B59B6", "#27AE60", "#3498DB", "#1A3D54"
    };
    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    private List<Object> items;
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ClienteChatsAdapter(List<Object> items, OnChatClickListener listener) {
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
        }
        View v = inflater.inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).tvHeader.setText((String) items.get(position));
            return;
        }

        Chat chat = (Chat) items.get(position);
        ChatViewHolder h = (ChatViewHolder) holder;

        String nombre = chat.getNombreAsesor() != null ? chat.getNombreAsesor() : "Asesor";
        h.tvName.setText(nombre);
        h.tvLastMessage.setText(chat.getUltimoMensaje() != null ? chat.getUltimoMensaje() : "");
        h.tvTime.setText(formatearTimestamp(chat));
        h.tvInitials.setText(obtenerIniciales(nombre));
        h.cardInitials.setCardBackgroundColor(Color.parseColor(colorParaNombre(nombre)));

        String proyecto = chat.getNombreProyecto();
        if (proyecto != null && !proyecto.isEmpty()) {
            h.tvProyecto.setVisibility(View.VISIBLE);
            h.tvProyecto.setText(proyecto);
        } else {
            h.tvProyecto.setVisibility(View.GONE);
        }

        h.cardUnread.setVisibility(View.GONE);
        h.llFinalizado.setVisibility(View.GONE);
        h.tvName.setAlpha(1f);
        h.tvLastMessage.setAlpha(1f);
        h.tvProyecto.setAlpha(1f);

        h.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<Object> newItems) {
        DiffUtil.DiffResult result =
                DiffUtil.calculateDiff(new ChatDiffCallback(this.items, newItems));
        this.items = newItems;
        result.dispatchUpdatesTo(this);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()
                && items.get(position) instanceof Chat) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String obtenerIniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 0) return "AS";
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
    }

    private static String colorParaNombre(String nombre) {
        int idx = Math.abs(nombre.hashCode()) % AVATAR_COLORS.length;
        return AVATAR_COLORS[idx];
    }

    private static String formatearTimestamp(Chat chat) {
        if (chat.getTimestampUltimoMensaje() == null) return "";
        try {
            return TIME_FMT.format(new Date(chat.getTimestampUltimoMensaje().toDate().getTime()));
        } catch (Exception e) {
            return "";
        }
    }

    // ── DiffUtil ──────────────────────────────────────────────────────────────

    private static class ChatDiffCallback extends DiffUtil.Callback {
        private final List<Object> oldList;
        private final List<Object> newList;

        ChatDiffCallback(List<Object> oldList, List<Object> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override public int getOldListSize() { return oldList.size(); }
        @Override public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            Object o = oldList.get(oldPos);
            Object n = newList.get(newPos);
            if (o instanceof String && n instanceof String) return o.equals(n);
            if (o instanceof Chat && n instanceof Chat)
                return Objects.equals(((Chat) o).getChatId(), ((Chat) n).getChatId());
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            Object o = oldList.get(oldPos);
            Object n = newList.get(newPos);
            if (o instanceof String) return o.equals(n);
            if (o instanceof Chat && n instanceof Chat) {
                Chat co = (Chat) o, cn = (Chat) n;
                return Objects.equals(co.getNombreAsesor(), cn.getNombreAsesor())
                    && Objects.equals(co.getUltimoMensaje(), cn.getUltimoMensaje())
                    && Objects.equals(co.getNombreProyecto(), cn.getNombreProyecto());
            }
            return false;
        }
    }

    // ── ViewHolders ───────────────────────────────────────────────────────────

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderVH(View v) {
            super(v);
            tvHeader = (TextView) v;
        }
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView         tvName, tvLastMessage, tvTime, tvInitials, tvUnreadCount, tvProyecto;
        MaterialCardView cardInitials, cardUnread;
        LinearLayout     llFinalizado;
        MaterialButton   btnReconectar;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName        = itemView.findViewById(R.id.tvChatName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime        = itemView.findViewById(R.id.tvChatTime);
            tvInitials    = itemView.findViewById(R.id.tvInitials);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            cardInitials  = itemView.findViewById(R.id.cardInitials);
            cardUnread    = itemView.findViewById(R.id.cardUnread);
            tvProyecto    = itemView.findViewById(R.id.tvChatProyecto);
            llFinalizado  = itemView.findViewById(R.id.ll_finalizado);
            btnReconectar = itemView.findViewById(R.id.btn_reconectar);
        }
    }
}
