package com.example.bitbusters.activities.cliente;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.SearchResultAdapter;
import com.example.bitbusters.models.Proyecto;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etBuscar;
    private TextView tvResultados;
    private LinearLayout layoutVacio;
    private RecyclerView rvResultados;
    private SearchResultAdapter adapter;

    // Estado de filtros activos
    private int    filtroPrecio = 0;     // 0=Todos, 1=<200k, 2=200k–300k, 3=>300k
    private String filtroTipo   = null;  // null=Todos, o "Departamento"/"Casa"/"Terreno"

    // Modo especial: mostrar todos sin necesidad de query de texto
    private boolean modoTodos = false;

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

        // Volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón de filtros — muestra BottomSheet con opciones de precio y tipo
        findViewById(R.id.btnFiltros).setOnClickListener(v -> mostrarFiltros());

        // Cambiar entre vista grid y lista
        findViewById(R.id.btnVistaGrid).setOnClickListener(v ->
                rvResultados.setLayoutManager(new GridLayoutManager(this, 2)));
        findViewById(R.id.btnVistaLista).setOnClickListener(v ->
                rvResultados.setLayoutManager(new LinearLayoutManager(this)));

        // Búsqueda en tiempo real al escribir
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                modoTodos = false;
                ejecutarBusqueda();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etBuscar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ejecutarBusqueda();
                return true;
            }
            return false;
        });

        findViewById(R.id.btnBuscar).setOnClickListener(v -> ejecutarBusqueda());

        // Leer intent: mostrar_todos (desde HomeActivity) o query inicial
        modoTodos = getIntent().getBooleanExtra("mostrar_todos", false);
        String queryInicial = getIntent().getStringExtra("query");
        if (modoTodos) {
            ejecutarBusqueda();
        } else if (queryInicial != null && !queryInicial.isEmpty()) {
            etBuscar.setText(queryInicial);
            ejecutarBusqueda();
        } else {
            mostrarVacio(0);
        }
    }

    // ── Búsqueda y filtrado ────────────────────────────────────────────────────

    /** Aplica la query de texto + los filtros activos de precio y tipo. */
    private void ejecutarBusqueda() {
        String query = etBuscar.getText().toString().trim();

        // Si no hay query, no hay filtros y no es modo "todos", mostrar estado vacío
        if (query.isEmpty() && !modoTodos && filtroTipo == null && filtroPrecio == 0) {
            mostrarVacio(0);
            return;
        }

        List<Proyecto> resultados = new ArrayList<>();
        for (Proyecto p : todosLosProyectos) {
            if (coincideConFiltros(p, query)) resultados.add(p);
        }

        if (resultados.isEmpty()) {
            mostrarVacio(0);
        } else {
            mostrarResultados(resultados);
        }
    }

    private boolean coincideConFiltros(Proyecto p, String query) {
        // Filtro de texto (vacío = todo coincide cuando modoTodos está activo)
        boolean matchTexto = query.isEmpty()
                || p.nombre.toLowerCase().contains(query.toLowerCase())
                || p.ubicacion.toLowerCase().contains(query.toLowerCase())
                || (p.tipo != null && p.tipo.toLowerCase().contains(query.toLowerCase()));

        // Filtro de tipo (null = todos los tipos)
        boolean matchTipo = filtroTipo == null || filtroTipo.equals(p.tipo);

        // Filtro de precio
        boolean matchPrecio = (filtroPrecio == 0) || coincideConFiltroPrecio(p.precio, filtroPrecio);

        return matchTexto && matchTipo && matchPrecio;
    }

    private boolean coincideConFiltroPrecio(String precioStr, int filtro) {
        int precio = parsearPrecio(precioStr);
        if (filtro == 1) return precio < 200_000;
        if (filtro == 2) return precio >= 200_000 && precio <= 300_000;
        if (filtro == 3) return precio > 300_000;
        return true;
    }

    /** Extrae el valor numérico de un precio con formato "S/. 280,000". */
    private int parsearPrecio(String precio) {
        if (precio == null || precio.isEmpty()) return 0;
        String soloDigitos = precio.replaceAll("[^0-9]", "");
        if (soloDigitos.isEmpty()) return 0;
        try {
            return Integer.parseInt(soloDigitos);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ── BottomSheet de filtros ─────────────────────────────────────────────────

    private void mostrarFiltros() {
        BottomSheetDialog bsd = new BottomSheetDialog(this);

        // Contenedor principal con scroll vertical
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(dp(24), dp(24), dp(24), dp(48));

        // ── Título ──
        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("Filtrar proyectos");
        tvTitulo.setTextSize(18f);
        tvTitulo.setTypeface(null, Typeface.BOLD);
        tvTitulo.setTextColor(0xFF1A1A2E);
        LinearLayout.LayoutParams tituloParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tituloParams.bottomMargin = dp(24);
        tvTitulo.setLayoutParams(tituloParams);
        root.addView(tvTitulo);

        // ── Sección Precio ──
        root.addView(crearSeccionLabel("Precio"));

        // Variables locales para capturar la selección dentro del BottomSheet
        final int[] precioLocal = {filtroPrecio};
        final String[] precioLabels = {"Todos", "< S/200k", "S/200k – S/300k", "> S/300k"};
        final int[]    precioVals   = {0, 1, 2, 3};
        final TextView[] chipsPrecio = new TextView[4];

        HorizontalScrollView hsvPrecio = new HorizontalScrollView(this);
        hsvPrecio.setHorizontalScrollBarEnabled(false);
        LinearLayout rowPrecio = new LinearLayout(this);
        rowPrecio.setOrientation(LinearLayout.HORIZONTAL);
        rowPrecio.setPadding(0, dp(12), 0, dp(24));
        for (int i = 0; i < precioLabels.length; i++) {
            final int idx = i;
            TextView chip = crearChip(precioLabels[i], precioLocal[0] == precioVals[i]);
            chipsPrecio[i] = chip;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(10));
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                precioLocal[0] = precioVals[idx];
                for (int j = 0; j < chipsPrecio.length; j++) {
                    actualizarChip(chipsPrecio[j], precioLocal[0] == precioVals[j]);
                }
            });
            rowPrecio.addView(chip);
        }
        hsvPrecio.addView(rowPrecio);
        root.addView(hsvPrecio);

        // ── Sección Tipo ──
        root.addView(crearSeccionLabel("Tipo de propiedad"));

        final String[] tipoLocal = {filtroTipo};
        final String[] tipoLabels = {"Todos", "Departamento", "Casa", "Terreno"};
        final String[] tipoVals   = {null, "Departamento", "Casa", "Terreno"};
        final TextView[] chipsTipo = new TextView[4];

        HorizontalScrollView hsvTipo = new HorizontalScrollView(this);
        hsvTipo.setHorizontalScrollBarEnabled(false);
        LinearLayout rowTipo = new LinearLayout(this);
        rowTipo.setOrientation(LinearLayout.HORIZONTAL);
        rowTipo.setPadding(0, dp(12), 0, dp(36));
        for (int i = 0; i < tipoLabels.length; i++) {
            final int idx = i;
            boolean seleccionado = (tipoLocal[0] == null && tipoVals[i] == null)
                    || (tipoLocal[0] != null && tipoLocal[0].equals(tipoVals[i]));
            TextView chip = crearChip(tipoLabels[i], seleccionado);
            chipsTipo[i] = chip;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(10));
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                tipoLocal[0] = tipoVals[idx];
                for (int j = 0; j < chipsTipo.length; j++) {
                    boolean sel = (tipoLocal[0] == null && tipoVals[j] == null)
                            || (tipoLocal[0] != null && tipoLocal[0].equals(tipoVals[j]));
                    actualizarChip(chipsTipo[j], sel);
                }
            });
            rowTipo.addView(chip);
        }
        hsvTipo.addView(rowTipo);
        root.addView(hsvTipo);

        // ── Botón Aplicar ──
        TextView btnAplicar = new TextView(this);
        LinearLayout.LayoutParams applyLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        btnAplicar.setLayoutParams(applyLp);
        btnAplicar.setText("Aplicar filtros");
        btnAplicar.setTextSize(15f);
        btnAplicar.setTypeface(null, Typeface.BOLD);
        btnAplicar.setTextColor(Color.WHITE);
        btnAplicar.setGravity(Gravity.CENTER);
        GradientDrawable bgAplicar = new GradientDrawable();
        bgAplicar.setShape(GradientDrawable.RECTANGLE);
        bgAplicar.setCornerRadius(dp(26));
        bgAplicar.setColor(0xFF1A7EBD);
        btnAplicar.setBackground(bgAplicar);
        btnAplicar.setOnClickListener(v -> {
            // Aplicar selección al estado del Activity
            filtroPrecio = precioLocal[0];
            filtroTipo   = tipoLocal[0];
            bsd.dismiss();
            // Indicar visualmente si hay filtros activos
            actualizarIndicadorFiltros();
            ejecutarBusqueda();
        });
        root.addView(btnAplicar);

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        bsd.setContentView(sv);
        bsd.show();
    }

    /** Cambia el tinte del botón de filtros cuando hay filtros activos. */
    private void actualizarIndicadorFiltros() {
        android.view.View btnFiltros = findViewById(R.id.btnFiltros);
        if (btnFiltros == null) return;
        boolean hayFiltros = filtroTipo != null || filtroPrecio != 0;
        btnFiltros.setBackgroundTintList(hayFiltros
                ? android.content.res.ColorStateList.valueOf(0xFF1A7EBD)
                : null);
    }

    // ── Helpers visuales de chips ──────────────────────────────────────────────

    private TextView crearSeccionLabel(String texto) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(14f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(0xFF1A1A2E);
        return tv;
    }

    private TextView crearChip(String texto, boolean seleccionado) {
        TextView chip = new TextView(this);
        chip.setText(texto);
        chip.setTextSize(13f);
        chip.setPadding(dp(16), dp(8), dp(16), dp(8));
        actualizarChip(chip, seleccionado);
        return chip;
    }

    private void actualizarChip(TextView chip, boolean seleccionado) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(20));
        if (seleccionado) {
            bg.setColor(0xFF1A7EBD);
            bg.setStroke(dp(2), 0xFF1A7EBD);
            chip.setTextColor(Color.WHITE);
        } else {
            bg.setColor(Color.WHITE);
            bg.setStroke(dp(2), 0xFFBDBDBD);
            chip.setTextColor(0xFF757575);
        }
        chip.setBackground(bg);
    }

    /** Convierte dp a píxeles usando la densidad de la pantalla. */
    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // ── Estado vacío / con resultados ──────────────────────────────────────────

    private void mostrarVacio(int cantidad) {
        tvResultados.setText("Encontrado " + cantidad + " respuestas");
        layoutVacio.setVisibility(android.view.View.VISIBLE);
        rvResultados.setVisibility(android.view.View.GONE);
    }

    private void mostrarResultados(List<Proyecto> resultados) {
        tvResultados.setText("Encontrado " + resultados.size() + " respuestas");
        layoutVacio.setVisibility(android.view.View.GONE);
        rvResultados.setVisibility(android.view.View.VISIBLE);
        adapter.setData(resultados);
    }
}
