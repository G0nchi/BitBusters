"use strict";

/**
 * Cloud Function: envío del código OTP de registro por email.
 *
 * Flujo:
 *   1. La app Android (RegisterOtpActivity) crea el documento
 *      `emailOtps/{correo}` con { code, expiresAt }.
 *   2. Esta función se dispara con ese onCreate y envía el `code` al correo
 *      (el ID del documento ES el correo destino) usando Gmail.
 *
 * Las credenciales NO van en el código: se leen de los secrets EMAIL_USER y
 * EMAIL_PASSWORD (ver functions/README.md para configurarlos y desplegar).
 */

const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { defineSecret } = require("firebase-functions/params");
const logger = require("firebase-functions/logger");
const nodemailer = require("nodemailer");

// Correo emisor y "App Password" de Gmail (no la contraseña normal de la cuenta).
const EMAIL_USER = defineSecret("EMAIL_USER");
const EMAIL_PASSWORD = defineSecret("EMAIL_PASSWORD");

exports.enviarOtp = onDocumentCreated(
  {
    document: "emailOtps/{email}",
    region: "us-central1",
    secrets: [EMAIL_USER, EMAIL_PASSWORD],
  },
  async (event) => {
    const snap = event.data;
    if (!snap) return;

    const { code } = snap.data();
    const email = event.params.email; // el ID del documento es el correo destino

    if (!code || !email) {
      logger.warn("Documento OTP sin 'code' o sin email; no se envía.", { email });
      return;
    }

    const transporter = nodemailer.createTransport({
      service: "gmail",
      auth: {
        user: EMAIL_USER.value(),
        pass: EMAIL_PASSWORD.value(),
      },
    });

    const html = `
      <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:24px;
                  border:1px solid #eee;border-radius:12px;color:#1a1a1a">
        <h2 style="margin:0 0 8px">BitBusters</h2>
        <p style="color:#555;margin:0 0 24px">Tu código de verificación es:</p>
        <div style="font-size:36px;font-weight:bold;letter-spacing:10px;
                    text-align:center;color:#1565c0">${code}</div>
        <p style="color:#888;font-size:13px;margin:24px 0 0">
          El código vence en 5 minutos. Si no intentaste registrarte, ignora este correo.
        </p>
      </div>`;

    try {
      await transporter.sendMail({
        from: `BitBusters <${EMAIL_USER.value()}>`,
        to: email,
        subject: "Tu código de verificación · BitBusters",
        text: `Tu código de verificación de BitBusters es: ${code} (vence en 5 minutos).`,
        html,
      });
      logger.info(`OTP enviado correctamente a ${email}`);
    } catch (err) {
      logger.error(`Error enviando OTP a ${email}`, err);
      throw err; // relanzar para que Cloud Functions reintente
    }
  }
);
