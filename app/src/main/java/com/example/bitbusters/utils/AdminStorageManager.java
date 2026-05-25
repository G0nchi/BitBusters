package com.example.bitbusters.utils;

import android.content.Context;
import android.util.Log;

import com.example.bitbusters.models.AdminActividad;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Gestor de almacenamiento interno (Internal Storage) para el Administrador (Lab 5).
 *
 * Guarda y lee el resumen de actividad del admin en el archivo "admin_actividad.json"
 * usando Context.MODE_PRIVATE (solo accesible por esta app, sin permisos externos).
 *
 * Usa Gson para serializar/deserializar el modelo AdminActividad a/desde JSON.
 * La ubicación del archivo es: /data/data/com.example.bitbusters/files/admin_actividad.json
 */
public class AdminStorageManager {

    // Nombre del archivo en Internal Storage (sin ruta, usa el directorio de files de la app)
    private static final String NOMBRE_ARCHIVO = "admin_actividad.json";

    // Etiqueta para mensajes en Logcat
    private static final String TAG = "AdminStorageManager";

    // ── Constantes de campos para actualizarContador() ──────────────────────

    /** Campo: incrementa proyectos_registrados en 1. */
    public static final String CAMPO_PROYECTOS_REGISTRADOS   = "proyectos_registrados";

    /** Campo: incrementa separaciones_pendientes en 1. */
    public static final String CAMPO_SEPARACIONES_PENDIENTES = "separaciones_pendientes";

    /**
     * Campo: decrementa separaciones_pendientes en 1 e incrementa separaciones_aprobadas en 1.
     * Refleja el flujo real: una separación pasa de "pendiente" a "aprobada".
     */
    public static final String CAMPO_SEPARACIONES_APROBADAS  = "separaciones_aprobadas";

    // Gson con pretty-printing para que el JSON sea legible al inspeccionar el archivo
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ── Operaciones CRUD ────────────────────────────────────────────────────

    /**
     * Escribe el objeto AdminActividad como JSON en Internal Storage (MODE_PRIVATE).
     * Crea el archivo si no existe; sobreescribe si ya existe.
     *
     * @param context   Contexto de la Activity o Application.
     * @param actividad Objeto con los datos de actividad a persistir.
     */
    public static void guardarActividad(Context context, AdminActividad actividad) {
        try {
            // Serializar el objeto a JSON con Gson
            String json = gson.toJson(actividad);

            // Abrir FileOutputStream en modo privado y escribir con OutputStreamWriter
            FileOutputStream fos = context.openFileOutput(NOMBRE_ARCHIVO, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos);
            writer.write(json);
            writer.flush();
            writer.close();
            fos.close();

            Log.d(TAG, "Actividad guardada correctamente: " + json);
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar actividad en Internal Storage: " + e.getMessage());
        }
    }

    /**
     * Lee el archivo admin_actividad.json y retorna el objeto AdminActividad deserializado.
     * Si el archivo aún no existe o hay un error de lectura, retorna un objeto por defecto
     * con todos los contadores en 0 y la fecha/hora actual.
     *
     * @param context Contexto de la Activity o Application.
     * @return AdminActividad con los datos leídos del JSON, o valores por defecto si falla.
     */
    public static AdminActividad leerActividad(Context context) {
        try {
            // Abrir el archivo de Internal Storage para lectura
            FileInputStream fis = context.openFileInput(NOMBRE_ARCHIVO);
            InputStreamReader reader = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(reader);

            // Leer línea por línea y concatenar el contenido completo
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = bufferedReader.readLine()) != null) {
                sb.append(linea);
            }
            bufferedReader.close();
            reader.close();
            fis.close();

            // Deserializar el JSON a AdminActividad con Gson
            AdminActividad actividad = gson.fromJson(sb.toString(), AdminActividad.class);
            Log.d(TAG, "Actividad leída: " + sb.toString());

            // Si Gson retorna null (JSON vacío o malformado), usar valores por defecto
            return actividad != null ? actividad : actividadPorDefecto();

        } catch (Exception e) {
            // El archivo no existe en el primer uso; retornar datos iniciales
            Log.d(TAG, "Archivo no encontrado, retornando actividad por defecto: "
                    + e.getMessage());
            return actividadPorDefecto();
        }
    }

    /**
     * Incrementa en 1 el contador especificado y actualiza el JSON en Internal Storage.
     * También actualiza el campo "ultimo_reporte" con la fecha y hora actuales.
     *
     * Para CAMPO_SEPARACIONES_APROBADAS: decrementa pendientes y aumenta aprobadas
     * reflejando el flujo real de aprobación.
     *
     * @param context Contexto de la Activity o Application.
     * @param campo   Nombre del campo a modificar. Usar las constantes CAMPO_* de esta clase:
     *                {@link #CAMPO_PROYECTOS_REGISTRADOS},
     *                {@link #CAMPO_SEPARACIONES_PENDIENTES},
     *                {@link #CAMPO_SEPARACIONES_APROBADAS}.
     */
    public static void actualizarContador(Context context, String campo) {
        // Leer el estado actual del archivo
        AdminActividad actividad = leerActividad(context);

        // Incrementar (o ajustar) el campo indicado
        switch (campo) {

            case CAMPO_PROYECTOS_REGISTRADOS:
                // Un nuevo proyecto fue registrado
                actividad.setProyectosRegistrados(actividad.getProyectosRegistrados() + 1);
                break;

            case CAMPO_SEPARACIONES_PENDIENTES:
                // Una nueva separación llegó y está pendiente de aprobación
                actividad.setSeparacionesPendientes(actividad.getSeparacionesPendientes() + 1);
                break;

            case CAMPO_SEPARACIONES_APROBADAS:
                // Una separación fue aprobada: pasa de pendiente → aprobada
                int pendientes = actividad.getSeparacionesPendientes();
                actividad.setSeparacionesPendientes(Math.max(0, pendientes - 1));
                actividad.setSeparacionesAprobadas(actividad.getSeparacionesAprobadas() + 1);
                break;

            default:
                Log.w(TAG, "Campo desconocido en actualizarContador: " + campo);
                return; // No guardar si el campo no es válido
        }

        // Actualizar la fecha y hora del último reporte antes de guardar
        String fechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());
        actividad.setUltimoReporte(fechaHora);

        // Persistir el objeto actualizado en Internal Storage
        guardarActividad(context, actividad);
    }

    // ── Métodos auxiliares ──────────────────────────────────────────────────

    /**
     * Crea y retorna un AdminActividad con valores iniciales (contadores en 0).
     * Se usa cuando el archivo no existe todavía (primer uso de la app).
     */
    private static AdminActividad actividadPorDefecto() {
        String fechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());
        return new AdminActividad(0, 0, 0, fechaHora);
    }
}
