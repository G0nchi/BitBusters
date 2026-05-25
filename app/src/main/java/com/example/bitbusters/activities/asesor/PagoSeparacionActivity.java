package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.ActivityPagoSeparacionBinding;
import com.google.android.material.card.MaterialCardView;

public class PagoSeparacionActivity extends AppCompatActivity {

    public static final String EXTRA_CLIENTE  = "extra_cliente";
    public static final String EXTRA_PROYECTO = "extra_proyecto";
    public static final String EXTRA_MONTO    = "extra_monto";

    private ActivityPagoSeparacionBinding binding;

    private String clienteNombre;
    private String proyectoNombre;
    private String monto;

    // Opciones de pago
    private MaterialCardView cardTarjeta;
    private MaterialCardView cardTransferencia;
    private MaterialCardView cardYape;
    private RadioButton rbTarjeta;
    private RadioButton rbTransferencia;
    private RadioButton rbYape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPagoSeparacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        clienteNombre  = getIntent().getStringExtra(EXTRA_CLIENTE);
        proyectoNombre = getIntent().getStringExtra(EXTRA_PROYECTO);
        monto          = getIntent().getStringExtra(EXTRA_MONTO);

        bindResumen();
        setupPaymentOptions();

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnConfirmar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConfirmacionSeparacionActivity.class);
            intent.putExtra(ConfirmacionSeparacionActivity.EXTRA_CLIENTE,  clienteNombre);
            intent.putExtra(ConfirmacionSeparacionActivity.EXTRA_PROYECTO, proyectoNombre);
            intent.putExtra(ConfirmacionSeparacionActivity.EXTRA_MONTO,    monto);
            startActivity(intent);
        });
    }

    // ── Métodos de pago ──────────────────────────────────────────────────────────

    private void setupPaymentOptions() {
        cardTarjeta       = binding.optionTarjeta;
        cardTransferencia = binding.optionTransferencia;
        cardYape          = binding.optionYape;
        rbTarjeta         = binding.rbTarjeta;
        rbTransferencia   = binding.rbTransferencia;
        rbYape            = binding.rbYape;

        // Estado inicial: tarjeta seleccionada
        selectPaymentOption(cardTarjeta, rbTarjeta);

        cardTarjeta.setOnClickListener(v      -> selectPaymentOption(cardTarjeta,       rbTarjeta));
        cardTransferencia.setOnClickListener(v -> selectPaymentOption(cardTransferencia, rbTransferencia));
        cardYape.setOnClickListener(v          -> selectPaymentOption(cardYape,          rbYape));
    }

    private void selectPaymentOption(MaterialCardView selected, RadioButton selectedRb) {
        // Reinicia todos
        applyCardState(cardTarjeta,       rbTarjeta,       false);
        applyCardState(cardTransferencia, rbTransferencia, false);
        applyCardState(cardYape,          rbYape,          false);
        // Activa el seleccionado
        applyCardState(selected, selectedRb, true);
    }

    private void applyCardState(MaterialCardView card, RadioButton rb, boolean selected) {
        if (card == null || rb == null) return;
        float density = getResources().getDisplayMetrics().density;
        if (selected) {
            card.setStrokeColor(ContextCompat.getColor(this, R.color.brand_lime));
            card.setStrokeWidth((int) (2 * density));
            card.setCardBackgroundColor(
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.neutral_surface)));
        } else {
            card.setStrokeColor(ContextCompat.getColor(this, R.color.neutral_mid_1));
            card.setStrokeWidth((int) (1 * density));
            card.setCardBackgroundColor(
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.neutral_surface)));
        }
        rb.setChecked(selected);
    }

    private void bindResumen() {
        setText(binding.tvPagoHeaderSubtitle, proyectoNombre);
        setText(binding.tvPagoProyectoVal, proyectoNombre);
        setText(binding.tvPagoClienteVal, clienteNombre);
        if (monto != null && !monto.isEmpty() && !monto.equals("0")) {
            setText(binding.tvPagoMontoVal, "S/ " + monto);
        }
    }

    private void setText(TextView tv, String value) {
        if (value == null || value.isEmpty() || tv == null) return;
        tv.setText(value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
