package com.example.bitbusters.models;

public class Notification {
    private String id;
    private String name;
    private String message;
    private String time;
    private int avatarResId;
    private int propertyResId;
    private boolean isOld;
    private String tipo;
    private String separacionId;
    private String proyectoId;
    private String proyectoNombre;
    private String uidAsesor;
    private String inmobiliariaId;
    private String montoSeparacion;

    public Notification(String id, String name, String message, String time, int avatarResId, int propertyResId, boolean isOld) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.time = time;
        this.avatarResId = avatarResId;
        this.propertyResId = propertyResId;
        this.isOld = isOld;
    }

    public Notification(String id, String name, String message, String time,
                        int avatarResId, int propertyResId, boolean isOld,
                        String tipo, String separacionId, String proyectoId,
                        String proyectoNombre, String uidAsesor,
                        String inmobiliariaId, String montoSeparacion) {
        this(id, name, message, time, avatarResId, propertyResId, isOld);
        this.tipo = tipo;
        this.separacionId = separacionId;
        this.proyectoId = proyectoId;
        this.proyectoNombre = proyectoNombre;
        this.uidAsesor = uidAsesor;
        this.inmobiliariaId = inmobiliariaId;
        this.montoSeparacion = montoSeparacion;
    }

    // Convenience constructor for simple test/sample notifications
    public Notification(String name, String message, String time) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.message = message;
        this.time = time;
        this.avatarResId = 0; // no avatar by default
        this.propertyResId = 0; // no property image by default
        this.isOld = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public int getAvatarResId() { return avatarResId; }
    public int getPropertyResId() { return propertyResId; }
    public boolean isOld() { return isOld; }
    public String getTipo() { return tipo; }
    public String getSeparacionId() { return separacionId; }
    public String getProyectoId() { return proyectoId; }
    public String getProyectoNombre() { return proyectoNombre; }
    public String getUidAsesor() { return uidAsesor; }
    public String getInmobiliariaId() { return inmobiliariaId; }
    public String getMontoSeparacion() { return montoSeparacion; }
}
