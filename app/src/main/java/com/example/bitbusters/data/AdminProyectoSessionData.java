package com.example.bitbusters.data;

import com.example.bitbusters.models.Tipologia;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton que guarda temporalmente los datos del proyecto
 * mientras el admin navega entre los pasos de creación.
 * Se limpia al guardar o cancelar.
 */
public class AdminProyectoSessionData {

    private static AdminProyectoSessionData instance;

    // ── Campos de texto ───────────────────────────────────────────────────────
    public String nombreProyecto  = "";
    public String descripcion     = "";
    public String direccion       = "";
    public String distrito        = "";
    public String costoSeparacion = "";
    public String precioTotal     = "";
    public String nombreComercial = "";
    public String precioPublicado = "";
    public String fechaEntrega    = "";
    public String estado          = ""; // "En planos" | "Preventa" | "En venta"

    // ── Parte 1: tipologías agregadas desde AdminAgregarTipologiaActivity ────
    public List<Tipologia> tipologias = new ArrayList<>();

    // ── Parte 2: asesores asignados desde AdminAsignarAsesoresActivity ───────
    public List<String> asesoresAsignados = new ArrayList<>();

    /** Constructor privado — solo se accede por getInstance() */
    private AdminProyectoSessionData() {}

    /** Devuelve la instancia única, creándola si no existe. */
    public static AdminProyectoSessionData getInstance() {
        if (instance == null) {
            instance = new AdminProyectoSessionData();
        }
        return instance;
    }

    /**
     * Limpia todos los datos al guardar o cancelar la creación del proyecto.
     * Setear instance = null fuerza la recreación al próximo getInstance().
     */
    public void clear() {
        instance          = null;
        nombreProyecto    = "";
        descripcion       = "";
        direccion         = "";
        distrito          = "";
        costoSeparacion   = "";
        precioTotal       = "";
        nombreComercial   = "";
        precioPublicado   = "";
        fechaEntrega      = "";
        estado            = "";
        tipologias        = new ArrayList<>();
        asesoresAsignados = new ArrayList<>();
    }
}
