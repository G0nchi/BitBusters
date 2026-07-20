# CITAS_DATA_MODEL.md — Modelo de datos para el flujo de citas

**Autor**: Pedro/Branch-1 — auditoría Android módulo Cliente  
**Fecha**: 2026-07-13  
**Estado**: APROBADO — implementado en Phase 2  
**Relacionado**: `PENDING_COORDINATION.md § PC-02`, `PENDING_COORDINATION.md § PC-04`

---

## 1. Esquema de colecciones aprobado

### 1.1 `citas/{citaId}`

`citaId` = ID auto-generado por Firestore (`.document()` sin args), pre-generado antes de la transacción para que el slot pueda referenciarlo.

```
citas/
  {citaId}/
    uidCliente:     String    — UID Firebase Auth del cliente
    uidAsesor:      String    — UID Firebase Auth del asesor asignado
    proyectoId:     String    — document ID en colección "proyectos"
    proyectoNombre: String    — nombre del proyecto (denorm. para display)
    nombreCliente:  String    — nombre del cliente (denorm. para módulo Asesor)
    slotId:         String    — "{proyectoId}_{yyyyMMdd}_{HHmm}" (zona America/Lima)
    fechaTimestamp: Timestamp — fecha+hora de la cita (calculado en cliente, zona Lima)
    estado:         String    — "pendiente"|"confirmada"|"cancelada"|"completada"|"valorada"
    creadoEn:       Timestamp — FieldValue.serverTimestamp()
    actualizadoEn:  Timestamp — FieldValue.serverTimestamp()
    historialReagendamientos: Array<Map> — historial de reagendamientos
      [{
        slotAnterior:          String    — slotId liberado
        fechaTimestampAnterior: Timestamp — fecha anterior (Date en Java)
        reagendadoEn:           Date      — timestamp cliente (serverTimestamp no soportado en arrays)
      }]
```

**Decisiones aprobadas:**
- **Sin `fechaDisplay` ni `horaDisplay`** en Firestore. El display se calcula client-side con `SimpleDateFormat(locale=es_PE, timezone=America/Lima)`.
- `fechaTimestamp` es calculado en el cliente con la fecha/hora seleccionada en zona Lima (no `serverTimestamp`).
- `historialReagendamientos` usa `FieldValue.arrayUnion()` — nunca se pierden entradas.

---

### 1.2 `slots/{slotId}`

`slotId` determinístico: **`{proyectoId}_{yyyyMMdd}_{HHmm}`** (zona `America/Lima`).

Ejemplos:
- `abc123_20260714_0900` → proyecto "abc123", 14 Jul 2026, 9:00 AM
- `abc123_20260714_1400` → mismo proyecto, 2:00 PM

```
slots/
  {slotId}/
    ocupado:        Boolean   — true si el slot está reservado
    citaId:         String    — document ID en citas/ (para auditoría)
    uidCliente:     String    — quién reservó (para reagendar/cancelar autorizado)
    proyectoId:     String    — para queries de disponibilidad por proyecto
    fechaTimestamp: Timestamp — fecha del slot (para queries de rango)
    creadoEn:       Timestamp — FieldValue.serverTimestamp()

    // Al cancelar o reagendar (ocupado vuelve a false):
    canceladoEn:    Timestamp — FieldValue.serverTimestamp() al cancelar/reagendar
    citaIdAnterior: String    — ID de la cita que liberó este slot (auditoría)
```

**Por qué ID determinístico en slots:**
- La transacción hace una sola lectura del slot exacto → falla atómicamente si `ocupado=true`.
- Dos clientes en el mismo slot: Firestore serializa transacciones, solo una gana. La segunda recibe `Code.ABORTED`.

---

## 2. Diagrama de la transacción de reserva

```
Cliente selecciona proyecto + fecha + hora
         │
         ▼
  slotId = "{proyectoId}_{yyyyMMdd}_{HHmm}" (zona Lima)
  fechaTimestamp = Calendar(Lima).set(year, month, day, hh, mm) → Date
  citaRef = db.collection("citas").document()  ← pre-genera ID sin escribir
         │
         ▼
┌─────────────────────────────────────────────────────────┐
│  db.runTransaction(tx -> {                              │
│                                                         │
│    1. READ: tx.get(slots/{slotId})                      │
│       └─ si existe Y ocupado == true:                   │
│             throw FirebaseFirestoreException(ABORTED)   │
│                                                         │
│    2. WRITE: tx.set(slots/{slotId}, {                   │
│         ocupado: true,                                  │
│         citaId: citaRef.getId(),                        │
│         uidCliente, proyectoId, fechaTimestamp,         │
│         creadoEn: serverTimestamp                       │
│       })                                                │
│                                                         │
│    3. WRITE: tx.set(citas/{citaId}, {                   │
│         uidCliente, uidAsesor,                          │
│         proyectoId, proyectoNombre, nombreCliente,      │
│         slotId, fechaTimestamp,                         │
│         estado: "pendiente",                            │
│         creadoEn: serverTimestamp,                      │
│         actualizadoEn: serverTimestamp,                 │
│         historialReagendamientos: []                    │
│       })                                                │
│                                                         │
│  }) → onSuccess: mostrar éxito → navegar a MisCitas     │
│     → onFailure(ABORTED): "Horario ya reservado"        │
│     → onFailure(otro): "No se pudo agendar. Reintenta"  │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Estrategia de cancelación (WriteBatch)

No requiere leer estado previo → `WriteBatch` es suficiente.

```java
WriteBatch batch = db.batch();

// Marcar la cita como cancelada (nunca se borra — auditoría)
batch.update(
    db.collection("citas").document(citaId),
    "estado", "cancelada",
    "actualizadoEn", FieldValue.serverTimestamp()
);

// Liberar el slot: ocupado=false + auditoría
batch.update(
    db.collection("slots").document(slotId),
    "ocupado",         false,
    "canceladoEn",     FieldValue.serverTimestamp(),
    "citaIdAnterior",  citaId
);

batch.commit();
```

**Decisión aprobada:** slot se mantiene con `ocupado=false` (no se borra). `canceladoEn` y `citaIdAnterior` quedan como auditoría. `uidCliente` original se preserva.

---

## 4. Estrategia de reagendamiento (runTransaction)

Requiere transaction para verificar nuevo slot antes de escribir.

```java
db.runTransaction(tx -> {

    // 1. Verificar nuevo slot disponible
    DocumentSnapshot nuevoSlot = tx.get(db.collection("slots").document(nuevoSlotId));
    if (nuevoSlot.exists() && Boolean.TRUE.equals(nuevoSlot.getBoolean("ocupado"))) {
        throw new FirebaseFirestoreException("Ocupado", Code.ABORTED);
    }

    // 2. Leer cita actual para historial
    DocumentSnapshot citaSnap = tx.get(db.collection("citas").document(citaId));
    Date fechaAnterior = citaSnap.getDate("fechaTimestamp");
    String slotAnterior = citaSnap.getString("slotId");
    String uidCliente = citaSnap.getString("uidCliente");
    String proyectoId = citaSnap.getString("proyectoId");

    Map<String, Object> histEntry = new HashMap<>();
    histEntry.put("slotAnterior", slotAnterior);
    histEntry.put("fechaTimestampAnterior", fechaAnterior);
    histEntry.put("reagendadoEn", new Date()); // cliente; serverTimestamp no válido en arrays

    // 3. Liberar slot anterior
    tx.update(slotAntRef, Map.of(
        "ocupado", false,
        "canceladoEn", FieldValue.serverTimestamp(),
        "citaIdAnterior", citaId
    ));

    // 4. Ocupar nuevo slot
    tx.set(nuevoSlotRef, Map.of(
        "ocupado", true,   "citaId", citaId,
        "uidCliente", uidCliente,   "proyectoId", proyectoId,
        "fechaTimestamp", nuevaFechaTimestamp,
        "creadoEn", FieldValue.serverTimestamp()
    ));

    // 5. Actualizar cita
    tx.update(citaRef, Map.of(
        "slotId", nuevoSlotId,
        "fechaTimestamp", nuevaFechaTimestamp,
        "actualizadoEn", FieldValue.serverTimestamp(),
        "historialReagendamientos", FieldValue.arrayUnion(histEntry)
    ));

    return null;
});
```

---

## 5. Lectura de disponibilidad para el calendario

```java
// CitaRepository.escucharSlotsOcupados()
// Requiere índice compuesto — ver PENDING_COORDINATION.md § PC-04
db.collection("slots")
    .whereEqualTo("proyectoId", proyectoId)
    .whereEqualTo("ocupado", true)
    .whereGreaterThanOrEqualTo("fechaTimestamp", inicioMes)
    .whereLessThan("fechaTimestamp", finMes)
    .addSnapshotListener((snap, e) -> {
        Set<String> slotsOcupados = new HashSet<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            slotsOcupados.add(doc.getId());
        }
        // actualizar UI
    });
```

---

## 6. Reglas de seguridad Firestore

Ver `firestore.rules` en raíz del repositorio.  
**No aplicar directamente** — ver `PENDING_COORDINATION.md § PC-04`.

---

## 7. Compatibilidad con módulo Asesor

El módulo Asesor tiene en `AsesorReportesActivity`:
```java
db.collection("citas").whereEqualTo("uidAsesor", uidAsesor).get()
```

El campo `uidAsesor` está en el esquema → **compatible sin cambios en el Asesor**.

Cuando el compañero Asesor migre `CitasAgendadasActivity` a Firestore:

| Campo que Asesor necesita | Campo en esquema  | Disponible |
|---------------------------|-------------------|------------|
| Nombre del cliente        | `nombreCliente`   | ✅         |
| Nombre del proyecto       | `proyectoNombre`  | ✅         |
| Fecha (display)           | `fechaTimestamp` → `SimpleDateFormat` | ✅ |
| Estado de la cita         | `estado`          | ✅         |
| ID de la cita             | `doc.getId()`     | ✅         |
| UID del cliente (chat)    | `uidCliente`      | ✅         |

Formato de fecha recomendado para el Asesor:
```java
SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy · h:mm a",
        new Locale("es", "PE"));
sdf.setTimeZone(TimeZone.getTimeZone("America/Lima"));
String display = sdf.format(cita.getFechaTimestamp());
```

---

## 8. Relación Proyecto → Asesor en el flujo de citas

### ¿Un asesor o múltiples?

El modelo `Proyecto` define `uidAsesores: List<String>` (array). Un proyecto **puede tener múltiples asesores** en Firestore. Sin embargo, el flujo de reserva de citas siempre asigna `uidAsesores[0]` (el primero del array).

| Campo en `proyectos/` | Tipo | Cardinalidad | Política actual |
|-----------------------|------|--------------|-----------------|
| `uidAsesores`         | Array\<String\> | 1..N | Se usa `get(0)` — el primer UID asignado |

### ¿Por qué `uidAsesores[0]`?

- Es suficiente para el flujo MVP: un proyecto con un asesor principal.
- Si en el futuro se implementa elección de asesor por el cliente, el campo ya soporta múltiples (sin migración de schema).
- El Admin ordena los UIDs según prioridad al registrar el proyecto.

### Consecuencia si el array está vacío o ausente

`ProjectDetailActivity.pintarDatosProyecto()` detecta este caso:
- Deshabilita el botón "Agendar cita" (alpha=0.4, enabled=false).
- Muestra: _"Este proyecto no tiene asesor asignado. Contacta al administrador."_
- Emite `Log.w("ProjectDetail", "Proyecto sin uidAsesores: {proyectoId}")`.

Para corregir proyectos existentes sin asesor: ver **PC-05**.

---

## 9. Horarios operativos válidos

```java
// Horarios disponibles en AgendaCitaActivity (ID layout → código 24h)
// hora900  → "0900"   hora1000 → "1000"
// hora1100 → "1100"   hora1200 → "1200"
// hora200  → "1400"   hora400  → "1600"
```

SlotIds para proyectoId="abc123", 14 Jul 2026:
- `abc123_20260714_0900`, `abc123_20260714_1000`, `abc123_20260714_1100`
- `abc123_20260714_1200`, `abc123_20260714_1400`, `abc123_20260714_1600`

---

## 10. Consideraciones de zona horaria

```java
// Calcular fechaTimestamp (zona Lima)
public static Date calcularFechaTimestamp(int year, int month0based, int day, String hora24) {
    int hh = Integer.parseInt(hora24.substring(0, 2));
    int mm = Integer.parseInt(hora24.substring(2, 4));
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"));
    cal.set(year, month0based, day, hh, mm, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
}

// Formatear para display
SimpleDateFormat sdfFecha = new SimpleDateFormat("d MMM yyyy", new Locale("es", "PE"));
SimpleDateFormat sdfHora  = new SimpleDateFormat("h:mm a", Locale.US);
sdfFecha.setTimeZone(TimeZone.getTimeZone("America/Lima"));
sdfHora.setTimeZone(TimeZone.getTimeZone("America/Lima"));
```
