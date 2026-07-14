# BUG — AgendaCitaActivity: Dos toasts consecutivos para todos los proyectos

**Fecha:** 2026-07-13  
**Estado:** DIAGNOSTICADO — pendiente aprobación de fix  
**Severidad:** Alta (bloquea el flujo de reserva de citas para todos los proyectos)

---

## 1. Síntoma reportado

Al pulsar el botón **"Confirmar cita"** en `AgendaCitaActivity`, aparecen dos toasts sucesivos:

1. `"Datos del proyecto no disponibles. Intenta de nuevo."`
2. `"Datos del asesor no disponibles. Intenta desde el detalle del proyecto."`

Ocurre para **todos los proyectos**, no solo uno, y la activity muestra datos estáticos de diseño ("Catalina Ventor / Juan Perez") porque ningún extra dinámico llegó.

---

## 2. Contrato esperado por `AgendaCitaActivity`

`AgendaCitaActivity.java` lee estos extras en `onCreate()` (líneas 106-111):

| Extra key (constante)      | Tipo    | Requerido en modo normal | Requerido en modo reagendar |
|----------------------------|---------|--------------------------|------------------------------|
| `EXTRA_PROYECTO`           | String  | No (solo display)        | No (solo display)            |
| `EXTRA_PROYECTO_ID`        | String  | **Sí** (slot listener + transacción) | **Sí** |
| `EXTRA_UID_ASESOR`         | String  | **Sí** (reservarCita)    | No (no se llama reservarCita) |
| `EXTRA_MODO`               | String  | No                        | Sí (valor `"reagendar"`)    |
| `EXTRA_CITA_ID`            | String  | No                        | Sí                           |
| `EXTRA_SLOT_ID_ANTERIOR`   | String  | No                        | Sí                           |

Validaciones en `confirmarCita()` (líneas 259–285):

```java
// Validación 1 — línea 259
if (proyectoId == null || proyectoId.isEmpty()) {
    Toast.makeText(this, "Datos del proyecto no disponibles. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
    return;   // <-- return temprano, NO llega a la siguiente validación
}

// Validación 2 — línea 281 (solo en modo NO-reagendar)
if (uidAsesor == null || uidAsesor.isEmpty()) {
    Toast.makeText(this, "Datos del asesor no disponibles. Intenta desde el detalle del proyecto.", Toast.LENGTH_LONG).show();
    return;
}
```

**Importante:** Las dos validaciones son secuenciales con `return` temprano. Desde un **único** tap solo puede aparecer **uno** de los dos toasts. Los dos toasts vistos en el bug son producto de **dos taps distintos** en momentos distintos.

---

## 3. Contrato entregado por cada caller

### 3.1 `ProjectDetailActivity.configurarNavegacion()` — líneas 134–141 (listener ESTÁTICO)

```java
View btnAgendar = findViewById(R.id.btnAgendarCita);
if (btnAgendar != null) {
    btnAgendar.setOnClickListener(v -> {
        Intent intent = new Intent(this, AgendaCitaActivity.class);
        intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO, nombreProyecto);
        // proyectoId y uidAsesor se sobreescriben en pintarDatosProyecto() cuando cargan
        startActivity(intent);
    });
}
```

**Extras enviados:** solo `EXTRA_PROYECTO`.  
**Extras faltantes:** `EXTRA_PROYECTO_ID`, `EXTRA_UID_ASESOR`.

Este listener es registrado en `onCreate()`, **antes** de que Firestore cargue el proyecto. Si el usuario toca el botón en este estado (ventana de tiempo hasta que llegue el callback de Firestore), AgendaCitaActivity recibe `proyectoId = null` → **primer toast**.

### 3.2 `ProjectDetailActivity.pintarDatosProyecto()` — líneas 638–649 (listener DINÁMICO)

```java
List<String> asesores = proyectoActual.getUidAsesores();
String uid1er = (asesores != null && !asesores.isEmpty()) ? asesores.get(0) : null;
String pid    = proyectoActual.getId();
btnAgendar.setOnClickListener(v -> {
    Intent intent = new Intent(this, AgendaCitaActivity.class);
    intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO, proyectoActual.getNombre());
    if (pid    != null) intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO_ID, pid);
    if (uid1er != null) intent.putExtra(AgendaCitaActivity.EXTRA_UID_ASESOR,  uid1er);
    startActivity(intent);
});
```

**Extras enviados:**
- `EXTRA_PROYECTO` → siempre ✓
- `EXTRA_PROYECTO_ID` → solo si `proyectoActual.getId() != null` (en la práctica, siempre presente en documentos bien formados) ✓
- `EXTRA_UID_ASESOR` → **solo si** `uid1er != null`, es decir, solo si `getUidAsesores()` devuelve una lista no vacía.

Si `proyectoActual.getUidAsesores()` es `null` o vacío (caso frecuente en proyectos seed), `EXTRA_UID_ASESOR` **se omite silenciosamente** → AgendaCitaActivity recibe `uidAsesor = null` → **segundo toast**.

### 3.3 `ProjectDetailActivity.configurarNavegacion()` — `btnSeparar` (líneas 164–172)

```java
findViewById(R.id.btnSeparar).setOnClickListener(v -> {
    Intent intentAgenda = new Intent(this, AgendaCitaActivity.class);
    NotificationHelper.lanzarNotificacion(this, "Separación Pendiente", ..., intentAgenda);
    startActivity(new Intent(this, PaymentMethodActivity.class));
});
```

`intentAgenda` se crea sin extras y se usa **únicamente como `PendingIntent`** para la notificación; `startActivity` va a `PaymentMethodActivity`, no a `AgendaCitaActivity`. Este caller **no** contribuye al bug.

### 3.4 `MisCitasActivity.accionReagendar()` — líneas 198–205

```java
intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO,         cita.getProjectName());
intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO_ID,      cita.getProyectoId());
intent.putExtra(AgendaCitaActivity.EXTRA_UID_ASESOR,       cita.getUidAsesorCita());
intent.putExtra(AgendaCitaActivity.EXTRA_MODO,             "reagendar");
intent.putExtra(AgendaCitaActivity.EXTRA_CITA_ID,          cita.getFirestoreId());
intent.putExtra(AgendaCitaActivity.EXTRA_SLOT_ID_ANTERIOR, cita.getSlotId());
```

Contrato completo. No contribuye al bug.

---

## 4. Estado real del modelo `Proyecto` en Firestore

`Proyecto.java` define (líneas 28-30):

```java
private List<String> asesores;      // nombres (display)
private List<String> uidAsesores;   // UIDs de Firestore
```

Los proyectos actuales **fueron creados manualmente / por seed antes del refactor de citas**. Es altamente probable que:

- El campo `uidAsesores` no exista en el documento de Firestore.
- Firestore deserializa un campo ausente como `null` en el modelo Java.
- `proyectoActual.getUidAsesores()` retorna `null` → condición `asesores != null && !asesores.isEmpty()` falla → `uid1er = null`.

---

## 5. Diagnóstico: causa raíz

El bug requiere exactamente **dos taps** del usuario en `btnAgendar`, separados por el tiempo de carga de Firestore:

### Causa Raíz 1 — Ventana de carrera (Race condition de timing)

```
Evento              │ Estado en AgendaCitaActivity
────────────────────┼────────────────────────────────────────────────────
onCreate()          │ btnAgendar registra listener ESTÁTICO (solo EXTRA_PROYECTO)
TAP 1 (usuario)     │ Firestore aún no cargó → startActivity con proyectoId=null
                    │ AgendaCitaActivity: confirmarCita() → proyectoId==null → TOAST 1
Firestore callback  │ pintarDatosProyecto() → listener DINÁMICO registrado
                    │ pero uid1er=null (campo ausente en Firestore)
TAP 2 (usuario)     │ startActivity con proyectoId=pid, EXTRA_UID_ASESOR ausente
                    │ AgendaCitaActivity: confirmarCita() → proyectoId OK
                    │ → uidAsesor==null → TOAST 2
```

Esto explica por qué ocurre para **todos** los proyectos (ninguno tiene `uidAsesores` en Firestore) y por qué son dos toasts sucesivos (dos taps distintos, no uno solo).

### Causa Raíz 2 — Campo `uidAsesores` ausente en Firestore

Incluso si el timing fuera perfecto (solo el listener dinámico activo), el campo `uidAsesores` no existe en los documentos actuales. La omisión silenciosa en `if (uid1er != null) intent.putExtra(...)` esconde el error en lugar de reportarlo.

---

## 6. Propuesta de fix

### Alternativa A — Fix Mínimo: tolerante con fallback en-memoria

**Filosofía:** `AgendaCitaActivity` carga el `uidAsesor` desde Firestore si no lo recibió, usando el `proyectoId` disponible.

**Cambios:**

1. **`AgendaCitaActivity.java`** — en `confirmarCita()`, al detectar `uidAsesor == null && proyectoId != null`:
   - En lugar de mostrar toast, lanzar una consulta `proyectos/{proyectoId}` → obtener `uidAsesores.get(0)` → continuar con la reserva.
   - Esto añade latencia pero no requiere cambios en callers ni en Firestore.

2. **`ProjectDetailActivity.configurarNavegacion()`** — cambio cosmético opcional: deshabilitar `btnAgendar` hasta que `pintarDatosProyecto()` haya corrido (evita el tap 1 inútil).

**Pros:** No toca datos en Firestore. No requiere migración.  
**Contras:** Añade lógica de carga dentro de `AgendaCitaActivity`; viola principio de que la activity recibe datos ya preparados; latencia adicional al confirmar.

---

### Alternativa B — Fix Correcto: garantizar el contrato en callers y en Firestore

**Filosofía:** El emisor es responsable de enviar datos completos; el receptor no debe compensar datos faltantes.

**Cambios en código (2 archivos, ~6 líneas):**

**1. `ProjectDetailActivity.configurarNavegacion()`** — deshabilitar `btnAgendar` hasta que carguen los datos:

```java
// ANTES (listener estático funcional pero sin datos):
View btnAgendar = findViewById(R.id.btnAgendarCita);
if (btnAgendar != null) {
    btnAgendar.setOnClickListener(v -> {
        Intent intent = new Intent(this, AgendaCitaActivity.class);
        intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO, nombreProyecto);
        startActivity(intent);
    });
}

// DESPUÉS (deshabilitar hasta que pintarDatosProyecto() registre el listener correcto):
View btnAgendar = findViewById(R.id.btnAgendarCita);
if (btnAgendar != null) {
    btnAgendar.setEnabled(false);
    btnAgendar.setAlpha(0.5f);
}
```

**2. `ProjectDetailActivity.pintarDatosProyecto()`** — mostrar error explícito si no hay asesor, habilitar botón solo si hay datos completos:

```java
// ANTES (omisión silenciosa):
List<String> asesores = proyectoActual.getUidAsesores();
String uid1er = (asesores != null && !asesores.isEmpty()) ? asesores.get(0) : null;
String pid    = proyectoActual.getId();
btnAgendar.setOnClickListener(v -> {
    Intent intent = new Intent(this, AgendaCitaActivity.class);
    intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO, proyectoActual.getNombre());
    if (pid    != null) intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO_ID, pid);
    if (uid1er != null) intent.putExtra(AgendaCitaActivity.EXTRA_UID_ASESOR,  uid1er);
    startActivity(intent);
});

// DESPUÉS (falla explícita si no hay asesor; botón se habilita siempre):
List<String> asesores = proyectoActual.getUidAsesores();
String uid1er = (asesores != null && !asesores.isEmpty()) ? asesores.get(0) : null;
String pid    = proyectoActual.getId();
btnAgendar.setEnabled(true);
btnAgendar.setAlpha(1f);
if (uid1er == null) {
    btnAgendar.setEnabled(false);
    btnAgendar.setAlpha(0.4f);
    Toast.makeText(this, "Este proyecto no tiene asesor asignado", Toast.LENGTH_LONG).show();
} else {
    btnAgendar.setOnClickListener(v -> {
        Intent intent = new Intent(this, AgendaCitaActivity.class);
        intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO,    proyectoActual.getNombre());
        intent.putExtra(AgendaCitaActivity.EXTRA_PROYECTO_ID, pid);
        intent.putExtra(AgendaCitaActivity.EXTRA_UID_ASESOR,  uid1er);
        startActivity(intent);
    });
}
```

**Cambio en Firestore (migración manual — tarea de coordinación):**

Agregar `uidAsesores: ["<uid_del_asesor>"]` a cada documento en la colección `proyectos` que actualmente no lo tenga. Esta es la causa raíz verdadera; sin este campo el botón quedará deshabilitado en la UI.

**Acción de coordinación:** Documentar en `PENDING_COORDINATION.md` como **PC-05**.

**Pros:** Contrato claro en todos los callers. Error visible para el usuario (botón deshabilitado) en lugar de error silencioso en tiempo de ejecución. `AgendaCitaActivity` permanece simple. Elimina ambas causas raíz.  
**Contras:** Requiere migración manual de datos en Firestore. El botón queda deshabilitado para proyectos sin asesor hasta que se migre el dato.

---

## 7. Recomendación

**Alternativa B (Fix Correcto)**, por las siguientes razones:

- Los errores de contrato deben detectarse en el emisor, no remendarse en el receptor.
- La Alternativa A oculta un problema de datos con lógica de recuperación, lo que hace más difícil detectar proyectos sin asesor asignado.
- El cambio de código es menor (~6 líneas en 1 archivo).
- La migración de datos en Firestore es necesaria de todas formas para que el flujo funcione correctamente.

---

**PAUSA — No implementar hasta aprobación de alternativa.**
