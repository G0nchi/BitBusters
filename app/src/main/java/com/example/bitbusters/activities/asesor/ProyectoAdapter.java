package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.bitbusters.R;
import com.example.bitbusters.models.ProyectoApi;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter de proyectos inmobiliarios.
 *
 * Recibe {@link ProyectoApi} (datos del servidor vía Retrofit) en lugar de
 * arrays estáticos, y usa DiffUtil para actualizar la lista sin parpadeo.
 */
public class ProyectoAdapter extends RecyclerView.Adapter<ProyectoAdapter.ViewHolder> {

    interface OnItemClickListener {
        void onItemClick(int originalIndex);
    }

    // Mapeo imagen_key → URL de imagen referencial (Unsplash)
    private static String imageUrlForKey(String key) {
        if (key == null) return URL_TORRES;
        switch (key) {
            case "marina": return URL_MARINA;
            case "pinos":  return URL_PINOS;
            default:       return URL_TORRES;
        }
    }

    private static final String URL_MARINA = "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=600&q=80";
    private static final String URL_TORRES = "https://images.unsplash.com/photo-1486325212027-8081e485255e?w=600&q=80";
    private static final String URL_PINOS  = "https://images.unsplash.com/photo-1502005229762-cf1b2da7c5d6?w=600&q=80";

    // Placeholder local mientras Glide carga la imagen remota
    private static int imagenResForKey(String key) {
        if (key == null) return R.drawable.bg_proyecto_torres;
        switch (key) {
            case "marina": return R.drawable.bg_proyecto_marina;
            case "pinos":  return R.drawable.bg_proyecto_pinos;
            default:       return R.drawable.bg_proyecto_torres;
        }
    }

    private final OnItemClickListener listener;
    private List<ProyectoApi> allProyectos  = new ArrayList<>(); // lista completa de la API
    private List<ProyectoApi> displayed     = new ArrayList<>(); // lista filtrada
    private String             currentFilter = "Todos";

    ProyectoAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ── Actualización de datos ────────────────────────────────────────────────

    /**
     * Reemplaza la lista completa (llamado desde AsesorHomeActivity con datos
     * de la API) y aplica el filtro activo con DiffUtil.
     */
    public void setProyectos(List<ProyectoApi> proyectos) {
        this.allProyectos = proyectos != null ? proyectos : new ArrayList<>();
        applyFilter(currentFilter);
    }

    /** Filtra por tipo de proyecto y actualiza con DiffUtil. */
    public void applyFilter(String tipo) {
        currentFilter = tipo;

        List<ProyectoApi> newDisplayed = new ArrayList<>();
        for (int i = 0; i < allProyectos.size(); i++) {
            ProyectoApi p = allProyectos.get(i);
            if ("Todos".equals(tipo) || tipo.equals(p.tipo)) {
                newDisplayed.add(p);
            }
        }

        DiffUtil.DiffResult result =
                DiffUtil.calculateDiff(new ProyectoDiffCallback(displayed, newDisplayed));
        displayed = newDisplayed;
        result.dispatchUpdatesTo(this);
    }

    // ── RecyclerView.Adapter ──────────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_proyecto_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProyectoApi p = displayed.get(position);

        holder.tvNombre.setText(p.nombre);
        holder.tvUbicacion.setText(p.ubicacion);
        holder.tvPrecio.setText(p.precio);
        holder.tvRating.setText(p.rating);
        Glide.with(holder.itemView.getContext())
            .load(imageUrlForKey(p.imagenKey))
            .placeholder(imagenResForKey(p.imagenKey))
            .error(imagenResForKey(p.imagenKey))
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.imgPlaceholder);

        holder.tvEstado.setText(p.estado);
        switch (p.estado != null ? p.estado : "") {
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

        // originalIndex = posición en allProyectos (para ProyectoDetalleActivity)
        int originalIndex = allProyectos.indexOf(p);
        holder.btnVerMas.setOnClickListener(v ->
            listener.onItemClick(Math.max(originalIndex, 0)));
    }

    @Override
    public int getItemCount() {
        return displayed.size();
    }

    // ── DiffUtil Callback ─────────────────────────────────────────────────────

    private static class ProyectoDiffCallback extends DiffUtil.Callback {
        private final List<ProyectoApi> oldList;
        private final List<ProyectoApi> newList;

        ProyectoDiffCallback(List<ProyectoApi> oldList, List<ProyectoApi> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override public int getOldListSize() { return oldList.size(); }
        @Override public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).id == newList.get(newPos).id;
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            ProyectoApi o = oldList.get(oldPos), n = newList.get(newPos);
            return o.nombre.equals(n.nombre)
                && o.precio.equals(n.precio)
                && o.estado.equals(n.estado)
                && o.rating.equals(n.rating);
        }
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlaceholder;
        TextView tvNombre, tvUbicacion, tvPrecio, tvEstado, tvRating;
        MaterialButton btnVerMas;

        ViewHolder(View itemView) {
            super(itemView);
            imgPlaceholder = itemView.findViewById(R.id.v_placeholder);
            tvNombre       = itemView.findViewById(R.id.tv_nombre);
            tvUbicacion    = itemView.findViewById(R.id.tv_ubicacion);
            tvPrecio       = itemView.findViewById(R.id.tv_precio);
            tvEstado       = itemView.findViewById(R.id.tv_estado);
            tvRating       = itemView.findViewById(R.id.tv_rating);
            btnVerMas      = itemView.findViewById(R.id.btn_ver_mas);
        }
    }
}
