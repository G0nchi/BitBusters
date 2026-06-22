package com.example.bitbusters.models;

import com.google.firebase.Timestamp;
import java.util.List;

public class Chat {
    private String chatId;
    private List<String> participantes;
    private String nombreCliente;
    private String nombreAsesor;
    private String fotoAsesor;
    private String idProyecto;
    private String nombreProyecto;
    private String ultimoMensaje;
    private Timestamp timestampUltimoMensaje;

    public Chat() {}

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public List<String> getParticipantes() { return participantes; }
    public void setParticipantes(List<String> participantes) { this.participantes = participantes; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getNombreAsesor() { return nombreAsesor; }
    public void setNombreAsesor(String nombreAsesor) { this.nombreAsesor = nombreAsesor; }

    public String getFotoAsesor() { return fotoAsesor; }
    public void setFotoAsesor(String fotoAsesor) { this.fotoAsesor = fotoAsesor; }

    public String getIdProyecto() { return idProyecto; }
    public void setIdProyecto(String idProyecto) { this.idProyecto = idProyecto; }

    public String getNombreProyecto() { return nombreProyecto; }
    public void setNombreProyecto(String nombreProyecto) { this.nombreProyecto = nombreProyecto; }

    public String getUltimoMensaje() { return ultimoMensaje; }
    public void setUltimoMensaje(String ultimoMensaje) { this.ultimoMensaje = ultimoMensaje; }

    public Timestamp getTimestampUltimoMensaje() { return timestampUltimoMensaje; }
    public void setTimestampUltimoMensaje(Timestamp timestampUltimoMensaje) {
        this.timestampUltimoMensaje = timestampUltimoMensaje;
    }
}
