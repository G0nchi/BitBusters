package com.example.bitbusters.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitbusters.R;
import com.example.bitbusters.activities.cliente.ProjectDetailActivity;
import com.example.bitbusters.models.Proyecto;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private static final String EXTRA_PROYECTO = "proyecto";

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
        Proyecto p = lista.get(position);
        holder.tvNombre.setText(p.nombre);
        holder.tvPrecio.setText(p.precio);
        holder.tvRating.setText("★ " + p.rating);
        holder.tvUbicacion.setText("  📍 " + p.ubicacion);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProjectDetailActivity.class);
            intent.putExtra(EXTRA_PROYECTO, p.nombre);
            context.startActivity(intent);
        });

        holder.btnFavorito.setOnClickListener(v -> {
            // TODO Lab 6: guardar favorito en Firebase
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvPrecio;
        TextView tvRating;
        TextView tvUbicacion;
        android.widget.ImageButton btnFavorito;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre   = itemView.findViewById(R.id.tvNombre);
            tvPrecio   = itemView.findViewById(R.id.tvPrecio);
            tvRating   = itemView.findViewById(R.id.tvRating);
            tvUbicacion= itemView.findViewById(R.id.tvUbicacion);
            btnFavorito= itemView.findViewById(R.id.btnFavorito);
        }
    }
}