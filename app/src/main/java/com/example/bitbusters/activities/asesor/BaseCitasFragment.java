package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.models.AsesorCita;
import com.example.bitbusters.utils.AsesorNotificationHelper;
import com.example.bitbusters.utils.AsesorStorage;
import com.example.bitbusters.utils.AsesorWorkHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Fragment base para los tabs de Citas Agendadas.
 *
 * Cada tab (Pendientes, Confirmadas, Pasadas) extiende esta clase y
 * sólo sobreescribe {@link #buildCitasForTab()} para proveer su lista.
 * Toda la lógica de negocio y los datos estáticos residen aquí para
 * evitar duplicación y demostrar el patrón Fragment con herencia.
 */
public abstract class BaseCitasFragment extends Fragment {

    protected CitaAdapter adapter;

    // ── Colores de badge ─────────────────────────────────────────────────────

    protected static final int COLOR_PEND   = Color.parseColor("#9A5700");
    protected static final int COLOR_CONF   = Color.parseColor("#186A3B");
    protected static final int COLOR_PASADA = Color.parseColor("#666666");
    protected static final int COLOR_VALOR  = Color.parseColor("#1A5799");
    protected static final int COLOR_CANCEL = Color.parseColor("#CC2222");

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_citas_tab, container, false);
        RecyclerView rv = view.findViewById(R.id.rv_citas_fragment);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CitaAdapter(buildCitasForTab(),
            new CitaAdapter.OnCitaActionListener() {
                @Override public void onLeftClick(int pos, CitaAdapter.Cita c)  { handleLeft(c); }
                @Override public void onRightClick(int pos, CitaAdapter.Cita c) { handleRight(c); }
            });
        rv.setAdapter(adapter);
        return view;
    }

    /** Refresca la lista cuando el usuario regresa de otra pantalla. */
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) adapter.updateCitas(buildCitasForTab());
    }

    // ── Método abstracto ──────────────────────────────────────────────────────

    /** Cada fragment hijo construye la lista de citas de su propio tab. */
    protected abstract List<CitaAdapter.Cita> buildCitasForTab();

    // ── Acciones ──────────────────────────────────────────────────────────────

    protected void handleLeft(CitaAdapter.Cita c) {
        switch (c.btnLeft) {
            case "Reagendar":      openReagendar(c);     break;
            case "Ver detalle":    openVerDetalle(c);    break;
            case "Ver valoración":
                startActivity(new Intent(requireContext(), ValorarVisitaActivity.class)); break;
        }
    }

    protected void handleRight(CitaAdapter.Cita c) {
        switch (c.btnRight) {
            case "Confirmar": showConfirmDialog(c); break;
            case "Separar":   openNuevaSeparacion(c); break;
            case "Valorar":
                startActivity(new Intent(requireContext(), ValorarVisitaActivity.class)); break;
            case "Cancelar":  showCancelDialog(c); break;
            case "Reagendar": openReagendar(c);    break;
        }
    }

    // ── Diálogos ──────────────────────────────────────────────────────────────

    protected void showConfirmDialog(CitaAdapter.Cita c) {
        new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.cita_dialog_title))
            .setMessage(getString(R.string.cita_dialog_msg))
            .setPositiveButton(getString(R.string.cita_dialog_ok), (d, w) -> {
                String key = citaKey(c);
                AsesorStorage.confirmPendienteCita(requireContext(),
                    c.nombre, c.proyecto, c.fecha, c.hora, c.initials, c.avatarColor);
                AsesorNotificationHelper.showCitaConfirmada(requireContext(), c.nombre);
                AsesorWorkHelper.scheduleRecordatorio(
                    requireContext(), key, c.nombre, c.fecha, c.hora);
                // Actualizar lista con DiffUtil (no rebuildea el adapter entero)
                if (adapter != null) adapter.updateCitas(buildCitasForTab());
            })
            .setNegativeButton("No", null)
            .show();
    }

    protected void showCancelDialog(CitaAdapter.Cita c) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Cancelar cita")
            .setMessage("¿Seguro que deseas cancelar esta cita? Se notificará al cliente.")
            .setPositiveButton("Sí, cancelar", (d, w) -> {
                String key = citaKey(c);
                AsesorStorage.cancelCita(requireContext(),
                    c.nombre, c.proyecto, c.fecha, c.hora, c.initials, c.avatarColor);
                AsesorNotificationHelper.showCitaCancelada(requireContext(), c.nombre);
                AsesorWorkHelper.cancelRecordatorio(requireContext(), key);
                if (adapter != null) adapter.updateCitas(buildCitasForTab());
            })
            .setNegativeButton("No", null)
            .show();
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    protected void openReagendar(CitaAdapter.Cita c) {
        Intent i = new Intent(requireContext(), ReagendarCitaActivity.class);
        i.putExtra(ReagendarCitaActivity.EXTRA_NOMBRE,       c.nombre);
        i.putExtra(ReagendarCitaActivity.EXTRA_PROYECTO,     c.proyecto);
        i.putExtra(ReagendarCitaActivity.EXTRA_FECHA,        c.fecha);
        i.putExtra(ReagendarCitaActivity.EXTRA_HORA,         c.hora);
        i.putExtra(ReagendarCitaActivity.EXTRA_INITIALS,     c.initials);
        i.putExtra(ReagendarCitaActivity.EXTRA_AVATAR_COLOR, c.avatarColor);
        startActivity(i);
    }

    /**
     * Navega a VerDetalleCitaFragment usando NavController (Navigation Component).
     * Los datos van como Bundle de argumentos en lugar de Intent extras.
     */
    protected void openVerDetalle(CitaAdapter.Cita c) {
        Bundle args = new Bundle();
        args.putString("nombre",      c.nombre);
        args.putString("proyecto",    c.proyecto);
        args.putString("fecha",       c.fecha);
        args.putString("hora",        c.hora);
        args.putString("badge",       c.badge);
        args.putString("initials",    c.initials);
        args.putInt("avatarColor",    c.avatarColor);

        // NavController sube por la jerarquía: tab fragment → CitasViewPagerFragment → NavHost
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_tabs_to_detalle, args);
    }

    protected void openNuevaSeparacion(CitaAdapter.Cita c) {
        Intent i = new Intent(requireContext(), NuevaSeparacionActivity.class);
        i.putExtra(NuevaSeparacionActivity.EXTRA_CLIENTE,  c.nombre);
        i.putExtra(NuevaSeparacionActivity.EXTRA_PROYECTO, c.proyecto);
        i.putExtra(NuevaSeparacionActivity.EXTRA_INITIALS, c.initials);
        i.putExtra(NuevaSeparacionActivity.EXTRA_COLOR,    c.avatarColor);
        startActivity(i);
    }

    // ── Helpers de datos ──────────────────────────────────────────────────────

    protected String citaKey(CitaAdapter.Cita c) {
        return AsesorStorage.buildCitaKey(c.nombre, c.fecha, c.hora);
    }

    protected CitaAdapter.Cita toConfirmada(AsesorCita a) {
        return new CitaAdapter.Cita(a.initials, a.avatarColor, a.nombre, a.proyecto,
            a.fecha, a.hora, "Confirmada", 0, COLOR_CONF,
            "Ver detalle", "Cancelar", false, false);
    }

    protected CitaAdapter.Cita toCancelada(AsesorCita a) {
        return new CitaAdapter.Cita(a.initials, a.avatarColor, a.nombre, a.proyecto,
            a.fecha, a.hora, "Cancelada", 0, COLOR_CANCEL,
            "Ver detalle", "Reagendar", false, false);
    }

    // ── Datos estáticos base ──────────────────────────────────────────────────

    protected List<CitaAdapter.Cita> staticPendientes() {
        List<CitaAdapter.Cita> list = new ArrayList<>();
        list.add(new CitaAdapter.Cita("CM", Color.parseColor("#4ECDC4"), "Carlos Mendoza",
            "Torres del Sol · Dpto 302", "Lun 7 Abr, 2025", "10:30 AM",
            "Pendiente", 0, COLOR_PEND, "Reagendar", "Confirmar", false, false));
        list.add(new CitaAdapter.Cita("AL", Color.parseColor("#FF8C42"), "Ana López",
            "Torres del Sol · Dpto 501", "Mar 8 Abr, 2025", "3:00 PM",
            "Pendiente", 0, COLOR_PEND, "Reagendar", "Confirmar", false, false));
        list.add(new CitaAdapter.Cita("RT", Color.parseColor("#FF6B9D"), "Rosa Torres",
            "Torres del Sol · Dpto 108", "Mié 9 Abr, 2025", "11:00 AM",
            "Confirmada", 0, COLOR_CONF, "Ver detalle", "Separar", false, false));
        return list;
    }

    protected List<CitaAdapter.Cita> staticConfirmadas() {
        List<CitaAdapter.Cita> list = new ArrayList<>();
        list.add(new CitaAdapter.Cita("RT", Color.parseColor("#FF6B9D"), "Rosa Torres",
            "Torres del Sol · Dpto 108", "Mié 9 Abr, 2025", "11:00 AM",
            "Confirmada", 0, COLOR_CONF, "Ver detalle", "Cancelar", false, false));
        list.add(new CitaAdapter.Cita("MP", Color.parseColor("#9B59B6"), "Marco Paredes",
            "Torres del Sol · Dpto 210", "Jue 10 Abr, 2025", "2:00 PM",
            "Confirmada", 0, COLOR_CONF, "Ver detalle", "Cancelar", false, false));
        list.add(new CitaAdapter.Cita("SV", Color.parseColor("#3498DB"), "Sandra Vega",
            "Torres del Sol · Dpto 415", "Vie 11 Abr, 2025", "4:30 PM",
            "Confirmada", 0, COLOR_CONF, "Ver detalle", "Cancelar", false, false));
        return list;
    }

    protected List<CitaAdapter.Cita> staticPasadas() {
        List<CitaAdapter.Cita> list = new ArrayList<>();
        list.add(new CitaAdapter.Cita("RT", Color.parseColor("#FF6B9D"), "Rosa Torres",
            "Torres del Sol · Dpto 108", "Mié 2 Abr, 2025", "11:00 AM",
            "Realizada", 0, COLOR_PASADA, "Ver detalle", "Valorar", true, false));
        list.add(new CitaAdapter.Cita("LV", Color.parseColor("#C8956C"), "Luis Vargas",
            "Torres del Sol · Dpto 204", "Mar 1 Abr, 2025", "2:00 PM",
            "Cancelada", 0, COLOR_CANCEL, "Ver detalle", "Reagendar", false, false));
        list.add(new CitaAdapter.Cita("JC", Color.parseColor("#27AE60"), "Jorge Castro",
            "Torres del Sol · Dpto 601", "Lun 28 Mar, 2025", "4:00 PM",
            "Valorada", 0, COLOR_VALOR, "Ver valoración", "Valorar", false, true));
        return list;
    }
}
