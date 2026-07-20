package com.example.bitbusters.utils;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Parsea el campo `separaciones.fecha`, que se guarda como texto libre generado por
 * {@code NuevaSeparacionActivity.showDatePicker()} con el formato exacto
 * {@code "%s %d %s, %d"} (ej. "Vie 12 Jul, 2026": día de semana, día, mes abreviado, año).
 * No es un Timestamp real, así que este parser es best-effort: si el texto no matchea
 * el formato esperado (dato antiguo, entrada manual distinta, etc.) devuelve null y el
 * llamador debe excluir ese registro del cálculo en vez de asumir una fecha incorrecta.
 */
public final class SeparacionFechaParser {

    private static final String[] MESES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private SeparacionFechaParser() {
    }

    /**
     * Variante para cuando el valor viene de {@link com.google.firebase.firestore.DocumentSnapshot#get}
     * (tipo desconocido en tiempo de compilación): `DocumentSnapshot#getString` lanza
     * RuntimeException si el campo existe con otro tipo (ej. un Timestamp guardado por error
     * en un alta manual/legado), así que este método evita llamarlo a ciegas.
     */
    @Nullable
    public static Date parse(@Nullable Object raw) {
        if (raw instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) raw).toDate();
        }
        if (raw instanceof String) {
            return parse((String) raw);
        }
        return null;
    }

    @Nullable
    public static Date parse(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String[] tokens = trimmed.split("\\s+");
        if (tokens.length < 4) {
            return null;
        }
        try {
            int dia = Integer.parseInt(tokens[1]);
            String mesToken = tokens[2].replace(",", "").trim();
            int mes = -1;
            for (int i = 0; i < MESES.length; i++) {
                if (MESES[i].equalsIgnoreCase(mesToken)) {
                    mes = i;
                    break;
                }
            }
            if (mes == -1) {
                return null;
            }
            int anio = Integer.parseInt(tokens[3]);

            Calendar cal = Calendar.getInstance(Locale.getDefault());
            cal.clear();
            cal.set(anio, mes, dia, 0, 0, 0);
            return cal.getTime();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
