# AUDIT_REPORT — Módulo Cliente BitBusters

**Auditor**: Staff Android Engineer  
**Rama**: `Pedro/Branch-1`  
**Fecha**: 2026-07-13  
**Fase actual**: Fase 0 — Reconocimiento completo

---

## 1. Árbol de paquetes del módulo Cliente

```
com.example.bitbusters/
├── activities/
│   ├── access/                         ← Auth base (compañeros)
│   │   ├── LoginActivity.java
│   │   ├── RegisterAccountActivity.java
│   │   ├── RegisterOtpActivity.java
│   │   ├── RegisterPasswordActivity.java
│   │   ├── ForgotPasswordActivity.java
│   │   ├── ForgotPasswordCodeActivity.java
│   │   ├── ResetPasswordActivity.java
│   │   ├── SplashActivity.java
│   │   ├── OnboardingTourActivity.java
│   │   ├── MainActivity.java
│   │   └── ProjectTypeSelectionActivity.java
│   ├── cliente/                         ← MÓDULO CLIENTE (ownership Pedro)
│   │   ├── HomeActivity.java
│   │   ├── SearchActivity.java
│   │   ├── ProjectDetailActivity.java
│   │   ├── AgendaCitaActivity.java
│   │   ├── MisCitasActivity.java
│   │   ├── ChatDetailActivity.java
│   │   ├── MessagesActivity.java
│   │   ├── NotificationsActivity.java
│   │   ├── ProfileActivity.java
│   │   ├── ReviewsActivity.java
│   │   ├── AddCommentActivity.java
│   │   ├── AgregarComentarioDialog.java
│   │   ├── ViewOnMapActivity.java
│   │   └── PaymentMethodActivity.java
│   ├── asesor/                          ← módulo Asesor (compañeros)
│   ├── admin/                           ← módulo Admin (compañeros)
│   └── common/
│       └── EscanearQRActivity.java      ← compartido
├── adapters/
│   ├── ClientAppointmentsAdapter.java   ← Cliente
│   ├── ClientChatMessageAdapter.java    ← Cliente
│   ├── ClientReviewsAdapter.java        ← Cliente
│   ├── MensajesAdapter.java             ← Chat cliente
│   ├── ClienteChatsAdapter.java         ← Chat cliente
│   ├── ComentariosAdapter.java          ← Cliente
│   ├── NotificationsAdapter.java        ← Cliente
│   ├── ProyectoAdapter.java             ← Cliente (Home)
│   └── SearchResultAdapter.java         ← Cliente (Search)
├── models/
│   ├── ClientAppointment.java           ← MOCK (sin Firestore)
│   ├── ClientMessage.java               ← MOCK
│   ├── ClientReview.java                ← MOCK
│   ├── Chat.java                        ← Firestore
│   ├── Mensaje.java                     ← Firestore
│   ├── Proyecto.java                    ← Firestore
│   ├── Notification.java                ← Firestore parcial
│   └── ComentarioEntity.java            ← Room
├── repository/
│   ├── ChatRepository.java              ← Firestore
│   ├── ProyectoRepository.java          ← Firestore
│   ├── AuthRepository.java              ← Auth + Firestore (compañeros)
│   ├── UbicacionRepository.java         ← HashMap estático (pendiente migración)
│   └── UsuarioRepository.java
├── data/
│   ├── ClientDataRepository.java        ← MOCK — citas y reviews hardcodeadas
│   └── ProjectSessionData.java
├── utils/
│   ├── PreferencesManager.java          ← SharedPreferences
│   ├── NotificationHelper.java          ← Push local
│   ├── ChatRepository.java (interface)  ← Asesor (compañeros)
│   ├── AsesorDatabase.java              ← Room
│   └── ImageUrls.java
└── receivers/
    ├── CitaReminderReceiver.java
    └── CitaReminderWorker.java
```

---

## 2. Inventario de pantallas del Cliente

| # | Pantalla | Clase | Layout | Estado Firebase |
|---|----------|-------|--------|-----------------|
| 1 | Splash / Router | SplashActivity | activity_splash | Lee Firestore (rol) |
| 2 | Inicio / Home | HomeActivity | activity_home | ✅ addSnapshotListener proyectos |
| 3 | Búsqueda | SearchActivity | activity_search | ✅ addSnapshotListener proyectos |
| 4 | Detalle Proyecto | ProjectDetailActivity | activity_project_detail | ✅ get() proyecto + asesor |
| 5 | Agendar Cita | AgendaCitaActivity | activity_agenda_cita | ❌ SIN FIREBASE |
| 6 | Mis Citas | MisCitasActivity | activity_mis_citas | ❌ datos mock hardcodeados |
| 7 | Chat (mensajes) | ChatDetailActivity | activity_chat_detail | ✅ addSnapshotListener mensajes |
| 8 | Conversaciones | MessagesActivity | activity_messages | ✅ addSnapshotListener chats |
| 9 | Notificaciones | NotificationsActivity | activity_notifications | ⚠️ get() en vez de listener |
| 10 | Perfil | ProfileActivity | activity_profile | ❌ SharedPreferences only |
| 11 | Reseñas | ReviewsActivity | activity_reviews | ❌ datos mock |
| 12 | Agregar comentario | AddCommentActivity | activity_add_comment | ❌ TODO comentado |
| 13 | Comentario dialog | AgregarComentarioDialog | dialog_agregar_comentario | ⚠️ Room solamente |
| 14 | Ver en mapa | ViewOnMapActivity | activity_view_on_map | ❌ coordenadas hardcodeadas |
| 15 | Método de pago | PaymentMethodActivity | activity_payment_method | ❌ SIN persistencia |

---

## 3. Mapa Firestore — colecciones que consume el Cliente

| Colección / Subcolección | Campos leídos | Campos escritos | Archivo |
|--------------------------|---------------|-----------------|---------|
| `proyectos` | nombre, ubicacion, precio, tipo, imageUrl, rating, visible, activo, esDemo, latitud, longitud, direccion, uidAsesores, tipologias, qrCode, imagenesUri | — | ProyectoRepository.java |
| `users/{uid}` | nombre, fotoUrl, role | — | ProjectDetailActivity.java:813 |
| `users/{uidCliente}` | nombre | — | ProjectDetailActivity.java:871 |
| `chats/{chatId}` | chatId, participantes, nombreCliente, nombreAsesor, fotoAsesor, idProyecto, nombreProyecto, ultimoMensaje, timestampUltimoMensaje | chatId, participantes, nombreCliente, nombreAsesor, fotoAsesor, idProyecto, nombreProyecto, ultimoMensaje, timestampUltimoMensaje | ChatRepository.java |
| `chats/{chatId}/mensajes` | idEmisor, texto, timestamp, leido | idEmisor, texto, timestamp, leido | ChatRepository.java |
| `notifications` | senderName, descripcion, tiempo, avatarName, propertyName, isOld, role, order | — | NotificationsActivity.java:56 |
| `usuarios/{uid}` | — | uid, nombre, email, telefono, dni, role="CLIENTE", activo | AuthRepository.java:110 |
| `users/{uid}` (legacy) | — | uid, nombre, email, role="cliente", activo | AuthRepository.java:119 |

**Colecciones NO implementadas que DEBERÍAN existir:**
- `citas/{citaId}` — **no existe ninguna escritura de citas**
- `slots/{proyectoId}_{fecha}_{hora}` — no existe control de disponibilidad
- `users/{uid}/notifications` — subcolección de notificaciones por usuario

---

## 4. Inventario de operaciones Firebase

### `addSnapshotListener` (tiempo real)
| Archivo | Colección | Línea aprox | ¿Se libera? |
|---------|-----------|-------------|-------------|
| HomeActivity.java:170 | `proyectos` | onStart | ✅ onStop |
| SearchActivity.java:118 | `proyectos` | onStart | ✅ onStop |
| ChatDetailActivity.java:100 | `chats/{id}/mensajes` | onResume | ✅ onPause |
| MessagesActivity.java:102 | `chats` (whereArrayContains) | onResume | ✅ onPause |

### `get()` (one-shot)
| Archivo | Colección | Contexto |
|---------|-----------|----------|
| NotificationsActivity.java:56 | `notifications` | onCreate — ⚠️ debería ser listener |
| ProyectoRepository.java:88 | `proyectos` | por nombre — oneshot OK |
| ProyectoRepository.java:117 | `proyectos` | por ID — oneshot OK |
| ProjectDetailActivity.java:813 | `users/{uid}` | cargar asesor — OK |
| ProjectDetailActivity.java:860 | `users/{uid}` | abrir chat — OK |
| ChatRepository.java:47 | `chats/{chatId}` | abrirOCrearChat — OK |
| AuthRepository.java:55 | `usuarios` | verificar DNI — OK |
| AuthRepository.java:70 | `usuarios` | verificar email — OK |

### `set()` / `update()` / `add()`
| Archivo | Colección | Operación |
|---------|-----------|-----------|
| ChatRepository.java:63 | `chats/{chatId}` | set (crear chat) |
| ChatRepository.java:87 | `chats/{chatId}/mensajes` | add (enviar mensaje) |
| ChatRepository.java:90 | `chats/{chatId}` | update (ultimoMensaje) |

### `WriteBatch`
| Archivo | Colecciones | Propósito |
|---------|-------------|-----------|
| AuthRepository.java:109 | `usuarios` + `users` | Registro de cliente |

### `runTransaction` — **No existe ninguna en el módulo Cliente** ⚠️

---

## 5. Hallazgos preliminares — clasificados por severidad

---

### CRÍTICO

---

### [CRÍTICO-01] AgendaCitaActivity no escribe nada en Firestore — las citas son fantasmas
- **Ubicación**: `activities/cliente/AgendaCitaActivity.java:116`
- **Qué hace hoy**: Muestra un BottomSheet de éxito, lanza notificación local y navega a HomeActivity. No hace ninguna operación Firestore.
- **Qué debería hacer**: Crear un documento en `citas/{autoId}` con proyectoId, fecha, hora, uidCliente, uidAsesor, estado="pendiente", y verificar atómicamente que el slot no esté ocupado.
- **Causa raíz**: La actividad fue implementada como prototipo UI sin integración Firebase.
- **Impacto funcional**: NINGUNA cita existe en base de datos. MisCitasActivity lee datos mock. El flujo completo de reserva es decorativo.
- **Solución propuesta**: Implementar `runTransaction` que lea el slot `slots/{proyectoId}_{yyyyMMdd}_{HHmm}` y, si no existe, lo cree y cree la cita. Si ya existe, mostrar error al usuario.
- **Archivos a tocar**: `AgendaCitaActivity.java`, nuevo documento-slot en Firestore.
- **Efectos secundarios posibles**: Requiere crear reglas Firestore para la colección `citas` y `slots`.
- **Cómo probar**: Dos emuladores simultáneos intentan reservar el mismo slot; solo uno debe tener éxito.

---

### [CRÍTICO-02] Días disponibles hardcodeados — solo válidos en el mes actual, sin año
- **Ubicación**: `activities/cliente/AgendaCitaActivity.java:27-33`
- **Qué hace hoy**: `diasDisponibles` y `diasOcupados` son `Set<Integer>` con números del 1 al 30, usados para cualquier mes y año.
- **Qué debería hacer**: Leer disponibilidad real de Firestore vía `addSnapshotListener` en la colección `slots` o `citas`, filtrada por proyectoId.
- **Causa raíz**: Implementación de prototipo sin backend.
- **Impacto funcional**: El día 2 de CUALQUIER mes siempre aparece como disponible; el día 1 siempre como ocupado. Los datos son completamente ficticios.
- **Solución propuesta**: Listener a `slots` donde `proyectoId == X AND fecha BETWEEN [inicio del mes, fin del mes]`. Pintar en gris los slots ocupados.
- **Archivos a tocar**: `AgendaCitaActivity.java`.
- **Efectos secundarios posibles**: Requiere índice compuesto Firestore (proyectoId + fecha).

---

### [CRÍTICO-03] MisCitasActivity — citas provienen de datos mock, sin Firestore
- **Ubicación**: `activities/cliente/MisCitasActivity.java:125`, `data/ClientDataRepository.java:32`
- **Qué hace hoy**: Llama a `ClientDataRepository.getAppointments()` que devuelve 4 citas hardcodeadas con proyectos, asesores y estados ficticios.
- **Qué debería hacer**: Escuchar `citas` en Firestore filtrada por `uidCliente == currentUser.uid` con `addSnapshotListener`.
- **Causa raíz**: Prototipo sin backend.
- **Impacto funcional**: El usuario nunca ve sus citas reales. Cancela citas que no existen en Firestore.
- **Solución propuesta**: Reemplazar `ClientDataRepository.getAppointments()` por listener Firestore en `onStart()`, liberar en `onStop()`.
- **Archivos a tocar**: `MisCitasActivity.java`, `ClientDataRepository.java`.

---

### [CRÍTICO-04] Cancelar cita persiste en SharedPreferences, no en Firestore
- **Ubicación**: `activities/cliente/MisCitasActivity.java:145`
- **Qué hace hoy**: `PreferencesManager.guardarCitaCancelada(this, cita.getId())` guarda la cancelación localmente.
- **Qué debería hacer**: `db.collection("citas").document(cita.getId()).update("estado", "cancelada")` + liberar el slot `slots/{id}` en batch atómico.
- **Causa raíz**: Sin Firestore para citas, la cancelación solo puede ser local.
- **Impacto funcional**: El asesor no ve la cancelación. Otros clientes no ven el slot liberado. Si el usuario reinstala la app, las cancelaciones se pierden.
- **Solución propuesta**: Depende de CRÍTICO-01 y CRÍTICO-03. Una vez implementadas las citas reales, la cancelación debe usar `WriteBatch` que actualice `citas/{id}.estado = "cancelada"` y elimine `slots/{slotId}`.

---

### [CRÍTICO-05] Reagendar no existe como funcionalidad del Cliente
- **Ubicación**: `activities/cliente/MisCitasActivity.java:62`
- **Qué hace hoy**: El botón "Reagendar" abre `AgendaCitaActivity` pasando solo el nombre del proyecto, como si fuera una cita nueva.
- **Qué debería hacer**: Abrir una pantalla de reagendamiento que: (1) muestre la cita actual, (2) permita seleccionar nuevo slot, (3) use `WriteBatch` para liberar slot anterior + reservar nuevo + actualizar estado + registrar `fechaReagendamiento`.
- **Causa raíz**: No implementado.
- **Impacto funcional**: El usuario puede "reagendar" pero en realidad está creando (o intentando crear) una cita nueva sin liberar la anterior.
- **Solución propuesta**: Crear `ReagendarCitaClienteActivity` (el módulo Asesor ya tiene `ReagendarCitaActivity`). Usar `WriteBatch` con 3 operaciones: delete slot viejo, set slot nuevo, update cita.
- **Archivos a tocar**: Nueva activity + modificar `MisCitasActivity.java`.

---

### ALTO

---

### [ALTO-01] AuthRepository escribe a colección "usuarios" en vez de "users"
- **Ubicación**: `repository/AuthRepository.java:28`
- **Qué hace hoy**: Colección primaria es `"usuarios"` (español, mayúsculas en rol "CLIENTE"). La legacy escribe a `"users"` con rol `"cliente"` (minúsculas).
- **Qué debería hacer**: La convención del proyecto es `"users"` + `role: "cliente"` (lowercase). La colección `"usuarios"` es redundante.
- **Causa raíz**: Implementación independiente sin coordinación con la convención global.
- **Impacto funcional**: Si cualquier Activity lee el rol desde `"users"`, funciona. Pero `"usuarios"` queda huérfana acumulando datos. Doble escritura por cada registro.
- **Solución propuesta**: Eliminar `COLLECTION_USUARIOS` y consolidar todo en `"users"` con campos: `uid`, `nombre`, `email`, `telefono`, `dni`, `role: "cliente"`, `activo: true`.
- **Archivos a tocar**: `AuthRepository.java`. Requiere coordinación (ver PENDING_COORDINATION.md).

---

### [ALTO-02] ProfileActivity no lee datos reales del usuario desde Firestore
- **Ubicación**: `activities/cliente/ProfileActivity.java:31-40`
- **Qué hace hoy**: Lee nombre de `PreferencesManager` (SharedPreferences). Avatar hardcodeado (`ImageUrls.AVATAR_JONATHAN`). Email muestra "Último acceso" en lugar del email real.
- **Qué debería hacer**: Leer `users/{uid}` de Firestore al entrar a la pantalla y mostrar: nombre real, email real, foto de perfil (si existe), fecha de último acceso.
- **Causa raíz**: Implementación de prototipo.
- **Impacto funcional**: El usuario siempre ve el nombre "Jonathan" y la foto de un avatar genérico, sin importar quién esté logueado.
- **Solución propuesta**: En `onResume()`, hacer `db.collection("users").document(uid).get()` y actualizar los campos de la UI.
- **Archivos a tocar**: `ProfileActivity.java`.

---

### [ALTO-03] AddCommentActivity tiene el TODO de Firebase comentado
- **Ubicación**: `activities/cliente/AddCommentActivity.java:66-72`
- **Qué hace hoy**: Muestra Toast "¡Comentario guardado!" pero no escribe nada. El código de Firebase está comentado con "TODO: en Lab 6".
- **Qué debería hacer**: Guardar en Firestore en colección `comentarios/{proyectoId}/items/{autoId}` con userId, rating, texto, timestamp.
- **Causa raíz**: Funcionalidad diferida de labs anteriores, nunca completada.
- **Impacto funcional**: Las reseñas desde `AddCommentActivity` no se persisten.
- **Solución propuesta**: Conectar con `AgregarComentarioDialog` (que sí guarda en Room) o migrar ambas a Firestore.
- **Archivos a tocar**: `AddCommentActivity.java`.

---

### [ALTO-04] ReviewsActivity usa datos mock estáticos
- **Ubicación**: `activities/cliente/ReviewsActivity.java:34`, `data/ClientDataRepository.java:53`
- **Qué hace hoy**: Llama a `ClientDataRepository.getReviews()` — 4 reseñas hardcodeadas con nombres ficticios.
- **Qué debería hacer**: Leer reseñas de Firestore o de Room (según la decisión de arquitectura de comentarios).
- **Impacto funcional**: Las reseñas siempre son las mismas 4, independientemente del proyecto.
- **Solución propuesta**: Leer de `db.comentarioDao().obtenerPorProyecto(nombreProyecto)` (Room ya implementado) o migrar a Firestore.
- **Archivos a tocar**: `ReviewsActivity.java`.

---

### [ALTO-05] PaymentMethodActivity — crash potencial por substring sin validación
- **Ubicación**: `activities/cliente/PaymentMethodActivity.java:124`
- **Qué hace hoy**: `numero.replace(" ", "").substring(12)` — extrae últimos 4 dígitos para el diálogo de confirmación.
- **Qué debería hacer**: Validar longitud antes del substring, o usar `substring(Math.max(0, numero.replace(" ","").length()-4))`.
- **Causa raíz**: Validación incompleta. La validación de `length() < 16` sucede antes, pero con el formateo de espacios la cadena puede tener diferente longitud.
- **Impacto funcional**: `StringIndexOutOfBoundsException` en runtime si el número tiene menos de 13 caracteres en texto procesado.
- **Solución propuesta**: Usar `String raw = numero.replace(" ",""); raw.substring(Math.max(0, raw.length()-4))`.
- **Archivos a tocar**: `PaymentMethodActivity.java:124`.

---

### MEDIO

---

### [MEDIO-01] NotificationsActivity usa get() en lugar de addSnapshotListener
- **Ubicación**: `activities/cliente/NotificationsActivity.java:55`
- **Qué hace hoy**: One-shot `.get()` al crear la Activity.
- **Qué debería hacer**: `addSnapshotListener` para ver notificaciones nuevas en tiempo real. Liberar en `onStop()`.
- **Impacto funcional**: Si el asesor envía una notificación mientras el usuario tiene la pantalla abierta, no la verá hasta que salga y vuelva.
- **Solución propuesta**: Convertir a listener, guardar `ListenerRegistration` como campo.
- **Archivos a tocar**: `NotificationsActivity.java`.

---

### [MEDIO-02] NotificationsActivity — swipe-to-delete no persiste en Firestore
- **Ubicación**: `activities/cliente/NotificationsActivity.java:117`
- **Qué hace hoy**: Remueve la notificación de la lista local. Al reiniciar, vuelve a aparecer.
- **Qué debería hacer**: Marcar como leída/eliminada en Firestore o en subcolección `users/{uid}/notifications`.
- **Impacto funcional**: Las notificaciones "eliminadas" resurgen en cada apertura de la pantalla.
- **Archivos a tocar**: `NotificationsActivity.java`.

---

### [MEDIO-03] ProjectDetailActivity — doble round-trip Firestore al abrir chat
- **Ubicación**: `activities/cliente/ProjectDetailActivity.java:860-903`
- **Qué hace hoy**: Consulta `users/{uidAsesor}` → callback → consulta `users/{uidCliente}` → callback → abrirOCrearChat. Dos round-trips secuenciales.
- **Qué debería hacer**: El nombre del cliente ya está en `FirebaseAuth.getCurrentUser().getDisplayName()` o en SharedPreferences. No necesita ir a Firestore para eso.
- **Impacto funcional**: Latencia doble al presionar el botón "Chatear". Si el usuario tiene mala conexión, puede ver un delay notable.
- **Solución propuesta**: Usar `PreferencesManager.obtenerNombre(this)` para el nombre del cliente en lugar de consultar Firestore.
- **Archivos a tocar**: `ProjectDetailActivity.java`.

---

### [MEDIO-04] MessagesActivity — botón "eliminar todo" solo limpia lista local
- **Ubicación**: `activities/cliente/MessagesActivity.java:68`
- **Qué hace hoy**: `chatItems.clear(); chatsAdapter.notifyDataSetChanged()` — limpia vista local.
- **Qué debería hacer**: Como mínimo, marcar chats como archivados/eliminados en Firestore, o no exponer esta funcionalidad si no está implementada.
- **Impacto funcional**: Los chats "eliminados" vuelven a aparecer al reabrir la pantalla porque el listener Firestore los trae de nuevo.
- **Archivos a tocar**: `MessagesActivity.java`.

---

### [MEDIO-05] HomActivity.aplicarFiltro() carga URLs Unsplash hardcodeadas
- **Ubicación**: `activities/cliente/HomeActivity.java:220-254`
- **Qué hace hoy**: Al filtrar por Departamento/Casa/Terreno, carga URLs de Unsplash hardcodeadas para los cards destacados y guardados.
- **Qué debería hacer**: Las imágenes de los cards deberían venir de los proyectos filtrados de `todaLaLista`.
- **Impacto funcional**: Al filtrar por tipo, las imágenes de la sección "Guardados" muestran fotos genéricas de internet que no corresponden a los proyectos reales.
- **Archivos a tocar**: `HomeActivity.java`.

---

### [MEDIO-06] ChatDetailActivity — listener en onResume/onPause en vez de onStart/onStop
- **Ubicación**: `activities/cliente/ChatDetailActivity.java:82-97`
- **Qué hace hoy**: Inicia listener en `onResume()`, lo libera en `onPause()`.
- **Qué debería hacer**: Para un chat, el listener debería estar activo cuando el Activity es visible. `onResume/onPause` es correcto pero más agresivo que `onStart/onStop` — si aparece un dialog o BottomSheet, el listener se detiene.
- **Impacto funcional**: Si el usuario toca "Ver imagen" o aparece cualquier dialog sobre el chat, el listener se pausa y los nuevos mensajes no llegan hasta que el usuario regresa al chat.
- **Solución propuesta**: Mover a `onStart/onStop`.
- **Archivos a tocar**: `ChatDetailActivity.java`.

---

### [MEDIO-07] ChatDetailActivity — sin estado "enviando" (envío optimista)
- **Ubicación**: `activities/cliente/ChatDetailActivity.java:117`
- **Qué hace hoy**: Al enviar, borra el campo de texto y deshabilita el botón. El mensaje aparece solo cuando el listener Firestore lo retorna.
- **Qué debería hacer**: Mostrar el mensaje inmediatamente con estado "enviando..." y actualizar a "enviado" cuando Firestore confirma.
- **Impacto funcional**: El usuario ve el campo vacío y ningún mensaje durante el round-trip. En conexiones lentas (3G), puede creer que el mensaje no se envió.
- **Archivos a tocar**: `ChatDetailActivity.java`, `MensajesAdapter.java`.

---

### [MEDIO-08] UbicacionRepository — coordenadas hardcodeadas en HashMap estático
- **Ubicación**: `repository/UbicacionRepository.java:39-64`
- **Qué hace hoy**: HashMap estático con 5 proyectos hardcodeados. Ya tiene TODO documentado para migrar a Firestore.
- **Qué debería hacer**: Leer latitud/longitud desde el documento del proyecto en Firestore (ya disponibles en `Proyecto.getLatitud()` y `Proyecto.getLongitud()` cuando `cargarDatosProyecto()` tiene éxito).
- **Impacto funcional**: Proyectos nuevos creados por el Admin nunca tendrán coordenadas en el mapa. Solo los 5 proyectos hardcodeados muestran el mapa.
- **Nota**: `ProjectDetailActivity.cargarMapaDesdeProyecto()` ya intenta leer lat/lng del modelo, por lo que `UbicacionRepository` quedaría obsoleto y puede eliminarse.
- **Archivos a tocar**: `ProjectDetailActivity.java`, `UbicacionRepository.java`.

---

### [MEDIO-09] MessagesActivity — mostrarDialogoNuevoChat nunca se invoca
- **Ubicación**: `activities/cliente/MessagesActivity.java:142`
- **Qué hace hoy**: El método `mostrarDialogoNuevoChat()` está definido pero ningún botón o acción lo llama.
- **Impacto funcional**: El usuario no puede iniciar una conversación nueva desde la pantalla de mensajes.
- **Solución propuesta**: Agregar FAB o botón "+" que invoque `mostrarDialogoNuevoChat()`, o cambiar el flujo para iniciar chat siempre desde `ProjectDetailActivity`.
- **Archivos a tocar**: `MessagesActivity.java`, `activity_messages.xml`.

---

### BAJO

---

### [BAJO-01] ClientAppointmentsAdapter usa notifyDataSetChanged() en vez de DiffUtil
- **Ubicación**: `adapters/ClientAppointmentsAdapter.java:103`
- **Qué hace hoy**: `notifyDataSetChanged()` re-renderiza toda la lista.
- **Qué debería hacer**: Implementar `DiffUtil.ItemCallback` y usar `ListAdapter` para animaciones eficientes.
- **Impacto funcional**: Parpadeo visible de la lista al cambiar de tab en MisCitasActivity.
- **Archivos a tocar**: `ClientAppointmentsAdapter.java`.

---

### [BAJO-02] AgendaCitaActivity — botón confirmar no se deshabilita durante procesamiento
- **Ubicación**: `activities/cliente/AgendaCitaActivity.java:75`
- **Qué hace hoy**: El botón "Siguiente" dispara `confirmarCita()` sin deshabilitar el botón.
- **Qué debería hacer**: Deshabilitar el botón antes de iniciar la operación Firestore, rehabilitar en error o éxito.
- **Impacto funcional**: El usuario puede tocar dos veces rápido y crear citas duplicadas (una vez que Firestore esté implementado).
- **Archivos a tocar**: `AgendaCitaActivity.java`.

---

### [BAJO-03] ProfileActivity tiene TODO sin implementar — editar perfil
- **Ubicación**: `activities/cliente/ProfileActivity.java:47`
- **Qué hace hoy**: `btnEditProfile` tiene listener vacío con comentario `// TODO: Implementar edición de perfil`.
- **Impacto funcional**: Botón visible en la UI que no hace nada.
- **Solución propuesta**: Si no se implementa en este ciclo, ocultar el botón con `View.GONE` hasta que esté listo.
- **Archivos a tocar**: `ProfileActivity.java`.

---

### [BAJO-04] Strings hardcodeados en Activities del Cliente
- **Ubicación**: Múltiples actividades.
- **Ejemplos**: `"Tarjeta •••• 1222 seleccionada"` (AgendaCitaActivity:71), `"Agenda tu cita"` (AgendaCitaActivity:117), mensajes de Toast en español directamente en el código.
- **Qué debería hacer**: Mover a `strings.xml` para soporte de internacionalización y mantenimiento centralizado.
- **Archivos a tocar**: Todas las activities del cliente + `strings.xml`.

---

### [BAJO-05] HomeActivity.onResume() re-aplica filtro sobre lista posiblemente vacía
- **Ubicación**: `activities/cliente/HomeActivity.java:200`
- **Qué hace hoy**: En `onResume()`, llama `aplicarFiltro(tipologiaGuardada)` antes de que el snapshot listener actualice `todaLaLista`.
- **Impacto funcional**: Si el usuario vuelve al Home después de un back, hay un breve instante donde la lista aparece vacía hasta que el listener actualiza.
- **Solución propuesta**: No llamar `aplicarFiltro()` en `onResume()` si `todaLaLista` está vacía; el listener en `onStart()` ya re-aplicará el filtro al recibir datos.
- **Archivos a tocar**: `HomeActivity.java`.

---

### [BAJO-06] ViewOnMapActivity — coordenadas hardcodeadas (-12.0600, -77.1200)
- **Ubicación**: `activities/cliente/ViewOnMapActivity.java:31`
- **Qué hace hoy**: `coordProyecto = new LatLng(-12.0600, -77.1200)` para todos los proyectos.
- **Qué debería hacer**: Recibir latitud/longitud como extras del Intent desde `ProjectDetailActivity`, o usar `UbicacionRepository`.
- **Impacto funcional**: El mapa siempre muestra La Perla, Callao, sin importar cuál proyecto se seleccionó.
- **Archivos a tocar**: `ViewOnMapActivity.java`, `ProjectDetailActivity.java`.

---

### [BAJO-07] AgregarComentarioDialog — System.currentTimeMillis() en timestamp
- **Ubicación**: `activities/cliente/AgregarComentarioDialog.java:95`
- **Qué hace hoy**: `System.currentTimeMillis()` para el timestamp en Room.
- **Impacto funcional**: Para Room local está bien, pero si se migra a Firestore debería usarse `FieldValue.serverTimestamp()`.
- **Archivos a tocar**: `AgregarComentarioDialog.java`.

---

---

## Fichas adicionales — Dominios 8 al 12

---

### [MEDIO-10] Dominio 8 — Notificaciones y Mensajes mezclados en el mismo TabLayout
- **Ubicación**: `activities/cliente/MessagesActivity.java:73`, `activities/cliente/NotificationsActivity.java`
- **Qué hace hoy**: `MessagesActivity` tiene un `TabLayout` con dos tabs: tab 0 = "Notificaciones", tab 1 = "Mensajes". Al seleccionar tab 0, navega a `NotificationsActivity` (otra Activity separada) y hace `finish()`. Esto causa un flash de navegación visible y rompe el stack de actividades.
- **Qué debería hacer**: O bien un único Activity con ViewPager2 (Fragment "Notificaciones" + Fragment "Mensajes"), o dos Activities distintas accedidas desde el header con íconos separados y badges de contador independientes.
- **Causa raíz**: Diseño de navegación inconsistente — se intentó emular tabs pero se implementó como Activities separadas.
- **Impacto funcional**: El tab "Notificaciones" no funciona como tab; el usuario experimenta una transición de Activity en lugar de un cambio de tab fluido. No hay contador de notificaciones no leídas visible en ningún ícono del header.
- **Solución propuesta** (conservadora, sin rediseño): En `MessagesActivity`, el tab 0 debe simplemente cambiar de tab, no navegar a otra Activity. Usar `ViewPager2` + dos Fragments.
- **Archivos a tocar**: `MessagesActivity.java`, `activity_messages.xml`.
- **Efectos secundarios posibles**: Requiere crear `NotificacionesFragment` y `MensajesFragment`.

---

### [BAJO-08] Dominio 9 — Header sin badge de notificaciones no leídas
- **Ubicación**: `activities/cliente/HomeActivity.java:139`
- **Qué hace hoy**: `btnNotificaciones` navega a `NotificationsActivity` pero no muestra ningún badge de contador.
- **Qué debería hacer**: Mostrar un badge rojo con el número de notificaciones no leídas, actualizado en tiempo real con un `addSnapshotListener` sobre `notifications` (o `users/{uid}/notifications`).
- **Causa raíz**: No implementado.
- **Impacto funcional**: El usuario no sabe si tiene notificaciones nuevas sin abrir la pantalla.
- **Solución propuesta**: Añadir `TextView` superpuesto sobre el ícono de notificaciones; listener sobre `notifications.whereEqualTo("role","cliente").whereEqualTo("leida",false)`.
- **Archivos a tocar**: `HomeActivity.java`, `activity_home.xml`.

---

### [ALTO-06] Dominio 10 — AgendaCitaActivity sin ningún indicador de carga
- **Ubicación**: `activities/cliente/AgendaCitaActivity.java:75-76`
- **Qué hace hoy**: Al presionar "Siguiente" / "btnNextArrow", llama `confirmarCita()` que muestra el diálogo de éxito inmediatamente (sin Firestore hoy). Una vez conectado a Firebase, el botón no se deshabilitará y no habrá loading.
- **Qué debería hacer**: Deshabilitar ambos botones (`btnNext` y `btnNextArrow`) mientras la transaction Firestore está en curso, mostrar `ProgressBar`, rehabilitar en éxito o error.
- **Causa raíz**: Patrón de loading no aplicado en el flujo de citas.
- **Impacto funcional**: Sin esto, el usuario puede crear múltiples citas duplicadas tocando el botón varias veces antes de que la transaction termine.
- **Solución propuesta**: `btnNext.setEnabled(false); progressBar.setVisibility(VISIBLE);` antes de la transaction; revertir en los callbacks de éxito y error.
- **Archivos a tocar**: `AgendaCitaActivity.java`, `activity_agenda_cita.xml`.

---

### [MEDIO-11] Dominio 10 — ProjectDetailActivity sin ProgressBar durante carga de datos
- **Ubicación**: `activities/cliente/ProjectDetailActivity.java:599`
- **Qué hace hoy**: `cargarDatosProyecto()` hace un `get()` a Firestore sin mostrar ningún indicador de carga. El contenido aparece de golpe cuando Firestore responde.
- **Qué debería hacer**: Mostrar un `ProgressBar` o `ShimmerLayout` mientras se carga el proyecto; ocultarlo cuando `pintarDatosProyecto()` termine.
- **Causa raíz**: Falta el patrón loading → dato → error en este Activity.
- **Impacto funcional**: En conexiones lentas, el usuario ve la pantalla vacía varios segundos sin retroalimentación.
- **Archivos a tocar**: `ProjectDetailActivity.java`, `activity_project_detail.xml`.

---

### [MEDIO-12] Dominio 11 — Sin validación de rol `role == "cliente"` en las pantallas del Cliente
- **Ubicación**: `activities/cliente/HomeActivity.java` (y todas las activities del cliente)
- **Qué hace hoy**: Ninguna Activity del módulo Cliente verifica que el usuario autenticado tenga `role == "cliente"` antes de mostrar el contenido.
- **Qué debería hacer**: En `HomeActivity.onCreate()` o `onResume()`, verificar via `FirebaseAuth.getCurrentUser()` que el usuario esté autenticado, y opcionalmente leer el campo `role` de Firestore para confirmar el rol.
- **Causa raíz**: El enrutamiento por rol se hace en `SplashActivity` al inicio de sesión, pero no se vuelve a verificar en cada Activity.
- **Impacto funcional** (bajo actualmente): Si un asesor o admin consigue navegar al módulo Cliente (deep link, intento de back stack), verá contenido de cliente sin restricción.
- **Solución propuesta** (conservadora): Verificar solo `FirebaseAuth.getCurrentUser() != null` en `HomeActivity`. La verificación de rol en Firestore es costosa y raramente necesaria si el flujo de login es correcto.
- **Archivos a tocar**: `HomeActivity.java`.

---

### [BAJO-09] Dominio 12 — UX: `ViewOnMapActivity` duplica funcionalidad de mapa en `ProjectDetailActivity`
- **Ubicación**: `activities/cliente/ViewOnMapActivity.java:31`
- **Qué hace hoy**: `ViewOnMapActivity` muestra un mapa con coordenadas hardcodeadas. `ProjectDetailActivity` ya tiene un mapa embebido con coordenadas reales.
- **Qué debería hacer**: Evaluar si `ViewOnMapActivity` debe eliminarse (funcionalidad duplicada con el mapa de ProjectDetail) o mantenerse como pantalla de mapa expandido a pantalla completa.
- **Causa raíz**: Dos implementaciones de mapa en paralelo.
- **Impacto funcional**: `ViewOnMapActivity` muestra siempre La Perla, Callao, sin importar el proyecto. Si se navega a ella, el usuario ve ubicación incorrecta.
- **Solución propuesta**: Si se mantiene, pasar lat/lng como extras del Intent. Si se elimina, verificar que nada la referencie en el manifest.
- **Archivos a tocar**: `ViewOnMapActivity.java` o eliminación + limpieza de `AndroidManifest.xml`.

---

## Resumen ejecutivo

| Severidad | Total | Bloqueantes para producción |
|-----------|-------|-----------------------------|
| CRÍTICO | 5 | Sí — el flujo de citas completo es decorativo |
| ALTO | 6 | Sí — perfil incorrecto, crash potencial, sin loading state en citas |
| MEDIO | 12 | Parcialmente — UX degradada, datos inconsistentes, listener incorrecto |
| BAJO | 9 | No — calidad de código y deuda técnica |

**El hallazgo más grave del sistema**: El flujo de reserva de citas (CRÍTICO-01 a CRÍTICO-05) está completamente desconectado de Firebase. Un cliente puede "agendar", "cancelar" y "reagendar" citas sin que ningún dato llegue a Firestore. El módulo Asesor (`ReagendarCitaActivity`, `VerDetalleCitaActivity`) asume que las citas existen en Firestore; mientras el Cliente no las escriba, el sistema es inconsistente por diseño.

---

## Estado de hallazgos (actualizado al finalizar cada fase)

| ID | Título corto | Severidad | Estado |
|----|-------------|-----------|--------|
| CRÍTICO-01 | AgendaCitaActivity sin Firestore | CRÍTICO | Pendiente Fase 2 |
| CRÍTICO-02 | Días hardcodeados en calendario | CRÍTICO | Pendiente Fase 2 |
| CRÍTICO-03 | MisCitasActivity datos mock | CRÍTICO | Pendiente Fase 2 |
| CRÍTICO-04 | Cancelar en SharedPreferences | CRÍTICO | Pendiente Fase 2 |
| CRÍTICO-05 | Reagendar no implementado | CRÍTICO | Pendiente Fase 2 |
| ALTO-01 | AuthRepository colección "usuarios" | ALTO | DIFERIDO — ver PENDING_COORDINATION.md |
| ALTO-02 | ProfileActivity sin Firestore | ALTO | Pendiente Fase 2 |
| ALTO-03 | AddCommentActivity TODO Firebase | ALTO | Pendiente Fase 2 |
| ALTO-04 | ReviewsActivity datos mock | ALTO | Pendiente Fase 2 |
| ALTO-05 | PaymentMethodActivity crash substring | ALTO | Pendiente Fase 2 |
| ALTO-06 | AgendaCitaActivity sin loading state | ALTO | Pendiente Fase 2 |
| MEDIO-01 | NotificationsActivity get() vs listener | MEDIO | Pendiente Fase 2 |
| MEDIO-02 | Swipe-delete no persiste | MEDIO | Pendiente Fase 2 |
| MEDIO-03 | Doble round-trip chat | MEDIO | Pendiente Fase 2 |
| MEDIO-04 | Delete chats solo local | MEDIO | Pendiente Fase 2 |
| MEDIO-05 | Imágenes Unsplash hardcodeadas filtros | MEDIO | Pendiente Fase 2 |
| MEDIO-06 | Chat listener onResume vs onStart | MEDIO | Pendiente Fase 2 |
| MEDIO-07 | Sin estado "enviando" en chat | MEDIO | Pendiente Fase 2 |
| MEDIO-08 | UbicacionRepository hardcodeado | MEDIO | Pendiente Fase 2 |
| MEDIO-09 | mostrarDialogoNuevoChat nunca se llama | MEDIO | Pendiente Fase 2 |
| BAJO-01 | notifyDataSetChanged vs DiffUtil | BAJO | Pendiente Fase 2 |
| BAJO-02 | Botón confirmar sin deshabilitar | BAJO | Pendiente Fase 2 |
| BAJO-03 | btnEditProfile vacío | BAJO | Pendiente Fase 2 |
| BAJO-04 | Strings hardcodeados | BAJO | Pendiente Fase 2 |
| BAJO-05 | onResume filtro sobre lista vacía | BAJO | Pendiente Fase 2 |
| BAJO-06 | ViewOnMapActivity coord hardcodeada | BAJO | Pendiente Fase 2 |
| BAJO-07 | System.currentTimeMillis en comentario | BAJO | Pendiente Fase 2 |
| BAJO-08 | Header sin badge de notificaciones | BAJO | Pendiente Fase 2 |
| BAJO-09 | ViewOnMapActivity duplica mapa | BAJO | Pendiente Fase 2 |
| MEDIO-10 | Notificaciones/Mensajes en TabLayout mixto | MEDIO | Pendiente Fase 2 |
| MEDIO-11 | ProjectDetailActivity sin ProgressBar | MEDIO | Pendiente Fase 2 |
| MEDIO-12 | Sin verificación de rol en pantallas cliente | MEDIO | Pendiente Fase 2 |
