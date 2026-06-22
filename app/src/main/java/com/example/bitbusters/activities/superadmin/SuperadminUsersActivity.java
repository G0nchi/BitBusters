package com.example.bitbusters.activities.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;
import com.example.bitbusters.utils.PreferencesManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuperadminUsersActivity extends AppCompatActivity {

    private static final int PAGE_SIZE = 4;
    private static final String TAG = "SA_USERS";

    private static final String STATUS_ACTIVE   = "active";
    private static final String STATUS_INACTIVE = "inactive";
    private static final String STATUS_PENDING  = "pending";

    private static final String TAB_ADMINS = "ADMINS";
    private static final String TAB_ADVISORS = "ADVISORS";
    private static final String TAB_CLIENTS = "CLIENTS";

    private TextView tabAdminsText;
    private TextView tabAdvisorsText;
    private TextView tabClientsText;
    private View tabAdminsLine;
    private View tabAdvisorsLine;
    private View tabClientsLine;

    private TextView filterAllChip;
    private TextView filterActiveChip;
    private TextView filterInactiveChip;
    private TextView filterPendingChip;

    private EditText searchUsersInput;
    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;

    private String currentTab = TAB_CLIENTS;
    private String currentStatusFilter = "ALL";
    private String currentQuery = "";

    private final List<UserItem> filteredUsers = new ArrayList<>();
    private final List<UserItem> visibleUsers = new ArrayList<>();
    private int renderedUsersCount = 0;
    private boolean isLoadingMore = false;
    private final List<UserItem> allUsersFromFirestore = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_users);

        bindInsets();
        bindViews();
        setupClicks();
        setupSearch();
        setupInfiniteScroll();

        // Restaurar tab y filtro — sanitizar valores stale de versiones anteriores
        String savedFilter = PreferencesManager.obtenerFiltroEstado(this);
        if (STATUS_ACTIVE.equals(savedFilter) || STATUS_INACTIVE.equals(savedFilter) || STATUS_PENDING.equals(savedFilter)) {
            currentStatusFilter = savedFilter;
        } else {
            currentStatusFilter = "ALL";
        }
        updateFilterChipStyles();
        String tabGuardado = PreferencesManager.obtenerTabUsuarios(this);
        switch (tabGuardado) {
            case TAB_ADMINS:   showAdmins();   break;
            case TAB_ADVISORS: showAdvisors(); break;
            default:           showClients();  break;
        }
        loadUsersFromFirestore();
    }

    private void setupInfiniteScroll() {
        if (usersRecyclerView == null) {
            return;
        }

        usersRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || isLoadingMore || renderedUsersCount >= filteredUsers.size()) {
                    return;
                }

                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (!(manager instanceof LinearLayoutManager)) {
                    return;
                }

                int lastVisible = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                if (lastVisible >= visibleUsers.size() - 2) {
                    Log.d(TAG, "onScrolled -> trigger loadMoreUsers, lastVisible=" + lastVisible + ", visible=" + visibleUsers.size() + ", filtered=" + filteredUsers.size());
                    loadMoreUsers();
                }
            }
        });
    }

    private void bindInsets() {
        View root = findViewById(R.id.main);
        if (root == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        tabAdminsText = findViewById(R.id.tabAdminsText);
        tabAdvisorsText = findViewById(R.id.tabAdvisorsText);
        tabClientsText = findViewById(R.id.tabClientsText);
        tabAdminsLine = findViewById(R.id.tabAdminsLine);
        tabAdvisorsLine = findViewById(R.id.tabAdvisorsLine);
        tabClientsLine = findViewById(R.id.tabClientsLine);

        filterAllChip = findViewById(R.id.filterAllChip);
        filterActiveChip = findViewById(R.id.filterActiveChip);
        filterInactiveChip = findViewById(R.id.filterInactiveChip);
        filterPendingChip = findViewById(R.id.filterPendingChip);

        searchUsersInput = findViewById(R.id.searchUsersInput);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        if (usersRecyclerView != null) {
            usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            userAdapter = new UserAdapter();
            usersRecyclerView.setAdapter(userAdapter);
        }
    }

    private void setupClicks() {
        View tabAdmins = findViewById(R.id.tabAdmins);
        View tabAdvisors = findViewById(R.id.tabAdvisors);
        View tabClients = findViewById(R.id.tabClients);
        View navHome = findViewById(R.id.navHome);
        View navApprovals = findViewById(R.id.navApprovals);
        View navReports = findViewById(R.id.navReports);
        View navLogs = findViewById(R.id.navLogs);

        if (tabAdmins != null) {
            tabAdmins.setOnClickListener(v -> showAdmins());
        }
        if (tabAdvisors != null) {
            tabAdvisors.setOnClickListener(v -> showAdvisors());
        }
        if (tabClients != null) {
            tabClients.setOnClickListener(v -> showClients());
        }

        if (filterAllChip != null) {
            filterAllChip.setOnClickListener(v -> {
                currentStatusFilter = "ALL";
                PreferencesManager.guardarFiltroEstado(this, currentStatusFilter);
                updateFilterChipStyles();
                applyFiltersAndRender(true);
            });
        }
        if (filterActiveChip != null) {
            filterActiveChip.setOnClickListener(v -> {
                currentStatusFilter = STATUS_ACTIVE;
                PreferencesManager.guardarFiltroEstado(this, currentStatusFilter);
                updateFilterChipStyles();
                applyFiltersAndRender(true);
            });
        }
        if (filterInactiveChip != null) {
            filterInactiveChip.setOnClickListener(v -> {
                currentStatusFilter = STATUS_INACTIVE;
                PreferencesManager.guardarFiltroEstado(this, currentStatusFilter);
                updateFilterChipStyles();
                applyFiltersAndRender(true);
            });
        }
        if (filterPendingChip != null) {
            filterPendingChip.setOnClickListener(v -> {
                currentStatusFilter = STATUS_PENDING;
                PreferencesManager.guardarFiltroEstado(this, currentStatusFilter);
                updateFilterChipStyles();
                applyFiltersAndRender(true);
            });
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> openAndFinish(SuperadminControlCenterActivity.class));
        }
        if (navApprovals != null) {
            navApprovals.setOnClickListener(v -> openAndFinish(SuperadminApprovalsActivity.class));
        }
        if (navReports != null) {
            navReports.setOnClickListener(v -> openAndFinish(SuperadminReportsActivity.class));
        }
        if (navLogs != null) {
            navLogs.setOnClickListener(v -> openAndFinish(SuperadminLogsActivity.class));
        }
    }

    private void setupSearch() {
        if (searchUsersInput == null) {
            return;
        }

        searchUsersInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no-op
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s == null ? "" : s.toString();
                applyFiltersAndRender(true);
            }
        });
    }

    private void showAdmins() {
        currentTab = TAB_ADMINS;
        PreferencesManager.guardarTabUsuarios(this, TAB_ADMINS);
        setTabState(tabAdminsText, tabAdvisorsText, tabClientsText, tabAdminsLine, tabAdvisorsLine, tabClientsLine);
        applyFiltersAndRender(true);
    }

    private void showAdvisors() {
        currentTab = TAB_ADVISORS;
        PreferencesManager.guardarTabUsuarios(this, TAB_ADVISORS);
        setTabState(tabAdvisorsText, tabAdminsText, tabClientsText, tabAdvisorsLine, tabAdminsLine, tabClientsLine);
        applyFiltersAndRender(true);
    }

    private void showClients() {
        currentTab = TAB_CLIENTS;
        PreferencesManager.guardarTabUsuarios(this, TAB_CLIENTS);
        setTabState(tabClientsText, tabAdminsText, tabAdvisorsText, tabClientsLine, tabAdminsLine, tabAdvisorsLine);
        applyFiltersAndRender(true);
    }

    private void applyFiltersAndRender(boolean resetScroll) {
        List<UserItem> source = getUsersForCurrentTab();
        filteredUsers.clear();

        String normalizedQuery = normalize(currentQuery).trim();
        for (UserItem user : source) {
            if (!"ALL".equals(currentStatusFilter) && !currentStatusFilter.equals(user.status)) {
                continue;
            }

            if (!normalizedQuery.isEmpty()) {
                String haystack = normalize(user.name + " " + user.company);
                if (!haystack.contains(normalizedQuery)) {
                    continue;
                }
            }
            filteredUsers.add(user);
        }

        renderedUsersCount = 0;
        visibleUsers.clear();
        Log.d(TAG, "applyFiltersAndRender -> tab=" + currentTab + " statusFilter=" + currentStatusFilter + " source=" + source.size() + " filtered=" + filteredUsers.size());
        loadMoreUsers();
        fillViewportIfNeeded();

        if (resetScroll && usersRecyclerView != null) {
            usersRecyclerView.scrollToPosition(0);
        }
    }

    private void loadMoreUsers() {
        appendNextPage();
        if (userAdapter != null) {
            userAdapter.submitList(visibleUsers);
        }
    }

    private boolean appendNextPage() {
        if (renderedUsersCount >= filteredUsers.size()) {
            return false;
        }

        isLoadingMore = true;
        int end = Math.min(renderedUsersCount + PAGE_SIZE, filteredUsers.size());
        visibleUsers.addAll(filteredUsers.subList(renderedUsersCount, end));
        renderedUsersCount = end;
        Log.d(TAG, "appendNextPage -> appendedUntil=" + renderedUsersCount + " of " + filteredUsers.size());
        isLoadingMore = false;
        return true;
    }

    private void fillViewportIfNeeded() {
        if (usersRecyclerView == null || userAdapter == null) {
            return;
        }

        usersRecyclerView.post(() -> {
            // Only preload one extra page if the first page does not fill the viewport.
            // This keeps incremental loading behavior visible while avoiding full eager load.
            if (!usersRecyclerView.canScrollVertically(1)
                    && renderedUsersCount < filteredUsers.size()) {
                appendNextPage();
            }

            userAdapter.submitList(visibleUsers);
        });
    }

    private View createUserCard(UserItem item, boolean withTopMargin) {
        LinearLayout card = new LinearLayout(this);
        card.setLayoutParams(cardParams(withTopMargin));
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), 0, dp(14), 0);
        card.setBackgroundResource(R.drawable.sa_user_row_bg);
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, SuperadminUserDetailActivity.class);
            intent.putExtra("userId", item.uid);
            startActivity(intent);
        });

        TextView initials = new TextView(this);
        LinearLayout.LayoutParams initialsParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        initials.setLayoutParams(initialsParams);
        initials.setBackgroundResource(R.drawable.sa_avatar_bg);
        initials.setGravity(Gravity.CENTER);
        initials.setText(item.initials);
        initials.setTextColor(ContextCompat.getColor(this, R.color.white));
        initials.setTextSize(16);
        initials.setTypeface(initials.getTypeface(), android.graphics.Typeface.BOLD);
        card.addView(initials);

        LinearLayout infoColumn = new LinearLayout(this);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        infoParams.setMarginStart(dp(10));
        infoColumn.setLayoutParams(infoParams);
        infoColumn.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        name.setTextSize(18);
        infoColumn.addView(name);

        TextView company = new TextView(this);
        company.setText(item.company);
        company.setTextColor(ContextCompat.getColor(this, R.color.text_medium));
        company.setTextSize(12);
        infoColumn.addView(company);

        card.addView(infoColumn);

        TextView status = new TextView(this);
        status.setPadding(dp(10), dp(4), dp(10), dp(4));
        status.setTextSize(12);
        if (STATUS_ACTIVE.equals(item.status)) {
            status.setText(R.string.sa_status_active);
            status.setTextColor(ContextCompat.getColor(this, R.color.success_green));
            status.setBackgroundResource(R.drawable.sa_status_active_pill_bg);
        } else if (STATUS_PENDING.equals(item.status)) {
            status.setText(R.string.sa_status_pending);
            status.setTextColor(0xFFD97706);
            status.setBackgroundResource(R.drawable.sa_status_pending_bg);
        } else {
            status.setText(R.string.sa_status_inactive);
            status.setTextColor(ContextCompat.getColor(this, R.color.text_medium));
            status.setBackgroundResource(R.drawable.sa_status_inactive_pill_bg);
        }
        card.addView(status);

        ImageView chevron = new ImageView(this);
        LinearLayout.LayoutParams chevronParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        chevronParams.setMarginStart(dp(10));
        chevron.setLayoutParams(chevronParams);
        chevron.setImageResource(R.drawable.ic_chevron_right_16);
        chevron.setColorFilter(ContextCompat.getColor(this, R.color.slate_300));
        card.addView(chevron);

        return card;
    }

    private LinearLayout.LayoutParams cardParams(boolean withTopMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(82)
        );
        if (withTopMargin) {
            params.topMargin = dp(12);
        }
        return params;
    }

    private void updateFilterChipStyles() {
        styleChip(filterAllChip,      "ALL".equals(currentStatusFilter));
        styleChip(filterActiveChip,   STATUS_ACTIVE.equals(currentStatusFilter));
        styleChip(filterInactiveChip, STATUS_INACTIVE.equals(currentStatusFilter));
        styleChip(filterPendingChip,  STATUS_PENDING.equals(currentStatusFilter));
    }

    private void styleChip(TextView chip, boolean selected) {
        if (chip == null) return;
        chip.setBackgroundResource(selected
                ? R.drawable.sa_filter_chip_selected_bg
                : R.drawable.sa_filter_chip_unselected_bg);
        chip.setTextColor(ContextCompat.getColor(
                this,
                selected ? R.color.white : R.color.overlay_white_30
        ));
    }

    private List<UserItem> getUsersForCurrentTab() {
        String roleFilter;
        switch (currentTab) {
            case TAB_ADMINS:   roleFilter = "admin";  break;
            case TAB_ADVISORS: roleFilter = "asesor"; break;
            default:           roleFilter = "cliente"; break;
        }
        List<UserItem> result = new ArrayList<>();
        for (UserItem u : allUsersFromFirestore) {
            if (roleFilter.equals(u.role)) result.add(u);
        }
        return result;
    }

    private void loadUsersFromFirestore() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .get()
            .addOnSuccessListener(snapshots -> {
                allUsersFromFirestore.clear();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    String nombre = doc.getString("nombre");
                    String role   = doc.getString("role");
                    String email  = doc.getString("email");
                    if (nombre == null || role == null || "superadmin".equals(role)) continue;
                    String initials = computeInitials(nombre);
                    String company  = doc.getString("inmobiliaria");
                    if (company == null) company = email != null ? email : "";
                    String status = doc.getString("status");
                    if (status == null) status = STATUS_ACTIVE;
                    allUsersFromFirestore.add(new UserItem(initials, nombre, company, status, role, doc.getId()));
                }
                Log.d(TAG, "Firestore loaded " + allUsersFromFirestore.size() + " users");
                applyFiltersAndRender(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Firestore load failed: " + e.getMessage(), e);
                android.widget.Toast.makeText(this, "Error cargando usuarios: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            });
    }

    private String computeInitials(String nombre) {
        String[] parts = nombre.trim().split("\\s+");
        if (parts.length >= 2) {
            return (String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[1].charAt(0))).toUpperCase(Locale.ROOT);
        }
        return nombre.length() >= 1 ? String.valueOf(nombre.charAt(0)).toUpperCase(Locale.ROOT) : "?";
    }

    private void setTabState(
            TextView selected,
            TextView inactive1,
            TextView inactive2,
            View selectedLine,
            View inactiveLine1,
            View inactiveLine2
    ) {
        setTextColor(selected, R.color.brand_deep_blue);
        setTextColor(inactive1, R.color.text_medium);
        setTextColor(inactive2, R.color.text_medium);

        selectedLine.setVisibility(View.VISIBLE);
        inactiveLine1.setVisibility(View.INVISIBLE);
        inactiveLine2.setVisibility(View.INVISIBLE);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void setTextColor(TextView view, @ColorRes int color) {
        if (view == null) {
            return;
        }
        view.setTextColor(ContextCompat.getColor(this, color));
    }

    private void open(Class<?> destination) {
        startActivity(new Intent(this, destination));
    }

    private void openAndFinish(Class<?> destination) {
        open(destination);
        finish();
    }

    private static class UserItem {
        private final String initials;
        private final String name;
        private final String company;
        private final String status;
        private final String role;
        private final String uid;

        private UserItem(String initials, String name, String company, String status, String role, String uid) {
            this.initials = initials;
            this.name = name;
            this.company = company;
            this.status = status;
            this.role = role;
            this.uid = uid;
        }
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private final List<UserItem> items = new ArrayList<>();

        private void submitList(List<UserItem> users) {
            items.clear();
            items.addAll(users);
            notifyDataSetChanged();
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout root = new FrameLayout(parent.getContext());
            root.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            ));
            return new UserViewHolder(root);
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            FrameLayout root = (FrameLayout) holder.itemView;
            root.removeAllViews();
            root.addView(createUserCard(items.get(position), position > 0));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            UserViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
