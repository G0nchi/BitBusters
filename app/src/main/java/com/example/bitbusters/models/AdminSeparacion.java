package com.example.bitbusters.models;

public class AdminSeparacion {
    public String id;
    public String nombreProyecto;
    public String monto;
    public String fecha;
    public String cliente;
    public String estado; // "Pendiente", "Aprobada", "Rechazada"

    public AdminSeparacion(String id, String nombreProyecto, String monto, String fecha, String cliente, String estado) {
        this.id = id;
        this.nombreProyecto = nombreProyecto;
        this.monto = monto;
        this.fecha = fecha;
        this.cliente = cliente;
        this.estado = estado;
    }

    public String getId() { return id; }
    public String getNombreProyecto() { return nombreProyecto; }
    public String getMonto() { return monto; }
    public String getFecha() { return fecha; }
    public String getCliente() { return cliente; }
    public String getEstado() { return estado; }
}
