package com.example.bitbusters.activities.asesor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityVerDetalleCitaBinding;

/**
 * Fragment destino en el grafo nav_citas.xml.
 *
 * Recibe los mismos datos que antes enviaba BaseCitasFragment.openVerDetalle()
 * via Intent extras, ahora como argumentos de Navigation Component (Bundle).
 *
 * Reutiliza el layout activity_ver_detalle_cita.xml — el nombre del binding
 * es ActivityVerDetalleCitaBinding independientemente de si lo usa una Activity
 * o un Fragment.
 */
public class VerDetalleCitaFragment extends Fragment {

    private ActivityVerDetalleCitaBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ActivityVerDetalleCitaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        String nombre      = args.getString("nombre",   "");
        String proyecto    = args.getString("proyecto", "");
        String fecha       = args.getString("fecha",    "");
        String hora        = args.getString("hora",     "");
        String badge       = args.getString("badge",    "");
        String initials    = args.getString("initials", "");
        int    avatarColor = args.getInt("avatarColor", Color.parseColor("#4ECDC4"));

        // ── Poblar vistas ──────────────────────────────────────────────────────
        binding.tvNombre.setText(nombre);
        if (!proyecto.isEmpty()) {
            binding.tvProyecto.setText(proyecto);
            String[] parts = proyecto.split(" · ");
            if (parts.length >= 2) {
                binding.tvProyectoNombre.setText(parts[0]);
                binding.tvUnidad.setText(parts[1]);
            } else {
                binding.tvProyectoNombre.setText(proyecto);
            }
        }
        binding.tvFecha.setText(fecha);
        binding.tvHora.setText(hora);
        binding.tvInitials.setText(initials);
        binding.cvAvatar.setCardBackgroundColor(avatarColor);

        // Badge de estado
        if (!badge.isEmpty()) {
            binding.tvBadgeHeader.setText(badge);
            switch (badge) {
                case "Pendiente":
                    binding.tvBadgeHeader.setBackgroundResource(R.drawable.badge_pendiente);
                    binding.tvBadgeHeader.setTextColor(Color.parseColor("#9A5700"));
                    break;
                case "Confirmada":
                    binding.tvBadgeHeader.setBackgroundResource(R.drawable.badge_confirmada);
                    binding.tvBadgeHeader.setTextColor(Color.parseColor("#186A3B"));
                    break;
                case "Cancelada":
                    binding.tvBadgeHeader.setBackgroundResource(R.drawable.badge_cancelada);
                    binding.tvBadgeHeader.setTextColor(Color.parseColor("#CC2222"));
                    break;
                default:
                    binding.tvBadgeHeader.setBackgroundResource(R.drawable.badge_pasada);
                    binding.tvBadgeHeader.setTextColor(Color.parseColor("#666666"));
            }
        }

        // ── Navegación ─────────────────────────────────────────────────────────
        // Botón volver: NavController.navigateUp() gestiona el back stack
        binding.btnBack.setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigateUp());

        // Reagendar: sigue usando startActivity (ReagendarCita aún es Activity)
        final String fnombre  = nombre;
        final String fproyecto = proyecto;
        final String ffecha   = fecha;
        final String fhora    = hora;
        final String finitials = initials;
        final int    fcolor   = avatarColor;

        binding.btnReagendar.setOnClickListener(v -> {
            android.content.Intent i =
                new android.content.Intent(requireContext(), ReagendarCitaActivity.class);
            i.putExtra(ReagendarCitaActivity.EXTRA_NOMBRE,       fnombre);
            i.putExtra(ReagendarCitaActivity.EXTRA_PROYECTO,     fproyecto);
            i.putExtra(ReagendarCitaActivity.EXTRA_FECHA,        ffecha);
            i.putExtra(ReagendarCitaActivity.EXTRA_HORA,         fhora);
            i.putExtra(ReagendarCitaActivity.EXTRA_INITIALS,     finitials);
            i.putExtra(ReagendarCitaActivity.EXTRA_AVATAR_COLOR, fcolor);
            startActivity(i);
        });

        binding.btnSeparar.setOnClickListener(v ->
            startActivity(new android.content.Intent(
                requireContext(), NuevaSeparacionActivity.class)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
