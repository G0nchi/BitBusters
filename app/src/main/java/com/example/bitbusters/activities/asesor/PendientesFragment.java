package com.example.bitbusters.activities.asesor;

import com.example.bitbusters.models.Cita;

import java.util.Collections;
import java.util.Set;

/**
 * Tab "Pendientes" — muestra citas aún sin confirmar ni cancelar.
 */
public class PendientesFragment extends BaseCitasFragment {

    @Override
    protected Set<String> estadosAceptados() {
        return Collections.singleton(Cita.ESTADO_PENDIENTE);
    }
}
