package com.example.bitbusters.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Singleton para mantener datos temporales del proyecto durante la sesión de creación/edición.
 * Se limpia cuando el usuario guarda, cancela o vuelve sin confirmar cambios.
 */
public class ProjectSessionData {
    private static ProjectSessionData instance;

    // Datos básicos del proyecto
    public String nombreProyecto = "";
    public String descripcion = "";
    public String estado = "";
    public String fechaEntrega = "";
    public String direccion = "";
    public String distrito = "";
    public String costoSeparacion = "";
    public String precioTotal = "";
    public String nombreComercial = "";
    public String precioPublicado = "";

    // Contenedores para datos agregados
    public List<TipologiaData> tipologias = new ArrayList<>();
    public List<AsesorData> asesores = new ArrayList<>();
    public List<String> imagenes = new ArrayList<>();
    public Set<String> areasSeleccionadas = new HashSet<>();

    private ProjectSessionData() {
    }

    public static synchronized ProjectSessionData getInstance() {
        if (instance == null) {
            instance = new ProjectSessionData();
        }
        return instance;
    }

    /**
     * Limpia todos los datos de la sesión.
     * Llamar cuando se guarda, cancela o vuelve.
     */
    public void clear() {
        nombreProyecto = "";
        descripcion = "";
        estado = "";
        fechaEntrega = "";
        direccion = "";
        distrito = "";
        costoSeparacion = "";
        precioTotal = "";
        nombreComercial = "";
        precioPublicado = "";
        tipologias.clear();
        asesores.clear();
        imagenes.clear();
        areasSeleccionadas.clear();
    }

    /**
     * Clase interna para datos de tipología
     */
    public static class TipologiaData {
        public String id;
        public String nombre;
        public int dormitorios;
        public int banos;
        public String metraje;
        public String precio;
        public String descripcion;
        public List<String> imagenes;

        public TipologiaData(String nombre) {
            this.nombre = nombre;
            this.imagenes = new ArrayList<>();
        }
    }

    /**
     * Clase interna para datos de asesor
     */
    public static class AsesorData {
        public String id;
        public String nombre;
        public String email;
        public String telefono;

        public AsesorData(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }
    }
}
