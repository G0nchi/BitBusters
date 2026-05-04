package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;

public class SeparacionAdapter extends RecyclerView.Adapter<SeparacionAdapter.ViewHolder> {

    static class Separacion {
        String referencia;
        String proyecto;
        String cliente;
        String monto;
        String estado;
        String fecha;
        int placeholderColor;

        Separacion(String referencia, String proyecto, String cliente,
                   String monto, String estado, String fecha, int placeholderColor) {
            this.referencia = referencia;
            this.proyecto = proyecto;
            this.cliente = cliente;
            this.monto = monto;
            this.estado = estado;
            this.fecha = fecha;
            this.placeholderColor = placeholderColor;
        }
    }

    private static final Separacion[] DATA = {
        new Separacion("SEP-2025-0041", "Vista Marina Residencial",
            "Carlos Mendoza", "S/ 320,000", "Confirmada", "28 Mar 2025",
            Color.parseColor("#B8C8D4")),
        new Separacion("SEP-2025-0042", "Torres del Sol · Dpto 302",
            "Ana López", "S/ 450,000", "En proceso", "02 Abr 2025",
            Color.parseColor("#D4B896")),
        new Separacion("SEP-2025-0043", "Torres del Sol · Dpto 501",
            "Rosa Torres", "S/ 320,000", "Vencida", "05 Abr 2025",
            Color.parseColor("#D4B896")),
        new Separacion("SEP-2025-0044", "Condominio Los Pinos",
            "Marco Paredes", "S/ 580,000", "Confirmada", "08 Abr 2025",
            Color.parseColor("#A8C8A0")),
        new Separacion("SEP-2025-0045", "Vista Marina · Dpto 204",
            "Sandra Vega", "S/ 320,000", "Pendiente", "10 Abr 2025",
            Color.parseColor("#B8C8D4")),
        new Separacion("SEP-2025-0046", "Torres del Sol · Dpto 108",
            "Luis Vargas", "S/ 450,000", "En proceso", "12 Abr 2025",
            Color.parseColor("#D4B896"))
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_separacion_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Separacion sep = DATA[position];
        holder.tvProyecto.setText(sep.proyecto);
        holder.tvCliente.setText(sep.cliente);
        holder.tvMonto.setText(sep.monto);
        holder.tvReferencia.setText(sep.referencia);
        holder.tvFecha.setText(sep.fecha);
        holder.vPlaceholder.setBackgroundColor(sep.placeholderColor);

        holder.tvEstado.setText(sep.estado);
        switch (sep.estado) {
            case "Confirmada":
                holder.tvEstado.setBackgroundResource(R.drawable.badge_confirmada);
                holder.tvEstado.setTextColor(Color.parseColor("#186A3B"));
                break;
            case "Pendiente":
                holder.tvEstado.setBackgroundResource(R.drawable.badge_pendiente);
                holder.tvEstado.setTextColor(Color.parseColor("#9A5700"));
                break;
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
        return DATA.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View vPlaceholder;
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
