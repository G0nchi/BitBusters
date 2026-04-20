package com.example.bitbusters.activities.admin.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteDialogFragment extends DialogFragment {

    public interface OnConfirmDeleteListener {
        void onConfirmDelete(String itemType, int itemIndex);
        void onCancelDelete();
    }

    private OnConfirmDeleteListener listener;
    private String itemType;
    private int itemIndex;

    public static ConfirmDeleteDialogFragment newInstance(String itemType, int itemIndex) {
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        Bundle args = new Bundle();
        args.putString("itemType", itemType);
        args.putInt("itemIndex", itemIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemType = getArguments().getString("itemType");
            itemIndex = getArguments().getInt("itemIndex");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        
        String message = "¿Deseas eliminar este " + itemType + "?";
        
        builder.setMessage(message)
                .setPositiveButton("Eliminar", (dialog, id) -> {
                    if (listener != null) {
                        listener.onConfirmDelete(itemType, itemIndex);
                    }
                })
                .setNegativeButton("Cancelar", (dialog, id) -> {
                    if (listener != null) {
                        listener.onCancelDelete();
                    }
                    dialog.dismiss();
                });

        return builder.create();
    }

    public void setOnConfirmDeleteListener(OnConfirmDeleteListener listener) {
        this.listener = listener;
    }
}
