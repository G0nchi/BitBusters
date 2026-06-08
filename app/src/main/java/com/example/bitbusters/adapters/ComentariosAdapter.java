package com.example.bitbusters.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bitbusters.R;
import com.example.bitbusters.models.ComentarioEntity;
import de.hdodenhof.circleimageview.CircleImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComentariosAdapter extends RecyclerView.Adapter<ComentariosAdapter.ViewHolder> {

    private List<ComentarioEntity> comentarios;
    private final Context context;

    public ComentariosAdapter(Context context, List<ComentarioEntity> comentarios) {
        this.context = context;
        this.comentarios = comentarios;
    }

    public void actualizarLista(List<ComentarioEntity> nuevaLista) {
        this.comentarios = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ComentarioEntity c = comentarios.get(position);
        holder.tvNombreAutor.setText(c.nombreUsuario);
        holder.tvFecha.setText(formatearFecha(c.timestamp));
        holder.tvTextoComentario.setText(c.texto);
        holder.ratingBar.setRating(c.rating);

        if (c.fotoUsuarioUrl != null && !c.fotoUsuarioUrl.isEmpty()) {
            Glide.with(context)
                    .load(c.fotoUsuarioUrl)
                    .centerCrop()
                    .placeholder(R.drawable.avatar_jonathan)
                    .error(R.drawable.avatar_jonathan)
                    .into(holder.imgAutor);
        } else {
            holder.imgAutor.setImageResource(R.drawable.avatar_jonathan);
        }
    }

    @Override
    public int getItemCount() {
        return comentarios != null ? comentarios.size() : 0;
    }

    private static String formatearFecha(long timestamp) {
        long ahora = System.currentTimeMillis();
        long diff = ahora - timestamp;
        long minutos = diff / (60 * 1000);
        long horas = diff / (60 * 60 * 1000);
        long dias = diff / (24 * 60 * 60 * 1000);

        if (dias >= 7) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } else if (dias >= 1) {
            return "Hace " + dias + (dias == 1 ? " día" : " días");
        } else if (horas >= 1) {
            return "Hace " + horas + (horas == 1 ? " hora" : " horas");
        } else if (minutos >= 1) {
            return "Hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        } else {
            return "Ahora mismo";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAutor;
        TextView tvNombreAutor;
        TextView tvFecha;
        RatingBar ratingBar;
        TextView tvTextoComentario;

        ViewHolder(View itemView) {
            super(itemView);
            imgAutor = itemView.findViewById(R.id.imgAutor);
            tvNombreAutor = itemView.findViewById(R.id.tvNombreAutor);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            ratingBar = itemView.findViewById(R.id.ratingBarItem);
            tvTextoComentario = itemView.findViewById(R.id.tvTextoComentario);
        }
    }
}
