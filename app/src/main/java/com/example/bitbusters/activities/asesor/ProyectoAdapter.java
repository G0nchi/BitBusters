package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProyectoAdapter extends RecyclerView.Adapter<ProyectoAdapter.ViewHolder> {

    interface OnItemClickListener {
        void onItemClick(int originalIndex);
    }

    private static final String[] NOMBRES = {
        "Vista Marina Residencial",
        "Torres del Sol",
        "Condominio Los Pinos"
    };
    private static final String[] UBICACIONES = {
        "San Miguel, Lima",
        "Miraflores, Lima",
        "Surco, Lima"
    };
    private static final String[] PRECIOS = {
        "S/ 320,000",
        "S/ 450,000",
        "S/ 580,000"
    };
    private static final String[] ESTADOS = {"En Venta", "Preventa", "En Planos"};
    private static final String[] RATINGS = {"4.9", "4.8", "4.7"};
    private static final String[] TIPOS = {"Departamento", "Departamento", "Villa"};
    private static final int[] IMAGENES = {
        R.drawable.bg_proyecto_marina,
        R.drawable.bg_proyecto_torres,
        R.drawable.bg_proyecto_pinos
    };

    private final OnItemClickListener listener;
    private final List<Integer> displayed = new ArrayList<>();

    ProyectoAdapter(OnItemClickListener listener) {
        this.listener = listener;
        applyFilter("Todos");
    }

    public void applyFilter(String tipo) {
        displayed.clear();
        for (int i = 0; i < NOMBRES.length; i++) {
            if ("Todos".equals(tipo) || TIPOS[i].equals(tipo)) {
                displayed.add(i);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_proyecto_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int idx = displayed.get(position);
        holder.tvNombre.setText(NOMBRES[idx]);
        holder.tvUbicacion.setText(UBICACIONES[idx]);
        holder.tvPrecio.setText(PRECIOS[idx]);
        holder.tvRating.setText(RATINGS[idx]);
        holder.imgPlaceholder.setImageResource(IMAGENES[idx]);

        holder.tvEstado.setText(ESTADOS[idx]);
        switch (ESTADOS[idx]) {
            case "En Venta":
                holder.tvEstado.setBackgroundResource(R.drawable.badge_en_venta);
                holder.tvEstado.setTextColor(Color.parseColor("#186A3B"));
                break;
            case "Preventa":
                holder.tvEstado.setBackgroundResource(R.drawable.badge_preventa);
                holder.tvEstado.setTextColor(Color.parseColor("#9A5700"));
                break;
            default:
                holder.tvEstado.setBackgroundResource(R.drawable.badge_en_planos);
                holder.tvEstado.setTextColor(Color.parseColor("#1A5799"));
                break;
        }

        holder.btnVerMas.setOnClickListener(v -> listener.onItemClick(idx));
    }

    @Override
    public int getItemCount() {
        return displayed.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlaceholder;
        TextView tvNombre, tvUbicacion, tvPrecio, tvEstado, tvRating;
        MaterialButton btnVerMas;

        ViewHolder(View itemView) {
            super(itemView);
            imgPlaceholder = itemView.findViewById(R.id.v_placeholder);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvUbicacion = itemView.findViewById(R.id.tv_ubicacion);
            tvPrecio = itemView.findViewById(R.id.tv_precio);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            tvRating = itemView.findViewById(R.id.tv_rating);
            btnVerMas = itemView.findViewById(R.id.btn_ver_mas);
        }
    }
}
