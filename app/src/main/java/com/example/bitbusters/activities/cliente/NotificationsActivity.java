package com.example.bitbusters.activities.cliente;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitbusters.R;
import com.example.bitbusters.adapters.NotificationsAdapter;
import com.example.bitbusters.models.Notification;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        loadDummyNotifications();
        
        adapter = new NotificationsAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        setupSwipeToDelete();

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Lógica para filtrar o cambiar vista
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadDummyNotifications() {
        notificationList = new ArrayList<>();
        notificationList.add(new Notification("1", "José Daniel", "Acaba de aprobar tu separación para el departamento de San Miguel. Tienes 10 minutos para completar el pago", "hace 1 hora", R.drawable.avatar_jonathan, R.drawable.proyecto_torre_miramar, false));
        notificationList.add(new Notification("2", "Gerardo", "Acaba de aprobar tu separación para el departamento de Santa Eulalia", "hace 2 horas", R.drawable.avatar_samuel, R.drawable.proyecto_residencial_park, false));
        notificationList.add(new Notification("3", "Wendy Cuzca", "Está revisando tu separación al inmueble.", "hace 4 horas", R.drawable.avatar_wendy, 0, false));
        notificationList.add(new Notification("4", "Velma Cole", "Está aprobando tu visita al inmueble.", "hace 2 días", R.drawable.avatar_velma, R.drawable.proyecto_catalina_sky, true));
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                adapter.removeItem(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
