package com.example.bitbusters.activities.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.AdminAsesorInmobiliariaAdapter;
import com.example.bitbusters.data.FirestoreAsesoresRepository;
import com.example.bitbusters.models.AdminAsesorInmobiliaria;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdminListaAsesoresInmobiliariaActivity extends AppCompatActivity {

    private TextInputEditText etBuscarAsesorLista;
    private ChipGroup chipGroupEstadosAsesor;
    private RecyclerView rvAsesoresLista;
    private TextView tvEmptyState;
    private AdminAsesorInmobiliariaAdapter adapter;
    private final FirestoreAsesoresRepository asesoresRepository = new FirestoreAsesoresRepository();

    private final List<AdminAsesorInmobiliaria> source = new ArrayList<>();
    private String currentQuery = "";
    private String currentStateFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lista_asesores_inmobiliaria);

        bindViews();
        setupBack();
        setupRecyclerView();
        setupSearch();
        setupChips();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cargarAsesoresDesdeFirestore();
    }

    private void bindViews() {
        etBuscarAsesorLista = findViewById(R.id.etBuscarAsesorLista);
        chipGroupEstadosAsesor = findViewById(R.id.chipGroupEstadosAsesor);
        rvAsesoresLista = findViewById(R.id.rvAsesoresLista);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupBack() {
        ImageButton backButton = findViewById(R.id.btnBackListaAsesores);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        rvAsesoresLista.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminAsesorInmobiliariaAdapter(new ArrayList<>(), null, false);
        rvAsesoresLista.setAdapter(adapter);
    }

    private void cargarAsesoresDesdeFirestore() {
        asesoresRepository.obtenerAsesoresRegistrados(this, new FirestoreAsesoresRepository.AsesoresCallback() {
            @Override
            public void onSuccess(List<AdminAsesorInmobiliaria> asesores) {
                source.clear();
                if (asesores != null) {
                    source.addAll(asesores);
                }
                setupChips();
                renderList();
            }

            @Override
            public void onError(String mensaje) {
                source.clear();
                setupChips();
                renderList();
            }
        });
    }

    private void setupSearch() {
        if (etBuscarAsesorLista == null) {
            return;
        }

        etBuscarAsesorLista.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s == null ? "" : s.toString();
                renderList();
            }
        });
    }

    private void setupChips() {
        if (chipGroupEstadosAsesor == null) {
            return;
        }

        chipGroupEstadosAsesor.removeAllViews();

        addFilterChip("Todos", "ALL", "ALL".equals(currentStateFilter));

        Set<String> estados = new LinkedHashSet<>();
        for (AdminAsesorInmobiliaria asesor : source) {
            if (asesor.getEstado() != null && !asesor.getEstado().trim().isEmpty()) {
                estados.add(asesor.getEstado().trim());
            }
        }

        for (String estado : estados) {
            addFilterChip(estado, estado, estado.equalsIgnoreCase(currentStateFilter));
        }

        updateChipSelection(currentStateFilter);
    }

    private void addFilterChip(String label, String value, boolean checked) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setChecked(checked);
        chip.setSingleLine(true);
        chip.setTextSize(12f);
        chip.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        chip.setChipBackgroundColorResource(R.color.neutral_soft_1);
        chip.setChipStrokeColorResource(R.color.brand_deep_blue);
        chip.setChipStrokeWidth(1f);
        chip.setOnClickListener(v -> {
            currentStateFilter = value;
            updateChipSelection(value);
            renderList();
        });
        chipGroupEstadosAsesor.addView(chip);
    }

    private void updateChipSelection(String selectedValue) {
        for (int i = 0; i < chipGroupEstadosAsesor.getChildCount(); i++) {
            View child = chipGroupEstadosAsesor.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String label = chip.getText() == null ? "" : chip.getText().toString();
                if ("Todos".equals(label)) {
                    chip.setChecked("ALL".equals(selectedValue));
                } else {
                    chip.setChecked(label.equalsIgnoreCase(selectedValue));
                }
            }
        }
    }

    private void renderList() {
        List<AdminAsesorInmobiliaria> filtered = new ArrayList<>();
        String query = normalize(currentQuery);

        for (AdminAsesorInmobiliaria asesor : source) {
            if (!matchesState(asesor) || !matchesQuery(asesor, query)) {
                continue;
            }
            filtered.add(asesor);
        }

        adapter.setData(filtered);
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private boolean matchesState(AdminAsesorInmobiliaria asesor) {
        if ("ALL".equals(currentStateFilter)) {
            return true;
        }
        String estado = asesor.getEstado() == null ? "" : asesor.getEstado().trim();
        return estado.equalsIgnoreCase(currentStateFilter);
    }

    private boolean matchesQuery(AdminAsesorInmobiliaria asesor, String query) {
        if (query.isEmpty()) {
            return true;
        }
        String nombre = normalize(asesor.getNombre());
        return nombre.contains(query);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }
}
