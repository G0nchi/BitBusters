package com.example.bitbusters.activities.asesor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.Proyecto;

import java.util.ArrayList;
import java.util.List;

/** Lista del panel inferior de {@link AsesorMapaActivity} — sincronizada con los marcadores del mapa. */
public class ProyectoMapaAdapter extends RecyclerView.Adapter<ProyectoMapaAdapter.ViewHolder> {

    public interface OnProyectoClickListener {
        void onProyectoClick(Proyecto proyecto);
    }

    private static final int[] DOTS = {
        R.drawable.bg_proyecto_marina, R.drawable.bg_proyecto_torres, R.drawable.bg_proyecto_pinos
    };

    private final List<Proyecto> proyectos = new ArrayList<>();
    private final OnProyectoClickListener listener;

    public ProyectoMapaAdapter(OnProyectoClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Proyecto> nuevaLista) {
        proyectos.clear();
        if (nuevaLista != null) proyectos.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_proyecto_mapa, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Proyecto p = proyectos.get(position);
        holder.tvNombre.setText(p.getNombre());
        String ubicacion = p.getUbicacion() != null && !p.getUbicacion().isEmpty()
                ? p.getUbicacion() : p.getDistrito();
        holder.tvUbicacion.setText(ubicacion != null ? ubicacion : "");
        holder.vDot.setBackgroundResource(DOTS[position % DOTS.length]);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProyectoClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return proyectos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View vDot;
        TextView tvNombre, tvUbicacion;

        ViewHolder(View itemView) {
            super(itemView);
            vDot = itemView.findViewById(R.id.v_dot);
            tvNombre = itemView.findViewById(R.id.tv_nombre);
            tvUbicacion = itemView.findViewById(R.id.tv_ubicacion);
        }
    }
}
