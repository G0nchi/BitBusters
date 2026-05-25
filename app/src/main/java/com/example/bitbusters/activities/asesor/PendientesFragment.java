package com.example.bitbusters.activities.asesor;

import com.example.bitbusters.utils.AsesorStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Tab "Pendientes" — muestra citas aún sin confirmar ni cancelar.
 * Extiende BaseCitasFragment; sólo define qué datos mostrar.
 */
public class PendientesFragment extends BaseCitasFragment {

    @Override
    protected List<CitaAdapter.Cita> buildCitasForTab() {
        Set<String> confirmedKeys = AsesorStorage.getConfirmedPendKeys(requireContext());
        Set<String> cancelledKeys = AsesorStorage.getCancelledKeys(requireContext());

        List<CitaAdapter.Cita> list = new ArrayList<>();
        for (CitaAdapter.Cita c : staticPendientes()) {
            String key = citaKey(c);
            if (!confirmedKeys.contains(key) && !cancelledKeys.contains(key)) {
                list.add(c);
            }
        }
        return list;
    }
}
