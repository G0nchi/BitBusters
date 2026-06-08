package com.example.bitbusters.models;

public class QRResult {
    public final String tipo;
    public final String id;
    public final boolean valido;

    public QRResult(String tipo, String id, boolean valido) {
        this.tipo = tipo;
        this.id = id;
        this.valido = valido;
    }

    public static QRResult invalido() {
        return new QRResult(null, null, false);
    }
}
