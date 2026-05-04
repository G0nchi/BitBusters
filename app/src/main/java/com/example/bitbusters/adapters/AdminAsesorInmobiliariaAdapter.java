package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.AdminAsesorInmobiliaria;

import java.util.List;

public class AdminAsesorInmobiliariaAdapter extends RecyclerView.Adapter<AdminAsesorInmobiliariaAdapter.AdminAsesorInmobiliariaViewHolder> {

    private List<AdminAsesorInmobiliaria> asesorList;
    private OnAsesorActionListener listener;

    public interface OnAsesorActionListener {
        void onEditAsesor(int position);
        void onDeleteAsesor(int position);
    }

    public AdminAsesorInmobiliariaAdapter(List<AdminAsesorInmobiliaria> asesorList, OnAsesorActionListener listener) {
        this.asesorList = asesorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminAsesorInmobiliariaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_asesor_inmobiliaria, parent, false);
        return new AdminAsesorInmobiliariaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminAsesorInmobiliariaViewHolder holder, int position) {
        AdminAsesorInmobiliaria asesor = asesorList.get(position);

        holder.tvNombreAsesor.setText(asesor.getNombre());
        holder.tvEmailAsesor.setText(asesor.getEmail());
        holder.tvTelefonoAsesor.setText(asesor.getTelefono());
        holder.tvInicialesAsesor.setText(asesor.getIniciales());
        holder.tvEstadoAsesor.setText(asesor.getEstado());

        if (listener != null) {
            holder.btnEditAsesor.setOnClickListener(v -> listener.onEditAsesor(position));
            holder.btnDeleteAsesor.setOnClickListener(v -> listener.onDeleteAsesor(position));
        }
    }

    @Override
    public int getItemCount() {
        return asesorList != null ? asesorList.size() : 0;
    }

    public void setData(List<AdminAsesorInmobiliaria> nuevaLista) {
        this.asesorList = nuevaLista;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (asesorList != null && position < asesorList.size()) {
            asesorList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class AdminAsesorInmobiliariaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreAsesor, tvEmailAsesor, tvTelefonoAsesor, tvInicialesAsesor, tvEstadoAsesor;
        ImageButton btnEditAsesor, btnDeleteAsesor;

        public AdminAsesorInmobiliariaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreAsesor = itemView.findViewById(R.id.tvNombreAsesor);
            tvEmailAsesor = itemView.findViewById(R.id.tvEmailAsesor);
            tvTelefonoAsesor = itemView.findViewById(R.id.tvTelefonoAsesor);
            tvInicialesAsesor = itemView.findViewById(R.id.tvInicialesAsesor);
            tvEstadoAsesor = itemView.findViewById(R.id.tvEstadoAsesor);
            btnEditAsesor = itemView.findViewById(R.id.btnEditAsesor);
            btnDeleteAsesor = itemView.findViewById(R.id.btnDeleteAsesor);
        }
    }
}
