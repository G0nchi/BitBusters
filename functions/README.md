# Cloud Functions — Envío de OTP por email

Función `enviarOtp`: cuando la app crea `emailOtps/{correo}` con `{ code, expiresAt }`,
envía el código por correo (Gmail) al `{correo}`.

## Requisitos previos (una sola vez)

1. **Plan Blaze** en el proyecto `bitbusters-8a5f3`
   (Consola Firebase → ⚙️ → *Usage and billing* → *Modify plan* → Blaze).
   Las Cloud Functions con red saliente (SMTP) no funcionan en el plan gratis.

2. **Firebase CLI**:
   ```bash
   npm install -g firebase-tools
   firebase login
   ```

3. **App Password de Gmail** (no la contraseña normal):
   - Activa la verificación en 2 pasos en tu cuenta Google.
   - Ve a https://myaccount.google.com/apppasswords y genera una contraseña de aplicación.

## Configurar credenciales (secrets)

```bash
# Desde la raíz del proyecto
firebase functions:secrets:set EMAIL_USER       # → tu correo Gmail (ej. bitbusters@gmail.com)
firebase functions:secrets:set EMAIL_PASSWORD   # → la App Password de 16 caracteres
```

## Instalar dependencias y desplegar

```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

## Probar

Registra un usuario en la app. Al llegar a la pantalla del código, debería llegar
el correo. Si no llega:

```bash
firebase functions:log --only enviarOtp
```

> Nota: el código sigue guardándose en Firestore (`emailOtps`) y registrándose en
> Logcat (tag `RegisterOtp`) como respaldo durante el desarrollo.
