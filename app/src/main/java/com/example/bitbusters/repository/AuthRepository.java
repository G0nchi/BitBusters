package com.example.bitbusters.repository;

import com.example.bitbusters.models.Usuario;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repositorio de autenticación con Firebase Auth y Firestore.
 * Centraliza el registro de clientes y la consulta de perfil.
 */
public class AuthRepository {

    private static final String COLECCION_USUARIOS = "usuarios";
    private static final String ROL_CLIENTE = "CLIENTE";

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // ── Interfaz de callback ─────────────────────────────────────────────────

    public interface AuthCallback {
        void onSuccess(Usuario usuario);
        void onError(String mensaje);
    }

    // ── Registro de cliente ──────────────────────────────────────────────────

    /**
     * Registra un nuevo cliente:
     * 1. Verifica que el DNI no esté en uso en Firestore.
     * 2. Crea la cuenta con Firebase Auth (email/password).
     * 3. Guarda el perfil completo en Firestore (colección "usuarios", doc id = uid).
     * 4. Si Firestore falla, elimina la cuenta de Auth para mantener consistencia.
     */
    public void registrarCliente(String nombre, String email, String password,
                                  String telefono, String dni,
                                  AuthCallback callback) {

        // Paso 1: verificar unicidad del DNI en Firestore
        db.collection(COLECCION_USUARIOS)
                .whereEqualTo("dni", dni)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        callback.onError("Ya existe una cuenta con este DNI");
                        return;
                    }
                    // DNI libre → crear cuenta en Auth
                    crearCuentaAuth(nombre, email, password, telefono, dni, callback);
                })
                .addOnFailureListener(e -> {
                    callback.onError("Error al verificar el DNI. Revisa tu conexión.");
                });
    }

    /** Crea la cuenta en Firebase Auth. */
    private void crearCuentaAuth(String nombre, String email, String password,
                                  String telefono, String dni,
                                  AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            callback.onError("Error inesperado al crear la cuenta");
                            return;
                        }
                        // Paso 3: guardar perfil en Firestore
                        guardarPerfilFirestore(firebaseUser, nombre, email,
                                telefono, dni, callback);
                    } else {
                        callback.onError(mapearErrorAuth(task.getException()));
                    }
                });
    }

    /** Guarda el perfil del usuario en Firestore. Si falla, hace rollback en Auth. */
    private void guardarPerfilFirestore(FirebaseUser firebaseUser,
                                        String nombre, String email,
                                        String telefono, String dni,
                                        AuthCallback callback) {
        String uid = firebaseUser.getUid();

        Usuario usuario = new Usuario(
                uid, nombre, email, telefono, dni,
                ROL_CLIENTE, null, Timestamp.now(), true
        );

        Map<String, Object> datos = new HashMap<>();
        datos.put("uid", uid);
        datos.put("nombre", nombre);
        datos.put("email", email);
        datos.put("telefono", telefono);
        datos.put("dni", dni);
        datos.put("rol", ROL_CLIENTE);
        datos.put("fotoUrl", null);
        datos.put("fechaRegistro", Timestamp.now());
        datos.put("activo", true);

        db.collection(COLECCION_USUARIOS)
                .document(uid)
                .set(datos)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(usuario);
                })
                .addOnFailureListener(e -> {
                    // Rollback: eliminar usuario de Auth para mantener consistencia
                    firebaseUser.delete().addOnCompleteListener(deleteTask -> {
                        callback.onError(
                                "Error al guardar el perfil. Inténtalo de nuevo.");
                    });
                });
    }

    /**
     * Convierte las excepciones de Firebase Auth en mensajes amigables en español.
     */
    private String mapearErrorAuth(Exception exception) {
        if (exception == null) {
            return "Error desconocido al crear la cuenta";
        }
        if (exception instanceof FirebaseAuthUserCollisionException) {
            return "Ya existe una cuenta con este email";
        }
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            return "La contraseña es demasiado débil";
        }
        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            return "El formato del email es inválido";
        }
        String mensaje = exception.getMessage();
        if (mensaje != null && mensaje.contains("NETWORK_ERROR")) {
            return "Sin conexión a internet, verifica tu red";
        }
        return "Error al crear la cuenta. Inténtalo de nuevo.";
    }
}
