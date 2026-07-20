package com.example.bitbusters.activities.asesor;

import com.example.bitbusters.models.Cita;

import java.util.HashSet;
import java.util.Set;

/**
 * Tab "Pasadas" — canceladas, realizadas y valoradas.
 */
public class PasadasFragment extends BaseCitasFragment {

    @Override
    protected Set<String> estadosAceptados() {
        Set<String> estados = new HashSet<>();
        estados.add(Cita.ESTADO_CANCELADA);
        estados.add(Cita.ESTADO_COMPLETADA);
        estados.add(Cita.ESTADO_VALORADA);
        return estados;
    }
}
