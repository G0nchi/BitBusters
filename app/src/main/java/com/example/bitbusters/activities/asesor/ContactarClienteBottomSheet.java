package com.example.bitbusters.activities.asesor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bitbusters.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ContactarClienteBottomSheet extends BottomSheetDialogFragment {

    public static ContactarClienteBottomSheet newInstance() {
        return new ContactarClienteBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contactar_cliente, container, false);

        view.findViewById(R.id.btn_llamar).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Llamando…", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        view.findViewById(R.id.btn_whatsapp).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Abriendo WhatsApp…", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        view.findViewById(R.id.btn_correo).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Abriendo correo…", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }
}
