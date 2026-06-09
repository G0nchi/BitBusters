package com.example.bitbusters.utils;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Helper centralizado para Firebase Authentication (Clase 10 — BaaS).
 *
 * Encapsula las llamadas a {@link FirebaseAuth} (inicio de sesión, registro,
 * sesión actual y cierre de sesión) y traduce las excepciones propias de
 * Firebase a mensajes en español listos para mostrarse en la UI — siguiendo
 * el mismo espíritu de helpers como {@link NotificationHelper} o
 * {@link AsesorNotificationHelper}: la Activity no conoce los detalles de la
 * librería, solo pide la operación y recibe un callback simple.
 *
 * Usado por el flujo de acceso del rol Asesor:
 *   - LoginActivity            → iniciarSesion()
 *   - RegisterPasswordActivity → registrarUsuario()
 *   - AsesorHomeActivity       → cerrarSesion() (logout)
 *
 * Requisito: `app/google-services.json` debe existir y el plugin
 * `com.google.gms.google-services` debe estar activo (ver FIREBASE_SETUP.md
 * en la raíz del proyecto). Sin esto, FirebaseAuth.getInstance() lanza
 * IllegalStateException porque FirebaseApp no fue inicializada.
 */
public final class AuthHelper {

    private AuthHelper() {
        // No instanciable: helper de métodos estáticos.
    }

    /** Resultado de una operación de autenticación (login o registro). */
    public interface AuthCallback {
        /** Operación exitosa; {@code usuario} es el FirebaseUser autenticado. */
        void onSuccess(FirebaseUser usuario);
        /** Operación fallida; {@code mensaje} ya viene traducido al español. */
        void onError(String mensaje);
    }

    // ── Sesión actual ────────────────────────────────────────────────────────

    /** Usuario autenticado actualmente, o {@code null} si no hay sesión activa. */
    @Nullable
    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /** {@code true} si ya existe una sesión de Firebase Auth abierta (auto-login). */
    public static boolean haySesionActiva() {
        return getCurrentUser() != null;
    }

    // ── Operaciones ──────────────────────────────────────────────────────────

    /**
     * Inicia sesión con correo y contraseña contra Firebase Authentication.
     * Usado por LoginActivity para validar al rol Asesor (Clase 10).
     *
     * @param email    correo ingresado por el usuario (debe ser un email válido)
     * @param password contraseña ingresada por el usuario
     * @param callback recibe el resultado en el hilo principal
     */
    public static void iniciarSesion(String email, String password, AuthCallback callback) {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(getCurrentUser());
                    } else {
                        callback.onError(traducirError(task.getException()));
                    }
                });
    }

    /**
     * Crea una cuenta nueva con correo y contraseña en Firebase Authentication.
     * Usado al final del flujo de registro
     * (RegisterAccountActivity → RegisterOtpActivity → RegisterPasswordActivity).
     *
     * @param email    correo recolectado en RegisterAccountActivity
     * @param password contraseña elegida en RegisterPasswordActivity
     * @param callback recibe el resultado en el hilo principal
     */
    public static void registrarUsuario(String email, String password, AuthCallback callback) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(getCurrentUser());
                    } else {
                        callback.onError(traducirError(task.getException()));
                    }
                });
    }

    /**
     * Cierra la sesión de Firebase Auth.
     * Llamar junto con la limpieza de SharedPreferences propia del rol
     * (ej. {@code AsesorStorage.clearAll(ctx)} en AsesorHomeActivity.logout()).
     */
    public static void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
    }

    // ── Traducción de errores de Firebase Auth a español ────────────────────

    private static String traducirError(@Nullable Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "No existe ninguna cuenta registrada con ese correo.";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Correo o contraseña incorrectos.";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Ya existe una cuenta registrada con ese correo.";
        }
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "La contraseña es muy débil. Usa al menos 6 caracteres.";
        }
        return (e != null && e.getMessage() != null)
                ? e.getMessage()
                : "Ocurrió un error al conectar con el servidor de autenticación.";
    }
}
