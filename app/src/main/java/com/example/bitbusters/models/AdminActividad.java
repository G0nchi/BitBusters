package com.example.bitbusters.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de datos para el resumen de actividad del administrador (Lab 5).
 * Serializado y deserializado como JSON con Gson en Internal Storage
 * bajo el archivo "admin_actividad.json".
 *
 * Formato JSON esperado:
 * {
 *   "proyectos_registrados": 3,
 *   "separaciones_pendientes": 2,
 *   "separaciones_aprobadas": 5,
 *   "ultimo_reporte": "25/05/2026 10:30"
 * }
 *
 * Los @SerializedName mapean los campos camelCase de Java
 * a los nombres snake_case del JSON especificado en el laboratorio.
 */
public class AdminActividad {

    /** Cantidad total de proyectos registrados por el admin. */
    @SerializedName("proyectos_registrados")
    private int proyectosRegistrados;

    /** Cantidad de separaciones pendientes de aprobación. */
    @SerializedName("separaciones_pendientes")
    private int separacionesPendientes;

    /** Cantidad de separaciones ya aprobadas por el admin. */
    @SerializedName("separaciones_aprobadas")
    private int separacionesAprobadas;

    /** Fecha y hora del último reporte guardado (formato "dd/MM/yyyy HH:mm"). */
    @SerializedName("ultimo_reporte")
    private String ultimoReporte;

    // ── Constructores ────────────────────────────────────────────────────────

    /** Constructor vacío requerido por Gson para deserializar el JSON. */
    public AdminActividad() {}

    /** Constructor completo para inicializar el objeto con todos los campos. */
    public AdminActividad(int proyectosRegistrados, int separacionesPendientes,
                          int separacionesAprobadas, String ultimoReporte) {
        this.proyectosRegistrados   = proyectosRegistrados;
        this.separacionesPendientes = separacionesPendientes;
        this.separacionesAprobadas  = separacionesAprobadas;
        this.ultimoReporte          = ultimoReporte;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /** Retorna la cantidad de proyectos registrados. */
    public int getProyectosRegistrados() {
        return proyectosRegistrados;
    }

    /** Retorna la cantidad de separaciones pendientes. */
    public int getSeparacionesPendientes() {
        return separacionesPendientes;
    }

    /** Retorna la cantidad de separaciones aprobadas. */
    public int getSeparacionesAprobadas() {
        return separacionesAprobadas;
    }

    /** Retorna la fecha/hora del último reporte guardado. */
    public String getUltimoReporte() {
        return ultimoReporte;
    }

    // ── Setters ──────────────────────────────────────────────────────────────

    /** Establece la cantidad de proyectos registrados. */
    public void setProyectosRegistrados(int proyectosRegistrados) {
        this.proyectosRegistrados = proyectosRegistrados;
    }

    /** Establece la cantidad de separaciones pendientes. */
    public void setSeparacionesPendientes(int separacionesPendientes) {
        this.separacionesPendientes = separacionesPendientes;
    }

    /** Establece la cantidad de separaciones aprobadas. */
    public void setSeparacionesAprobadas(int separacionesAprobadas) {
        this.separacionesAprobadas = separacionesAprobadas;
    }

    /** Establece la fecha/hora del último reporte. */
    public void setUltimoReporte(String ultimoReporte) {
        this.ultimoReporte = ultimoReporte;
    }

    /** Representación en texto para depuración (Logcat). */
    @Override
    public String toString() {
        return "AdminActividad{"
                + "proyectosRegistrados=" + proyectosRegistrados
                + ", separacionesPendientes=" + separacionesPendientes
                + ", separacionesAprobadas=" + separacionesAprobadas
                + ", ultimoReporte='" + ultimoReporte + '\''
                + '}';
    }
}
