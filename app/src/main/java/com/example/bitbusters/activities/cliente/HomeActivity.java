package com.example.bitbusters.activities.cliente;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.bitbusters.R;
public class HomeActivity extends AppCompatActivity {

    private static final String EXTRA_PROYECTO = "proyecto";

    private TextView btnTodos;
    private TextView btnTipo1;
    private TextView btnTipo2;
    private TextView btnTipo3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnTodos = findViewById(R.id.btnTodos);
        btnTipo1 = findViewById(R.id.btnTipo1);
        btnTipo2 = findViewById(R.id.btnTipo2);
        btnTipo3 = findViewById(R.id.btnTipo3);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Filtros de tipo
        View.OnClickListener filtroListener = v -> {
            resetFiltros();
            v.setBackgroundResource(R.drawable.bg_filter_selected);
            ((TextView) v).setTextColor(getResources().getColor(android.R.color.white, getTheme()));
        };
        btnTodos.setOnClickListener(filtroListener);
        btnTipo1.setOnClickListener(filtroListener);
        btnTipo2.setOnClickListener(filtroListener);
        btnTipo3.setOnClickListener(filtroListener);

        // Cards destacados
        findViewById(R.id.cardDestacado1).setOnClickListener(v -> abrirDetalle("Catalina Ventor"));
        findViewById(R.id.cardDestacado2).setOnClickListener(v -> abrirDetalle("Residencial Park"));

        // Cards guardados
        findViewById(R.id.cardTorreMiramar).setOnClickListener(v -> abrirDetalle("Torre Miramar"));
        findViewById(R.id.cardResidencialPark).setOnClickListener(v -> abrirDetalle("Residencial El Park"));
        findViewById(R.id.cardCondominioLomas).setOnClickListener(v -> abrirDetalle("Condominio Las Lomas"));
        findViewById(R.id.cardCatalinaSky).setOnClickListener(v -> abrirDetalle("Catalina Sky"));

        // Ver todos
        findViewById(R.id.tvVerTodos).setOnClickListener(v -> {
            // TODO: ir a lista completa de proyectos
        });

        // Notificaciones
        findViewById(R.id.btnNotificaciones).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class))
        );
        findViewById(R.id.etBuscar).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));

        // Bottom Navigation
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (id == R.id.nav_favoritos) {
                // TODO: ir a favoritos
                return true;
            } else if (id == R.id.nav_perfil) {
                // TODO: ir a perfil
                return true;
            }
            return false;
        });
    }

    private void resetFiltros() {
        btnTodos.setBackgroundResource(R.drawable.bg_filter_unselected);
        btnTipo1.setBackgroundResource(R.drawable.bg_filter_unselected);
        btnTipo2.setBackgroundResource(R.drawable.bg_filter_unselected);
        btnTipo3.setBackgroundResource(R.drawable.bg_filter_unselected);
        int grisColor = getResources().getColor(android.R.color.darker_gray, getTheme());
        btnTodos.setTextColor(grisColor);
        btnTipo1.setTextColor(grisColor);
        btnTipo2.setTextColor(grisColor);
        btnTipo3.setTextColor(grisColor);
    }

    private void abrirDetalle(String nombreProyecto) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(EXTRA_PROYECTO, nombreProyecto);
        startActivity(intent);
    }
}