# PENDING_COORDINATION.md

Cambios que requieren coordinación con compañeros, decisiones de modelo de datos compartido, o modificación de reglas Firestore.  
**No implementar sin alineación previa.**

---

## PC-01 — Colección duplicada: `usuarios` vs `users`

### Contexto

`AuthRepository.java` (dueño: módulo Auth base) escribe en **dos colecciones** al registrar un cliente:

| Colección | Campo rol | Campo activación | Uso actual |
|-----------|-----------|------------------|------------|
| `usuarios` | `"CLIENTE"` (mayúsculas) | `activo: true` | Solo escritura en registro |
| `users` | `"cliente"` (minúsculas) | `activo: true` | Lectura universal: Login, Chat, Proyectos |

La convención establecida del proyecto es `"users"` + `role: "cliente"` (lowercase). La colección `"usuarios"` es redundante y puede crecer indefinidamente sin que nadie la lea.

### Quiénes deben participar

- Dueño del módulo Auth base (quien mantiene `AuthRepository.java` y `LoginActivity.java`)
- Dueño del módulo Asesor (quien lee `users/{uid}` para verificar rol en `AsesorHomeActivity`)
- Pedro/Branch-1 (módulo Cliente — esta auditoría)

### Propuesta de migración en 3 pasos

#### Paso 1 — Auditoría previa (sin tocar código)

Ejecutar estas queries en la consola Firestore para cuantificar el impacto:

```javascript
// Query Firestore Console → Rules Playground o Firebase CLI

// Contar documentos en cada colección
// firebase firestore:get --all-collections (CLI)

// O desde la app, ejecutar una vez:
FirebaseFirestore.getInstance()
    .collection("usuarios")
    .get()
    .addOnSuccessListener(snap ->
        Log.d("AUDIT", "Documentos en 'usuarios': " + snap.size()));

FirebaseFirestore.getInstance()
    .collection("users")
    .get()
    .addOnSuccessListener(snap ->
        Log.d("AUDIT", "Documentos en 'users': " + snap.size()));
```

Resultado esperado: los mismos UIDs deberían aparecer en ambas colecciones tras el registro. Si hay divergencia, hay registros incompletos que necesitan limpieza manual.

#### Paso 2 — Consolidar lecturas en `users` (sin eliminar `usuarios` aún)

En `AuthRepository.java`, cambiar la escritura principal para que el documento en `users` sea el **único** con todos los campos (incluyendo `telefono`, `dni`). El documento en `usuarios` se puede mantener temporalmente como backup.

```java
// ANTES (dual-write con campos distintos):
batch.set(firestore.collection("usuarios").document(uid), usuario);         // rol = "CLIENTE"
batch.set(firestore.collection("users").document(uid), legacyUser, merge); // rol = "cliente"

// DESPUÉS (solo "users", campos completos):
Map<String, Object> userDoc = new HashMap<>();
userDoc.put("uid",      uid);
userDoc.put("nombre",   nombre);
userDoc.put("email",    email);
userDoc.put("telefono", telefono);
userDoc.put("dni",      dni);
userDoc.put("role",     "cliente");    // lowercase, convención global
userDoc.put("activo",   true);
userDoc.put("creadoEn", FieldValue.serverTimestamp());
batch.set(firestore.collection("users").document(uid), userDoc);
// Eliminar la línea de "usuarios"
```

#### Paso 3 — Deprecar `usuarios` (sprint siguiente)

Una vez confirmado que nadie lee de `"usuarios"`, crear un script de migración one-off (puede ser una Cloud Function) que:
1. Lea todos los documentos de `"usuarios"`.
2. Haga merge de los campos faltantes en `"users"`.
3. Borre `"usuarios"` o lo archive.

### Impacto si no se resuelve

- Cada nuevo cliente crea 2 documentos. La colección `"usuarios"` crece indefinidamente sin consumidores.
- Si alguien lee el rol desde `"usuarios"` encontrará `"CLIENTE"` (mayúsculas) y fallará las comparaciones con `"cliente"` (el resto del sistema usa lowercase).
- Los campos `telefono` y `dni` solo existen en `"usuarios"`, no en `"users"`. Si se necesitan (ej: perfil), habrá un read miss.

---

## PC-02 — Esquema de `citas` — coordinación con módulo Asesor

### Contexto

El módulo Cliente implementará `citas/{citaId}` y `slots/{slotId}` (ver `docs/CITAS_DATA_MODEL.md`).
El módulo Asesor actualmente usa datos estáticos pero tiene en `AsesorReportesActivity` una query preparada:

```java
db.collection("citas").whereEqualTo("uidAsesor", uidAsesor).get()
```

El esquema propuesto incluye `uidAsesor` como campo de primer nivel, lo que hace esta query compatible. Sin embargo, el módulo Asesor deberá:

1. **Migrar `CitasAgendadasActivity`** para leer de `citas` en vez de datos estáticos (tarea del compañero dueño del Asesor).
2. **Confirmar los campos de display** que necesitan leer: el modelo propuesto incluye `proyectoNombre`, `fechaDisplay`, `horaDisplay`, `estado`, `nombreCliente` — suficiente para renderizar las tarjetas de cita del Asesor sin queries adicionales.
3. **Confirmar la lógica de confirmación**: cuando el Asesor confirma una cita (desde `BaseCitasFragment.showConfirmDialog`), actualmente solo guarda en `AsesorStorage` local. Deberá hacer `db.collection("citas").document(citaId).update("estado", "confirmada")`.

### Acción requerida

Comunicar a compañero Asesor el esquema de `docs/CITAS_DATA_MODEL.md` para alineamiento antes de merge a `master`.

---

## PC-03 — Reglas de Firestore: `slots` añadido, `citas` sigue permisiva

La colección `slots` tiene reglas restrictivas en `firestore.rules` (raíz del repo).  
La colección `citas` sigue con `allow read, write: if request.auth != null` (permisiva).

### Antes de producción

Reemplazar la sección `citas` en `firestore.rules` con las reglas específicas del diseño aprobado:

```javascript
match /citas/{citaId} {
  allow create: if request.auth != null
    && request.resource.data.uidCliente == request.auth.uid
    && request.resource.data.estado == "pendiente";
  allow read: if request.auth != null
    && (resource.data.uidCliente == request.auth.uid
        || resource.data.uidAsesor == request.auth.uid);
  allow update: if request.auth != null
    && (
      (resource.data.uidCliente == request.auth.uid
       && request.resource.data.estado in ["cancelada", "pendiente"])
      || (resource.data.uidAsesor == request.auth.uid
          && request.resource.data.estado in ["confirmada","completada","valorada"])
    );
  allow delete: if false;
}
```

### Acción requerida

Responsable Firebase aplica los cambios en consola → Firestore → Rules → Publicar.

---

## PC-05 — Política de `uidAsesores` en proyectos: campo obligatorio desde el origen

### Contexto

El módulo Cliente usa `proyectos.uidAsesores` (array de UIDs) para:

1. Mostrar el botón "Agendar cita" habilitado en `ProjectDetailActivity`.
2. Pasar `uidAsesor = uidAsesores[0]` a `AgendaCitaActivity` → campo `uidAsesor` en `citas/{citaId}`.

Los proyectos creados antes del refactor de citas (seeds, creación manual en consola) no tienen este campo → el botón "Agendar cita" queda deshabilitado con el mensaje "Este proyecto no tiene asesor asignado."

### Definición del campo

| Campo | Tipo | Cardinalidad | Regla |
|-------|------|--------------|-------|
| `uidAsesores` | `Array<String>` | 1..N UIDs de Firebase Auth | **Obligatorio en todo proyecto nuevo** |

- Es un **array** para soportar proyectos con múltiples asesores en el futuro.
- La política actual usa `uidAsesores[0]` (primer asesor del array) para asignar citas.
- El Admin debe elegir el asesor al crear o editar un proyecto desde `AdminCrearProyectoActivity` / `AdminEditarProyectoActivity`.

### Acción para proyectos existentes (migración one-shot)

Usar `AdminSeedActivity` (herramienta de desarrollo incluida en esta rama):

```
adb shell am start -n com.example.bitbusters/.activities.admin.AdminSeedActivity
```

La activity:
1. Lee usuarios con `role = "asesor"` de la colección `users`.
2. Si no hay ninguno, crea `asesor_demo_001` automáticamente.
3. Asigna en round-robin un asesor a cada proyecto sin `uidAsesores`.
4. Escribe con `WriteBatch`. Reporta en Logcat y en pantalla.

### Acción para proyectos nuevos

En `AdminCrearProyectoActivity` y `AdminEditarProyectoActivity` (módulo Admin), verificar que el formulario de creación/edición de proyectos incluya la selección de asesor y persista `uidAsesores` como array en Firestore. **Esta tarea corresponde al dueño del módulo Admin.**

---

## PC-04 — Índices compuestos Firestore requeridos por el módulo Cliente

Las queries de `CitaRepository` requieren índices compuestos. Sin ellos, Firestore falla con error que incluye un link directo para crearlos.

### Índices a crear

| Colección | Campos | Orden | Usado en |
|-----------|--------|-------|----------|
| `slots`   | `proyectoId` ASC + `ocupado` ASC + `fechaTimestamp` ASC | Todos ASC | `escucharSlotsOcupados()` |
| `citas`   | `uidCliente` ASC + `fechaTimestamp` DESC | — | `escucharCitasCliente()` (solo si se añade `orderBy`) |

### Acción requerida

En la consola Firebase → Firestore → Indexes → Composite → Agregar índice para cada fila de la tabla.

---

## PC-06 — `valoradaCliente`: booleano independiente de `estado`

### Contexto

`AddCommentActivity` (pantalla donde el cliente valora una visita) originalmente marcaba `citas/{citaId}.estado = "valorada"` al guardar la reseña. Esto se corrigió porque **`PC-03` ya reserva el valor `"valorada"` de `estado` para un uso distinto**: las reglas propuestas en `firestore.rules` (ver sección PC-03 arriba) permiten que el **asesor** transicione `estado` a `"confirmada" | "completada" | "valorada"` — es decir, `estado="valorada"` está pensado como parte del ciclo de vida que controla el asesor, no como "el cliente ya escribió su reseña".

Mezclar ambos significados habría acoplado el flujo de reseña del cliente con el ciclo de vida de la cita que gestiona el módulo Asesor (confirmado en auditoría: hoy el módulo Asesor no lee `estado` de Firestore en ningún lado, pero `ValorarVisitaActivity` ya existe del lado asesor y podría conectarse a futuro con un filtro `estado == "completada"`, que dejaría de matchear si el cliente sobrescribe `estado` a `"valorada"` primero).

### Solución aplicada

| Campo | Tipo | Dueño de la escritura | Significado |
|-------|------|------------------------|-------------|
| `estado` | `String` | Módulo Asesor (ciclo de vida de la cita) | `pendiente` → `confirmada` → `completada` → (`valorada`, futuro, la escribe el asesor) → `cancelada` |
| `valoradaCliente` | `Boolean` | Módulo Cliente (`AddCommentActivity`) | `true` si el cliente ya dejó su valoración de la visita. No participa del ciclo de vida de `estado`. |

Citas antiguas sin el campo `valoradaCliente` se tratan como `false` (no valorada aún). También se mantiene, por compatibilidad, un fallback: si una cita quedó con `estado == "valorada"` por el comportamiento anterior (ya corregido), se interpreta como valorada por el cliente aunque `valoradaCliente` no esté seteado — ver `MisCitasActivity.mapearCita()`.

### Quiénes deben participar

- Dueño del módulo Asesor: al implementar la transición real `estado → "valorada"` (o el nombre que se decida), confirmar que no colisiona con `valoradaCliente` y que ambos campos pueden coexistir en el mismo documento sin conflicto semántico.

### Acción requerida

Ninguna inmediata — dejar este documento como referencia para cuando se conecte el flujo real de `ValorarVisitaActivity` del asesor a Firestore.
