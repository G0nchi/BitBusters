package com.example.bitbusters.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.activities.cliente.ProjectDetailActivity;
import com.example.bitbusters.models.Proyecto;
import java.util.List;

/** Adapter para mostrar la lista de proyectos inmobiliarios en el RecyclerView de HomeActivity. */
public class ProyectoAdapter extends RecyclerView.Adapter<ProyectoAdapter.ViewHolder> {

    private static final String EXTRA_PROYECTO = "proyecto";

    private final Context context;
    private List<Proyecto> lista;

    public ProyectoAdapter(Context context, List<Proyecto> lista) {
        this.context = context;
        this.lista   = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_proyecto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Proyecto p = lista.get(position);

        holder.tvNombre.setText(p.nombre);
        holder.tvPrecio.setText(p.precio);
        holder.tvRating.setText("★ " + p.rating);
        holder.tvUbicacion.setText("📍 " + p.ubicacion);

        // Cargar imagen con Glide desde URL
        if (p.imageUrl != null && !p.imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(p.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.bg_rounded_8)
                    .into(holder.imgPropiedad);
        }

        // Navegar al detalle al tocar la card
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProjectDetailActivity.class);
            intent.putExtra(EXTRA_PROYECTO, p.nombre);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    // Actualizar lista y refrescar RecyclerView
    public void setData(List<Proyecto> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    /** ViewHolder que referencia las vistas definidas en item_proyecto.xml */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPropiedad;
        TextView tvNombre, tvPrecio, tvRating, tvUbicacion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPropiedad = itemView.findViewById(R.id.imgPropiedad);
            tvNombre     = itemView.findViewById(R.id.tvNombre);
            tvPrecio     = itemView.findViewById(R.id.tvPrecio);
            tvRating     = itemView.findViewById(R.id.tvRating);
            tvUbicacion  = itemView.findViewById(R.id.tvUbicacion);
        }
    }
}