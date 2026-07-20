package com.example.bitbusters.models;

public class ClienteSeparacion {
    private String id;
    private String proyectoNombre;
    private String asesorNombre;
    private String monto;
    private String estado;
    private String estadoPago;
    private String metodoPago;
    private String fecha;
    private long ordenMillis;

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getProyectoNombre() { return proyectoNombre != null ? proyectoNombre : ""; }
    public void setProyectoNombre(String proyectoNombre) { this.proyectoNombre = proyectoNombre; }

    public String getAsesorNombre() { return asesorNombre != null ? asesorNombre : ""; }
    public void setAsesorNombre(String asesorNombre) { this.asesorNombre = asesorNombre; }

    public String getMonto() { return monto != null ? monto : ""; }
    public void setMonto(String monto) { this.monto = monto; }

    public String getEstado() { return estado != null ? estado : ""; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEstadoPago() { return estadoPago != null ? estadoPago : ""; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public String getMetodoPago() { return metodoPago != null ? metodoPago : ""; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getFecha() { return fecha != null ? fecha : ""; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public long getOrdenMillis() { return ordenMillis; }
    public void setOrdenMillis(long ordenMillis) { this.ordenMillis = ordenMillis; }
}
