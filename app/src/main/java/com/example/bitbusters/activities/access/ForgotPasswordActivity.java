package com.example.bitbusters.activities.access;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

/**
 * "Olvidé mi contraseña": pide el correo y usa el reseteo nativo de Firebase
 * ({@link FirebaseAuth#sendPasswordResetEmail}), que envía un enlace al correo
 * para que el usuario cree una nueva contraseña en la página de Firebase.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        MaterialButton backButton = findViewById(R.id.backButton);
        MaterialButton sendRecoveryButton = findViewById(R.id.sendRecoveryButton);
        EditText emailRecoveryInput = findViewById(R.id.emailRecoveryInput);

        backButton.setOnClickListener(v -> finish());
        if (sendRecoveryButton != null) {
            sendRecoveryButton.setOnClickListener(v ->
                    enviarEnlaceRecuperacion(sendRecoveryButton, emailRecoveryInput));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void enviarEnlaceRecuperacion(MaterialButton boton, EditText emailRecoveryInput) {
        String email = emailRecoveryInput != null && emailRecoveryInput.getText() != null
                ? emailRecoveryInput.getText().toString().trim()
                : "";

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (emailRecoveryInput != null) {
                emailRecoveryInput.setError(getString(R.string.forgot_password_invalid_email));
            }
            return;
        }

        boton.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            getString(R.string.forgot_password_link_sent, email),
                            Toast.LENGTH_LONG).show();
                    finish(); // volver al login
                })
                .addOnFailureListener(e -> {
                    boton.setEnabled(true);
                    boolean sinCuenta = e instanceof FirebaseAuthInvalidUserException;
                    int mensaje = sinCuenta
                            ? R.string.forgot_password_no_account
                            : R.string.forgot_password_send_error;
                    if (sinCuenta && emailRecoveryInput != null) {
                        emailRecoveryInput.setError(getString(mensaje));
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                });
    }
}
