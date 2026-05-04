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
        // Departamentos
        add(new Proyecto("Torres Unidas",            "S/. 280,000", "4.9", "La Perla, Callao",      "Departamento", "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400"));
        add(new Proyecto("Catalina Ventor",          "S/. 280,000", "4.9", "Santa Catalina, Lima",  "Departamento", "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=400"));
        add(new Proyecto("Torre Miramar",            "S/. 195,000", "4.9", "Miraflores, Lima",      "Departamento", "https://images.unsplash.com/photo-1574362848149-11496d93a7c7?w=400"));
        add(new Proyecto("Residencial El Park",      "S/. 248,000", "4.8", "San Miguel, Lima",      "Departamento", "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=400"));
        add(new Proyecto("Vista Marina Residencial", "S/. 310,000", "4.6", "San Miguel, Lima",      "Departamento", "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=400"));
        add(new Proyecto("Torres del Sol",           "S/. 195,000", "4.5", "Miraflores, Lima",      "Departamento", "https://images.unsplash.com/photo-1567684014761-b65e2e59b9eb?w=400"));
        // Casas
        add(new Proyecto("Casa Linda",               "S/. 235,000", "4.7", "La Perla, Callao",      "Casa",         "https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=400"));
        add(new Proyecto("Casa Fuapa",               "S/. 271,000", "4.8", "La Perla, Callao",      "Casa",         "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"));
        add(new Proyecto("Casa Goti",                "S/. 322,000", "4.7", "La Perla, Callao",      "Casa",         "https://images.unsplash.com/photo-1523217582562-09d0def993a6?w=400"));
        add(new Proyecto("Los Robles",               "S/. 220,000", "4.8", "La Perla, Callao",      "Casa",         "https://images.unsplash.com/photo-1583608205776-bfd35f0d9f83?w=400"));
        add(new Proyecto("Condominio Las Lomas",     "S/. 220,000", "4.7", "Surco, Lima",           "Casa",         "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"));
        // Terrenos
        add(new Proyecto("Catalina Sky",             "S/. 320,000", "4.9", "Miraflores, Lima",      "Terreno",      "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400"));
        add(new Proyecto("Los Álamos Residencial",   "S/. 180,000", "4.6", "San Miguel, Lima",      "Terreno",      "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400"));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etBuscar     = findViewById(R.id.etBuscar);
        tvResultados = findViewById(R.id.tvResultados);
        layoutVacio  = findViewById(R.id.layoutVacio);
        rvResultados = findViewById(R.id.rvResultados);

        adapter = new SearchResultAdapter(this, new ArrayList<>());
        rvResultados.setLayoutManager(new GridLayoutManager(this, 2));
        rvResultados.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnFiltros).setOnClickListener(v -> { });

        findViewById(R.id.btnVistaGrid).setOnClickListener(v ->
                rvResultados.setLayoutManager(new GridLayoutManager(this, 2)));
        findViewById(R.id.btnVistaLista).setOnClickListener(v ->
                rvResultados.setLayoutManager(new LinearLayoutManager(this)));

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
                    || p.ubicacion.toLowerCase().contains(query.toLowerCase())
                    || (p.tipo != null && p.tipo.toLowerCase().contains(query.toLowerCase()))) {
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