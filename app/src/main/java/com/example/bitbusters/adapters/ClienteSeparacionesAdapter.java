package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.ClienteSeparacion;

import java.util.ArrayList;
import java.util.List;

public class ClienteSeparacionesAdapter extends RecyclerView.Adapter<ClienteSeparacionesAdapter.ViewHolder> {

    private final List<ClienteSeparacion> items = new ArrayList<>();

    public void setData(List<ClienteSeparacion> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_separacion_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClienteSeparacion item = items.get(position);
        holder.tvProyecto.setText(valueOrDefault(item.getProyectoNombre(), "Proyecto sin nombre"));
        holder.tvCliente.setText("Asesor: " + valueOrDefault(item.getAsesorNombre(), "No registrado")
                + " · Pago: " + valueOrDefault(item.getEstadoPago(), "Pendiente"));
        holder.tvMonto.setText(valueOrDefault(item.getMonto(), "S/ 0"));
        holder.tvEstado.setText(valueOrDefault(item.getEstado(), "Pendiente"));
        holder.tvReferencia.setText(item.getId().isEmpty() ? "Sin referencia" : item.getId());
        holder.tvFecha.setText(valueOrDefault(item.getFecha(), "Sin fecha"));
        holder.tvEstado.setBackgroundResource(backgroundPorEstado(item.getEstado(), item.getEstadoPago()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int backgroundPorEstado(String estado, String estadoPago) {
        if ("Pagado".equalsIgnoreCase(estadoPago) || "Pagada".equalsIgnoreCase(estadoPago)) {
            return R.drawable.badge_confirmada;
        }
        if ("Rechazada".equalsIgnoreCase(estado) || "Vencida".equalsIgnoreCase(estado)
                || "Vencido".equalsIgnoreCase(estadoPago)) {
            return R.drawable.badge_cancelada;
        }
        if ("Aprobada".equalsIgnoreCase(estado)) {
            return R.drawable.badge_confirmada;
        }
        return R.drawable.badge_pendiente;
    }

    private String valueOrDefault(String value, String fallback) {
        return value != null && !value.trim().isEmpty() ? value : fallback;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProyecto;
        TextView tvCliente;
        TextView tvMonto;
        TextView tvEstado;
        TextView tvReferencia;
        TextView tvFecha;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProyecto = itemView.findViewById(R.id.tv_proyecto);
            tvCliente = itemView.findViewById(R.id.tv_cliente);
            tvMonto = itemView.findViewById(R.id.tv_monto);
            tvEstado = itemView.findViewById(R.id.tv_estado);
            tvReferencia = itemView.findViewById(R.id.tv_referencia);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
        }
    }
}
