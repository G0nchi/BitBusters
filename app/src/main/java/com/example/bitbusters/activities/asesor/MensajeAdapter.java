package com.example.bitbusters.activities.asesor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;

import java.util.ArrayList;
import java.util.List;

public class MensajeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_RECEIVED = 0;
    private static final int TYPE_SENT = 1;

    public static class Mensaje {
        public final String texto;
        public final String hora;
        public final boolean sent;

        public Mensaje(String texto, String hora, boolean sent) {
            this.texto = texto;
            this.hora = hora;
            this.sent = sent;
        }
    }

    private List<Mensaje> mensajes;

    public MensajeAdapter(List<Mensaje> mensajes) {
        this.mensajes = new ArrayList<>(mensajes);
    }

    // ── Actualización de datos ────────────────────────────────────────────────

    /**
     * Agrega un mensaje al final y notifica con animación (sin parpadeo).
     * Usado por ConversacionActivity cuando el asesor envía un mensaje.
     */
    public void addMensaje(Mensaje m) {
        int pos = mensajes.size();
        mensajes.add(m);
        notifyItemInserted(pos);
    }

    /**
     * Reemplaza la lista completa usando DiffUtil.
     * Usado cuando llegan actualizaciones en tiempo real de Firestore.
     */
    public void updateMensajes(List<Mensaje> newList) {
        DiffUtil.DiffResult result =
                DiffUtil.calculateDiff(new MensajeDiffCallback(mensajes, newList));
        mensajes = new ArrayList<>(newList);
        result.dispatchUpdatesTo(this);
    }

    private static class MensajeDiffCallback extends DiffUtil.Callback {
        private final List<Mensaje> oldList;
        private final List<Mensaje> newList;

        MensajeDiffCallback(List<Mensaje> oldList, List<Mensaje> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override public int getOldListSize() { return oldList.size(); }
        @Override public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            Mensaje o = oldList.get(oldPos), n = newList.get(newPos);
            // Identidad: mismo texto + misma hora + mismo emisor
            return o.texto.equals(n.texto) && o.hora.equals(n.hora) && o.sent == n.sent;
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            return areItemsTheSame(oldPos, newPos);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mensajes.get(position).sent ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SENT) {
            View v = inflater.inflate(R.layout.item_mensaje_enviado, parent, false);
            return new SentViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_mensaje_recibido, parent, false);
            return new ReceivedViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensaje m = mensajes.get(position);
        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind(m);
        } else {
            ((ReceivedViewHolder) holder).bind(m);
        }
    }

    @Override
    public int getItemCount() { return mensajes.size(); }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje, tvHora;
        SentViewHolder(View v) {
            super(v);
            tvMensaje = v.findViewById(R.id.tv_mensaje);
            tvHora = v.findViewById(R.id.tv_hora);
        }
        void bind(Mensaje m) {
            tvMensaje.setText(m.texto);
            tvHora.setText(m.hora);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensaje, tvHora;
        ReceivedViewHolder(View v) {
            super(v);
            tvMensaje = v.findViewById(R.id.tv_mensaje);
            tvHora = v.findViewById(R.id.tv_hora);
        }
        void bind(Mensaje m) {
            tvMensaje.setText(m.texto);
            tvHora.setText(m.hora);
        }
    }
}
