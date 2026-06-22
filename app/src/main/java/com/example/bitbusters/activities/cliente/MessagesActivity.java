package com.example.bitbusters.activities.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ClienteChatsAdapter;
import com.example.bitbusters.models.Chat;
import com.example.bitbusters.repository.ChatRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChats;
    private ClienteChatsAdapter chatsAdapter;
    private final List<Object> chatItems = new ArrayList<>();

    private ChatRepository chatRepository;
    private ListenerRegistration chatsListener;
    private String uidActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        uidActual = user.getUid();
        chatRepository = new ChatRepository();

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        recyclerViewChats = findViewById(R.id.recyclerViewChats);

        setupRecyclerView();
        setupSwipeToDelete();

        TabLayout.Tab tabMensajes = tabLayout.getTabAt(1);
        if (tabMensajes != null) tabMensajes.select();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            chatItems.clear();
            chatsAdapter.notifyDataSetChanged();
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    startActivity(new Intent(MessagesActivity.this, NotificationsActivity.class));
                    finish();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        iniciarListenerChats();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (chatsListener != null) {
            chatsListener.remove();
            chatsListener = null;
        }
    }

    private void iniciarListenerChats() {
        chatsListener = chatRepository.escucharChatsDelUsuario(uidActual,
            new ChatRepository.ChatsListener() {
                @Override
                public void onChats(List<Chat> chats) {
                    List<Object> nuevosItems = new ArrayList<>(chats);
                    chatsAdapter.updateItems(nuevosItems);
                }

                @Override
                public void onError(String mensaje) {
                    Toast.makeText(MessagesActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setupRecyclerView() {
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        chatsAdapter = new ClienteChatsAdapter(chatItems, this::abrirChat);
        recyclerViewChats.setAdapter(chatsAdapter);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder t) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < chatItems.size()) {
                    chatsAdapter.removeItem(position);
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerViewChats);
    }

    private void abrirChat(Chat chat) {
        String uidAsesor = null;
        if (chat.getParticipantes() != null) {
            for (String uid : chat.getParticipantes()) {
                if (!uid.equals(uidActual)) {
                    uidAsesor = uid;
                    break;
                }
            }
        }
        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("chatId", chat.getChatId());
        intent.putExtra("uidAsesor", uidAsesor);
        intent.putExtra("nombreAsesor", chat.getNombreAsesor());
        intent.putExtra("fotoAsesor", chat.getFotoAsesor());
        startActivity(intent);
    }
}
