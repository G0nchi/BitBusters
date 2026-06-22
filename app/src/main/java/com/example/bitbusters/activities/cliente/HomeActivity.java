package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;
import com.example.bitbusters.R;
import com.example.bitbusters.activities.common.EscanearQRActivity;
import android.util.Log;
import android.widget.Toast;
import com.example.bitbusters.adapters.ProyectoAdapter;
import com.example.bitbusters.models.Proyecto;
import com.example.bitbusters.repository.ProyectoRepository;
import com.example.bitbusters.utils.ImageUrls;
import com.example.bitbusters.utils.NotificationHelper;
import com.example.bitbusters.utils.PreferencesManager;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final String EXTRA_PROYECTO = "proyecto";

    private TextView tvSaludoNombre;
    private TextView btnTodos, btnTipo1, btnTipo2, btnTipo3;
    private RecyclerView rvProyectos;
    private ProyectoAdapter adapter;
    private List<Proyecto> todaLaLista;

    private ProyectoRepository proyectoRepository;
    private ListenerRegistration listenerProyectos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Crear el canal de notificaciones (solo tiene efecto en Android 8+)
        NotificationHelper.crearCanal(this);
        // Solicitar permiso POST_NOTIFICATIONS en Android 13+
        NotificationHelper.solicitarPermiso(this);

        // Leer el nombre guardado en SharedPreferences y mostrar el saludo
        tvSaludoNombre = findViewById(R.id.tvSaludoNombre);
        String nombre = PreferencesManager.obtenerNombre(this);
        if (tvSaludoNombre != null) {
            tvSaludoNombre.setText("¡Hola, " + nombre + "!");
        }

        // Imágenes destacados
        ImageView imgDestacado1 = findViewById(R.id.imgDestacado1);
        if (imgDestacado1 != null)
            Glide.with(this).load(ImageUrls.PROYECTO_CATALINA_VENTOR).centerCrop().into(imgDestacado1);

        ImageView imgDestacado2 = findViewById(R.id.imgDestacado2);
        if (imgDestacado2 != null)
            Glide.with(this).load(ImageUrls.PROYECTO_RESIDENCIAL_PARK).centerCrop().into(imgDestacado2);

        // Proyectos guardados
        cargarImagenProyecto(R.id.imgTorreMiramar,    ImageUrls.PROYECTO_TORRE_MIRAMAR);
        cargarImagenProyecto(R.id.imgResidencialPark, ImageUrls.PROYECTO_RESIDENCIAL_ELPARK);
        cargarImagenProyecto(R.id.imgCondominioLomas, ImageUrls.PROYECTO_CONDOMINIO_LOMAS);
        cargarImagenProyecto(R.id.imgCatalinaSky,     ImageUrls.PROYECTO_CATALINA_SKY);

        // Avatar perfil
        ImageView imgPerfil = findViewById(R.id.imgPerfil);
        if (imgPerfil != null) {
            Glide.with(this).load(ImageUrls.AVATAR_JONATHAN).centerCrop().into(imgPerfil);
            imgPerfil.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));
        }

        // Referencias a los botones de filtro
        btnTodos = findViewById(R.id.btnTodos);
        btnTipo1 = findViewById(R.id.btnTipo1);
        btnTipo2 = findViewById(R.id.btnTipo2);
        btnTipo3 = findViewById(R.id.btnTipo3);

        // RecyclerView — lista vacía al inicio; Firestore la puebla en onStart
        rvProyectos  = findViewById(R.id.rvProyectos);
        todaLaLista  = new ArrayList<>();
        adapter      = new ProyectoAdapter(this, new ArrayList<>());
        rvProyectos.setLayoutManager(new LinearLayoutManager(this));
        rvProyectos.setAdapter(adapter);

        proyectoRepository = new ProyectoRepository();

        // Filtro Todos — guarda preferencia y aplica vista completa
        btnTodos.setOnClickListener(v -> {
            PreferencesManager.guardarTipologiaFavorita(this, "Todos");
            aplicarFiltro("Todos");
        });

        // Filtro Tipo 1 → Departamento
        btnTipo1.setOnClickListener(v -> {
            PreferencesManager.guardarTipologiaFavorita(this, "Departamento");
            aplicarFiltro("Departamento");
        });

        // Filtro Tipo 2 → Casa
        btnTipo2.setOnClickListener(v -> {
            PreferencesManager.guardarTipologiaFavorita(this, "Casa");
            aplicarFiltro("Casa");
        });

        // Filtro Tipo 3 → Terreno
        btnTipo3.setOnClickListener(v -> {
            PreferencesManager.guardarTipologiaFavorita(this, "Terreno");
            aplicarFiltro("Terreno");
        });

        // "Ver todos" en la sección Destacados → SearchActivity mostrando todos los proyectos
        android.view.View tvVerTodos = findViewById(R.id.tvVerTodos);
        if (tvVerTodos != null) {
            tvVerTodos.setOnClickListener(v -> {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("mostrar_todos", true);
                startActivity(intent);
            });
        }

        // Cards destacados
        findViewById(R.id.cardDestacado1).setOnClickListener(v -> abrirDetalle("Catalina Ventor"));
        findViewById(R.id.cardDestacado2).setOnClickListener(v -> abrirDetalle("Residencial Park"));

        // Cards guardados
        findViewById(R.id.cardTorreMiramar).setOnClickListener(v -> abrirDetalle("Torre Miramar"));
        findViewById(R.id.cardResidencialPark).setOnClickListener(v -> abrirDetalle("Residencial El Park"));
        findViewById(R.id.cardCondominioLomas).setOnClickListener(v -> abrirDetalle("Condominio Las Lomas"));
        findViewById(R.id.cardCatalinaSky).setOnClickListener(v -> abrirDetalle("Catalina Sky"));

        // Notificaciones
        findViewById(R.id.btnNotificaciones).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        // Búsqueda
        findViewById(R.id.etBuscar).setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        // FAB Escanear QR
        com.google.android.material.floatingactionbutton.FloatingActionButton fabQR =
                findViewById(R.id.fabEscanearQR);
        if (fabQR != null) {
            fabQR.setOnClickListener(v -> {
                Intent intent = new Intent(this, EscanearQRActivity.class);
                intent.putExtra(EscanearQRActivity.EXTRA_ROL, EscanearQRActivity.ROL_CLIENTE);
                startActivity(intent);
            });
        }

        // Bottom Navigation
        LinearLayout navSearch   = findViewById(R.id.navSearch);
        LinearLayout navCitas    = findViewById(R.id.navCitas);
        LinearLayout navPerfil   = findViewById(R.id.navPerfil);

        navSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        navCitas.setOnClickListener(v  -> startActivity(new Intent(this, MisCitasActivity.class)));
        navPerfil.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenerProyectos = proyectoRepository.escucharProyectosCliente(
            new ProyectoRepository.ProyectosListener() {
                @Override
                public void onProyectosActualizados(List<Proyecto> proyectos) {
                    todaLaLista.clear();
                    todaLaLista.addAll(proyectos);
                    String filtroActual = PreferencesManager.obtenerTipologiaFavorita(HomeActivity.this);
                    aplicarFiltro(filtroActual != null ? filtroActual : "Todos");
                }

                @Override
                public void onError(String mensaje) {
                    Log.e("HomeActivity", "Error cargando proyectos: " + mensaje);
                    Toast.makeText(HomeActivity.this,
                        "Error al cargar proyectos: " + mensaje, Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listenerProyectos != null) {
            listenerProyectos.remove();
            listenerProyectos = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restaurar el último filtro seleccionado al volver a esta pantalla
        String tipologiaGuardada = PreferencesManager.obtenerTipologiaFavorita(this);
        aplicarFiltro(tipologiaGuardada);
    }

    /**
     * Aplica el filtro indicado: actualiza estado visual de botones,
     * lista del RecyclerView e imágenes de los cards.
     */
    private void aplicarFiltro(String tipologia) {
        resetFiltros();
        int blanco = getResources().getColor(android.R.color.white, getTheme());

        switch (tipologia) {
            case "Departamento":
                btnTipo1.setBackgroundResource(R.drawable.bg_filter_selected);
                btnTipo1.setTextColor(blanco);
                filtrar("Departamento");
                actualizarDestacados(
                    "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=400",
                    "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=400");
                actualizarGuardados(
                    "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=400",
                    "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=400",
                    "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=400",
                    "https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=400");
                break;

            case "Casa":
                btnTipo2.setBackgroundResource(R.drawable.bg_filter_selected);
                btnTipo2.setTextColor(blanco);
                filtrar("Casa");
                actualizarDestacados(
                    "https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400",
                    "https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=400");
                actualizarGuardados(
                    "https://images.unsplash.com/photo-1523217582562-09d0def993a6?w=400",
                    "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=400",
                    "https://images.unsplash.com/photo-1600585154526-990dced4db0d?w=400",
                    "https://images.unsplash.com/photo-1576941089067-2de3c901e126?w=400");
                break;

            case "Terreno":
                btnTipo3.setBackgroundResource(R.drawable.bg_filter_selected);
                btnTipo3.setTextColor(blanco);
                filtrar("Terreno");
                actualizarDestacados(
                    "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=400",
                    "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?w=400");
                actualizarGuardados(
                    "https://images.unsplash.com/photo-1462275646964-a0e3386b89fa?w=400",
                    "https://images.unsplash.com/photo-1446329813274-7c9036bd9a1f?w=400",
                    "https://images.unsplash.com/photo-1588421357574-87938a86fa28?w=400",
                    "https://images.unsplash.com/photo-1473773508845-188df298d2d1?w=400");
                break;

            default: // "Todos"
                btnTodos.setBackgroundResource(R.drawable.bg_filter_selected);
                btnTodos.setTextColor(blanco);
                adapter.setData(new ArrayList<>(todaLaLista));
                restaurarImagenesOriginales();
                break;
        }
    }

    private void filtrar(String tipo) {
        List<Proyecto> filtrados = new ArrayList<>();
        for (Proyecto p : todaLaLista) {
            if (p.tipo != null && p.tipo.equals(tipo))
                filtrados.add(p);
        }
        adapter.setData(filtrados);
    }

    private void resetFiltros() {
        int gris = getResources().getColor(android.R.color.darker_gray, getTheme());
        btnTodos.setBackgroundResource(R.drawable.bg_filter_unselected); btnTodos.setTextColor(gris);
        btnTipo1.setBackgroundResource(R.drawable.bg_filter_unselected); btnTipo1.setTextColor(gris);
        btnTipo2.setBackgroundResource(R.drawable.bg_filter_unselected); btnTipo2.setTextColor(gris);
        btnTipo3.setBackgroundResource(R.drawable.bg_filter_unselected); btnTipo3.setTextColor(gris);
    }

    private void abrirDetalle(String nombreProyecto) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(EXTRA_PROYECTO, nombreProyecto);
        startActivity(intent);
    }

    private void cargarImagenProyecto(int imgId, int drawableId) {
        ImageView img = findViewById(imgId);
        if (img != null)
            Glide.with(this).load(drawableId).centerCrop().into(img);
    }

    private void cargarUrl(int imgId, String url) {
        ImageView img = findViewById(imgId);
        if (img != null)
            Glide.with(this).load(url).centerCrop().into(img);
    }

    private void actualizarDestacados(String url1, String url2) {
        cargarUrl(R.id.imgDestacado1, url1);
        cargarUrl(R.id.imgDestacado2, url2);
    }

    private void actualizarGuardados(String urlG1, String urlG2, String urlG3, String urlG4) {
        cargarUrl(R.id.imgTorreMiramar,    urlG1);
        cargarUrl(R.id.imgResidencialPark, urlG2);
        cargarUrl(R.id.imgCondominioLomas, urlG3);
        cargarUrl(R.id.imgCatalinaSky,     urlG4);
    }

    private void restaurarImagenesOriginales() {
        ImageView d1 = findViewById(R.id.imgDestacado1);
        if (d1 != null) Glide.with(this).load(ImageUrls.PROYECTO_CATALINA_VENTOR).centerCrop().into(d1);
        ImageView d2 = findViewById(R.id.imgDestacado2);
        if (d2 != null) Glide.with(this).load(ImageUrls.PROYECTO_RESIDENCIAL_PARK).centerCrop().into(d2);
        cargarImagenProyecto(R.id.imgTorreMiramar,    ImageUrls.PROYECTO_TORRE_MIRAMAR);
        cargarImagenProyecto(R.id.imgResidencialPark, ImageUrls.PROYECTO_RESIDENCIAL_ELPARK);
        cargarImagenProyecto(R.id.imgCondominioLomas, ImageUrls.PROYECTO_CONDOMINIO_LOMAS);
        cargarImagenProyecto(R.id.imgCatalinaSky,     ImageUrls.PROYECTO_CATALINA_SKY);
    }
}
