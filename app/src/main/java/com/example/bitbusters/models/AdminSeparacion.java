package com.example.bitbusters.models;

public class AdminSeparacion {
    public String id;
    public String nombreProyecto;
    public String monto;
    public String fecha;
    public String cliente;
    public String estado; // "Pendiente", "Aprobada", "Rechazada"
    public String clienteUid;
    public String uidAsesor;
    public String asesorNombre;
    public String proyectoId;
    public String inmobiliariaId;
    public String estadoPago;
    public String metodoPago;
    public String comprobantePago;
    public String observacionPago;

    public AdminSeparacion() {
        // Constructor vacío requerido para compatibilidad con Firestore/serialización.
    }

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
    public String getClienteUid() { return clienteUid != null ? clienteUid : ""; }
    public String getUidAsesor() { return uidAsesor != null ? uidAsesor : ""; }
    public String getAsesorNombre() { return asesorNombre != null ? asesorNombre : ""; }
    public String getProyectoId() { return proyectoId != null ? proyectoId : ""; }
    public String getInmobiliariaId() { return inmobiliariaId != null ? inmobiliariaId : ""; }
    public String getEstadoPago() { return estadoPago != null ? estadoPago : ""; }
    public String getMetodoPago() { return metodoPago != null ? metodoPago : ""; }
    public String getComprobantePago() { return comprobantePago != null ? comprobantePago : ""; }
    public String getObservacionPago() { return observacionPago != null ? observacionPago : ""; }

    public void setId(String id) { this.id = id; }
    public void setNombreProyecto(String nombreProyecto) { this.nombreProyecto = nombreProyecto; }
    public void setMonto(String monto) { this.monto = monto; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public void setClienteUid(String clienteUid) { this.clienteUid = clienteUid; }
    public void setUidAsesor(String uidAsesor) { this.uidAsesor = uidAsesor; }
    public void setAsesorNombre(String asesorNombre) { this.asesorNombre = asesorNombre; }
    public void setProyectoId(String proyectoId) { this.proyectoId = proyectoId; }
    public void setInmobiliariaId(String inmobiliariaId) { this.inmobiliariaId = inmobiliariaId; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public void setComprobantePago(String comprobantePago) { this.comprobantePago = comprobantePago; }
    public void setObservacionPago(String observacionPago) { this.observacionPago = observacionPago; }

    /**
     * Actualiza el estado de la separación.
     * Usado por SeparacionesRepository.actualizarEstado() al aprobar o rechazar.
     *
     * @param nuevoEstado "Pendiente", "Aprobada" o "Rechazada"
     */
    public void setEstado(String nuevoEstado) { this.estado = nuevoEstado; }
}
