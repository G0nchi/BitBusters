package com.example.bitbusters.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.activities.cliente.ProjectDetailActivity;
import com.example.bitbusters.models.Proyecto;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private static final String EXTRA_PROYECTO = "proyecto";
    private static final String EXTRA_PROYECTO_ID = "proyecto_id";
    private final Context context;
    private List<Proyecto> lista;

    public SearchResultAdapter(Context context, List<Proyecto> lista) {
        this.context = context;
        this.lista   = lista;
    }

    public void setData(List<Proyecto> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Proyecto p = lista.get(position); // ← primero inicializar p

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
                    .into(holder.imgProyecto);
        }

        // Navegar al detalle
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProjectDetailActivity.class);
            intent.putExtra(EXTRA_PROYECTO, p.nombre);
            intent.putExtra(EXTRA_PROYECTO_ID, p.getId());
            context.startActivity(intent);
        });

        // Favorito
        holder.btnFavorito.setOnClickListener(v -> {
            // TODO Lab 6: guardar favorito en Firebase
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProyecto;
        TextView tvNombre, tvPrecio, tvRating, tvUbicacion;
        ImageButton btnFavorito;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProyecto = itemView.findViewById(R.id.imgProyecto);
            tvNombre    = itemView.findViewById(R.id.tvNombre);
            tvPrecio    = itemView.findViewById(R.id.tvPrecio);
            tvRating    = itemView.findViewById(R.id.tvRating);
            tvUbicacion = itemView.findViewById(R.id.tvUbicacion);
            btnFavorito = itemView.findViewById(R.id.btnFavorito);
        }
    }
}
