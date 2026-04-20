package com.example.bitbusters.activities.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SuperadminUsersActivity extends AppCompatActivity {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";

    private static final String TAB_ADMINS = "ADMINS";
    private static final String TAB_ADVISORS = "ADVISORS";
    private static final String TAB_CLIENTS = "CLIENTS";

    private static final int PAGE_SIZE = 8;

    private TextView tabAdminsText;
    private TextView tabAdvisorsText;
    private TextView tabClientsText;
    private View tabAdminsLine;
    private View tabAdvisorsLine;
    private View tabClientsLine;

    private TextView filterAllChip;
    private TextView filterActiveChip;
    private TextView filterInactiveChip;

    private EditText searchUsersInput;
    private ScrollView usersScrollView;
    private LinearLayout usersListContainer;

    private String currentTab = TAB_CLIENTS;
    private String currentStatusFilter = "ALL";
    private String currentQuery = "";

    private final List<UserItem> filteredUsers = new ArrayList<>();
    private int renderedUsersCount = 0;
    private boolean isLoadingMore = false;

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

        showClients();
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

        searchUsersInput = findViewById(R.id.searchUsersInput);
        usersScrollView = findViewById(R.id.usersScrollView);
        usersListContainer = findViewById(R.id.usersListContainer);
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
                updateFilterChipStyles();
                applyFiltersAndRender(true);
            });
        }
        if (filterActiveChip != null) {
            filterActiveChip.setOnClickListener(v -> {
                currentStatusFilter = STATUS_ACTIVE;
                updateFilterChipStyles();
                applyFiltersAndRender(true);
            });
        }
        if (filterInactiveChip != null) {
            filterInactiveChip.setOnClickListener(v -> {
                currentStatusFilter = STATUS_INACTIVE;
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

    private void setupInfiniteScroll() {
        if (usersScrollView == null) {
            return;
        }

        usersScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (isLoadingMore || usersListContainer == null) {
                return;
            }

            View content = usersScrollView.getChildAt(0);
            if (content == null) {
                return;
            }

            int threshold = dp(48);
            int distanceToBottom = content.getBottom() - (usersScrollView.getHeight() + scrollY);
            if (distanceToBottom <= threshold) {
                loadMoreUsers();
            }
        });
    }

    private void showAdmins() {
        currentTab = TAB_ADMINS;
        setTabState(tabAdminsText, tabAdvisorsText, tabClientsText, tabAdminsLine, tabAdvisorsLine, tabClientsLine);
        applyFiltersAndRender(true);
    }

    private void showAdvisors() {
        currentTab = TAB_ADVISORS;
        setTabState(tabAdvisorsText, tabAdminsText, tabClientsText, tabAdvisorsLine, tabAdminsLine, tabClientsLine);
        applyFiltersAndRender(true);
    }

    private void showClients() {
        currentTab = TAB_CLIENTS;
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
        if (usersListContainer != null) {
            usersListContainer.removeAllViews();
        }
        loadMoreUsers();

        if (resetScroll && usersScrollView != null) {
            usersScrollView.post(() -> usersScrollView.scrollTo(0, 0));
        }
    }

    private void loadMoreUsers() {
        if (usersListContainer == null || renderedUsersCount >= filteredUsers.size()) {
            return;
        }

        isLoadingMore = true;
        int end = Math.min(renderedUsersCount + PAGE_SIZE, filteredUsers.size());
        for (int i = renderedUsersCount; i < end; i++) {
            usersListContainer.addView(createUserCard(filteredUsers.get(i), i > 0));
        }
        renderedUsersCount = end;
        isLoadingMore = false;
    }

    private View createUserCard(UserItem item, boolean withTopMargin) {
        LinearLayout card = new LinearLayout(this);
        card.setLayoutParams(cardParams(withTopMargin));
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), 0, dp(14), 0);
        card.setBackgroundResource(R.drawable.sa_user_row_bg);
        card.setOnClickListener(v -> open(SuperadminUserDetailActivity.class));

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
        if (filterAllChip == null || filterActiveChip == null || filterInactiveChip == null) {
            return;
        }

        styleChip(filterAllChip, "ALL".equals(currentStatusFilter));
        styleChip(filterActiveChip, STATUS_ACTIVE.equals(currentStatusFilter));
        styleChip(filterInactiveChip, STATUS_INACTIVE.equals(currentStatusFilter));
    }

    private void styleChip(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected
                ? R.drawable.sa_filter_chip_selected_bg
                : R.drawable.sa_filter_chip_unselected_bg);
        chip.setTextColor(ContextCompat.getColor(
                this,
                selected ? R.color.white : R.color.overlay_white_30
        ));
    }

    private List<UserItem> getUsersForCurrentTab() {
        if (TAB_ADMINS.equals(currentTab)) {
            return Arrays.asList(
                    new UserItem("CM", "Carlos Mendoza", "Inmobiliaria Miraflores", STATUS_ACTIVE),
                    new UserItem("AG", "Ana Garcia", "Inmobiliaria San Isidro", STATUS_ACTIVE),
                    new UserItem("RS", "Roberto Silva", "Inmobiliaria Surco", STATUS_INACTIVE),
                    new UserItem("LV", "Luis Vargas", "Inmobiliaria San Borja", STATUS_ACTIVE),
                    new UserItem("IO", "Ines Ortega", "Inmobiliaria La Molina", STATUS_ACTIVE),
                    new UserItem("JV", "Javier Nuñez", "Inmobiliaria Magdalena", STATUS_ACTIVE),
                    new UserItem("MP", "Marta Paredes", "Inmobiliaria Pueblo Libre", STATUS_ACTIVE),
                    new UserItem("DR", "Daniel Rios", "Inmobiliaria San Isidro", STATUS_INACTIVE),
                    new UserItem("FL", "Fernanda Lopez", "Inmobiliaria Surco", STATUS_ACTIVE),
                    new UserItem("PG", "Paula Gutierrez", "Inmobiliaria Miraflores", STATUS_ACTIVE),
                    new UserItem("HR", "Hector Ruiz", "Inmobiliaria San Borja", STATUS_ACTIVE),
                    new UserItem("CT", "Camila Torres", "Inmobiliaria La Molina", STATUS_INACTIVE)
            );
        }

        if (TAB_ADVISORS.equals(currentTab)) {
            return Arrays.asList(
                    new UserItem("ML", "Maria Lopez", "Inmobiliaria Miraflores", STATUS_ACTIVE),
                    new UserItem("PR", "Pedro Ramirez", "Inmobiliaria San Isidro", STATUS_ACTIVE),
                    new UserItem("JL", "Jorge Luna", "Inmobiliaria Surco", STATUS_INACTIVE),
                    new UserItem("SF", "Sara Flores", "Inmobiliaria San Borja", STATUS_ACTIVE),
                    new UserItem("DT", "Diego Torres", "Inmobiliaria La Molina", STATUS_ACTIVE),
                    new UserItem("CR", "Claudia Rojas", "Inmobiliaria Magdalena", STATUS_ACTIVE),
                    new UserItem("BG", "Bruno Gomez", "Inmobiliaria Pueblo Libre", STATUS_INACTIVE),
                    new UserItem("AR", "Andrea Romero", "Inmobiliaria Jesus Maria", STATUS_ACTIVE),
                    new UserItem("VP", "Valeria Ponce", "Inmobiliaria San Isidro", STATUS_ACTIVE),
                    new UserItem("NV", "Nicolas Vega", "Inmobiliaria Surco", STATUS_ACTIVE),
                    new UserItem("MC", "Mateo Cardenas", "Inmobiliaria Pueblo Libre", STATUS_INACTIVE),
                    new UserItem("LM", "Laura Mejia", "Inmobiliaria Jesus Maria", STATUS_ACTIVE)
            );
        }

        return Arrays.asList(
                new UserItem("SR", "Sofia Ruiz", "Cliente Directo", STATUS_ACTIVE),
                new UserItem("MA", "Miguel Angel", "Cliente Directo", STATUS_ACTIVE),
                new UserItem("LV", "Lucia Vargas", "Cliente Directo", STATUS_INACTIVE),
                new UserItem("SC", "Sebastian Castro", "Cliente Directo", STATUS_ACTIVE),
                new UserItem("IO", "Isabella Ortiz", "Cliente Directo", STATUS_ACTIVE),
                new UserItem("PA", "Pablo Arias", "Cliente Directo", STATUS_ACTIVE),
                new UserItem("DM", "Daniela Mora", "Cliente Directo", STATUS_ACTIVE),
                new UserItem("JR", "Juan Rios", "Cliente Directo", STATUS_INACTIVE),
                new UserItem("VP", "Valentina Pardo", "Cliente Directo", STATUS_ACTIVE),
                new UserItem("GC", "Gabriel Cardenas", "Cliente Directo", STATUS_ACTIVE),
                new UserItem("TC", "Tatiana Cruz", "Cliente Directo", STATUS_INACTIVE),
                new UserItem("EB", "Esteban Blanco", "Cliente Directo", STATUS_ACTIVE)
        );
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

        private UserItem(String initials, String name, String company, String status) {
            this.initials = initials;
            this.name = name;
            this.company = company;
            this.status = status;
        }
    }
}
