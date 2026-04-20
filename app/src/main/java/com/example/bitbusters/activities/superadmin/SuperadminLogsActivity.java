package com.example.bitbusters.activities.superadmin;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
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

import com.example.bitbusters.R;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SuperadminLogsActivity extends AppCompatActivity {

    private static final String TYPE_RESERVATION = "RESERVATION";
    private static final String TYPE_PAYMENT = "PAYMENT";
    private static final String TYPE_APPOINTMENT = "APPOINTMENT";
    private static final String TYPE_ENABLEMENT = "ENABLEMENT";
    private static final String TAG_ALL = "ALL";
    private static final String TAG_STATUS_CONFIRMED = "status:confirmado";
    private static final String TAG_STATUS_SUSPENDED = "status:suspendido";
    private static final String TAG_STATUS_FAILED = "status:fallido";
    private static final String TAG_MODULE_PAYMENTS = "modulo:pagos";
    private static final String TAG_MODULE_APPOINTMENTS = "modulo:citas";
    private static final String TAG_COMPANY_NORTH = "empresa:norte";
    private static final String TAG_COMPANY_SUR = "empresa:sur";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_SUSPENDED = "SUSPENDED";
    private static final String STATUS_FAILED = "FAILED";

    private EditText searchLogsInput;
    private TextView logsCountText;
    private LinearLayout logsListContainer;

    private TextView filterAllLogsChip;
    private TextView filterReservationsLogsChip;
    private TextView filterPaymentsLogsChip;
    private TextView filterAppointmentsLogsChip;
    private TextView filterEnablementsLogsChip;
    private View logsFiltersButton;
    private TextView logsActiveTagText;

    private String selectedTypeFilter = "ALL";
    private String selectedTagFilter = TAG_ALL;
    private final List<LogItem> allLogs = new ArrayList<>();
    private final List<LogItem> filteredLogs = new ArrayList<>();
    private int expandedLogId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_logs);

        bindInsets();
        bindViews();
        setupClicks();
        setupSearch();

        seedLogs();
        applyFiltersAndRender("");
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
        searchLogsInput = findViewById(R.id.searchLogsInput);
        logsCountText = findViewById(R.id.logsCountText);
        logsListContainer = findViewById(R.id.logsListContainer);

        filterAllLogsChip = findViewById(R.id.filterAllLogsChip);
        filterReservationsLogsChip = findViewById(R.id.filterReservationsLogsChip);
        filterPaymentsLogsChip = findViewById(R.id.filterPaymentsLogsChip);
        filterAppointmentsLogsChip = findViewById(R.id.filterAppointmentsLogsChip);
        filterEnablementsLogsChip = findViewById(R.id.filterEnablementsLogsChip);
        logsFiltersButton = findViewById(R.id.logsFiltersButton);
        logsActiveTagText = findViewById(R.id.logsActiveTagText);
    }

    private void setupClicks() {
        View navHome = findViewById(R.id.navHome);
        View navUsers = findViewById(R.id.navUsers);
        View navApprovals = findViewById(R.id.navApprovals);
        View navReports = findViewById(R.id.navReports);

        if (navHome != null) {
            navHome.setOnClickListener(v -> openAndFinish(SuperadminControlCenterActivity.class));
        }
        if (navUsers != null) {
            navUsers.setOnClickListener(v -> openAndFinish(SuperadminUsersActivity.class));
        }
        if (navApprovals != null) {
            navApprovals.setOnClickListener(v -> openAndFinish(SuperadminApprovalsActivity.class));
        }
        if (navReports != null) {
            navReports.setOnClickListener(v -> openAndFinish(SuperadminReportsActivity.class));
        }

        if (filterAllLogsChip != null) {
            filterAllLogsChip.setOnClickListener(v -> {
                selectedTypeFilter = "ALL";
                expandedLogId = -1;
                updateChipStyles();
                applyFiltersAndRender(getSearchText());
            });
        }
        if (filterReservationsLogsChip != null) {
            filterReservationsLogsChip.setOnClickListener(v -> {
                selectedTypeFilter = TYPE_RESERVATION;
                expandedLogId = -1;
                updateChipStyles();
                applyFiltersAndRender(getSearchText());
            });
        }
        if (filterPaymentsLogsChip != null) {
            filterPaymentsLogsChip.setOnClickListener(v -> {
                selectedTypeFilter = TYPE_PAYMENT;
                expandedLogId = -1;
                updateChipStyles();
                applyFiltersAndRender(getSearchText());
            });
        }
        if (filterAppointmentsLogsChip != null) {
            filterAppointmentsLogsChip.setOnClickListener(v -> {
                selectedTypeFilter = TYPE_APPOINTMENT;
                expandedLogId = -1;
                updateChipStyles();
                applyFiltersAndRender(getSearchText());
            });
        }
        if (filterEnablementsLogsChip != null) {
            filterEnablementsLogsChip.setOnClickListener(v -> {
                selectedTypeFilter = TYPE_ENABLEMENT;
                expandedLogId = -1;
                updateChipStyles();
                applyFiltersAndRender(getSearchText());
            });
        }

        if (logsFiltersButton != null) {
            logsFiltersButton.setOnClickListener(v -> showTagFilterMenu());
        }
    }

    private void setupSearch() {
        if (searchLogsInput == null) {
            return;
        }

        searchLogsInput.addTextChangedListener(new TextWatcher() {
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
                expandedLogId = -1;
                applyFiltersAndRender(s == null ? "" : s.toString());
            }
        });
    }

    private void seedLogs() {
        allLogs.clear();
        allLogs.addAll(Arrays.asList(
                new LogItem(1, TYPE_RESERVATION, STATUS_CONFIRMED, "14:50 hrs", "Reserva registrada en proyecto Catalina Sky", "asesor.norte@inmo.com", "Accion Especifica", "Se registro separacion de unidad D-1204", "Recurso Afectado", "Reserva #R-2143 - S/ 5,000", "Empresa: Inmobiliaria Norte | Cliente: DNI 74211233", tags(TAG_STATUS_CONFIRMED, "modulo:reservas", TAG_COMPANY_NORTH)),
                new LogItem(2, TYPE_PAYMENT, STATUS_CONFIRMED, "14:32 hrs", "Pago de separacion aprobado", "pasarela-pagos@inmoapp", "Accion Especifica", "Checkout validado y cobro confirmado", "Recurso Afectado", "Pago #P-8831 - S/ 5,000", "Reserva #R-2143 | Metodo: Tarjeta debito", tags(TAG_STATUS_CONFIRMED, TAG_MODULE_PAYMENTS, TAG_COMPANY_NORTH)),
                new LogItem(3, TYPE_APPOINTMENT, STATUS_CONFIRMED, "14:15 hrs", "Asistencia a cita confirmada", "cliente@correo.com", "Accion Especifica", "Cliente asistio y asesor cerro visita", "Recurso Afectado", "Cita #C-9921 - 21/04 16:00", "Proyecto: Condominio Pueblo Libre", tags(TAG_STATUS_CONFIRMED, TAG_MODULE_APPOINTMENTS, "empresa:centro")),
                new LogItem(4, TYPE_ENABLEMENT, STATUS_CONFIRMED, "13:59 hrs", "Asesor habilitado por superadmin", "superadmin@inmoapp.com", "Accion Especifica", "Cambio de estado a habilitado", "Detalle del Cambio", "Asesor ID 145: Pendiente -> Activo", "Empresa: Inmobiliaria Centro", tags(TAG_STATUS_CONFIRMED, "modulo:habilitaciones", "empresa:centro")),
                new LogItem(5, TYPE_PAYMENT, STATUS_FAILED, "13:44 hrs", "Pago rechazado por banco emisor", "pasarela-pagos@inmoapp", "Accion Especifica", "Intento de cobro fallido", "Recurso Afectado", "Pago #P-8824 - S/ 4,500", "Reserva #R-2138 | Motivo: Fondos insuficientes", tags(TAG_STATUS_FAILED, TAG_MODULE_PAYMENTS, "empresa:este")),
                new LogItem(6, TYPE_PAYMENT, STATUS_SUSPENDED, "13:38 hrs", "Pago suspendido por validacion antifraude", "pasarela-pagos@inmoapp", "Accion Especifica", "Operacion retenida para revision manual", "Recurso Afectado", "Pago #P-8821 - S/ 8,300", "Reserva #R-2136 | Riesgo: Alto | SLA: 2h", tags(TAG_STATUS_SUSPENDED, TAG_MODULE_PAYMENTS, TAG_COMPANY_SUR)),
                new LogItem(7, TYPE_RESERVATION, STATUS_FAILED, "13:28 hrs", "Reserva cancelada por vencimiento", "sistema@inmoapp", "Accion Especifica", "Expiracion por falta de pago en 10 min", "Recurso Afectado", "Reserva #R-2133", "Empresa: Inmobiliaria Sur | Estado final: Cancelada", tags(TAG_STATUS_FAILED, "modulo:reservas", TAG_COMPANY_SUR)),
                new LogItem(8, TYPE_APPOINTMENT, STATUS_FAILED, "12:57 hrs", "Bloqueo por cruce de horario de cita", "cliente2@correo.com", "Accion Especifica", "Se rechazo agenda por superposicion", "Detalle del Cambio", "Cliente ID 588 sin cambio de agenda", "Proyecto: Catalina Sky | Franja: 18:00", tags(TAG_STATUS_FAILED, TAG_MODULE_APPOINTMENTS, TAG_COMPANY_NORTH)),
                new LogItem(9, TYPE_APPOINTMENT, STATUS_CONFIRMED, "12:41 hrs", "Asistencia a cita confirmada", "asesor.este@inmo.com", "Accion Especifica", "Check-in exitoso de cliente en sala de ventas", "Recurso Afectado", "Cita #C-9877 - 21/04 12:30", "Proyecto: Torres del Sol | Duracion: 42 min", tags(TAG_STATUS_CONFIRMED, TAG_MODULE_APPOINTMENTS, "empresa:este")),
                new LogItem(10, TYPE_ENABLEMENT, STATUS_SUSPENDED, "12:30 hrs", "Administrador suspendido temporalmente", "superadmin@inmoapp.com", "Accion Especifica", "Bloqueo temporal de cuenta", "Detalle del Cambio", "Admin ID 33: Activo -> Suspendido", "Empresa: Inmobiliaria Este | Motivo: Auditoria", tags(TAG_STATUS_SUSPENDED, "modulo:habilitaciones", "empresa:este")),
                new LogItem(11, TYPE_RESERVATION, STATUS_CONFIRMED, "11:45 hrs", "Nueva separacion con valoracion del asesor", "asesor.sur@inmo.com", "Accion Especifica", "Cliente dejo valoracion y observacion", "Recurso Afectado", "Reserva #R-2125 - Valoracion: 4/5", "Comentario: Buena atencion, resolver dudas de entrega", tags(TAG_STATUS_CONFIRMED, "modulo:reservas", TAG_COMPANY_SUR)),
                new LogItem(12, TYPE_PAYMENT, STATUS_CONFIRMED, "11:20 hrs", "Pago de separacion aprobado", "pasarela-pagos@inmoapp", "Accion Especifica", "Cobro exitoso en checkout", "Recurso Afectado", "Pago #P-8801 - S/ 6,200", "Reserva #R-2119 | Empresa: Inmobiliaria Norte", tags(TAG_STATUS_CONFIRMED, TAG_MODULE_PAYMENTS, TAG_COMPANY_NORTH))
        ));
    }

    private void applyFiltersAndRender(String rawQuery) {
        String query = normalize(rawQuery).trim();

        filteredLogs.clear();
        for (LogItem item : allLogs) {
            boolean matchesType = "ALL".equals(selectedTypeFilter) || selectedTypeFilter.equals(item.type);
            boolean matchesTag = TAG_ALL.equals(selectedTagFilter) || item.tags.contains(selectedTagFilter);
            boolean matchesQuery = query.isEmpty() || normalize(item.title + " " + item.source + " " + item.metaPrimary + " " + item.metaSecondary + " " + String.join(" ", item.tags)).contains(query);
            if (matchesType && matchesTag && matchesQuery) {
                filteredLogs.add(item);
            }
        }

        if (logsListContainer != null) {
            logsListContainer.removeAllViews();
        }
        for (int i = 0; i < filteredLogs.size(); i++) {
            logsListContainer.addView(createLogCard(filteredLogs.get(i), i > 0));
        }

        updateCountText();
    }

    private void updateCountText() {
        if (logsCountText == null) {
            return;
        }
        int total = filteredLogs.size();
        int from = total == 0 ? 0 : 1;
        logsCountText.setText(getString(R.string.sa_logs_count_template, from, total, total));
        if (logsActiveTagText != null) {
            logsActiveTagText.setText(getString(R.string.sa_logs_active_tag_template, labelForTag(selectedTagFilter)));
        }
    }

    private View createLogCard(LogItem item, boolean withTopMargin) {
        LinearLayout card = new LinearLayout(this);
        card.setLayoutParams(cardParams(withTopMargin));
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.sa_card_bg);

        View accent = new View(this);
        LinearLayout.LayoutParams accentParams = new LinearLayout.LayoutParams(dp(3), LinearLayout.LayoutParams.MATCH_PARENT);
        accent.setLayoutParams(accentParams);
        accent.setBackgroundColor(colorForType(item.type));
        card.addView(accent);

        LinearLayout content = new LinearLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        content.setLayoutParams(contentParams);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.addView(content);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout timeWrap = new LinearLayout(this);
        timeWrap.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        timeWrap.setOrientation(LinearLayout.HORIZONTAL);
        timeWrap.setGravity(Gravity.CENTER_VERTICAL);

        ImageView timeIcon = new ImageView(this);
        LinearLayout.LayoutParams timeIconParams = new LinearLayout.LayoutParams(dp(14), dp(14));
        timeIcon.setLayoutParams(timeIconParams);
        timeIcon.setImageResource(R.drawable.ic_timer_20);
        timeIcon.setColorFilter(ContextCompat.getColor(this, R.color.slate_500));
        timeWrap.addView(timeIcon);

        TextView timeText = new TextView(this);
        LinearLayout.LayoutParams timeTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        timeTextParams.setMarginStart(dp(4));
        timeText.setLayoutParams(timeTextParams);
        timeText.setText(item.time);
        timeText.setTextColor(ContextCompat.getColor(this, R.color.slate_500));
        timeText.setTextSize(12);
        timeWrap.addView(timeText);
        topRow.addView(timeWrap);

        ImageView expandIcon = new ImageView(this);
        LinearLayout.LayoutParams expandParams = new LinearLayout.LayoutParams(dp(16), dp(16));
        expandIcon.setLayoutParams(expandParams);
        expandIcon.setImageResource(item.id == expandedLogId ? R.drawable.ic_chevron_up_16 : R.drawable.ic_chevron_down_16);
        topRow.addView(expandIcon);

        content.addView(topRow);

        TextView title = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(2);
        title.setLayoutParams(titleParams);
        title.setText(item.title);
        title.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        title.setTextSize(16);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        content.addView(title);

        TextView source = new TextView(this);
        LinearLayout.LayoutParams sourceParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sourceParams.topMargin = dp(2);
        source.setLayoutParams(sourceParams);
        source.setText(getString(R.string.sa_logs_source_template, item.source));
        source.setTextColor(ContextCompat.getColor(this, R.color.slate_400));
        source.setTextSize(11);
        content.addView(source);

        LinearLayout badgesRow = new LinearLayout(this);
        LinearLayout.LayoutParams badgesRowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        badgesRowParams.topMargin = dp(6);
        badgesRow.setLayoutParams(badgesRowParams);
        badgesRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView typeBadge = new TextView(this);
        typeBadge.setPadding(dp(8), dp(2), dp(8), dp(2));
        typeBadge.setText(labelForType(item.type));
        typeBadge.setTextColor(colorForType(item.type));
        typeBadge.setTextSize(10);
        typeBadge.setBackground(makePill(colorForType(item.type), 0x14));
        badgesRow.addView(typeBadge);

        TextView statusBadge = new TextView(this);
        LinearLayout.LayoutParams statusBadgeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        statusBadgeParams.setMarginStart(dp(6));
        statusBadge.setLayoutParams(statusBadgeParams);
        statusBadge.setPadding(dp(8), dp(2), dp(8), dp(2));
        int statusColor = colorForStatus(item.status);
        statusBadge.setText(labelForStatus(item.status));
        statusBadge.setTextColor(statusColor);
        statusBadge.setTextSize(10);
        statusBadge.setBackground(makePill(statusColor, 0x14));
        badgesRow.addView(statusBadge);

        content.addView(badgesRow);

        if (item.id == expandedLogId) {
            content.addView(buildSeparator(dp(10)));

            content.addView(detailPair(getString(R.string.sa_logs_detail_action), item.detailAction));
            content.addView(detailPair(getString(R.string.sa_logs_detail_actor), item.source));
            content.addView(detailPair(getString(R.string.sa_logs_detail_status), labelForStatus(item.status)));
            content.addView(detailPair(item.metaLabel, item.metaPrimary));
            content.addView(detailPair(getString(R.string.sa_logs_detail_secondary), item.metaSecondary));

            content.addView(buildSeparator(dp(8)));

            TextView metaTitle = new TextView(this);
            metaTitle.setText(getString(R.string.sa_logs_technical_metadata));
            metaTitle.setTextColor(ContextCompat.getColor(this, R.color.slate_500));
            metaTitle.setTextSize(11);
            content.addView(metaTitle);

            LinearLayout metadataBox = new LinearLayout(this);
            LinearLayout.LayoutParams metadataParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            metadataParams.topMargin = dp(6);
            metadataBox.setLayoutParams(metadataParams);
            metadataBox.setOrientation(LinearLayout.VERTICAL);
            metadataBox.setPadding(dp(10), dp(8), dp(10), dp(8));
            metadataBox.setBackgroundColor(0xFFF1F5F9);

            TextView metaPrimary = new TextView(this);
            metaPrimary.setText(item.metaPrimary);
            metaPrimary.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
            metaPrimary.setTextSize(12);
            metadataBox.addView(metaPrimary);

            TextView metaSecondary = new TextView(this);
            LinearLayout.LayoutParams metaSecondaryParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            metaSecondaryParams.topMargin = dp(4);
            metaSecondary.setLayoutParams(metaSecondaryParams);
            metaSecondary.setText(item.metaSecondary);
            metaSecondary.setTextColor(ContextCompat.getColor(this, R.color.slate_500));
            metaSecondary.setTextSize(11);
            metadataBox.addView(metaSecondary);

            content.addView(metadataBox);

            TextView footerState = new TextView(this);
            LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            footerParams.topMargin = dp(8);
            footerState.setLayoutParams(footerParams);
            footerState.setText(getString(R.string.sa_logs_detail_status_footer, labelForStatus(item.status)));
            footerState.setTextColor(ContextCompat.getColor(this, R.color.slate_500));
            footerState.setTextSize(10);
            footerState.setGravity(Gravity.END);
            content.addView(footerState);
        }

        card.setOnClickListener(v -> {
            if (expandedLogId == item.id) {
                expandedLogId = -1;
            } else {
                expandedLogId = item.id;
            }
            applyFiltersAndRender(getSearchText());
        });

        return card;
    }

    private LinearLayout detailPair(String label, String value) {
        LinearLayout block = new LinearLayout(this);
        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        blockParams.topMargin = dp(8);
        block.setLayoutParams(blockParams);
        block.setOrientation(LinearLayout.VERTICAL);

        TextView labelText = new TextView(this);
        labelText.setText(label);
        labelText.setTextColor(ContextCompat.getColor(this, R.color.slate_500));
        labelText.setTextSize(11);
        block.addView(labelText);

        TextView valueText = new TextView(this);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        valueParams.topMargin = dp(2);
        valueText.setLayoutParams(valueParams);
        valueText.setText(value);
        valueText.setTextColor(ContextCompat.getColor(this, R.color.brand_deep_blue));
        valueText.setTextSize(12);
        valueText.setTypeface(Typeface.DEFAULT_BOLD);
        block.addView(valueText);

        return block;
    }

    private View buildSeparator(int topMargin) {
        View separator = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        params.topMargin = topMargin;
        separator.setLayoutParams(params);
        separator.setBackgroundColor(0xFFE2E8F0);
        return separator;
    }

    private LinearLayout.LayoutParams cardParams(boolean withTopMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (withTopMargin) {
            params.topMargin = dp(10);
        }
        return params;
    }

    private GradientDrawable makePill(int color, int alpha) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(12));
        drawable.setColor((alpha << 24) | (color & 0x00FFFFFF));
        return drawable;
    }

    private int colorForType(String type) {
        if (TYPE_PAYMENT.equals(type)) {
            return 0xFF16A34A;
        }
        if (TYPE_RESERVATION.equals(type)) {
            return 0xFF2563EB;
        }
        if (TYPE_APPOINTMENT.equals(type)) {
            return 0xFFF59E0B;
        }
        return 0xFF7C3AED;
    }

    private int colorForStatus(String status) {
        if (STATUS_CONFIRMED.equals(status)) {
            return 0xFF15803D;
        }
        if (STATUS_SUSPENDED.equals(status)) {
            return 0xFFB45309;
        }
        return 0xFFB91C1C;
    }

    private String labelForType(String type) {
        if (TYPE_RESERVATION.equals(type)) {
            return getString(R.string.sa_logs_filter_reservations);
        }
        if (TYPE_PAYMENT.equals(type)) {
            return getString(R.string.sa_logs_filter_payments);
        }
        if (TYPE_APPOINTMENT.equals(type)) {
            return getString(R.string.sa_logs_filter_appointments);
        }
        return getString(R.string.sa_logs_filter_enablements);
    }

    private String labelForStatus(String status) {
        if (STATUS_CONFIRMED.equals(status)) {
            return getString(R.string.sa_logs_filter_confirmed);
        }
        if (STATUS_SUSPENDED.equals(status)) {
            return getString(R.string.sa_logs_filter_suspended);
        }
        return getString(R.string.sa_logs_filter_failed);
    }

    private void updateChipStyles() {
        styleChip(filterAllLogsChip, "ALL".equals(selectedTypeFilter));
        styleChip(filterReservationsLogsChip, TYPE_RESERVATION.equals(selectedTypeFilter));
        styleChip(filterPaymentsLogsChip, TYPE_PAYMENT.equals(selectedTypeFilter));
        styleChip(filterAppointmentsLogsChip, TYPE_APPOINTMENT.equals(selectedTypeFilter));
        styleChip(filterEnablementsLogsChip, TYPE_ENABLEMENT.equals(selectedTypeFilter));
    }

    private void showTagFilterMenu() {
        if (logsFiltersButton == null) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(this, logsFiltersButton);
        popupMenu.getMenu().add(0, 1, 0, getString(R.string.sa_logs_tag_all));
        popupMenu.getMenu().add(0, 2, 1, getString(R.string.sa_logs_tag_status_confirmed));
        popupMenu.getMenu().add(0, 3, 2, getString(R.string.sa_logs_tag_status_suspended));
        popupMenu.getMenu().add(0, 4, 3, getString(R.string.sa_logs_tag_status_failed));
        popupMenu.getMenu().add(0, 5, 4, getString(R.string.sa_logs_tag_module_payments));
        popupMenu.getMenu().add(0, 6, 5, getString(R.string.sa_logs_tag_module_appointments));
        popupMenu.getMenu().add(0, 7, 6, getString(R.string.sa_logs_tag_company_north));
        popupMenu.getMenu().add(0, 8, 7, getString(R.string.sa_logs_tag_company_sur));

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    selectedTagFilter = TAG_ALL;
                    break;
                case 2:
                    selectedTagFilter = TAG_STATUS_CONFIRMED;
                    break;
                case 3:
                    selectedTagFilter = TAG_STATUS_SUSPENDED;
                    break;
                case 4:
                    selectedTagFilter = TAG_STATUS_FAILED;
                    break;
                case 5:
                    selectedTagFilter = TAG_MODULE_PAYMENTS;
                    break;
                case 6:
                    selectedTagFilter = TAG_MODULE_APPOINTMENTS;
                    break;
                case 7:
                    selectedTagFilter = TAG_COMPANY_NORTH;
                    break;
                case 8:
                    selectedTagFilter = TAG_COMPANY_SUR;
                    break;
                default:
                    selectedTagFilter = TAG_ALL;
                    break;
            }
            expandedLogId = -1;
            applyFiltersAndRender(getSearchText());
            return true;
        });
        popupMenu.show();
    }

    private String labelForTag(String tag) {
        if (TAG_STATUS_CONFIRMED.equals(tag)) {
            return getString(R.string.sa_logs_tag_status_confirmed);
        }
        if (TAG_STATUS_SUSPENDED.equals(tag)) {
            return getString(R.string.sa_logs_tag_status_suspended);
        }
        if (TAG_STATUS_FAILED.equals(tag)) {
            return getString(R.string.sa_logs_tag_status_failed);
        }
        if (TAG_MODULE_PAYMENTS.equals(tag)) {
            return getString(R.string.sa_logs_tag_module_payments);
        }
        if (TAG_MODULE_APPOINTMENTS.equals(tag)) {
            return getString(R.string.sa_logs_tag_module_appointments);
        }
        if (TAG_COMPANY_NORTH.equals(tag)) {
            return getString(R.string.sa_logs_tag_company_north);
        }
        if (TAG_COMPANY_SUR.equals(tag)) {
            return getString(R.string.sa_logs_tag_company_sur);
        }
        return getString(R.string.sa_logs_tag_all);
    }

    private List<String> tags(String... values) {
        return Arrays.asList(values);
    }

    private void styleChip(TextView chip, boolean selected) {
        if (chip == null) {
            return;
        }
        chip.setBackgroundResource(selected
                ? R.drawable.sa_filter_chip_selected_bg
                : R.drawable.sa_filter_chip_unselected_bg);
        chip.setTextColor(ContextCompat.getColor(
                this,
                selected ? R.color.white : R.color.overlay_white_30
        ));
    }

    private String getSearchText() {
        if (searchLogsInput == null || searchLogsInput.getText() == null) {
            return "";
        }
        return searchLogsInput.getText().toString();
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

    private static class LogItem {
        private final int id;
        private final String type;
        private final String status;
        private final String time;
        private final String title;
        private final String source;
        private final String detailLabel;
        private final String detailAction;
        private final String metaLabel;
        private final String metaPrimary;
        private final String metaSecondary;
        private final List<String> tags;

        private LogItem(int id, String type, String status, String time, String title, String source, String detailLabel, String detailAction, String metaLabel, String metaPrimary, String metaSecondary, List<String> tags) {
            this.id = id;
            this.type = type;
            this.status = status;
            this.time = time;
            this.title = title;
            this.source = source;
            this.detailLabel = detailLabel;
            this.detailAction = detailAction;
            this.metaLabel = metaLabel;
            this.metaPrimary = metaPrimary;
            this.metaSecondary = metaSecondary;
            this.tags = tags;
        }
    }
}
