# Setup de Firebase (Clase 09.1 / 09.2 / 10 — BaaS)

Este documento explica cómo terminar de activar Firebase en el proyecto.
La parte de Gradle (plugin + dependencias) **ya está lista** en `build.gradle` /
`app/build.gradle`. Lo único que falta es generar tu propio proyecto de Firebase
(requiere una cuenta de Google) y conectar la app a él.

## 1. Crear el proyecto en Firebase Console

1. Ir a https://console.firebase.google.com/ e iniciar sesión con una cuenta de Google.
2. **Agregar proyecto** → ponerle un nombre (ej. `BitBusters`).
3. Dentro del proyecto: **Agregar app → Android**.
4. En "Nombre del paquete de Android" escribir exactamente:
   ```
   com.example.bitbusters
   ```
   (debe coincidir con `applicationId` de `app/build.gradle`, si no, el build falla).
5. (Opcional) Registrar el SHA-1 del certificado de debug si luego se usa
   Google Sign-In — se obtiene con:
   ```
   ./gradlew signingReport
   ```

## 2. Descargar y colocar `google-services.json`

1. Firebase Console te ofrece descargar `google-services.json` al terminar el registro.
2. Colocar ese archivo en: `app/google-services.json`
   (al mismo nivel que `app/build.gradle`, **NO** dentro de `src/`).
3. Este archivo está en `.gitignore` a propósito — es específico de cada
   proyecto Firebase y no debe subirse al repo. Cada integrante que quiera
   correr el código con Firebase real necesita el suyo (o que el dueño del
   proyecto Firebase lo comparta por un canal seguro, ej. Drive privado).

## 3. Activar el plugin de Gradle

En `app/build.gradle`, descomentar la línea (ya está preparada, solo falta quitar el `//`):

```groovy
plugins {
    alias(libs.plugins.android.application)
    id 'com.google.gms.google-services'   // <- descomentar esta línea
}
```

> ⚠️ Si activas el plugin **antes** de colocar `google-services.json`, el build
> falla con: `File google-services.json is missing from module root folder`.
> Por eso quedó comentado: así el resto del equipo puede seguir compilando
> sin Firebase mientras tú terminas de configurarlo en tu rama.

Sincronizar Gradle (`./gradlew build` o "Sync Now" en Android Studio). Si todo
está bien, debería compilar sin errores y ya tendrás `FirebaseApp` inicializada
automáticamente al arrancar la app.

## 4. Habilitar los productos de Firebase que vamos a usar

En la consola del proyecto:

- **Authentication** → pestaña "Sign-in method" → habilitar **Correo
  electrónico/contraseña** (es el método mínimo para "presentar hasta BaaS de
  Firebase Authentication").
- **Firestore Database** → "Crear base de datos" → modo de prueba (test mode)
  para desarrollo — luego ajustar reglas de seguridad antes de producción.

## 5. Próximos pasos de código (una vez el setup compile)

1. **Login del asesor con Firebase Auth** — reemplazar la validación
   hardcodeada de `LoginActivity` por:
   ```java
   FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
       .addOnCompleteListener(task -> { ... });
   ```
2. **Registro** — conectar `RegisterAccountActivity` / `RegisterPasswordActivity`
   con `FirebaseAuth.getInstance().createUserWithEmailAndPassword(...)`.
3. **Perfil del asesor** — usar `FirebaseAuth.getInstance().getCurrentUser()`
   para mostrar datos reales en `AsesorPerfilActivity`.
4. **Chat en tiempo real** — destapar el código comentado en
   `FirestoreChatRepository` (ya tiene la estructura de colecciones documentada)
   y cambiar `new MockChatRepository()` por `new FirestoreChatRepository()`
   en `ConversacionActivity`.

## Resumen de lo agregado en este commit

- `build.gradle` (raíz): declarado el plugin `com.google.gms.google-services`
  con `apply false`.
- `app/build.gradle`: línea del plugin lista para descomentar + dependencias
  `firebase-bom`, `firebase-auth`, `firebase-firestore`.
- `.gitignore`: se ignora `app/google-services.json`.
