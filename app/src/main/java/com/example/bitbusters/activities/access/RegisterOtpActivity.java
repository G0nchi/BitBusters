package com.example.bitbusters.activities.access;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.utils.ImmersiveMode;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class RegisterOtpActivity extends AppCompatActivity {

    private static final String TAG = "RegisterOtp";
    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000;

    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4;
    private TextView otpEmail, otpTimerText;
    private MaterialButton verifyButton;
    private CountDownTimer countDownTimer;
    private boolean timerExpired = false;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_register_otp);

        email = getIntent().getStringExtra("email");

        otpDigit1   = findViewById(R.id.otpDigit1);
        otpDigit2   = findViewById(R.id.otpDigit2);
        otpDigit3   = findViewById(R.id.otpDigit3);
        otpDigit4   = findViewById(R.id.otpDigit4);
        otpEmail    = findViewById(R.id.otpEmail);
        otpTimerText = findViewById(R.id.otpTimerText);
        verifyButton = findViewById(R.id.verifyButton);

        if (otpEmail != null && email != null) {
            otpEmail.setText(email);
        }

        setupAutoAdvance();

        MaterialButton backButton = findViewById(R.id.backButton);
        if (backButton != null) backButton.setOnClickListener(v -> finish());
        if (verifyButton != null) verifyButton.setOnClickListener(v -> verifyOtp());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        generateAndSendOtp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void generateAndSendOtp() {
        if (email == null) return;

        String code = String.format(Locale.ROOT, "%04d", new Random().nextInt(10000));
        Log.d(TAG, "OTP para " + email + ": " + code); // solo para desarrollo

        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("expiresAt", System.currentTimeMillis() + OTP_EXPIRY_MS);

        FirebaseFirestore.getInstance()
                .collection("emailOtps")
                .document(email)
                .set(data)
                .addOnSuccessListener(unused -> startTimer())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error generando código", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error guardando OTP", e);
                });
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        timerExpired = false;

        countDownTimer = new CountDownTimer(OTP_EXPIRY_MS, 1000) {
            @Override
            public void onTick(long msLeft) {
                long mins = msLeft / 60000;
                long secs = (msLeft % 60000) / 1000;
                if (otpTimerText != null) {
                    otpTimerText.setText(String.format(Locale.ROOT, "%02d.%02d", mins, secs));
                }
            }

            @Override
            public void onFinish() {
                timerExpired = true;
                if (otpTimerText != null) otpTimerText.setText("00.00");
                if (verifyButton != null) verifyButton.setEnabled(false);
                Toast.makeText(RegisterOtpActivity.this,
                        "El código expiró. Vuelve atrás y reintenta.", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void verifyOtp() {
        if (timerExpired) {
            Toast.makeText(this, "El código expiró. Vuelve atrás y reintenta.", Toast.LENGTH_SHORT).show();
            return;
        }

        String entered = otpDigit1.getText().toString()
                + otpDigit2.getText().toString()
                + otpDigit3.getText().toString()
                + otpDigit4.getText().toString();

        if (entered.length() < 4) {
            Toast.makeText(this, getString(R.string.validation_invalid_otp), Toast.LENGTH_SHORT).show();
            return;
        }

        if (email == null) return;

        verifyButton.setEnabled(false);

        FirebaseFirestore.getInstance()
                .collection("emailOtps")
                .document(email)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        showOtpError();
                        return;
                    }
                    String savedCode = doc.getString("code");
                    Long expiresAt   = doc.getLong("expiresAt");

                    if (savedCode == null || expiresAt == null
                            || System.currentTimeMillis() > expiresAt
                            || !savedCode.equals(entered)) {
                        showOtpError();
                        return;
                    }

                    doc.getReference().delete();
                    if (countDownTimer != null) countDownTimer.cancel();

                    Intent intent = new Intent(this, RegisterPasswordActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    verifyButton.setEnabled(true);
                    Toast.makeText(this, "Error verificando código", Toast.LENGTH_SHORT).show();
                });
    }

    private void showOtpError() {
        verifyButton.setEnabled(true);
        Toast.makeText(this, "Código incorrecto o expirado", Toast.LENGTH_SHORT).show();
    }

    private void setupAutoAdvance() {
        addAutoAdvance(otpDigit1, null,      otpDigit2);
        addAutoAdvance(otpDigit2, otpDigit1, otpDigit3);
        addAutoAdvance(otpDigit3, otpDigit2, otpDigit4);
        addAutoAdvance(otpDigit4, otpDigit3, null);
    }

    private void addAutoAdvance(EditText current, EditText prev, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1 && next != null) {
                    next.requestFocus();
                } else if (s.length() == 0 && prev != null) {
                    prev.requestFocus();
                }
            }
        });
    }
}
