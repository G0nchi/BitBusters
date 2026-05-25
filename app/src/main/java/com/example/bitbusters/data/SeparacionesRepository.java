package com.example.bitbusters.data;

import com.example.bitbusters.models.AdminSeparacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio estático en memoria para la lista de separaciones del Administrador.
 * Actúa como fuente única de verdad (single source of truth) compartida entre
 * AdminSeparacionesActivity y AdminDetallesSeparacionActivity.
 *
 * La lista se inicializa una sola vez con los datos de AdminDataRepository y
 * persiste durante la sesión de la app (en memoria de proceso).
 */
public final class SeparacionesRepository {

    // Lista estática compartida — se inicializa solo la primera vez
    private static List<AdminSeparacion> lista = null;

    // Constructor privado — no instanciar
    private SeparacionesRepository() {}

    /**
     * Retorna la lista viva de separaciones.
     * Si aún no se ha inicializado, la carga desde AdminDataRepository.
     */
    public static List<AdminSeparacion> getLista() {
        if (lista == null) {
            lista = new ArrayList<>(AdminDataRepository.getSeparaciones());
        }
        return lista;
    }

    /**
     * Agrega una nueva separación al INICIO de la lista (más reciente primero).
     *
     * @param separacion La separación recién creada para añadir.
     */
    public static void agregar(AdminSeparacion separacion) {
        getLista().add(0, separacion);
    }

    /**
     * Busca una separación por su ID único.
     *
     * @param id El ID de la separación a buscar.
     * @return La separación encontrada, o null si no existe.
     */
    public static AdminSeparacion getById(String id) {
        if (id == null) return null;
        for (AdminSeparacion s : getLista()) {
            if (id.equals(s.getId())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Actualiza el estado de una separación existente por ID.
     * Los estados válidos son: "Pendiente", "Aprobada", "Rechazada".
     *
     * @param id          El ID de la separación a actualizar.
     * @param nuevoEstado El nuevo estado a asignar.
     */
    public static void actualizarEstado(String id, String nuevoEstado) {
        if (id == null || nuevoEstado == null) return;
        for (AdminSeparacion s : getLista()) {
            if (id.equals(s.getId())) {
                s.setEstado(nuevoEstado);
                return;
            }
        }
    }

    /**
     * Retorna el índice (posición) de una separación en la lista por su ID.
     * Útil para hacer scroll hasta ella en el RecyclerView.
     *
     * @param id El ID de la separación.
     * @return El índice (0-based), o -1 si no se encuentra.
     */
    public static int getPosicion(String id) {
        if (id == null) return -1;
        List<AdminSeparacion> l = getLista();
        for (int i = 0; i < l.size(); i++) {
            if (id.equals(l.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }
}
