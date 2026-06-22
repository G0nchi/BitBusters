package com.example.bitbusters.repository;

import androidx.annotation.NonNull;

import com.example.bitbusters.models.Usuario;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AuthRepository {

    public interface AuthCallback {
        void onSuccess(Usuario usuario);
        void onError(String mensaje);
    }

    private static final String COLLECTION_USUARIOS = "usuarios";
    private static final String COLLECTION_USERS_LEGACY = "users";

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        this(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
    }

    public AuthRepository(FirebaseAuth firebaseAuth, FirebaseFirestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.firestore = firestore;
    }

    public void registrarCliente(String nombre, String email, String password,
                                 String telefono, String dni,
                                 AuthCallback callback) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        String dniNormalizado = dni.trim();

        verificarDniUnico(dniNormalizado, callback, () ->
                verificarEmailUnico(emailNormalizado, callback, () ->
                        crearAuthYGuardarPerfil(nombre.trim(), emailNormalizado, password,
                                telefono.trim(), dniNormalizado, callback)));
    }

    private void verificarDniUnico(String dni, AuthCallback callback, Runnable onOk) {
        firestore.collection(COLLECTION_USUARIOS)
                .whereEqualTo("dni", dni)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        onOk.run();
                    } else {
                        callback.onError("El DNI ya está registrado");
                    }
                })
                .addOnFailureListener(e -> callback.onError(mapearErrorGeneral(e)));
    }

    private void verificarEmailUnico(String email, AuthCallback callback, Runnable onOk) {
        firestore.collection(COLLECTION_USUARIOS)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        onOk.run();
                    } else {
                        callback.onError("Ya existe una cuenta con este email");
                    }
                })
                .addOnFailureListener(e -> callback.onError(mapearErrorGeneral(e)));
    }

    private void crearAuthYGuardarPerfil(String nombre, String email, String password,
                                         String telefono, String dni,
                                         AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser authUser = result.getUser();
                    if (authUser == null) {
                        callback.onError("No se pudo crear la cuenta");
                        return;
                    }

                    String uid = authUser.getUid();
                    Usuario usuario = new Usuario(
                            uid,
                            nombre,
                            email,
                            telefono,
                            dni,
                            "CLIENTE",
                            null,
                            Timestamp.now(),
                            true
                    );

                    WriteBatch batch = firestore.batch();
                    batch.set(firestore.collection(COLLECTION_USUARIOS).document(uid), usuario);

                    // Compatibilidad con flujo de login actual (coleccion users + campo role).
                    Map<String, Object> legacyUser = new HashMap<>();
                    legacyUser.put("uid", uid);
                    legacyUser.put("nombre", nombre);
                    legacyUser.put("email", email);
                    legacyUser.put("role", "cliente");
                    legacyUser.put("activo", true);
                    batch.set(firestore.collection(COLLECTION_USERS_LEGACY).document(uid),
                            legacyUser,
                            SetOptions.merge());

                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess(usuario))
                            .addOnFailureListener(e -> rollbackUsuarioAuth(authUser, callback));
                })
                .addOnFailureListener(e -> callback.onError(mapearErrorAuth(e)));
    }

    private void rollbackUsuarioAuth(@NonNull FirebaseUser authUser, AuthCallback callback) {
        authUser.delete()
                .addOnCompleteListener(task ->
                        callback.onError("No se pudo guardar el perfil. Inténtalo de nuevo."));
    }

    private String mapearErrorAuth(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Ya existe una cuenta con este email";
        }
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "La contraseña es demasiado débil";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Email inválido";
        }
        if (e instanceof FirebaseNetworkException) {
            return "Sin conexión a internet, verifica tu red";
        }
        return "No se pudo registrar la cuenta. Inténtalo nuevamente";
    }

    private String mapearErrorGeneral(Exception e) {
        if (e instanceof FirebaseNetworkException) {
            return "Sin conexión a internet, verifica tu red";
        }
        return "No se pudo validar la información. Inténtalo nuevamente";
    }
}
