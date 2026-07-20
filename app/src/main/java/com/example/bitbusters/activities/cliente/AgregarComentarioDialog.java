package com.example.bitbusters.activities.cliente;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.example.bitbusters.R;
import com.example.bitbusters.models.ComentarioEntity;
import com.example.bitbusters.utils.AsesorDatabase;
import com.example.bitbusters.utils.PreferencesManager;

import java.util.concurrent.Executors;

public class AgregarComentarioDialog extends DialogFragment {

    public interface OnComentarioPublicadoListener {
        void onComentarioPublicado();
    }

    private static final String ARG_ID_PROYECTO = "idProyecto";

    private String idProyecto;
    private OnComentarioPublicadoListener listener;

    public static AgregarComentarioDialog newInstance(String idProyecto) {
        AgregarComentarioDialog dialog = new AgregarComentarioDialog();
        Bundle args = new Bundle();
        args.putString(ARG_ID_PROYECTO, idProyecto);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnComentarioPublicadoListener(OnComentarioPublicadoListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        idProyecto = getArguments() != null ? getArguments().getString(ARG_ID_PROYECTO, "") : "";

        android.view.View view = requireActivity().getLayoutInflater()
                .inflate(R.layout.dialog_agregar_comentario, null);

        RatingBar ratingBar = view.findViewById(R.id.ratingBarDialog);
        TextInputEditText etComentario = view.findViewById(R.id.etComentario);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Agregar reseña")
                .setView(view)
                .setNegativeButton("Cancelar", (d, which) -> dismiss())
                .setPositiveButton("Publicar", null) // null para manejar validación manualmente
                .create();

        dialog.setOnShowListener(d -> {
            Button btnPublicar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPublicar.setOnClickListener(v -> publicarComentario(dialog, ratingBar, etComentario));
        });

        return dialog;
    }

    private void publicarComentario(AlertDialog dialog, RatingBar ratingBar, TextInputEditText etComentario) {
        String texto = etComentario.getText() != null
                ? etComentario.getText().toString().trim()
                : "";
        int rating = (int) ratingBar.getRating();

        if (texto.length() < 5) {
            etComentario.setError("Mínimo 5 caracteres");
            return;
        }
        if (rating < 1) {
            Toast.makeText(requireContext(), "Selecciona al menos 1 estrella", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = PreferencesManager.obtenerNombre(requireContext());
        ComentarioEntity comentario = new ComentarioEntity(
                idProyecto,
                nombre,
                nombre,
                null,
                rating,
                texto,
                System.currentTimeMillis()
        );

        // Capturar referencias antes del hilo de fondo
        Activity activity = requireActivity();
        OnComentarioPublicadoListener cb = listener;

        Executors.newSingleThreadExecutor().execute(() -> {
            AsesorDatabase.getInstance(activity.getApplicationContext())
                    .comentarioDao()
                    .insertar(comentario);

            activity.runOnUiThread(() -> {
                if (!activity.isFinishing()) {
                    Toast.makeText(activity, "Reseña publicada", Toast.LENGTH_SHORT).show();
                    if (cb != null) cb.onComentarioPublicado();
                    dismiss();
                }
            });
        });
    }
}
