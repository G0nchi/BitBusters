package com.example.bitbusters.activities.asesor;

import com.example.bitbusters.models.Cita;

import java.util.Collections;
import java.util.Set;

/**
 * Tab "Confirmadas" — citas confirmadas por el asesor, aún no realizadas.
 */
public class ConfirmadasFragment extends BaseCitasFragment {

    @Override
    protected Set<String> estadosAceptados() {
        return Collections.singleton(Cita.ESTADO_CONFIRMADA);
    }
}
