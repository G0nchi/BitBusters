package com.example.bitbusters.models;

import com.google.firebase.Timestamp;

public class Mensaje {
    private String idEmisor;
    private String texto;
    private Timestamp timestamp;
    private boolean leido;

    public Mensaje() {}

    public String getIdEmisor() { return idEmisor; }
    public void setIdEmisor(String idEmisor) { this.idEmisor = idEmisor; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}
