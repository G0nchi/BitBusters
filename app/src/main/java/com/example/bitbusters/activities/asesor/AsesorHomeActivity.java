package com.example.bitbusters.activities.asesor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;

import com.example.bitbusters.R;
import com.example.bitbusters.activities.access.LoginActivity;
import com.example.bitbusters.activities.common.EscanearQRActivity;
import com.example.bitbusters.databinding.ActivityAsesorHomeBinding;
import com.example.bitbusters.models.ProyectoApi;
import com.example.bitbusters.utils.ApiClient;
import com.example.bitbusters.utils.AsesorNotificationHelper;
import com.example.bitbusters.utils.AuthHelper;
import com.example.bitbusters.utils.AsesorStorage;
import com.example.bitbusters.utils.BitBustersApiService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsesorHomeActivity extends AppCompatActivity {

    private ActivityAsesorHomeBinding binding;
    private ProyectoAdapter proyectoAdapter;
    private MaterialButton chipTodos, chipDepartamentos, chipVillas;
    private TextView badgeCampana;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAsesorHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AsesorNotificationHelper.createChannel(this);
        requestNotifPermission();
        cargarAsesorId();
        setupRecyclerView();
        setupChips();
        setupQuickActions();
        setupBottomNav();

        View imgPerfil = binding.imgPerfilAsesor;
        if (imgPerfil != null) {
            imgPerfil.setOnClickListener(v -> showProfileMenu(v));
        }
    }

    /**
     * Lee el campo "asesorId" del documento del asesor en Firestore (colección "users")
     * y lo guarda en SharedPreferences para usarlo en el chat.
     * Si el campo no existe, usa el Firebase Auth UID como fallback.
     */
    private void cargarAsesorId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String cachedId = AsesorStorage.getAsesorId(this);
        if (cachedId != null) return; // ya guardado

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(doc -> {
                String asesorId = null;
                if (doc.exists()) {
                    asesorId = doc.getString("asesorId");
                }
                if (asesorId == null || asesorId.isEmpty()) {
                    asesorId = user.getUid(); // fallback: usar UID de Firebase Auth
                }
                AsesorStorage.saveAsesorId(this, asesorId);
            })
            .addOnFailureListener(e ->
                AsesorStorage.saveAsesorId(this, user.getUid()));
    }

    private void showProfileMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Cerrar Sesión");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Cerrar Sesión")) {
                logout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void logout() {
        AsesorStorage.clearAll(this);
        AuthHelper.cerrarSesion(); // cierra también la sesión de Firebase Authentication (Clase 10)
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void requestNotifPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS}, 102);
        }
    }

    private void setupRecyclerView() {
        if (binding.rvProyectos != null) {
            binding.rvProyectos.setLayoutManager(new LinearLayoutManager(this));
            binding.rvProyectos.setNestedScrollingEnabled(false);
            proyectoAdapter = new ProyectoAdapter(position -> {
                Intent intent = new Intent(this, ProyectoDetalleActivity.class);
                intent.putExtra(ProyectoDetalleActivity.EXTRA_PROYECTO_INDEX, position);
                startActivity(intent);
            });
            binding.rvProyectos.setAdapter(proyectoAdapter);
            loadProyectosFromApi();
        }
    }

    /**
     * Carga proyectos desde la API (Retrofit + MockInterceptor en esta fase).
     * Callback en hilo principal: actualiza el adapter con DiffUtil al recibir datos.
     */
    private void loadProyectosFromApi() {
        BitBustersApiService api = ApiClient.getApiService();
        api.getAllProyectos().enqueue(new Callback<List<ProyectoApi>>() {
            @Override
            public void onResponse(Call<List<ProyectoApi>> call,
                                   Response<List<ProyectoApi>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    proyectoAdapter.setProyectos(response.body());
                    // Reaplicar filtro activo (chip seleccionado)
                    if (proyectoAdapter != null) {
                        proyectoAdapter.applyFilter(AsesorStorage.getHomeFilter(
                            AsesorHomeActivity.this));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ProyectoApi>> call, Throwable t) {
                Log.e("BitBusters", "Error al cargar proyectos: " + t.getMessage());
                // La lista queda vacía; en producción se mostraría un estado de error
            }
        });
    }

    private void setupChips() {
        chipTodos = binding.chipTodos;
        chipDepartamentos = binding.chipDepartamentos;
        chipVillas = binding.chipVillas;
        if (chipTodos == null) return;
        chipTodos.setOnClickListener(v -> activateChip("Todos"));
        chipDepartamentos.setOnClickListener(v -> activateChip("Departamento"));
        chipVillas.setOnClickListener(v -> activateChip("Villa"));

        // Aplica el estado inicial del chip activo
        activateChip("Todos");
    }

    private void activateChip(String tipo) {
        int activeColor   = getColor(R.color.brand_lime);
        int inactiveColor = android.graphics.Color.TRANSPARENT;
        int activeText    = android.graphics.Color.WHITE;
        int inactiveText  = getColor(R.color.text_secondary);

        boolean isTodos = "Todos".equals(tipo);
        boolean isDept = "Departamento".equals(tipo);
        boolean isVilla = "Villa".equals(tipo);

        chipTodos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isTodos ? activeColor : inactiveColor));
        chipTodos.setTextColor(isTodos ? activeText : inactiveText);
        chipDepartamentos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isDept ? activeColor : inactiveColor));
        chipDepartamentos.setTextColor(isDept ? activeText : inactiveText);
        chipVillas.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isVilla ? activeColor : inactiveColor));
        chipVillas.setTextColor(isVilla ? activeText : inactiveText);

        if (proyectoAdapter != null) proyectoAdapter.applyFilter(tipo);
        AsesorStorage.saveHomeFilter(this, tipo);
    }

    private void setupQuickActions() {
        if (binding.cardMapa != null) {
            binding.cardMapa.setOnClickListener(v ->
                startActivity(new Intent(this, AsesorMapaActivity.class)));
        }

        if (binding.cardOfertas != null) {
            binding.cardOfertas.setOnClickListener(v ->
                startActivity(new Intent(this, AsesorOfertasActivity.class)));
        }

        if (binding.cardMisCitas != null) {
            binding.cardMisCitas.setOnClickListener(v ->
                startActivity(new Intent(this, CitasAgendadasActivity.class)));
        }

        badgeCampana = binding.tvBadgeCampana;

        if (binding.imgCampana != null) {
            binding.imgCampana.setOnClickListener(v ->
                startActivity(new Intent(this, AsesorNotificacionesActivity.class)));
        }

        if (binding.imgQrScan != null) {
            binding.imgQrScan.setOnClickListener(v -> {
                Intent intent = new Intent(this, EscanearQRActivity.class);
                intent.putExtra(EscanearQRActivity.EXTRA_ROL, EscanearQRActivity.ROL_ASESOR);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBadge();
    }

    private void refreshBadge() {
        if (badgeCampana == null) return;
        int count = AsesorStorage.getNotifCount(this);
        if (count > 0) {
            badgeCampana.setVisibility(View.VISIBLE);
            badgeCampana.setText(count > 9 ? "9+" : String.valueOf(count));
        } else {
            badgeCampana.setVisibility(View.GONE);
        }
    }

    private void setupBottomNav() {
        if (binding.bottomNav != null) {
            binding.bottomNav.setSelectedItemId(R.id.nav_inicio);
            binding.bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_citas) {
                    startActivity(new Intent(this, CitasAgendadasActivity.class));
                } else if (id == R.id.nav_chat) {
                    startActivity(new Intent(this, MensajesActivity.class));
                } else if (id == R.id.nav_perfil) {
                    startActivity(new Intent(this, AsesorPerfilActivity.class));
                }
                return true;
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
