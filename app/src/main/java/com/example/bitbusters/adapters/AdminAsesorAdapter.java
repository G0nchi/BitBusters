package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.AdminAsesor;

import java.util.List;

public class AdminAsesorAdapter extends RecyclerView.Adapter<AdminAsesorAdapter.AdminAsesorViewHolder> {

    private List<AdminAsesor> asesorList;
    private OnAsesorCheckedListener listener;

    public interface OnAsesorCheckedListener {
        void onAsesorChecked(int position, boolean isChecked);
    }

    public AdminAsesorAdapter(List<AdminAsesor> asesorList, OnAsesorCheckedListener listener) {
        this.asesorList = asesorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminAsesorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_asesor_checkbox, parent, false);
        return new AdminAsesorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminAsesorViewHolder holder, int position) {
        AdminAsesor asesor = asesorList.get(position);

        holder.cbAsesor.setOnCheckedChangeListener(null);
        holder.cbAsesor.setChecked(false);

        holder.cbAsesor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onAsesorChecked(position, isChecked);
            }
        });

        holder.tvNombreAsesor.setText(asesor.getNombre());
        holder.tvEstadoAsesor.setText(asesor.getNumSeparaciones() + " separaciones · " + asesor.getEstado());
        holder.tvInicialesAsesor.setText(asesor.getIniciales());
    }

    @Override
    public int getItemCount() {
        return asesorList != null ? asesorList.size() : 0;
    }

    public void setData(List<AdminAsesor> nuevaLista) {
        this.asesorList = nuevaLista;
        notifyDataSetChanged();
    }

    public static class AdminAsesorViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbAsesor;
        TextView tvNombreAsesor, tvEstadoAsesor, tvInicialesAsesor;

        public AdminAsesorViewHolder(@NonNull View itemView) {
            super(itemView);
            cbAsesor = itemView.findViewById(R.id.cbAsesor);
            tvNombreAsesor = itemView.findViewById(R.id.tvNombreAsesor);
            tvEstadoAsesor = itemView.findViewById(R.id.tvEstadoAsesor);
            tvInicialesAsesor = itemView.findViewById(R.id.tvInicialesAsesor);
        }
    }
}
