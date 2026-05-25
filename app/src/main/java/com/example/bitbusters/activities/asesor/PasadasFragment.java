package com.example.bitbusters.activities.asesor;

import com.example.bitbusters.models.AsesorCita;
import com.example.bitbusters.utils.AsesorStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab "Pasadas" — canceladas dinámicamente primero, luego las estáticas.
 */
public class PasadasFragment extends BaseCitasFragment {

    @Override
    protected List<CitaAdapter.Cita> buildCitasForTab() {
        List<CitaAdapter.Cita> list = new ArrayList<>();

        // Citas canceladas por el asesor (Room)
        for (AsesorCita a : AsesorStorage.getCancelledCitas(requireContext())) {
            list.add(toCancelada(a));
        }

        // Citas estáticas (realizadas, valoradas, canceladas de muestra)
        list.addAll(staticPasadas());

        return list;
    }
}
