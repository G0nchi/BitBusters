package com.example.bitbusters.activities.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.adapters.SuperadminNotificationsAdapter;
import com.example.bitbusters.adapters.SuperadminNotificationsAdapter.Entry;
import com.example.bitbusters.adapters.SuperadminNotificationsAdapter.Header;
import com.example.bitbusters.adapters.SuperadminNotificationsAdapter.Item;
import com.example.bitbusters.adapters.SuperadminNotificationsAdapter.NotifEntry;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SuperadminNotificationsActivity extends AppCompatActivity {

    private static final String TAG = "SA_NOTIF";

    private static final Set<String> leidasEnSesion      = new HashSet<>();
    private static final Set<String> descartadasEnSesion  = new HashSet<>();

    private final List<Item> allItemsList = new ArrayList<>();

    private SuperadminNotificationsAdapter adapter;
    private List<Entry> entries;
    private View emptyState;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_notifications);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        emptyState = findViewById(R.id.emptyState);
        rv         = findViewById(R.id.rvNotificaciones);
        rv.setLayoutManager(new LinearLayoutManager(this));

        entries = new ArrayList<>();
        adapter = new SuperadminNotificationsAdapter(
                entries,
                item -> {
                    leidasEnSesion.add(item.id);
                    startActivity(new Intent(this, item.destination));
                },
                () -> {
                    leidasEnSesion.clear();
                    rebuildList();
                }
        );
        rv.setAdapter(adapter);
        setupSwipeToDelete();
        updateEmptyState();

        loadNotificationsFromFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!allItemsList.isEmpty()) {
            rebuildList();
        }
    }

    private void loadNotificationsFromFirestore() {
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("role", "superadmin")
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(snapshots -> {
                allItemsList.clear();
                if (!snapshots.isEmpty()) {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Item item = docToItem(doc);
                        if (item != null) allItemsList.add(item);
                    }
                } else {
                    allItemsList.addAll(defaultItems());
                }
                rebuildList();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Firestore fetch failed, using defaults", e);
                allItemsList.addAll(defaultItems());
                rebuildList();
            });
    }

    private Item docToItem(DocumentSnapshot doc) {
        String tipo       = doc.getString("tipo");
        String titulo     = doc.getString("titulo");
        String descripcion = doc.getString("descripcion");
        String tiempo     = doc.getString("tiempo");
        if (tipo == null || titulo == null) return null;

        int iconRes, iconTint, iconBg;
        Class<?> destination;
        switch (tipo) {
            case "approvals":
                iconRes     = R.drawable.ic_approvals_20;
                iconTint    = R.color.badge_pendiente_text;
                iconBg      = R.color.badge_pendiente_bg;
                destination = SuperadminApprovalEvaluationActivity.class;
                break;
            case "users":
                iconRes     = R.drawable.ic_users_20;
                iconTint    = R.color.brand_deep_blue;
                iconBg      = R.color.badge_en_planos_bg;
                destination = SuperadminUserDetailActivity.class;
                break;
            case "logs":
                iconRes     = R.drawable.ic_logs_20;
                iconTint    = R.color.status_error;
                iconBg      = R.color.badge_cancelada_bg;
                destination = SuperadminLogsActivity.class;
                break;
            default:
                return null;
        }

        return new Item(doc.getId(), iconRes, iconTint, iconBg,
                titulo,
                descripcion != null ? descripcion : "",
                tiempo      != null ? tiempo      : "",
                destination, false);
    }

    private List<Item> defaultItems() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(
                "sa_aprobacion",
                R.drawable.ic_approvals_20,
                R.color.badge_pendiente_text,
                R.color.badge_pendiente_bg,
                "Nueva aprobación pendiente",
                "Hay un proyecto que requiere tu revisión y aprobación.",
                "Ahora",
                SuperadminApprovalEvaluationActivity.class,
                false
        ));
        list.add(new Item(
                "sa_usuario",
                R.drawable.ic_users_20,
                R.color.brand_deep_blue,
                R.color.badge_en_planos_bg,
                "Nuevo usuario registrado",
                "Un nuevo asesor se ha registrado en la plataforma.",
                "5 min",
                SuperadminUserDetailActivity.class,
                false
        ));
        list.add(new Item(
                "sa_log_critico",
                R.drawable.ic_logs_20,
                R.color.status_error,
                R.color.badge_cancelada_bg,
                "Error crítico detectado",
                "Se registró un error crítico en el sistema. Revisa los logs.",
                "10 min",
                SuperadminLogsActivity.class,
                false
        ));
        return list;
    }

    private void rebuildList() {
        entries.clear();
        entries.addAll(buildEntries());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder vh) {
                if (!(vh instanceof SuperadminNotificationsAdapter.ItemVH)) return 0;
                return super.getSwipeDirs(rv, vh);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int pos = vh.getAdapterPosition();
                Entry entry = entries.get(pos);
                if (entry instanceof NotifEntry) {
                    descartadasEnSesion.add(((NotifEntry) entry).item.id);
                }
                adapter.removeEntryAt(pos);
                cleanOrphanHeaders();
                updateEmptyState();
            }
        }).attachToRecyclerView(rv);
    }

    private void cleanOrphanHeaders() {
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (!(entries.get(i) instanceof Header)) continue;
            boolean hasItem = false;
            for (int j = i + 1; j < entries.size(); j++) {
                if (entries.get(j) instanceof NotifEntry) { hasItem = true; break; }
                if (entries.get(j) instanceof Header) break;
            }
            if (!hasItem) {
                entries.remove(i);
                adapter.notifyItemRemoved(i);
            }
        }
    }

    private void updateEmptyState() {
        long itemCount = entries.stream().filter(e -> e instanceof NotifEntry).count();
        rv.setVisibility(itemCount > 0 ? View.VISIBLE : View.GONE);
        emptyState.setVisibility(itemCount > 0 ? View.GONE : View.VISIBLE);
    }

    private List<Entry> buildEntries() {
        Set<String> descartadas = descartadasEnSesion;
        Set<String> leidas      = leidasEnSesion;

        List<Item> unread = new ArrayList<>();
        List<Item> read   = new ArrayList<>();

        for (Item item : allItemsList) {
            if (descartadas.contains(item.id)) continue;
            if (leidas.contains(item.id)) {
                read.add(itemWithRead(item, true));
            } else {
                unread.add(item);
            }
        }

        List<Entry> result = new ArrayList<>();
        if (!unread.isEmpty()) {
            result.add(new Header("Sin leer", false));
            for (Item i : unread) result.add(new NotifEntry(i));
        }
        if (!read.isEmpty()) {
            result.add(new Header("Leídas", true));
            for (Item i : read) result.add(new NotifEntry(i));
        }
        return result;
    }

    private Item itemWithRead(Item original, boolean read) {
        return new Item(original.id, original.iconRes, original.iconTint, original.iconBg,
                original.titulo, original.descripcion, original.tiempo,
                original.destination, read);
    }
}
