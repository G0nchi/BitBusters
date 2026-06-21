package com.example.bitbusters.repository;

import com.example.bitbusters.models.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UsuarioRepository {

    public interface UsuarioCallback {
        void onSuccess(Usuario usuario);
        void onError(String mensaje);
    }

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public UsuarioRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void obtenerUsuarioActual(UsuarioCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("No hay sesión activa");
            return;
        }

        String uid = firebaseUser.getUid();
        firestore.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Usuario usuario = doc.toObject(Usuario.class);
                        if (usuario != null) {
                            callback.onSuccess(usuario);
                            return;
                        }
                    }

                    Usuario fallback = new Usuario();
                    fallback.setUid(uid);
                    fallback.setEmail(firebaseUser.getEmail());
                    fallback.setNombre(firebaseUser.getDisplayName() != null
                            ? firebaseUser.getDisplayName()
                            : "Cliente");
                    fallback.setRol("CLIENTE");
                    fallback.setActivo(true);
                    callback.onSuccess(fallback);
                })
                .addOnFailureListener(e -> callback.onError("No se pudo cargar el perfil"));
    }
}
