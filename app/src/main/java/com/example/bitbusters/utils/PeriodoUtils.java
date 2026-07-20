package com.example.bitbusters.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Límites de calendario (mes/trimestre/año) reutilizados por el dashboard y los
 * reportes de superadmin para acotar rangos de fechas reales al agregar métricas.
 */
public final class PeriodoUtils {

    private PeriodoUtils() {
    }

    public static Date inicioDeHoy() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date inicioDeMes(int offsetMeses) {
        Calendar cal = Calendar.getInstance();
        // DAY_OF_MONTH se fija a 1 ANTES de mover el mes: si se moviera después,
        // un día alto (ej. 31) podría desbordar hacia el mes siguiente en Calendar.set().
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, offsetMeses);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date inicioDeTrimestre(int offsetTrimestres) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int mesActual = cal.get(Calendar.MONTH);
        int inicioTrimestreMes = (mesActual / 3) * 3;
        cal.set(Calendar.MONTH, inicioTrimestreMes);
        cal.add(Calendar.MONTH, offsetTrimestres * 3);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date inicioDeAnio(int offsetAnios) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.add(Calendar.YEAR, offsetAnios);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date ahora() {
        return new Date();
    }
}
