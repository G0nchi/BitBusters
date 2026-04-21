package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ChatsAdapter;
import com.example.bitbusters.models.Chat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MensajesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatsAdapter adapter;
    private List<Chat> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes);

        recyclerView = findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadDummyChats();

        adapter = new ChatsAdapter(chatList, chat -> {
            Intent intent = new Intent(this, ConversacionActivity.class);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        setupSwipeToDelete();
        setupBottomNav();
    }

    private void loadDummyChats() {
        chatList = new ArrayList<>();
        chatList.add(new Chat("1", "Carlos Mendoza", "¿Podemos confirmar la cita del lunes?", "10:32", "CM", "#4DB6AC", 2, true));
        chatList.add(new Chat("2", "Rosa Torres", "Muchas gracias por la atención 🙏", "9:15", "RT", "#F06292", 1, true));
        chatList.add(new Chat("3", "Ana López", "Perfecto, quedamos el martes entonces", "Ayer", "AL", "#FF8A65", 0, true));
        chatList.add(new Chat("4", "Marco Paredes", "¿El departamento tiene estacionamiento?", "Ayer", "MP", "#9575CD", 0, true));
        chatList.add(new Chat("5", "Jorge Castro", "Gracias, lo voy a pensar con mi familia", "Lun", "JC", "#BDBDBD", 0, false));
        chatList.add(new Chat("6", "Sandra Vega", "¿Pueden enviarme los planos del dpto 4…", "Dom", "SV", "#BDBDBD", 0, false));
        chatList.add(new Chat("7", "Luis Vargas", "Ok, reagendamos para la próxima semana", "Sáb", "LV", "#BDBDBD", 0, false));
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

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_chat);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class));
                finish();
            } else if (id == R.id.nav_citas) {
                startActivity(new Intent(this, CitasAgendadasActivity.class));
                finish();
            }
            return true;
        });
    }
}
