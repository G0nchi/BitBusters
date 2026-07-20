package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.ClienteInmobiliaria;

import java.util.ArrayList;
import java.util.List;

public class ClienteInmobiliariasAdapter extends RecyclerView.Adapter<ClienteInmobiliariasAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(ClienteInmobiliaria inmobiliaria);
    }

    private final List<ClienteInmobiliaria> items = new ArrayList<>();
    private final OnClickListener listener;

    public ClienteInmobiliariasAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<ClienteInmobiliaria> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cliente_inmobiliaria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClienteInmobiliaria item = items.get(position);
        holder.tvInitial.setText(inicial(item.getNombre()));
        holder.tvName.setText(item.getNombre());
        holder.tvDescription.setText(item.getDescripcion());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String inicial(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "I";
        return nombre.trim().substring(0, 1).toUpperCase(java.util.Locale.ROOT);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitial;
        TextView tvName;
        TextView tvDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvInitial);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
