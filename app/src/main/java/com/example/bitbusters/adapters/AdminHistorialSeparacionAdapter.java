package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.AdminHistorialSeparacion;

import java.util.List;

public class AdminHistorialSeparacionAdapter extends RecyclerView.Adapter<AdminHistorialSeparacionAdapter.AdminHistorialViewHolder> {

    private List<AdminHistorialSeparacion> historialList;

    public AdminHistorialSeparacionAdapter(List<AdminHistorialSeparacion> historialList) {
        this.historialList = historialList;
    }

    @NonNull
    @Override
    public AdminHistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_historial_separacion, parent, false);
        return new AdminHistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminHistorialViewHolder holder, int position) {
        AdminHistorialSeparacion historial = historialList.get(position);

        holder.tvProyectoHistorial.setText(historial.getProyecto());
        holder.tvMontoDiario.setText(historial.getMonto());
        holder.tvNumSeparacionesHistorial.setText(String.valueOf(historial.getNumSeparaciones()));
        holder.tvNumAsesoresHistorial.setText(String.valueOf(historial.getNumAsesores()));
        holder.tvFechaHistorial.setText(historial.getFecha());
    }

    @Override
    public int getItemCount() {
        return historialList != null ? historialList.size() : 0;
    }

    public void setData(List<AdminHistorialSeparacion> nuevaLista) {
        this.historialList = nuevaLista;
        notifyDataSetChanged();
    }

    public static class AdminHistorialViewHolder extends RecyclerView.ViewHolder {
        TextView tvProyectoHistorial, tvMontoDiario, tvNumSeparacionesHistorial, tvNumAsesoresHistorial, tvFechaHistorial;

        public AdminHistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProyectoHistorial = itemView.findViewById(R.id.tvProyectoHistorial);
            tvMontoDiario = itemView.findViewById(R.id.tvMontoDiario);
            tvNumSeparacionesHistorial = itemView.findViewById(R.id.tvNumSeparacionesHistorial);
            tvNumAsesoresHistorial = itemView.findViewById(R.id.tvNumAsesoresHistorial);
            tvFechaHistorial = itemView.findViewById(R.id.tvFechaHistorial);
        }
    }
}
