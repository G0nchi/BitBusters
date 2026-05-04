package com.example.bitbusters.models;

public class AdminHistorialSeparacion {
    public String id;
    public String periodo;
    public String monto;
    public int numSeparaciones;
    public int numAsesores;
    public String fecha;
    public String proyecto;

    public AdminHistorialSeparacion(String id, String periodo, String monto, int numSeparaciones, int numAsesores, String fecha, String proyecto) {
        this.id = id;
        this.periodo = periodo;
        this.monto = monto;
        this.numSeparaciones = numSeparaciones;
        this.numAsesores = numAsesores;
        this.fecha = fecha;
        this.proyecto = proyecto;
    }

    public String getId() { return id; }
    public String getPeriodo() { return periodo; }
    public String getMonto() { return monto; }
    public int getNumSeparaciones() { return numSeparaciones; }
    public int getNumAsesores() { return numAsesores; }
    public String getFecha() { return fecha; }
    public String getProyecto() { return proyecto; }
}
