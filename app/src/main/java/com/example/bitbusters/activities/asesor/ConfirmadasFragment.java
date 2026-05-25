package com.example.bitbusters.activities.asesor;

import com.example.bitbusters.models.AsesorCita;
import com.example.bitbusters.utils.AsesorStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Tab "Confirmadas" — dinámicas primero, estáticas después.
 * Excluye las que hayan sido canceladas.
 */
public class ConfirmadasFragment extends BaseCitasFragment {

    @Override
    protected List<CitaAdapter.Cita> buildCitasForTab() {
        Set<String> cancelledKeys = AsesorStorage.getCancelledKeys(requireContext());

        List<CitaAdapter.Cita> list = new ArrayList<>();

        // Citas confirmadas por el asesor en sesión (Room)
        for (AsesorCita a : AsesorStorage.getConfirmedCitas(requireContext())) {
            if (!cancelledKeys.contains(a.key())) {
                list.add(toConfirmada(a));
            }
        }

        // Citas estáticas confirmadas (filtrar canceladas)
        for (CitaAdapter.Cita c : staticConfirmadas()) {
            if (!cancelledKeys.contains(citaKey(c))) {
                list.add(c);
            }
        }

        return list;
    }
}
