package com.example.bitbusters.activities.cliente;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.NotificationsAdapter;
import com.example.bitbusters.models.Notification;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "CLIENTE_NOTIF";

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationsAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        setupSwipeToDelete();
        loadNotificationsFromFirestore();

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {}
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadNotificationsFromFirestore() {
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("role", "cliente")
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(snapshots -> {
                List<Notification> list;
                if (!snapshots.isEmpty()) {
                    list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String senderName  = doc.getString("senderName");
                        String descripcion = doc.getString("descripcion");
                        String tiempo      = doc.getString("tiempo");
                        String avatarName  = doc.getString("avatarName");
                        String propertyName = doc.getString("propertyName");
                        Boolean isOld      = doc.getBoolean("isOld");
                        if (senderName == null) continue;
                        int avatarRes   = resolveDrawable(avatarName);
                        int propertyRes = resolveDrawable(propertyName);
                        list.add(new Notification(
                                doc.getId(),
                                senderName,
                                descripcion  != null ? descripcion  : "",
                                tiempo       != null ? tiempo       : "",
                                avatarRes,
                                propertyRes,
                                isOld != null && isOld));
                    }
                } else {
                    list = defaultNotifications();
                }
                adapter.setData(list);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Firestore fetch failed, using defaults", e);
                adapter.setData(defaultNotifications());
            });
    }

    private int resolveDrawable(String name) {
        if (name == null || name.isEmpty()) return 0;
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }

    private List<Notification> defaultNotifications() {
        List<Notification> list = new ArrayList<>();
        list.add(new Notification("1", "José Daniel",
            "Acaba de aprobar tu separación para el departamento de San Miguel. Tienes 10 minutos para completar el pago",
            "hace 1 hora", R.drawable.avatar_jonathan, R.drawable.proyecto_torre_miramar, false));
        list.add(new Notification("2", "Gerardo",
            "Acaba de aprobar tu separación para el departamento de Santa Eulalia",
            "hace 2 horas", R.drawable.avatar_samuel, R.drawable.proyecto_residencial_park, false));
        list.add(new Notification("3", "Wendy Cuzca",
            "Está revisando tu separación al inmueble.",
            "hace 4 horas", R.drawable.avatar_wendy, 0, false));
        list.add(new Notification("4", "Velma Cole",
            "Está aprobando tu visita al inmueble.",
            "hace 2 días", R.drawable.avatar_velma, R.drawable.proyecto_catalina_sky, true));
        return list;
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                adapter.removeItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }
}
