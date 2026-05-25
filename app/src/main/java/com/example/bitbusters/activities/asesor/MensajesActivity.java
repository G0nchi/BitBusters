package com.example.bitbusters.activities.asesor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.ChatsAdapter;
import com.example.bitbusters.databinding.ActivityMensajesBinding;
import com.example.bitbusters.models.Chat;
import com.example.bitbusters.utils.AsesorStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MensajesActivity extends AppCompatActivity {

    private ActivityMensajesBinding binding;
    private ChatsAdapter   adapter;
    private List<Object>   items;

    /** IDs de chats finalizados que el asesor reconectó (persiste en sesión). */
    private final Set<String> reconnectedIds = new HashSet<>();
    /** Chats nuevos creados con el botón "+". */
    private final List<Chat>  newChats       = new ArrayList<>();

    // ── Pool de clientes disponibles para nuevo chat ──────────────────────────
    private static final String[][] AVAILABLE_CLIENTS = {
        {"María Quispe",    "MQ", "#26A69A", "Vista Marina · Dpto 105"},
        {"Roberto Flores",  "RF", "#5C6BC0", "Vista Marina · Dpto 210"},
        {"Elena García",    "EG", "#EC407A", "Parque Norte · Dpto 301"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMensajesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));

        buildItems();

        adapter = new ChatsAdapter(items, chat -> openConversation(chat));
        adapter.setOnReconectarListener(chat -> reconectar(chat));
        binding.recyclerViewChats.setAdapter(adapter);

        setupSearch();
        setupNewChat();
        setupSwipeToDelete();
        setupBottomNav();
    }

    // ── Abrir conversación ────────────────────────────────────────────────────

    private void openConversation(Chat chat) {
        Intent intent = new Intent(this, ConversacionActivity.class);
        intent.putExtra(ConversacionActivity.EXTRA_CHAT_ID,  chat.getId());
        intent.putExtra(ConversacionActivity.EXTRA_NOMBRE,   chat.getName());
        intent.putExtra(ConversacionActivity.EXTRA_INITIALS, chat.getInitials());
        intent.putExtra(ConversacionActivity.EXTRA_COLOR,    chat.getColorHex());
        intent.putExtra(ConversacionActivity.EXTRA_PROYECTO, chat.getProyecto());
        startActivity(intent);
    }

    // ── Búsqueda en tiempo real ───────────────────────────────────────────────

    private void setupSearch() {
        if (binding.etBuscarChat == null) return;
        binding.etBuscarChat.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString().trim());
            }
        });
    }

    private void filterChats(String query) {
        if (query.isEmpty()) {
            buildItems();
            adapter.updateItems(items);
            return;
        }

        buildItems(); // construye la lista completa primero
        String lower = query.toLowerCase(Locale.getDefault());

        List<Object> filtered   = new ArrayList<>();
        String currentSection   = null;
        boolean sectionAdded    = false;

        for (Object obj : items) {
            if (obj instanceof String) {
                currentSection = (String) obj;
                sectionAdded   = false;
            } else if (obj instanceof Chat) {
                Chat chat = (Chat) obj;
                if (chat.getName().toLowerCase(Locale.getDefault()).contains(lower)
                        || (chat.getProyecto() != null
                            && chat.getProyecto().toLowerCase(Locale.getDefault()).contains(lower))) {
                    if (!sectionAdded) {
                        filtered.add(currentSection);
                        sectionAdded = true;
                    }
                    filtered.add(chat);
                }
            }
        }

        items = filtered;
        adapter.updateItems(items);
    }

    // ── Nuevo chat (botón "+") ────────────────────────────────────────────────

    private void setupNewChat() {
        if (binding.btnNewChat != null) binding.btnNewChat.setOnClickListener(v -> showNewChatDialog());
    }

    private void showNewChatDialog() {
        // Armar listado de nombres para el diálogo
        String[] labels = new String[AVAILABLE_CLIENTS.length];
        for (int i = 0; i < AVAILABLE_CLIENTS.length; i++) {
            labels[i] = AVAILABLE_CLIENTS[i][0] + "  ·  " + AVAILABLE_CLIENTS[i][3];
        }

        new AlertDialog.Builder(this)
            .setTitle("Seleccionar cliente")
            .setItems(labels, (dialog, which) -> createAndOpenChat(AVAILABLE_CLIENTS[which]))
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void createAndOpenChat(String[] client) {
        // Verificar si ya existe un chat con este cliente
        for (Chat c : newChats) {
            if (c.getName().equals(client[0])) {
                openConversation(c);
                return;
            }
        }

        String newId = "new_" + (newChats.size() + 8);
        Chat newChat = new Chat(newId, client[0], "Chat iniciado", "Ahora",
            client[1], client[2], 0, true, client[3]);
        newChats.add(newChat);

        buildItems();
        adapter.updateItems(items);
        openConversation(newChat);
    }

    // ── Reconectar chat finalizado ────────────────────────────────────────────

    private void reconectar(Chat chat) {
        reconnectedIds.add(chat.getId());
        buildItems();
        adapter.updateItems(items);
    }

    // ── Construcción de la lista ──────────────────────────────────────────────

    /**
     * Reconstruye `items` respetando:
     * - chats eliminados (AsesorStorage)
     * - chats reconectados (reconnectedIds → pasan a ACTIVOS)
     * - nuevos chats creados en sesión (newChats)
     */
    private void buildItems() {
        Set<String> deleted = AsesorStorage.getDeletedChatIds(this);
        items = new ArrayList<>();

        // ── ACTIVOS ──────────────────────────────────────────────────────────
        List<Chat> activos = new ArrayList<>(getAllActivos());

        // Chats finalizados que fueron reconectados → agregar a activos
        for (Chat c : getAllFinalizadas()) {
            if (reconnectedIds.contains(c.getId())) {
                activos.add(new Chat(c.getId(), c.getName(),
                    "Conversación reiniciada 🔄", "Ahora",
                    c.getInitials(), "#4DB6AC", 0, true, c.getProyecto()));
            }
        }

        boolean tieneActivos = false;
        for (Chat c : activos) {
            if (!deleted.contains(c.getId())) { tieneActivos = true; break; }
        }
        if (tieneActivos) {
            items.add("ACTIVOS");
            for (Chat c : activos) {
                if (!deleted.contains(c.getId())) items.add(c);
            }
        }

        // ── FINALIZADAS (excluye reconectados y eliminados) ──────────────────
        boolean tieneFinalizadas = false;
        for (Chat c : getAllFinalizadas()) {
            if (!deleted.contains(c.getId()) && !reconnectedIds.contains(c.getId())) {
                tieneFinalizadas = true; break;
            }
        }
        if (tieneFinalizadas) {
            items.add("FINALIZADAS");
            for (Chat c : getAllFinalizadas()) {
                if (!deleted.contains(c.getId()) && !reconnectedIds.contains(c.getId())) {
                    items.add(c);
                }
            }
        }
    }

    // ── Fuentes de datos estáticos ────────────────────────────────────────────

    private List<Chat> getAllActivos() {
        List<Chat> list = new ArrayList<>();
        list.add(new Chat("1", "Carlos Mendoza",  "¿Podemos confirmar la cita del lunes?",  "10:32", "CM", "#4DB6AC", 2, true, "Torres del Sol · Dpto 302"));
        list.add(new Chat("2", "Rosa Torres",     "Muchas gracias por la atención 🙏",       "9:15",  "RT", "#F06292", 1, true, "Torres del Sol · Dpto 108"));
        list.add(new Chat("3", "Ana López",       "Perfecto, quedamos el martes entonces",   "Ayer",  "AL", "#FF8A65", 0, true, "Torres del Sol · Dpto 501"));
        list.add(new Chat("4", "Marco Paredes",   "¿El departamento tiene estacionamiento?", "Ayer",  "MP", "#9575CD", 0, true, "Torres del Sol · Dpto 210"));
        // Nuevos chats creados en sesión
        list.addAll(newChats);
        return list;
    }

    private List<Chat> getAllFinalizadas() {
        List<Chat> list = new ArrayList<>();
        list.add(new Chat("5", "Jorge Castro", "Gracias, lo voy a pensar con mi familia",  "Lun", "JC", "#BDBDBD", 0, false, "Torres del Sol · Dpto 601"));
        list.add(new Chat("6", "Sandra Vega",  "¿Pueden enviarme los planos del dpto 4…",  "Dom", "SV", "#BDBDBD", 0, false, "Torres del Sol · Dpto 415"));
        list.add(new Chat("7", "Luis Vargas",  "Ok, reagendamos para la próxima semana",    "Sáb", "LV", "#BDBDBD", 0, false, "Torres del Sol · Dpto 204"));
        return list;
    }

    // ── Swipe para eliminar ───────────────────────────────────────────────────

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int position = vh.getAdapterPosition();
                Object item = items.get(position);
                if (item instanceof Chat) {
                    AsesorStorage.saveDeletedChatId(
                        MensajesActivity.this, ((Chat) item).getId());
                }
                adapter.removeItem(position);
            }
        }).attachToRecyclerView(binding.recyclerViewChats);
    }

    // ── Bottom nav ────────────────────────────────────────────────────────────

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_chat);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, AsesorHomeActivity.class)); finish();
            } else if (id == R.id.nav_citas) {
                startActivity(new Intent(this, CitasAgendadasActivity.class)); finish();
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, AsesorPerfilActivity.class)); finish();
            }
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
