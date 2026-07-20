package com.example.bitbusters.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ClienteSeparacionRepository {

    private static final String COLECCION = "separaciones";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static class SeparacionPendienteRequest {
        public final ClienteInfo clienteInfo;
        public final ProyectoInfo proyectoInfo;
        public final PagoInfo pagoInfo;

        public SeparacionPendienteRequest(ClienteInfo clienteInfo,
                                          ProyectoInfo proyectoInfo,
                                          PagoInfo pagoInfo) {
            this.clienteInfo = clienteInfo;
            this.proyectoInfo = proyectoInfo;
            this.pagoInfo = pagoInfo;
        }
    }

    public static class ClienteInfo {
        public final String uidCliente;
        public final String nombreCliente;

        public ClienteInfo(String uidCliente, String nombreCliente) {
            this.uidCliente = uidCliente;
            this.nombreCliente = nombreCliente;
        }
    }

    public static class ProyectoInfo {
        public final String uidAsesor;
        public final String proyectoId;
        public final String proyectoNombre;
        public final String inmobiliariaId;

        public ProyectoInfo(String uidAsesor, String proyectoId, String proyectoNombre, String inmobiliariaId) {
            this.uidAsesor = uidAsesor;
            this.proyectoId = proyectoId;
            this.proyectoNombre = proyectoNombre;
            this.inmobiliariaId = inmobiliariaId;
        }
    }

    public static class PagoInfo {
        public final double monto;
        public final String metodoPago;
        public final String cardLast4;

        public PagoInfo(double monto, String metodoPago, String cardLast4) {
            this.monto = monto;
            this.metodoPago = metodoPago;
            this.cardLast4 = cardLast4;
        }
    }

    public Task<DocumentReference> crearSeparacionPendiente(SeparacionPendienteRequest request) {
        Instant venceEn = Instant.now().plus(Duration.ofMinutes(10));

        Map<String, Object> data = new HashMap<>();
        putClienteData(data, request.clienteInfo);
        putProyectoData(data, request.proyectoInfo);
        putPagoData(data, request.pagoInfo);
        data.put("estado", "pago_pendiente");
        data.put("pagoVenceEn", venceEn);
        data.put("cardLast4", request.pagoInfo != null && request.pagoInfo.cardLast4 != null
            ? request.pagoInfo.cardLast4 : "");
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        return db.collection(COLECCION).add(data);
    }

    public Task<Void> registrarPagoDeSeparacionAprobada(String separacionId,
                                                         SeparacionPendienteRequest request) {
        Map<String, Object> data = new HashMap<>();
        putClienteData(data, request.clienteInfo);
        putProyectoData(data, request.proyectoInfo);
        putPagoData(data, request.pagoInfo);
        data.put("estado", "pago_registrado");
        data.put("pagoRegistradoEn", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());
        data.put("origen", "notificacion_separacion_aprobada");

        return db.collection(COLECCION)
                .document(separacionId)
                .set(data, SetOptions.merge());
    }

    private void putClienteData(Map<String, Object> data, ClienteInfo clienteInfo) {
        data.put("uidCliente", clienteInfo != null ? clienteInfo.uidCliente : "");
        data.put("nombreCliente", clienteInfo != null && clienteInfo.nombreCliente != null
            ? clienteInfo.nombreCliente : "");
    }

    private void putProyectoData(Map<String, Object> data, ProyectoInfo proyectoInfo) {
        data.put("uidAsesor", proyectoInfo != null && proyectoInfo.uidAsesor != null
            ? proyectoInfo.uidAsesor : "");
        data.put("proyectoId", proyectoInfo != null && proyectoInfo.proyectoId != null
            ? proyectoInfo.proyectoId : "");
        data.put("proyectoNombre", proyectoInfo != null && proyectoInfo.proyectoNombre != null
            ? proyectoInfo.proyectoNombre : "");
        data.put("inmobiliariaId", proyectoInfo != null && proyectoInfo.inmobiliariaId != null
            ? proyectoInfo.inmobiliariaId : "");
    }

    private void putPagoData(Map<String, Object> data, PagoInfo pagoInfo) {
        data.put("monto", pagoInfo != null ? pagoInfo.monto : 0d);
        data.put("metodoPago", pagoInfo != null && pagoInfo.metodoPago != null
            ? pagoInfo.metodoPago : "tarjeta");
        data.put("cardLast4", pagoInfo != null && pagoInfo.cardLast4 != null
            ? pagoInfo.cardLast4 : "");
    }
}
