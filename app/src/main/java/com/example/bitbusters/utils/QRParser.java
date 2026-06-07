package com.example.bitbusters.utils;

import com.example.bitbusters.models.QRResult;
import org.json.JSONException;
import org.json.JSONObject;

public class QRParser {

    private static final String URI_PREFIX = "inmobiliaria://proyecto/";

    public static QRResult parse(String raw) {
        if (raw == null || raw.isEmpty()) return QRResult.invalido();

        // Formato URI: inmobiliaria://proyecto/{id}
        if (raw.startsWith(URI_PREFIX)) {
            String id = raw.substring(URI_PREFIX.length()).trim();
            return id.isEmpty() ? QRResult.invalido() : new QRResult("proyecto", id, true);
        }

        // Formato JSON: {"tipo":"proyecto","id":"123"}
        if (raw.startsWith("{")) {
            try {
                JSONObject json = new JSONObject(raw);
                String tipo = json.optString("tipo", "").trim();
                String id = json.optString("id", "").trim();
                if (!tipo.isEmpty() && !id.isEmpty()) {
                    return new QRResult(tipo, id, true);
                }
            } catch (JSONException e) {
                // cae al resultado inválido
            }
        }

        return QRResult.invalido();
    }
}
