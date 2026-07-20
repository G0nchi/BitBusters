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
import com.example.bitbusters.models.Cita;
import com.example.bitbusters.repository.CitaRepository;
import com.example.bitbusters.utils.AsesorNotificationHelper;
import com.example.bitbusters.utils.AsesorWorkHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fragment base para los tabs de Citas Agendadas.
 *
 * Cada tab (Pendientes, Confirmadas, Pasadas) extiende esta clase y solo declara
 * qué {@link Cita#getEstado()} le corresponden mostrar. Los datos vienen en
 * tiempo real de {@code citas} (Firestore) vía {@link CitaRepository#escucharCitasAsesor}.
 */
public abstract class BaseCitasFragment extends Fragment {

    protected CitaAdapter adapter;
    private final CitaRepository citaRepository = new CitaRepository();
    private ListenerRegistration citasListener;

    private static final java.util.TimeZone LIMA = java.util.TimeZone.getTimeZone("America/Lima");
    private static final SimpleDateFormat FECHA_FMT =
            new SimpleDateFormat("EEE d MMM, yyyy", new Locale("es", "PE"));
    private static final SimpleDateFormat HORA_FMT =
            new SimpleDateFormat("h:mm a", Locale.US);
    static {
        FECHA_FMT.setTimeZone(LIMA);
        HORA_FMT.setTimeZone(LIMA);
    }

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

        adapter = new CitaAdapter(new ArrayList<>(),
            new CitaAdapter.OnCitaActionListener() {
                @Override public void onLeftClick(int pos, CitaAdapter.Cita c)  { handleLeft(c); }
                @Override public void onRightClick(int pos, CitaAdapter.Cita c) { handleRight(c); }
            });
        rv.setAdapter(adapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        iniciarListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (citasListener != null) {
            citasListener.remove();
            citasListener = null;
        }
    }

    private void iniciarListener() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        citasListener = citaRepository.escucharCitasAsesor(user.getUid(), new CitaRepository.CitasClienteListener() {
            @Override
            public void onCitasActualizadas(List<Cita> citas) {
                if (adapter == null) return;
                List<CitaAdapter.Cita> filtradas = new ArrayList<>();
                Set<String> estados = estadosAceptados();
                for (Cita c : citas) {
                    if (estados.contains(c.getEstado())) {
                        filtradas.add(mapearCita(c));
                    }
                }
                adapter.updateCitas(filtradas);
            }

            @Override
            public void onError(String mensaje) { /* silencioso: el listener reintenta solo */ }
        });
    }

    /** Estados de {@code citas.estado} que corresponden a este tab. */
    protected abstract Set<String> estadosAceptados();

    // ── Mapeo Cita (Firestore) → CitaAdapter.Cita (UI) ──────────────────────────

    private CitaAdapter.Cita mapearCita(Cita c) {
        String nombre = c.getNombreCliente() != null ? c.getNombreCliente() : "Cliente";
        String proyecto = c.getProyectoNombre() != null ? c.getProyectoNombre() : "";
        Date fechaDate = c.getFechaTimestamp();
        String fecha = fechaDate != null ? capitalizar(FECHA_FMT.format(fechaDate)) : "";
        String hora = fechaDate != null ? HORA_FMT.format(fechaDate) : "";
        String initials = iniciales(nombre);
        int avatarColor = colorParaNombre(nombre);

        String estado = c.getEstado() != null ? c.getEstado() : Cita.ESTADO_PENDIENTE;
        String badge; int badgeColor; String btnLeft; String btnRight;
        boolean showSeparacion = false; boolean showRating = false;

        switch (estado) {
            case Cita.ESTADO_CONFIRMADA:
                badge = "Confirmada"; badgeColor = COLOR_CONF;
                btnLeft = "Ver detalle"; btnRight = "Cancelar";
                break;
            case Cita.ESTADO_CANCELADA:
                badge = "Cancelada"; badgeColor = COLOR_CANCEL;
                btnLeft = "Ver detalle"; btnRight = "Reagendar";
                break;
            case Cita.ESTADO_COMPLETADA:
                badge = "Realizada"; badgeColor = COLOR_PASADA;
                btnLeft = "Ver detalle"; btnRight = "Valorar";
                showRating = true;
                break;
            case Cita.ESTADO_VALORADA:
                badge = "Valorada"; badgeColor = COLOR_VALOR;
                btnLeft = "Ver valoración"; btnRight = "Ver detalle";
                break;
            case Cita.ESTADO_PENDIENTE:
            default:
                badge = "Pendiente"; badgeColor = COLOR_PEND;
                btnLeft = "Reagendar"; btnRight = "Confirmar";
                break;
        }

        CitaAdapter.Cita ui = new CitaAdapter.Cita(initials, avatarColor, nombre, proyecto,
                fecha, hora, badge, 0, badgeColor, btnLeft, btnRight, showSeparacion, showRating);
        ui.conIdentidad(c.getId(), c.getUidCliente(), c.getUidAsesor(), c.getProyectoId(),
                c.getSlotId());
        return ui;
    }

    private static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static String iniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "--";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length >= 2) {
            return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase(Locale.ROOT);
        }
        return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase(Locale.ROOT);
    }

    private static final int[] PALETA_AVATAR = {
        Color.parseColor("#4ECDC4"), Color.parseColor("#FF8C42"), Color.parseColor("#FF6B9D"),
        Color.parseColor("#9B59B6"), Color.parseColor("#3498DB"), Color.parseColor("#27AE60"),
        Color.parseColor("#C8956C")
    };

    private static int colorParaNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) return PALETA_AVATAR[0];
        int idx = Math.abs(nombre.hashCode()) % PALETA_AVATAR.length;
        return PALETA_AVATAR[idx];
    }

    // ── Acciones ──────────────────────────────────────────────────────────────

    protected void handleLeft(CitaAdapter.Cita c) {
        switch (c.btnLeft) {
            case "Reagendar":      openReagendar(c);     break;
            case "Ver detalle":    openVerDetalle(c);    break;
            case "Ver valoración":
                startActivity(buildValorarIntent(c)); break;
        }
    }

    protected void handleRight(CitaAdapter.Cita c) {
        switch (c.btnRight) {
            case "Confirmar": showConfirmDialog(c); break;
            case "Separar":   openNuevaSeparacion(c); break;
            case "Valorar":
                startActivity(buildValorarIntent(c)); break;
            case "Cancelar":  showCancelDialog(c); break;
            case "Reagendar": openReagendar(c);    break;
            case "Ver detalle": openVerDetalle(c); break;
        }
    }

    // ── Diálogos ──────────────────────────────────────────────────────────────

    protected void showConfirmDialog(CitaAdapter.Cita c) {
        new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.cita_dialog_title))
            .setMessage(getString(R.string.cita_dialog_msg))
            .setPositiveButton(getString(R.string.cita_dialog_ok), (d, w) -> {
                citaRepository.confirmarCita(c.citaId)
                    .addOnSuccessListener(unused -> {
                        AsesorNotificationHelper.showCitaConfirmada(requireContext(), c.nombre);
                        AsesorWorkHelper.scheduleRecordatorio(
                            requireContext(), c.citaId, c.nombre, c.fecha, c.hora);
                    });
            })
            .setNegativeButton("No", null)
            .show();
    }

    protected void showCancelDialog(CitaAdapter.Cita c) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Cancelar cita")
            .setMessage("¿Seguro que deseas cancelar esta cita? Se notificará al cliente.")
            .setPositiveButton("Sí, cancelar", (d, w) -> {
                citaRepository.cancelarCita(c.citaId, c.slotId, "Cancelada por el asesor")
                    .addOnSuccessListener(unused -> {
                        AsesorNotificationHelper.showCitaCancelada(requireContext(), c.nombre);
                        AsesorWorkHelper.cancelRecordatorio(requireContext(), c.citaId);
                    });
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
        i.putExtra(ReagendarCitaActivity.EXTRA_CITA_ID,      c.citaId);
        i.putExtra(ReagendarCitaActivity.EXTRA_SLOT_ID,      c.slotId);
        i.putExtra(ReagendarCitaActivity.EXTRA_PROYECTO_ID,  c.proyectoId);
        i.putExtra(ReagendarCitaActivity.EXTRA_UID_CLIENTE,  c.uidCliente);
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
        args.putString("citaId",      c.citaId);
        args.putString("slotId",      c.slotId);
        args.putString("proyectoId",  c.proyectoId);
        args.putString("uidCliente",  c.uidCliente);

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
        i.putExtra(NuevaSeparacionActivity.EXTRA_CITA_ID,     c.citaId);
        i.putExtra(NuevaSeparacionActivity.EXTRA_UID_CLIENTE, c.uidCliente);
        i.putExtra(NuevaSeparacionActivity.EXTRA_PROYECTO_ID, c.proyectoId);
        startActivity(i);
    }

    // ── Helpers de datos ──────────────────────────────────────────────────────

    private Intent buildValorarIntent(CitaAdapter.Cita c) {
        Intent i = new Intent(requireContext(), ValorarVisitaActivity.class);
        i.putExtra(ValorarVisitaActivity.EXTRA_NOMBRE,   c.nombre);
        i.putExtra(ValorarVisitaActivity.EXTRA_INITIALS, c.initials);
        i.putExtra(ValorarVisitaActivity.EXTRA_PROYECTO, c.proyecto);
        i.putExtra(ValorarVisitaActivity.EXTRA_FECHA,    c.fecha);
        return i;
    }
}
