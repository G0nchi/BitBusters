package com.example.bitbusters.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.AdminAsesorInmobiliaria;

import java.util.List;

public class AdminAsesorInmobiliariaAdapter extends RecyclerView.Adapter<AdminAsesorInmobiliariaAdapter.AdminAsesorInmobiliariaViewHolder> {

    private List<AdminAsesorInmobiliaria> asesorList;
    private OnAsesorActionListener listener;
    private final boolean showActions;

    public interface OnAsesorActionListener {
        void onEditAsesor(int position);
        void onDeleteAsesor(int position);
    }

    public AdminAsesorInmobiliariaAdapter(List<AdminAsesorInmobiliaria> asesorList, OnAsesorActionListener listener) {
        this(asesorList, listener, true);
    }

    public AdminAsesorInmobiliariaAdapter(List<AdminAsesorInmobiliaria> asesorList,
                                          OnAsesorActionListener listener,
                                          boolean showActions) {
        this.asesorList = asesorList;
        this.listener = listener;
        this.showActions = showActions;
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
        aplicarEstiloEstado(holder.tvEstadoAsesor, asesor.getEstado());

        holder.btnEditAsesor.setVisibility(showActions ? View.VISIBLE : View.GONE);
        holder.btnDeleteAsesor.setVisibility(showActions ? View.VISIBLE : View.GONE);

        if (listener != null && showActions) {
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

    private void aplicarEstiloEstado(TextView tvEstado, String estado) {
        if (tvEstado == null) {
            return;
        }
        String estadoNormalizado = estado == null ? "" : estado.trim().toLowerCase();
        int colorTexto = Color.parseColor("#1B5E20");
        if ("inactivo".equals(estadoNormalizado) || "suspendido".equals(estadoNormalizado)) {
            colorTexto = Color.parseColor("#C62828");
        } else if ("pendiente".equals(estadoNormalizado) || "en revisión".equals(estadoNormalizado)) {
            colorTexto = Color.parseColor("#EF6C00");
        }
        tvEstado.setTextColor(colorTexto);
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
