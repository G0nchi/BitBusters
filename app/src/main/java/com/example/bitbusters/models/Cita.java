package com.example.bitbusters.models;

import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Cita {

    public static final String ESTADO_PENDIENTE  = "pendiente";
    public static final String ESTADO_CONFIRMADA = "confirmada";
    public static final String ESTADO_CANCELADA  = "cancelada";
    public static final String ESTADO_COMPLETADA = "completada";
    public static final String ESTADO_VALORADA   = "valorada";

    private String id;
    private String uidCliente;
    private String uidAsesor;
    private String proyectoId;
    private String proyectoNombre;
    private String nombreCliente;
    private String slotId;
    private Date   fechaTimestamp;
    private String estado;
    private Date   creadoEn;
    private Date   actualizadoEn;
    private List<Map<String, Object>> historialReagendamientos;

    public Cita() {}

    /** ID del documento Firestore; se asigna manualmente tras la lectura — no se persiste. */
    @Exclude
    public String getId() { return id; }
    public void   setId(String id) { this.id = id; }

    public String getUidCliente() { return uidCliente; }
    public void   setUidCliente(String uidCliente) { this.uidCliente = uidCliente; }

    public String getUidAsesor() { return uidAsesor; }
    public void   setUidAsesor(String uidAsesor) { this.uidAsesor = uidAsesor; }

    public String getProyectoId() { return proyectoId; }
    public void   setProyectoId(String proyectoId) { this.proyectoId = proyectoId; }

    public String getProyectoNombre() { return proyectoNombre; }
    public void   setProyectoNombre(String proyectoNombre) { this.proyectoNombre = proyectoNombre; }

    public String getNombreCliente() { return nombreCliente; }
    public void   setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getSlotId() { return slotId; }
    public void   setSlotId(String slotId) { this.slotId = slotId; }

    public Date getFechaTimestamp() { return fechaTimestamp; }
    public void setFechaTimestamp(Date fechaTimestamp) { this.fechaTimestamp = fechaTimestamp; }

    public String getEstado() { return estado; }
    public void   setEstado(String estado) { this.estado = estado; }

    public Date getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Date creadoEn) { this.creadoEn = creadoEn; }

    public Date getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Date actualizadoEn) { this.actualizadoEn = actualizadoEn; }

    public List<Map<String, Object>> getHistorialReagendamientos() { return historialReagendamientos; }
    public void setHistorialReagendamientos(List<Map<String, Object>> v) { this.historialReagendamientos = v; }
}
