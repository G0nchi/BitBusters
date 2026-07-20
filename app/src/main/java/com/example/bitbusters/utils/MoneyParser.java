package com.example.bitbusters.utils;

import androidx.annotation.Nullable;

/**
 * Parsea montos guardados como texto libre (ej. "S/ 150,000", "150000", "150000.50")
 * en Firestore (campo `separaciones.monto`). Devuelve null si el texto no contiene
 * ningún número reconocible, para que el llamador pueda excluirlo de sumas/promedios
 * en vez de arrastrar un 0 que distorsione la métrica.
 */
public final class MoneyParser {

    private MoneyParser() {
    }

    /**
     * Variante para cuando el valor viene de {@link com.google.firebase.firestore.DocumentSnapshot#get}
     * (tipo desconocido en tiempo de compilación): algunos documentos de `separaciones` tienen
     * `monto` guardado como Number (alta manual/legado) en vez del String que escribe
     * {@code ConfirmacionSeparacionActivity}. {@code DocumentSnapshot#getString} lanza
     * RuntimeException si el campo existe con otro tipo, así que este método evita llamarlo
     * a ciegas.
     */
    @Nullable
    public static Double parse(@Nullable Object raw) {
        if (raw instanceof Number) {
            double value = ((Number) raw).doubleValue();
            return value > 0 ? value : null;
        }
        if (raw instanceof String) {
            return parse((String) raw);
        }
        return null;
    }

    @Nullable
    public static Double parse(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        // Deja solo dígitos, comas y puntos.
        cleaned = cleaned.replaceAll("[^0-9.,]", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        // Trata la coma como separador de miles (formato "S/ 150,000").
        cleaned = cleaned.replace(",", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(cleaned);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Formatea un monto como "S/ 1.26M" / "S/ 45.3K" / "S/ 320" para tarjetas de dashboard. */
    public static String formatCompacto(double value) {
        if (value >= 1_000_000) {
            return String.format(java.util.Locale.getDefault(), "S/ %.2fM", value / 1_000_000);
        }
        if (value >= 1_000) {
            return String.format(java.util.Locale.getDefault(), "S/ %.1fK", value / 1_000);
        }
        return String.format(java.util.Locale.getDefault(), "S/ %.0f", value);
    }
}
