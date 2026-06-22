package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.Mensaje;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MensajesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PROPIO   = 1;
    private static final int TYPE_RECIBIDO = 0;
    private static final SimpleDateFormat TIME_FMT =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    private final List<Mensaje> mensajes = new ArrayList<>();
    private final String uidActual;
    private final String inicialesAsesor;

    public MensajesAdapter(String uidActual, String inicialesAsesor) {
        this.uidActual = uidActual;
        this.inicialesAsesor = inicialesAsesor;
    }

    public void setMensajes(List<Mensaje> nuevos) {
        mensajes.clear();
        mensajes.addAll(nuevos);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        String emisor = mensajes.get(position).getIdEmisor();
        return (emisor != null && emisor.equals(uidActual)) ? TYPE_PROPIO : TYPE_RECIBIDO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_PROPIO) {
            return new PropioVH(inf.inflate(R.layout.item_mensaje_enviado, parent, false));
        }
        return new RecibidoVH(inf.inflate(R.layout.item_mensaje_recibido, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensaje m = mensajes.get(position);
        String hora = m.getTimestamp() != null
                ? TIME_FMT.format(new Date(m.getTimestamp().toDate().getTime()))
                : "";
        if (holder instanceof PropioVH) {
            ((PropioVH) holder).bind(m.getTexto() != null ? m.getTexto() : "", hora);
        } else {
            ((RecibidoVH) holder).bind(m.getTexto() != null ? m.getTexto() : "", hora, inicialesAsesor);
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    static class PropioVH extends RecyclerView.ViewHolder {
        final TextView tvMensaje, tvHora;
        PropioVH(@NonNull View v) {
            super(v);
            tvMensaje = v.findViewById(R.id.tv_mensaje);
            tvHora    = v.findViewById(R.id.tv_hora);
        }
        void bind(String texto, String hora) {
            tvMensaje.setText(texto);
            tvHora.setText(hora);
        }
    }

    static class RecibidoVH extends RecyclerView.ViewHolder {
        final TextView tvMensaje, tvHora, tvInitials;
        RecibidoVH(@NonNull View v) {
            super(v);
            tvMensaje  = v.findViewById(R.id.tv_mensaje);
            tvHora     = v.findViewById(R.id.tv_hora);
            tvInitials = v.findViewById(R.id.tv_initials);
        }
        void bind(String texto, String hora, String initials) {
            tvMensaje.setText(texto);
            tvHora.setText(hora);
            if (tvInitials != null) tvInitials.setText(initials);
        }
    }
}
