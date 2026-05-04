package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.AdminSeparacion;

import java.util.List;

public class AdminSeparacionAdapter extends RecyclerView.Adapter<AdminSeparacionAdapter.AdminSeparacionViewHolder> {

    private List<AdminSeparacion> separacionList;
    private OnSeparacionClickListener listener;

    public interface OnSeparacionClickListener {
        void onSeparacionClick(AdminSeparacion separacion);
    }

    public AdminSeparacionAdapter(List<AdminSeparacion> separacionList, OnSeparacionClickListener listener) {
        this.separacionList = separacionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminSeparacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_separacion, parent, false);
        return new AdminSeparacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminSeparacionViewHolder holder, int position) {
        AdminSeparacion separacion = separacionList.get(position);

        holder.tvNombreProyecto.setText(separacion.getNombreProyecto());
        holder.tvMonto.setText(separacion.getMonto());
        holder.tvFecha.setText(separacion.getFecha());
        holder.tvCliente.setText(separacion.getCliente());
        holder.tvEstado.setText(separacion.getEstado());

        // Colorear según estado
        if ("Aprobada".equals(separacion.getEstado())) {
            holder.tvEstado.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.brand_lime, null));
        } else if ("Rechazada".equals(separacion.getEstado())) {
            holder.tvEstado.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.status_error, null));
        } else {
            holder.tvEstado.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.neutral_medium, null));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSeparacionClick(separacion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return separacionList != null ? separacionList.size() : 0;
    }

    public void setData(List<AdminSeparacion> nuevaLista) {
        this.separacionList = nuevaLista;
        notifyDataSetChanged();
    }

    public static class AdminSeparacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreProyecto, tvMonto, tvFecha, tvCliente, tvEstado;

        public AdminSeparacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreProyecto = itemView.findViewById(R.id.tvNombreProyecto);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvCliente = itemView.findViewById(R.id.tvCliente);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}
