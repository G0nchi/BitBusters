package com.example.bitbusters.activities.cliente;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitbusters.R;
import com.example.bitbusters.adapters.SearchResultAdapter;
import com.example.bitbusters.models.Proyecto;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etBuscar;
    private TextView tvResultados;
    private LinearLayout layoutVacio;
    private RecyclerView rvResultados;
    private SearchResultAdapter adapter;

    private final List<Proyecto> todosLosProyectos = new ArrayList<Proyecto>() {{
        add(new Proyecto("Casa Linda",              "S/. 235/mes", "4.7", "La Perla, Callao"));
        add(new Proyecto("Casa Fea",                "S/. 265/mes", "4.9", "La Perla, Callao"));
        add(new Proyecto("Casa Fuapa",              "S/. 271/mes", "4.8", "La Perla, Callao"));
        add(new Proyecto("Casa Goti",               "S/. 322/mes", "4.7", "La Perla, Callao"));
        add(new Proyecto("Torres Unidas",           "S/. 280,000", "4.9", "La Peral, Callao"));
        add(new Proyecto("Catalina Ventor",         "S/. 280,000", "4.9", "Santa Catalina, Lima"));
        add(new Proyecto("Torre Miramar",           "S/. 195,000", "4.9", "Miraflores, Lima"));
        add(new Proyecto("Residencial El Park",     "S/. 248,000", "4.8", "San Miguel, Lima"));
        add(new Proyecto("Condominio Las Lomas",    "S/. 220,000", "4.7", "Surco, Lima"));
        add(new Proyecto("Catalina Sky",            "S/. 280,000", "4.9", "Jakarta, Indonesia"));
        add(new Proyecto("Los Robles",              "S/. 220/mes", "4.8", "La Perla, Callao"));
        add(new Proyecto("Vista Marina Residencial","S/. 310,000", "4.6", "San Miguel, Lima"));
        add(new Proyecto("Torres del Sol",          "S/. 195,000", "4.5", "Miraflores, Lima"));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etBuscar     = findViewById(R.id.etBuscar);
        tvResultados = findViewById(R.id.tvResultados);
        layoutVacio  = findViewById(R.id.layoutVacio);
        rvResultados = findViewById(R.id.rvResultados);

        // RecyclerView en grid 2 columnas por defecto
        adapter = new SearchResultAdapter(this, new ArrayList<>());
        rvResultados.setLayoutManager(new GridLayoutManager(this, 2));
        rvResultados.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnFiltros).setOnClickListener(v -> { });

        // Cambiar vista grid / lista
        findViewById(R.id.btnVistaGrid).setOnClickListener(v ->
                rvResultados.setLayoutManager(new GridLayoutManager(this, 2)));
        findViewById(R.id.btnVistaLista).setOnClickListener(v ->
                rvResultados.setLayoutManager(new LinearLayoutManager(this)));

        // Búsqueda en tiempo real
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                buscar(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etBuscar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                buscar(etBuscar.getText().toString().trim());
                return true;
            }
            return false;
        });

        findViewById(R.id.btnBuscar).setOnClickListener(v ->
                buscar(etBuscar.getText().toString().trim()));

        // Query inicial desde HomeActivity
        String queryInicial = getIntent().getStringExtra("query");
        if (queryInicial != null && !queryInicial.isEmpty()) {
            etBuscar.setText(queryInicial);
            buscar(queryInicial);
        } else {
            mostrarVacio(0);
        }
    }

    private void buscar(String query) {
        if (query.isEmpty()) {
            mostrarVacio(0);
            return;
        }
        List<Proyecto> resultados = new ArrayList<>();
        for (Proyecto p : todosLosProyectos) {
            if (p.nombre.toLowerCase().contains(query.toLowerCase())
                    || p.ubicacion.toLowerCase().contains(query.toLowerCase())) {
                resultados.add(p);
            }
        }
        if (resultados.isEmpty()) {
            mostrarVacio(0);
        } else {
            mostrarResultados(resultados);
        }
    }

    private void mostrarVacio(int cantidad) {
        tvResultados.setText("Encontrado " + cantidad + " respuestas");
        layoutVacio.setVisibility(View.VISIBLE);
        rvResultados.setVisibility(View.GONE);
    }

    private void mostrarResultados(List<Proyecto> resultados) {
        tvResultados.setText("Encontrado " + resultados.size() + " respuestas");
        layoutVacio.setVisibility(View.GONE);
        rvResultados.setVisibility(View.VISIBLE);
        adapter.setData(resultados);
    }
}