package com.example.bitbusters.adapters;

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
import com.example.bitbusters.models.AdminProyecto;

import java.io.File;
import java.util.List;

/**
 * Adapter para la lista de proyectos del Administrador (AdminProyecto).
 * Diferente a AdminProyectoAdapter, que usa el modelo Proyecto del cliente.
 */
public class AdminProyectoListAdapter
        extends RecyclerView.Adapter<AdminProyectoListAdapter.ViewHolder> {

    /** Callback al hacer clic en un ítem de la lista */
    public interface OnProyectoClickListener {
        void onProyectoClick(AdminProyecto proyecto);
    }

    private List<AdminProyecto>    lista;
    private OnProyectoClickListener listener;

    public AdminProyectoListAdapter(List<AdminProyecto> lista,
                                    OnProyectoClickListener listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_proyecto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminProyecto p = lista.get(position);

        holder.tvNombre.setText(p.getNombre());
        holder.tvDistrito.setText(p.getDistrito().isEmpty() ? "Sin distrito" : p.getDistrito());
        holder.tvPrecio.setText("Desde S/ " + p.getPrecioTotal());
        holder.tvEstado.setText(p.getEstado());

        // Color del badge según el estado del proyecto
        switch (p.getEstado()) {
            case "En venta":
                // Verde: proyecto disponible para venta
                holder.tvEstado.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
            case "Preventa":
                // Naranja: en preventa
                holder.tvEstado.setBackgroundColor(Color.parseColor("#FF9800"));
                break;
            case "En planos":
                // Azul: solo en planos aún
                holder.tvEstado.setBackgroundColor(Color.parseColor("#2196F3"));
                break;
            default:
                // Gris: estado desconocido
                holder.tvEstado.setBackgroundColor(Color.parseColor("#9E9E9E"));
        }

        // Cargar primera imagen del proyecto o mostrar avatar de texto
        List<String> imagenes = p.getImagenesUri();
        if (imagenes != null && !imagenes.isEmpty()) {
            String primera = imagenes.get(0);
            if (!primera.isEmpty()) {
                holder.tvAvatar.setVisibility(View.GONE);
                holder.imgProyecto.setVisibility(View.VISIBLE);
                Glide.with(holder.itemView.getContext())
                        .load(primera.startsWith("/") ? new File(primera) : primera)
                        .centerCrop()
                        .into(holder.imgProyecto);
            } else {
                holder.imgProyecto.setVisibility(View.GONE);
                holder.tvAvatar.setVisibility(View.VISIBLE);
            }
        } else {
            holder.imgProyecto.setVisibility(View.GONE);
            holder.tvAvatar.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProyectoClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    /**
     * Reemplaza la lista completa y notifica al RecyclerView para que se refresque.
     *
     * @param nuevaLista Lista actualizada de proyectos.
     */
    public void setData(List<AdminProyecto> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvNombre, tvDistrito, tvPrecio, tvEstado, tvAvatar;
        ImageView imgProyecto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre    = itemView.findViewById(R.id.tvNombreProyecto);
            tvDistrito  = itemView.findViewById(R.id.tvDistritoProyecto);
            tvPrecio    = itemView.findViewById(R.id.tvPrecioProyecto);
            tvEstado    = itemView.findViewById(R.id.tvEstadoProyecto);
            tvAvatar    = itemView.findViewById(R.id.tvAvatarProyecto);
            imgProyecto = itemView.findViewById(R.id.imgProyecto);
        }
    }
}
