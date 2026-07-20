package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;

import java.util.ArrayList;
import java.util.List;

public class SeparacionAdapter extends RecyclerView.Adapter<SeparacionAdapter.ViewHolder> {

    static class Separacion {
        final String referencia;
        final String proyecto;
        final String cliente;
        final String monto;
        final String estado;
        final String fecha;
        final int placeholderColor;
        final String imageUrl;

        Separacion(String referencia, String proyecto, String cliente,
                   String monto, String estado, String fecha, int placeholderColor,
                   String imageUrl) {
            this.referencia = referencia;
            this.proyecto = proyecto;
            this.cliente = cliente;
            this.monto = monto;
            this.estado = estado;
            this.fecha = fecha;
            this.placeholderColor = placeholderColor;
            this.imageUrl = imageUrl;
        }
    }

    private List<Separacion> data = new ArrayList<>();

    /** Reemplaza los datos mostrados (llamado con separaciones reales de Firestore). */
    public void setData(List<Separacion> nuevaLista) {
        this.data = nuevaLista != null ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_separacion_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Separacion sep = data.get(position);
        holder.tvProyecto.setText(sep.proyecto);
        holder.tvCliente.setText(sep.cliente);
        holder.tvMonto.setText(sep.monto);
        holder.tvReferencia.setText(sep.referencia);
        holder.tvFecha.setText(sep.fecha);
        if (sep.imageUrl != null && !sep.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(sep.imageUrl)
                .centerCrop()
                .into(holder.vPlaceholder);
        } else {
            holder.vPlaceholder.setImageDrawable(null);
            holder.vPlaceholder.setBackgroundColor(sep.placeholderColor);
        }

        holder.tvEstado.setText(sep.estado);
        switch (sep.estado) {
            case "Aprobada":
                holder.tvEstado.setBackgroundResource(R.drawable.badge_confirmada);
                holder.tvEstado.setTextColor(Color.parseColor("#186A3B"));
                break;
            case "Pendiente":
                holder.tvEstado.setBackgroundResource(R.drawable.badge_pendiente);
                holder.tvEstado.setTextColor(Color.parseColor("#9A5700"));
                break;
            case "Rechazada":
            case "Vencida":
                holder.tvEstado.setBackgroundResource(R.drawable.badge_cancelada);
                holder.tvEstado.setTextColor(Color.parseColor("#CC2222"));
                break;
            default:
                holder.tvEstado.setBackgroundResource(R.drawable.badge_en_planos);
                holder.tvEstado.setTextColor(Color.parseColor("#1A5799"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView vPlaceholder;
        TextView tvProyecto, tvCliente, tvMonto, tvEstado, tvReferencia, tvFecha;

        ViewHolder(View itemView) {
            super(itemView);
            vPlaceholder = itemView.findViewById(R.id.v_placeholder);
            tvProyecto = itemView.findViewById(R.id.tv_proyecto);
            tvCliente = itemView.findViewById(R.id.tv_cliente);
            tvMonto = itemView.findViewById(R.id.tv_monto);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            tvReferencia = itemView.findViewById(R.id.tv_referencia);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
        }
    }
}
