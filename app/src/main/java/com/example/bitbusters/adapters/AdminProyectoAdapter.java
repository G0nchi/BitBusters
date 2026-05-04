package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.models.Proyecto;

import java.util.List;

public class AdminProyectoAdapter extends RecyclerView.Adapter<AdminProyectoAdapter.AdminProyectoViewHolder> {

    private List<Proyecto> proyectoList;
    private OnProyectoClickListener listener;

    public interface OnProyectoClickListener {
        void onProyectoClick(Proyecto proyecto);
    }

    public AdminProyectoAdapter(List<Proyecto> proyectoList, OnProyectoClickListener listener) {
        this.proyectoList = proyectoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminProyectoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proyecto, parent, false);
        return new AdminProyectoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProyectoViewHolder holder, int position) {
        Proyecto proyecto = proyectoList.get(position);

        holder.tvNombre.setText(proyecto.getNombre());
        holder.tvPrecio.setText(proyecto.getPrecio());
        holder.tvUbicacion.setText(proyecto.getUbicacion());
        holder.tvRating.setText("★ " + proyecto.getRating());

        // Cargar imagen con Glide
        if (proyecto.getImageUrl() != null && !proyecto.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(proyecto.getImageUrl())
                    .centerCrop()
                    .into(holder.imgPropiedad);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProyectoClick(proyecto);
            }
        });
    }

    @Override
    public int getItemCount() {
        return proyectoList != null ? proyectoList.size() : 0;
    }

    public void setData(List<Proyecto> nuevaLista) {
        this.proyectoList = nuevaLista;
        notifyDataSetChanged();
    }

    public static class AdminProyectoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPrecio, tvUbicacion, tvRating;
        ImageView imgPropiedad;

        public AdminProyectoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
            tvRating = itemView.findViewById(R.id.tvRating);
            imgPropiedad = itemView.findViewById(R.id.imgPropiedad);
        }
    }
}
