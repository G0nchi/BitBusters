package com.example.bitbusters.activities.superadmin;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.bitbusters.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SuperadminNotificationsActivity extends AppCompatActivity {

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

        entries = buildEntries();
        adapter = new SuperadminNotificationsAdapter(
                entries,
                item -> {
                    PreferencesManager.marcarNotificacionLeidaSA(this, item.id);
                    startActivity(new Intent(this, item.destination));
                },
                () -> {
                    PreferencesManager.eliminarTodasLeidasSA(this);
                    rebuildList();
                }
        );
        rv.setAdapter(adapter);
        setupSwipeToDelete();
        updateEmptyState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        rebuildList();
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
                    String id = ((NotifEntry) entry).item.id;
                    PreferencesManager.descartarNotificacionSA(
                            SuperadminNotificationsActivity.this, id);
                }
                adapter.removeEntryAt(pos);
                cleanOrphanHeaders();
                updateEmptyState();
            }
        }).attachToRecyclerView(rv);
    }

    /**
     * Elimina headers que quedaron sin items debajo tras un swipe.
     */
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

    // ── Data ────────────────────────────────────────────────────────────────

    private List<Entry> buildEntries() {
        Set<String> descartadas = PreferencesManager.obtenerNotificacionesDescartadasSA(this);
        Set<String> leidas      = PreferencesManager.obtenerNotificacionesLeidasSA(this);

        List<Item> unread = new ArrayList<>();
        List<Item> read   = new ArrayList<>();

        for (Item item : allItems()) {
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

    private List<Item> allItems() {
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

    private Item itemWithRead(Item original, boolean read) {
        return new Item(original.id, original.iconRes, original.iconTint, original.iconBg,
                original.titulo, original.descripcion, original.tiempo,
                original.destination, read);
    }
}
