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

## PC-04 — Índices compuestos Firestore requeridos por el módulo Cliente

Las queries de `CitaRepository` requieren índices compuestos. Sin ellos, Firestore falla con error que incluye un link directo para crearlos.

### Índices a crear

| Colección | Campos | Orden | Usado en |
|-----------|--------|-------|----------|
| `slots`   | `proyectoId` ASC + `ocupado` ASC + `fechaTimestamp` ASC | Todos ASC | `escucharSlotsOcupados()` |
| `citas`   | `uidCliente` ASC + `fechaTimestamp` DESC | — | `escucharCitasCliente()` (solo si se añade `orderBy`) |

### Acción requerida

En la consola Firebase → Firestore → Indexes → Composite → Agregar índice para cada fila de la tabla.
