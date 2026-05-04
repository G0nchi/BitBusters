package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.ClientMessage;

import java.util.List;

public class ClientChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_RECEIVED = 0;
    private static final int TYPE_SENT = 1;

    private final List<ClientMessage> messages;
    private final String receivedInitials;

    public ClientChatMessageAdapter(List<ClientMessage> messages, String receivedInitials) {
        this.messages = messages;
        this.receivedInitials = receivedInitials;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isSentByUser() ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_mensaje_enviado, parent, false);
            return new SentViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_mensaje_recibido, parent, false);
        return new ReceivedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ClientMessage message = messages.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(message);
        } else {
            ((ReceivedViewHolder) holder).bind(message, receivedInitials);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ClientMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final TextView tvTime;

        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_mensaje);
            tvTime = itemView.findViewById(R.id.tv_hora);
        }

        void bind(ClientMessage message) {
            tvMessage.setText(message.getText());
            tvTime.setText(message.getTime());
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvInitials;
        private final TextView tvMessage;
        private final TextView tvTime;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tv_initials);
            tvMessage = itemView.findViewById(R.id.tv_mensaje);
            tvTime = itemView.findViewById(R.id.tv_hora);
        }

        void bind(ClientMessage message, String initials) {
            tvInitials.setText(initials);
            tvMessage.setText(message.getText());
            tvTime.setText(message.getTime());
        }
    }
}
