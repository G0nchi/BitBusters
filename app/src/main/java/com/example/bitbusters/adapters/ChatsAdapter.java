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
import com.example.bitbusters.models.AsesorChatItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Objects;

/**
 * Adapter multi-tipo para la lista de mensajes del ASESOR.
 * Tipos: encabezado de sección (String) y fila de chat (AsesorChatItem).
 */
public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CHAT   = 1;

    private List<Object> items;
    private final OnChatClickListener listener;
    private OnReconectarListener reconectarListener;

    // ── Interfaces ────────────────────────────────────────────────────────────

    public interface OnChatClickListener {
        void onChatClick(AsesorChatItem chat);
    }

    public interface OnReconectarListener {
        void onReconectar(AsesorChatItem chat);
    }

    public void setOnReconectarListener(OnReconectarListener l) {
        reconectarListener = l;
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    public ChatsAdapter(List<Object> items, OnChatClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    // ── Adapter overrides ─────────────────────────────────────────────────────

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
            AsesorChatItem chat = (AsesorChatItem) items.get(position);
            ChatViewHolder h = (ChatViewHolder) holder;

            h.tvName.setText(chat.getName());
            h.tvLastMessage.setText(chat.getLastMessage());
            h.tvTime.setText(chat.getTime());
            h.tvInitials.setText(chat.getInitials());
            h.cardInitials.setCardBackgroundColor(Color.parseColor(chat.getColorHex()));

            String proyecto = chat.getProyecto();
            if (proyecto != null && !proyecto.isEmpty()) {
                h.tvProyecto.setVisibility(View.VISIBLE);
                h.tvProyecto.setText(proyecto);
            } else {
                h.tvProyecto.setVisibility(View.GONE);
            }

            if (chat.getUnreadCount() > 0) {
                h.cardUnread.setVisibility(View.VISIBLE);
                h.tvUnreadCount.setText(String.valueOf(chat.getUnreadCount()));
            } else {
                h.cardUnread.setVisibility(View.GONE);
            }

            if (chat.isRecent()) {
                h.tvName.setAlpha(1f);
                h.tvLastMessage.setAlpha(1f);
                h.tvProyecto.setAlpha(1f);
                h.llFinalizado.setVisibility(View.GONE);
            } else {
                h.tvName.setAlpha(0.6f);
                h.tvLastMessage.setAlpha(0.6f);
                h.tvProyecto.setAlpha(0.6f);
                h.llFinalizado.setVisibility(View.VISIBLE);

                if (reconectarListener != null) {
                    h.btnReconectar.setOnClickListener(v -> reconectarListener.onReconectar(chat));
                } else {
                    h.btnReconectar.setOnClickListener(null);
                }
            }

            h.itemView.setOnClickListener(v -> listener.onChatClick(chat));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ── Métodos públicos ──────────────────────────────────────────────────────

    public void updateItems(List<Object> newItems) {
        DiffUtil.DiffResult result =
                DiffUtil.calculateDiff(new ChatDiffCallback(this.items, newItems));
        this.items = newItems;
        result.dispatchUpdatesTo(this);
    }

    // ── DiffUtil callback ─────────────────────────────────────────────────────

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
            if (o instanceof AsesorChatItem && n instanceof AsesorChatItem)
                return ((AsesorChatItem) o).getId().equals(((AsesorChatItem) n).getId());
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            Object o = oldList.get(oldPos);
            Object n = newList.get(newPos);
            if (o instanceof String) return o.equals(n);
            if (o instanceof AsesorChatItem && n instanceof AsesorChatItem) {
                AsesorChatItem co = (AsesorChatItem) o, cn = (AsesorChatItem) n;
                return co.getName().equals(cn.getName())
                    && co.getLastMessage().equals(cn.getLastMessage())
                    && co.getTime().equals(cn.getTime())
                    && co.getUnreadCount() == cn.getUnreadCount()
                    && co.isRecent() == cn.isRecent()
                    && Objects.equals(co.getProyecto(), cn.getProyecto());
            }
            return false;
        }
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()
                && items.get(position) instanceof AsesorChatItem) {
            items.remove(position);
            notifyItemRemoved(position);
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
        TextView        tvName, tvLastMessage, tvTime, tvInitials, tvUnreadCount, tvProyecto;
        MaterialCardView cardInitials, cardUnread;
        LinearLayout    llFinalizado;
        MaterialButton  btnReconectar;

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
