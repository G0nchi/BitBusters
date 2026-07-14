package com.example.bitbusters.models;

public class ClientAppointment {
    public static final String STATUS_PENDING = "Pendiente";
    public static final String STATUS_CONFIRMED = "Confirmada";
    public static final String STATUS_COMPLETED = "Realizada";
    public static final String STATUS_CANCELED = "Cancelada";
    public static final String STATUS_REVIEWED = "Valorada";

    private final String id;
    private final String projectName;
    private final String location;
    private final String date;
    private final String time;
    private final String advisorName;
    private final String advisorInitials;
    private final int advisorColor;
    private final String status;

    // Campos adicionales para operaciones Firestore (mutable, no expuestos al adapter)
    private String firestoreId;
    private String slotId;
    private String proyectoId;
    private String uidAsesorCita;

    public ClientAppointment(
            String id,
            String projectName,
            String location,
            String date,
            String time,
            String advisorName,
            String advisorInitials,
            int advisorColor,
            String status
    ) {
        this.id = id;
        this.projectName = projectName;
        this.location = location;
        this.date = date;
        this.time = time;
        this.advisorName = advisorName;
        this.advisorInitials = advisorInitials;
        this.advisorColor = advisorColor;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getAdvisorName() {
        return advisorName;
    }

    public String getAdvisorInitials() {
        return advisorInitials;
    }

    public int getAdvisorColor() {
        return advisorColor;
    }

    public String getStatus() {
        return status;
    }

    // ── Setters de campos Firestore (fluent, para uso post-construcción) ──────────

    public ClientAppointment setFirestoreId(String v)    { firestoreId   = v; return this; }
    public ClientAppointment setSlotId(String v)         { slotId        = v; return this; }
    public ClientAppointment setProyectoId(String v)     { proyectoId    = v; return this; }
    public ClientAppointment setUidAsesorCita(String v)  { uidAsesorCita = v; return this; }

    public String getFirestoreId()   { return firestoreId; }
    public String getSlotId()        { return slotId; }
    public String getProyectoId()    { return proyectoId; }
    public String getUidAsesorCita() { return uidAsesorCita; }

    /** Devuelve una copia con estado diferente, copiando los campos Firestore. */
    public ClientAppointment withStatus(String nuevoEstado) {
        ClientAppointment copy = new ClientAppointment(id, projectName, location, date, time,
                advisorName, advisorInitials, advisorColor, nuevoEstado);
        copy.firestoreId   = this.firestoreId;
        copy.slotId        = this.slotId;
        copy.proyectoId    = this.proyectoId;
        copy.uidAsesorCita = this.uidAsesorCita;
        return copy;
    }
}
