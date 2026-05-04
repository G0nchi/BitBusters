package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ChatsAdapter;
import com.example.bitbusters.data.ClientDataRepository;
import com.example.bitbusters.models.Chat;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChats;
    private ChatsAdapter chatsAdapter;
    private final List<Chat> chats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        recyclerViewChats = findViewById(R.id.recyclerViewChats);
        chats.addAll(ClientDataRepository.getChats());

        setupRecyclerView();
        setupSwipeToDelete();

        // Seleccionar el tab Mensajes por defecto (posición 1)
        TabLayout.Tab tabMensajes = tabLayout.getTabAt(1);
        if (tabMensajes != null) tabMensajes.select();

        // Botón volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Botón eliminar todos los chats
        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            chats.clear();
            chatsAdapter.notifyDataSetChanged();
        });

        // Listener de tabs — al tocar Notificaciones vuelve a esa pantalla
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    startActivity(new Intent(MessagesActivity.this, NotificationsActivity.class));
                    finish();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { /* No-op */ }
            @Override public void onTabReselected(TabLayout.Tab tab) { /* No-op */ }
        });
    }

    private void setupRecyclerView() {
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        chatsAdapter = new ChatsAdapter(chats, chat -> abrirChat(chat.getName()));
        recyclerViewChats.setAdapter(chatsAdapter);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < chats.size()) {
                    chats.remove(position);
                    chatsAdapter.notifyItemRemoved(position);
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerViewChats);
    }

    private void abrirChat(String nombreContacto) {
        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("contacto", nombreContacto);
        startActivity(intent);
    }
}