package com.example.bitbusters.data;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Foto en memoria de los datos reales que respaldan el dashboard y los reportes de
 * superadmin (colecciones `users`, `separaciones`, `citas`, `logs`). No hace ninguna
 * llamada a Firestore: {@link SuperadminMetricsRepository} arma esta foto una vez y
 * los cálculos de aquí son puros, para poder recalcular al cambiar de período sin
 * volver a consultar la base de datos.
 */
public class SuperadminMetricsSnapshot {

    public static class UserRecord {
        public final String role;
        public final String status;
        @Nullable public final String inmobiliaria;
        @Nullable public final Date fechaRegistro;

        public UserRecord(String role, String status, @Nullable String inmobiliaria, @Nullable Date fechaRegistro) {
            this.role = role;
            this.status = status;
            this.inmobiliaria = inmobiliaria;
            this.fechaRegistro = fechaRegistro;
        }
    }

    public static class ReservationRecord {
        @Nullable public final String inmobiliaria;
        @Nullable public final Double monto;
        @Nullable public final Date fechaVisita;
        @Nullable public final Date fechaCreacion;

        public ReservationRecord(@Nullable String inmobiliaria, @Nullable Double monto,
                                  @Nullable Date fechaVisita, @Nullable Date fechaCreacion) {
            this.inmobiliaria = inmobiliaria;
            this.monto = monto;
            this.fechaVisita = fechaVisita;
            this.fechaCreacion = fechaCreacion;
        }
    }

    public static class CitaRecord {
        public final String estado;
        @Nullable public final Date fechaCreacion;

        public CitaRecord(String estado, @Nullable Date fechaCreacion) {
            this.estado = estado;
            this.fechaCreacion = fechaCreacion;
        }
    }

    public static class CompanyShare {
        public final String nombre;
        public final int cantidad;
        public final double porcentaje;

        public CompanyShare(String nombre, int cantidad, double porcentaje) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.porcentaje = porcentaje;
        }
    }

    private static final String SIN_ASIGNAR = "Sin asignar";
    private static final String OTRAS = "Otras";

    private final List<UserRecord> usuarios;
    private final List<ReservationRecord> reservas;
    private final List<CitaRecord> citas;
    private final int totalLogs;

    public SuperadminMetricsSnapshot(List<UserRecord> usuarios, List<ReservationRecord> reservas,
                                      List<CitaRecord> citas, int totalLogs) {
        this.usuarios = usuarios;
        this.reservas = reservas;
        this.citas = citas;
        this.totalLogs = totalLogs;
    }

    public int totalLogs() {
        return totalLogs;
    }

    public int totalUsuarios() {
        int count = 0;
        for (UserRecord u : usuarios) {
            if (u.role != null && !"superadmin".equals(u.role)) count++;
        }
        return count;
    }

    public int totalAsesores() {
        int count = 0;
        for (UserRecord u : usuarios) {
            if ("asesor".equals(u.role)) count++;
        }
        return count;
    }

    public int asesoresRegistradosDesde(Date since) {
        int count = 0;
        for (UserRecord u : usuarios) {
            if ("asesor".equals(u.role) && u.fechaRegistro != null && !u.fechaRegistro.before(since)) {
                count++;
            }
        }
        return count;
    }

    public int empresasDistintas() {
        return new java.util.HashSet<>(companyNames()).size();
    }

    private List<String> companyNames() {
        List<String> names = new ArrayList<>();
        for (UserRecord u : usuarios) {
            if (("asesor".equals(u.role) || "admin".equals(u.role))
                    && u.inmobiliaria != null && !u.inmobiliaria.trim().isEmpty()) {
                names.add(u.inmobiliaria.trim());
            }
        }
        return names;
    }

    public int aprobacionesPendientes() {
        int count = 0;
        for (UserRecord u : usuarios) {
            if ("asesor".equals(u.role) && "pending".equals(u.status)) count++;
        }
        return count;
    }

    public int reservasEnRango(@Nullable Date from, @Nullable Date to) {
        int count = 0;
        for (ReservationRecord r : reservas) {
            if (enRango(r.fechaCreacion, from, to)) count++;
        }
        return count;
    }

    @Nullable
    public Double ingresosEnRango(@Nullable Date from, @Nullable Date to) {
        double suma = 0;
        boolean huboAlguno = false;
        for (ReservationRecord r : reservas) {
            if (enRango(r.fechaCreacion, from, to) && r.monto != null) {
                suma += r.monto;
                huboAlguno = true;
            }
        }
        return huboAlguno ? suma : null;
    }

    @Nullable
    public Double tasaVencidasEnRango(@Nullable Date from, @Nullable Date to) {
        Date ahora = new Date();
        int total = 0;
        int vencidas = 0;
        for (ReservationRecord r : reservas) {
            if (!enRango(r.fechaCreacion, from, to) || r.fechaVisita == null) continue;
            total++;
            if (r.fechaVisita.before(ahora)) vencidas++;
        }
        return total > 0 ? (vencidas * 100.0) / total : null;
    }

    @Nullable
    public Double tasaCancelacionCitasEnRango(@Nullable Date from, @Nullable Date to) {
        int total = 0;
        int canceladas = 0;
        for (CitaRecord c : citas) {
            if (!enRango(c.fechaCreacion, from, to)) continue;
            total++;
            if ("cancelada".equals(c.estado)) canceladas++;
        }
        return total > 0 ? (canceladas * 100.0) / total : null;
    }

    @Nullable
    public Double tasaHabilitacionAsesoresEnRango(@Nullable Date from, @Nullable Date to) {
        int total = 0;
        int activos = 0;
        for (UserRecord u : usuarios) {
            if (!"asesor".equals(u.role) || !enRango(u.fechaRegistro, from, to)) continue;
            total++;
            if ("active".equals(u.status)) activos++;
        }
        return total > 0 ? (activos * 100.0) / total : null;
    }

    /** % de reservas vencidas propias de cada inmobiliaria (solo entre las que tienen fechaVisita parseable). */
    public Map<String, Double> tasaVencidasPorInmobiliariaEnRango(@Nullable Date from, @Nullable Date to) {
        Date ahora = new Date();
        Map<String, Integer> totales = new LinkedHashMap<>();
        Map<String, Integer> vencidas = new LinkedHashMap<>();
        for (ReservationRecord r : reservas) {
            if (!enRango(r.fechaCreacion, from, to) || r.fechaVisita == null
                    || r.inmobiliaria == null || r.inmobiliaria.trim().isEmpty()) continue;
            String nombre = r.inmobiliaria.trim();
            totales.merge(nombre, 1, Integer::sum);
            if (r.fechaVisita.before(ahora)) {
                vencidas.merge(nombre, 1, Integer::sum);
            }
        }
        Map<String, Double> resultado = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : totales.entrySet()) {
            int vencidasCount = vencidas.getOrDefault(entry.getKey(), 0);
            resultado.put(entry.getKey(), (vencidasCount * 100.0) / entry.getValue());
        }
        return resultado;
    }

    public List<CompanyShare> repartoPorInmobiliariaEnRango(@Nullable Date from, @Nullable Date to, int maxCompanias) {
        Map<String, Integer> conteos = new LinkedHashMap<>();
        int total = 0;
        for (ReservationRecord r : reservas) {
            if (!enRango(r.fechaCreacion, from, to)) continue;
            String nombre = (r.inmobiliaria != null && !r.inmobiliaria.trim().isEmpty())
                    ? r.inmobiliaria.trim() : SIN_ASIGNAR;
            conteos.merge(nombre, 1, Integer::sum);
            total++;
        }
        List<CompanyShare> resultado = new ArrayList<>();
        if (total == 0) return resultado;

        List<Map.Entry<String, Integer>> ordenados = new ArrayList<>(conteos.entrySet());
        ordenados.sort((a, b) -> b.getValue() - a.getValue());

        int mostrados = 0;
        int otrasCantidad = 0;
        for (Map.Entry<String, Integer> entry : ordenados) {
            if (mostrados < maxCompanias) {
                resultado.add(new CompanyShare(entry.getKey(), entry.getValue(), (entry.getValue() * 100.0) / total));
                mostrados++;
            } else {
                otrasCantidad += entry.getValue();
            }
        }
        if (otrasCantidad > 0) {
            resultado.add(new CompanyShare(OTRAS, otrasCantidad, (otrasCantidad * 100.0) / total));
        }
        return resultado;
    }

    /** Cuenta reservas por bucket de calendario (mes/trimestre/año), terminando en el bucket actual. */
    public float[] tendenciaPorBuckets(int cantidadBuckets, int mesesPorBucket) {
        float[] valores = new float[cantidadBuckets];
        for (int i = 0; i < cantidadBuckets; i++) {
            int offsetDesdeActual = cantidadBuckets - 1 - i;
            Date inicio = inicioDeBucket(-offsetDesdeActual, mesesPorBucket);
            Date fin = inicioDeBucket(-offsetDesdeActual + 1, mesesPorBucket);
            valores[i] = reservasEnRango(inicio, fin);
        }
        return valores;
    }

    public String[] etiquetasBuckets(int cantidadBuckets, int mesesPorBucket) {
        String[] etiquetas = new String[cantidadBuckets];
        String[] mesesAbrev = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        for (int i = 0; i < cantidadBuckets; i++) {
            int offsetDesdeActual = cantidadBuckets - 1 - i;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -offsetDesdeActual * mesesPorBucket);
            if (mesesPorBucket == 12) {
                etiquetas[i] = String.valueOf(cal.get(Calendar.YEAR));
            } else if (mesesPorBucket == 3) {
                int trimestre = (cal.get(Calendar.MONTH) / 3) + 1;
                etiquetas[i] = "T" + trimestre;
            } else {
                etiquetas[i] = mesesAbrev[cal.get(Calendar.MONTH)];
            }
        }
        return etiquetas;
    }

    private Date inicioDeBucket(int offsetBuckets, int mesesPorBucket) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, offsetBuckets * mesesPorBucket);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private boolean enRango(@Nullable Date fecha, @Nullable Date from, @Nullable Date to) {
        if (fecha == null) return false;
        if (from != null && fecha.before(from)) return false;
        if (to != null && !fecha.before(to)) return false;
        return true;
    }
}
