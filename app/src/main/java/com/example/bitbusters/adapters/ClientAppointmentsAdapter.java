package com.example.bitbusters.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.ClientAppointment;

import java.util.ArrayList;
import java.util.List;

public class ClientAppointmentsAdapter extends RecyclerView.Adapter<ClientAppointmentsAdapter.ViewHolder> {

    private static final String ACTION_REAGENDAR = "Reagendar";
    private static final String ACTION_VER_DETALLE = "Ver detalle";
    private static final String ACTION_CANCELAR = "Cancelar";
    private static final String ACTION_VALORAR = "Valorar";
    private static final String ACTION_ESCRIBIR_NUEVO = "Escribir nuevo";

    public interface OnAppointmentActionListener {
        void onPrimaryAction(@NonNull ClientAppointment appointment);
        void onSecondaryAction(@NonNull ClientAppointment appointment);
    }

    private final OnAppointmentActionListener listener;
    private final List<ClientAppointment> appointments = new ArrayList<>();

    public ClientAppointmentsAdapter(OnAppointmentActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClientAppointment item = appointments.get(position);

        holder.tvProject.setText(item.getProjectName());
        holder.tvLocation.setText(item.getLocation());
        holder.tvDate.setText(item.getDate());
        holder.tvTime.setText(item.getTime());
        holder.tvAdvisor.setText("Asesor: " + item.getAdvisorName());

        bindStatusAndButtons(holder, item);

        holder.btnLeft.setOnClickListener(v -> listener.onPrimaryAction(item));
        holder.btnRight.setOnClickListener(v -> listener.onSecondaryAction(item));
    }

    private void bindStatusAndButtons(@NonNull ViewHolder holder, @NonNull ClientAppointment item) {
        String status = item.getStatus();
        holder.tvStatus.setText(status);

        if (ClientAppointment.STATUS_CONFIRMED.equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_confirmada);
            holder.tvStatus.setTextColor(0xFF2E7D32);
            holder.btnLeft.setText(ACTION_REAGENDAR);
            holder.btnRight.setText(ACTION_CANCELAR);
        } else if (ClientAppointment.STATUS_PENDING.equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_pendiente);
            holder.tvStatus.setTextColor(0xFFE65100);
            holder.btnLeft.setText(ACTION_VER_DETALLE);
            holder.btnRight.setText(ACTION_REAGENDAR);
        } else if (ClientAppointment.STATUS_COMPLETED.equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_completada);
            holder.tvStatus.setTextColor(0xFF1565C0);
            holder.btnLeft.setText(ACTION_REAGENDAR);
            holder.btnRight.setText(ACTION_VALORAR);
        } else if (ClientAppointment.STATUS_REVIEWED.equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_completada);
            holder.tvStatus.setTextColor(0xFF1565C0);
            holder.btnLeft.setText(ACTION_VER_DETALLE);
            holder.btnRight.setText(ACTION_ESCRIBIR_NUEVO);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_cancelar);
            holder.tvStatus.setTextColor(0xFFB71C1C);
            holder.btnLeft.setText(ACTION_VER_DETALLE);
            holder.btnRight.setText(ACTION_REAGENDAR);
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    public void submitList(@NonNull List<ClientAppointment> items) {
        appointments.clear();
        appointments.addAll(items);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvProject;
        final TextView tvLocation;
        final TextView tvStatus;
        final TextView tvDate;
        final TextView tvTime;
        final TextView tvAdvisor;
        final TextView btnLeft;
        final TextView btnRight;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProject = itemView.findViewById(R.id.tvAppointmentProject);
            tvLocation = itemView.findViewById(R.id.tvAppointmentLocation);
            tvStatus = itemView.findViewById(R.id.tvAppointmentStatus);
            tvDate = itemView.findViewById(R.id.tvAppointmentDate);
            tvTime = itemView.findViewById(R.id.tvAppointmentTime);
            tvAdvisor = itemView.findViewById(R.id.tvAppointmentAdvisor);
            btnLeft = itemView.findViewById(R.id.btnAppointmentLeft);
            btnRight = itemView.findViewById(R.id.btnAppointmentRight);
        }
    }
}
