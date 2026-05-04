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
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitbusters.R;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SuperadminApprovalsActivity extends AppCompatActivity {

    private static final int PAGE_SIZE = 4;
    private static final String TAG = "SA_APPROVALS";

    private RecyclerView approvalsRecyclerView;
    private ApprovalsAdapter approvalsAdapter;
    private EditText searchApprovalsInput;

    private TextView filterDateButton;
    private TextView filterLocationButton;
    private TextView filterCompanyButton;

    private final List<ApprovalItem> allApprovals = new ArrayList<>();
    private final List<ApprovalItem> filteredApprovals = new ArrayList<>();
    private final List<ApprovalItem> visibleApprovals = new ArrayList<>();
    private int renderedApprovalsCount = 0;
    private boolean isLoadingMore = false;

    // 0 = any, 1 = last 7 days, 2 = last 30 days
    private int selectedDateFilter = 0;
    private String selectedLocation = "";
    private String selectedCompany = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_approvals);

        bindInsets();
        bindViews();
        setupClicks();
        setupSearchFilter();
        setupInfiniteScroll();

        seedApprovals();
        updateFilterButtonLabels();
        updateFilterButtonStyles();
        applySearchAndRender("", true);
    }

    private void setupInfiniteScroll() {
        if (approvalsRecyclerView == null) {
            return;
        }

        approvalsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || isLoadingMore || renderedApprovalsCount >= filteredApprovals.size()) {
                    return;
                }

                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (!(manager instanceof LinearLayoutManager)) {
                    return;
                }

                int lastVisible = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                if (lastVisible >= visibleApprovals.size() - 2) {
                    Log.d(TAG, "onScrolled -> trigger loadMoreApprovals, lastVisible=" + lastVisible + ", visible=" + visibleApprovals.size() + ", filtered=" + filteredApprovals.size());
                    loadMoreApprovals();
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
        approvalsRecyclerView = findViewById(R.id.approvalsRecyclerView);
        if (approvalsRecyclerView != null) {
            approvalsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            approvalsAdapter = new ApprovalsAdapter();
            approvalsRecyclerView.setAdapter(approvalsAdapter);
        }
        searchApprovalsInput = findViewById(R.id.searchApprovalsInput);

        filterDateButton = findViewById(R.id.filterDateButton);
        filterLocationButton = findViewById(R.id.filterLocationButton);
        filterCompanyButton = findViewById(R.id.filterCompanyButton);
    }

    private void setupClicks() {
        View navHome = findViewById(R.id.navHome);
        View navUsers = findViewById(R.id.navUsers);
        View navReports = findViewById(R.id.navReports);
        View navLogs = findViewById(R.id.navLogs);

        if (navHome != null) {
            navHome.setOnClickListener(v -> openAndFinish(SuperadminControlCenterActivity.class));
        }
        if (navUsers != null) {
            navUsers.setOnClickListener(v -> openAndFinish(SuperadminUsersActivity.class));
        }
        if (navReports != null) {
            navReports.setOnClickListener(v -> openAndFinish(SuperadminReportsActivity.class));
        }
        if (navLogs != null) {
            navLogs.setOnClickListener(v -> openAndFinish(SuperadminLogsActivity.class));
        }

        if (filterDateButton != null) {
            filterDateButton.setOnClickListener(this::showDateFilterMenu);
        }
        if (filterLocationButton != null) {
            filterLocationButton.setOnClickListener(this::showLocationFilterMenu);
        }
        if (filterCompanyButton != null) {
            filterCompanyButton.setOnClickListener(this::showCompanyFilterMenu);
        }
    }

    private void setupSearchFilter() {
        if (searchApprovalsInput == null) {
            return;
        }

        searchApprovalsInput.addTextChangedListener(new TextWatcher() {
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
                applySearchAndRender(s == null ? "" : s.toString(), true);
            }
        });
    }

    private void seedApprovals() {
        allApprovals.clear();
        allApprovals.addAll(Arrays.asList(
                new ApprovalItem("JP", "Juan Perez", "Inmobiliaria Norte", "Bogota", "2026-04-03"),
                new ApprovalItem("DT", "Diana Torres", "Inmobiliaria Sur", "Medellin", "2026-04-02"),
                new ApprovalItem("CR", "Carlos Ruiz", "Inmobiliaria Este", "Cali", "2026-04-01"),
                new ApprovalItem("MS", "Monica Silva", "Inmobiliaria Centro", "Bogota", "2026-03-30"),
                new ApprovalItem("RD", "Roberto Diaz", "Inmobiliaria Norte", "Barranquilla", "2026-03-29"),
                new ApprovalItem("AC", "Andrea Castillo", "Inmobiliaria Centro", "Bogota", "2026-03-28"),
                new ApprovalItem("FM", "Felipe Moreno", "Inmobiliaria Sur", "Medellin", "2026-03-27"),
                new ApprovalItem("DV", "Daniela Vega", "Inmobiliaria Este", "Cali", "2026-03-26"),
                new ApprovalItem("LR", "Laura Romero", "Inmobiliaria Capital", "Bogota", "2026-03-25"),
                new ApprovalItem("NC", "Nicolas Cardenas", "Inmobiliaria Norte", "Medellin", "2026-03-24"),
                new ApprovalItem("PP", "Paula Paredes", "Inmobiliaria Sur", "Bogota", "2026-03-23"),
                new ApprovalItem("HV", "Hector Vargas", "Inmobiliaria Centro", "Cali", "2026-03-22"),
                new ApprovalItem("BG", "Brenda Gomez", "Inmobiliaria Este", "Barranquilla", "2026-03-21"),
                new ApprovalItem("AM", "Alberto Mena", "Inmobiliaria Norte", "Bogota", "2026-03-20"),
                new ApprovalItem("TC", "Tatiana Cruz", "Inmobiliaria Sur", "Medellin", "2026-03-19"),
                new ApprovalItem("JL", "Jorge Luna", "Inmobiliaria Capital", "Bogota", "2026-03-18")
        ));
    }

    private void applySearchAndRender(String rawQuery, boolean resetScrollPosition) {
        String query = normalize(rawQuery).trim();

        filteredApprovals.clear();
        for (ApprovalItem item : allApprovals) {
            boolean matchesName = query.isEmpty() || normalize(item.name).contains(query);
            boolean matchesLocation = selectedLocation.isEmpty() || selectedLocation.equals(item.location);
            boolean matchesCompany = selectedCompany.isEmpty() || selectedCompany.equals(item.company);
            boolean matchesDate = matchesDateFilter(item.date);

            if (matchesName && matchesLocation && matchesCompany && matchesDate) {
                filteredApprovals.add(item);
            }
        }

        renderedApprovalsCount = 0;
        visibleApprovals.clear();
        loadMoreApprovals();
        Log.d(TAG, "applySearchAndRender -> filtered=" + filteredApprovals.size() + ", rendered=" + renderedApprovalsCount);

        if (resetScrollPosition && approvalsRecyclerView != null) {
            approvalsRecyclerView.scrollToPosition(0);
        }
    }

    private void loadMoreApprovals() {
        if (renderedApprovalsCount >= filteredApprovals.size()) {
            return;
        }

        isLoadingMore = true;
        int end = Math.min(renderedApprovalsCount + PAGE_SIZE, filteredApprovals.size());
        visibleApprovals.addAll(filteredApprovals.subList(renderedApprovalsCount, end));
        renderedApprovalsCount = end;
        Log.d(TAG, "loadMoreApprovals -> appendedUntil=" + renderedApprovalsCount + " of " + filteredApprovals.size());

        if (approvalsAdapter != null) {
            approvalsAdapter.submitList(visibleApprovals);
        }
        isLoadingMore = false;
    }

    private boolean matchesDateFilter(String itemDate) {
        if (selectedDateFilter == 0) {
            return true;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);

        Date parsedDate;
        try {
            parsedDate = format.parse(itemDate);
        } catch (ParseException e) {
            return false;
        }

        if (parsedDate == null) {
            return false;
        }

        Calendar boundary = Calendar.getInstance();
        if (selectedDateFilter == 1) {
            boundary.add(Calendar.DAY_OF_YEAR, -7);
        } else {
            boundary.add(Calendar.DAY_OF_YEAR, -30);
        }
        return !parsedDate.before(boundary.getTime());
    }

    private void showDateFilterMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 0, 0, getString(R.string.sa_filter_date_any));
        menu.getMenu().add(0, 1, 1, getString(R.string.sa_filter_date_7));
        menu.getMenu().add(0, 2, 2, getString(R.string.sa_filter_date_30));

        menu.setOnMenuItemClickListener(item -> {
            selectedDateFilter = item.getItemId();
            updateFilterButtonLabels();
            updateFilterButtonStyles();
            applySearchAndRender(getSearchText(), true);
            return true;
        });
        menu.show();
    }

    private void showLocationFilterMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 0, 0, getString(R.string.sa_filter_location_all));

        List<String> locations = new ArrayList<>();
        for (ApprovalItem item : allApprovals) {
            if (!locations.contains(item.location)) {
                locations.add(item.location);
            }
        }

        for (int i = 0; i < locations.size(); i++) {
            menu.getMenu().add(0, i + 1, i + 1, "Ubicacion: " + locations.get(i));
        }

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                selectedLocation = "";
            } else {
                int index = item.getItemId() - 1;
                if (index >= 0 && index < locations.size()) {
                    selectedLocation = locations.get(index);
                }
            }
            updateFilterButtonLabels();
            updateFilterButtonStyles();
            applySearchAndRender(getSearchText(), true);
            return true;
        });
        menu.show();
    }

    private void showCompanyFilterMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 0, 0, getString(R.string.sa_filter_company_all));

        List<String> companies = new ArrayList<>();
        for (ApprovalItem item : allApprovals) {
            if (!companies.contains(item.company)) {
                companies.add(item.company);
            }
        }

        for (int i = 0; i < companies.size(); i++) {
            menu.getMenu().add(0, i + 1, i + 1, "Empresa: " + companies.get(i));
        }

        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 0) {
                selectedCompany = "";
            } else {
                int index = item.getItemId() - 1;
                if (index >= 0 && index < companies.size()) {
                    selectedCompany = companies.get(index);
                }
            }
            updateFilterButtonLabels();
            updateFilterButtonStyles();
            applySearchAndRender(getSearchText(), true);
            return true;
        });
        menu.show();
    }

    private void updateFilterButtonLabels() {
        if (filterDateButton != null) {
            if (selectedDateFilter == 1) {
                filterDateButton.setText(R.string.sa_filter_date_7);
            } else if (selectedDateFilter == 2) {
                filterDateButton.setText(R.string.sa_filter_date_30);
            } else {
                filterDateButton.setText(R.string.sa_filter_date_any);
            }
        }

        if (filterLocationButton != null) {
            if (selectedLocation.isEmpty()) {
                filterLocationButton.setText(R.string.sa_filter_location_all);
            } else {
                filterLocationButton.setText("Ubicacion: " + selectedLocation);
            }
        }

        if (filterCompanyButton != null) {
            if (selectedCompany.isEmpty()) {
                filterCompanyButton.setText(R.string.sa_filter_company_all);
            } else {
                filterCompanyButton.setText("Empresa: " + selectedCompany);
            }
        }
    }

    private void updateFilterButtonStyles() {
        styleFilterButton(filterDateButton, selectedDateFilter != 0);
        styleFilterButton(filterLocationButton, !selectedLocation.isEmpty());
        styleFilterButton(filterCompanyButton, !selectedCompany.isEmpty());
    }

    private void styleFilterButton(TextView button, boolean selected) {
        if (button == null) {
            return;
        }

        button.setBackgroundResource(selected
                ? R.drawable.sa_filter_chip_selected_bg
                : R.drawable.sa_filter_chip_unselected_bg);
        button.setTextColor(ContextCompat.getColor(
                this,
                selected ? R.color.white : R.color.overlay_white_30
        ));
    }

    private String getSearchText() {
        if (searchApprovalsInput == null || searchApprovalsInput.getText() == null) {
            return "";
        }
        return searchApprovalsInput.getText().toString();
    }

    private View createApprovalCard(ApprovalItem item, boolean withTopMargin) {
        LinearLayout card = new LinearLayout(this);
        card.setLayoutParams(cardParams(withTopMargin));
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), 0, dp(12), 0);
        card.setBackgroundResource(R.drawable.sa_user_row_bg);
        card.setOnClickListener(v -> open(SuperadminApprovalEvaluationActivity.class));

        TextView initials = new TextView(this);
        LinearLayout.LayoutParams initialsParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        initials.setLayoutParams(initialsParams);
        initials.setBackgroundResource(R.drawable.sa_avatar_bg);
        initials.setGravity(Gravity.CENTER);
        initials.setText(item.initials);
        initials.setTextColor(ContextCompat.getColor(this, R.color.white));
        initials.setTextSize(14);
        card.addView(initials);

        LinearLayout details = new LinearLayout(this);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        detailsParams.setMarginStart(dp(10));
        details.setLayoutParams(detailsParams);
        details.setOrientation(LinearLayout.VERTICAL);

        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        name.setTextSize(18);
        details.addView(name);

        TextView company = new TextView(this);
        company.setText(item.company + " - " + item.location);
        company.setTextColor(ContextCompat.getColor(this, R.color.text_medium));
        company.setTextSize(12);
        details.addView(company);

        TextView date = new TextView(this);
        date.setText(getString(R.string.sa_approval_date_template, item.date));
        date.setTextColor(ContextCompat.getColor(this, R.color.slate_400));
        date.setTextSize(12);
        details.addView(date);

        card.addView(details);

        TextView status = new TextView(this);
        status.setBackgroundResource(R.drawable.sa_status_pending_bg);
        status.setPadding(dp(8), dp(3), dp(8), dp(3));
        status.setText(R.string.sa_status_pending);
        status.setTextColor(0xFFD97706);
        status.setTextSize(10);
        card.addView(status);

        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        iconParams.setMarginStart(dp(8));
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.ic_users_20);
        icon.setColorFilter(ContextCompat.getColor(this, R.color.slate_500));
        card.addView(icon);

        return card;
    }

    private LinearLayout.LayoutParams cardParams(boolean withTopMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(82)
        );
        params.topMargin = withTopMargin ? dp(10) : dp(12);
        return params;
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

    private void open(Class<?> destination) {
        startActivity(new Intent(this, destination));
    }

    private void openAndFinish(Class<?> destination) {
        open(destination);
        finish();
    }

    private static class ApprovalItem {
        private final String initials;
        private final String name;
        private final String company;
        private final String location;
        private final String date;

        private ApprovalItem(String initials, String name, String company, String location, String date) {
            this.initials = initials;
            this.name = name;
            this.company = company;
            this.location = location;
            this.date = date;
        }
    }

    private class ApprovalsAdapter extends RecyclerView.Adapter<ApprovalsAdapter.ApprovalViewHolder> {
        private final List<ApprovalItem> items = new ArrayList<>();

        private void submitList(List<ApprovalItem> approvals) {
            items.clear();
            items.addAll(approvals);
            notifyDataSetChanged();
        }

        @Override
        public ApprovalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout root = new FrameLayout(parent.getContext());
            root.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            ));
            return new ApprovalViewHolder(root);
        }

        @Override
        public void onBindViewHolder(ApprovalViewHolder holder, int position) {
            FrameLayout root = (FrameLayout) holder.itemView;
            root.removeAllViews();
            root.addView(createApprovalCard(items.get(position), position > 0));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ApprovalViewHolder extends RecyclerView.ViewHolder {
            ApprovalViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
