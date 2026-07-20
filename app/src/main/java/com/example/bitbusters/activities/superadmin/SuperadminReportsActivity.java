package com.example.bitbusters.activities.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.bitbusters.utils.ImmersiveMode;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bitbusters.R;
import com.example.bitbusters.views.charts.SuperadminTrendLineChartView;

public class SuperadminReportsActivity extends AppCompatActivity {

    private static final int PERIOD_MONTH = 0;
    private static final int PERIOD_QUARTER = 1;
    private static final int PERIOD_YEAR = 2;

    private TextView reportsPeriodText;
    private TextView reservationsValueText;
    private TextView paymentsValueText;
    private TextView attendanceValueText;
    private TextView expiredValueText;
    private TextView paymentApprovalValueText;
    private TextView enablementTimeValueText;
    private TextView donutTotalText;
    private TextView trendXAxisText;
    private TextView growthValueText;
    private TextView monthlyAvgValueText;
    private SuperadminTrendLineChartView monthlyTrendChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_reports);

        bindInsets();
        setupClicks();
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

    private void setupClicks() {
        reportsPeriodText = findViewById(R.id.reportsPeriodText);
        reservationsValueText = findViewById(R.id.reportsReservationsValueText);
        paymentsValueText = findViewById(R.id.reportsPaymentsValueText);
        attendanceValueText = findViewById(R.id.reportsAttendanceValueText);
        expiredValueText = findViewById(R.id.reportsExpiredValueText);
        paymentApprovalValueText = findViewById(R.id.reportsPaymentApprovalValueText);
        enablementTimeValueText = findViewById(R.id.reportsEnablementTimeValueText);
        donutTotalText = findViewById(R.id.reportsDonutTotalText);
        trendXAxisText = findViewById(R.id.reportsTrendXAxisText);
        growthValueText = findViewById(R.id.reportsGrowthValueText);
        monthlyAvgValueText = findViewById(R.id.reportsMonthlyAvgValueText);
        monthlyTrendChart = findViewById(R.id.monthlyTrendChart);

        View reportsPeriodChip = findViewById(R.id.reportsPeriodChip);
        View navHome = findViewById(R.id.navHome);
        View navUsers = findViewById(R.id.navUsers);
        View navApprovals = findViewById(R.id.navApprovals);
        View navLogs = findViewById(R.id.navLogs);

        applyPeriodData(PERIOD_MONTH);

        if (reportsPeriodChip != null) {
            reportsPeriodChip.setOnClickListener(this::showPeriodMenu);
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> openAndFinish(SuperadminControlCenterActivity.class));
        }
        if (navUsers != null) {
            navUsers.setOnClickListener(v -> openAndFinish(SuperadminUsersActivity.class));
        }
        if (navApprovals != null) {
            navApprovals.setOnClickListener(v -> openAndFinish(SuperadminApprovalsActivity.class));
        }
        if (navLogs != null) {
            navLogs.setOnClickListener(v -> openAndFinish(SuperadminLogsActivity.class));
        }
    }

    private void showPeriodMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, PERIOD_MONTH, 0, getString(R.string.sa_reports_period_month));
        menu.getMenu().add(0, PERIOD_QUARTER, 1, getString(R.string.sa_reports_period_quarter));
        menu.getMenu().add(0, PERIOD_YEAR, 2, getString(R.string.sa_reports_period_year));

        menu.setOnMenuItemClickListener(item -> {
            applyPeriodData(item.getItemId());
            return true;
        });
        menu.show();
    }

    private void applyPeriodData(int period) {
        ReportPeriodData data;
        if (period == PERIOD_QUARTER) {
            data = getQuarterData();
        } else if (period == PERIOD_YEAR) {
            data = getYearData();
        } else {
            data = getMonthData();
        }

        if (reportsPeriodText != null) {
            reportsPeriodText.setText(data.periodLabel);
        }
        if (reservationsValueText != null) {
            reservationsValueText.setText(data.totalReservations);
        }
        if (paymentsValueText != null) {
            paymentsValueText.setText(data.totalIncome);
        }
        if (attendanceValueText != null) {
            attendanceValueText.setText(data.attendanceRate);
        }
        if (expiredValueText != null) {
            expiredValueText.setText(data.expiredRate);
        }
        if (paymentApprovalValueText != null) {
            paymentApprovalValueText.setText(data.paymentApprovalRate);
        }
        if (enablementTimeValueText != null) {
            enablementTimeValueText.setText(data.enablementTime);
        }
        if (donutTotalText != null) {
            donutTotalText.setText(data.totalReservations);
        }
        if (growthValueText != null) {
            growthValueText.setText(data.growth);
        }
        if (monthlyAvgValueText != null) {
            monthlyAvgValueText.setText(data.monthlyAverage);
        }
        if (trendXAxisText != null) {
            trendXAxisText.setText(data.axisLabels);
        }
        if (monthlyTrendChart != null) {
            monthlyTrendChart.setValues(data.trendValues);
        }
    }

    private ReportPeriodData getMonthData() {
        return new ReportPeriodData(
                getString(R.string.sa_reports_period_month),
                "182",
                "S/ 1.26M",
                "89%",
                "11%",
                "93%",
                "2.6h",
                "34%",
                "89%",
                "Ene   Feb   Mar   Abr   May   Jun   Jul   Ago   Sep",
                new float[]{10f, 14f, 18f, 16f, 21f, 23f, 24.5f, 26f, 28f}
        );
    }

    private ReportPeriodData getQuarterData() {
        return new ReportPeriodData(
                getString(R.string.sa_reports_period_quarter),
                "524",
                "S/ 3.54M",
                "91%",
                "9%",
                "95%",
                "2.2h",
                "18%",
                "90%",
                "T1         T2         T3         T4",
                new float[]{58f, 66f, 74f, 79f}
        );
    }

    private ReportPeriodData getYearData() {
        return new ReportPeriodData(
                getString(R.string.sa_reports_period_year),
                "2,041",
                "S/ 13.72M",
                "88%",
                "12%",
                "92%",
                "3.0h",
                "27%",
                "87%",
                "2022      2023      2024      2025      2026",
                new float[]{280f, 325f, 352f, 401f, 447f}
        );
    }

    private static class ReportPeriodData {
        private final String periodLabel;
        private final String totalReservations;
        private final String totalIncome;
        private final String attendanceRate;
        private final String expiredRate;
        private final String paymentApprovalRate;
        private final String enablementTime;
        private final String growth;
        private final String monthlyAverage;
        private final String axisLabels;
        private final float[] trendValues;

        private ReportPeriodData(
                String periodLabel,
                String totalReservations,
                String totalIncome,
                String attendanceRate,
                String expiredRate,
                String paymentApprovalRate,
                String enablementTime,
                String growth,
                String monthlyAverage,
                String axisLabels,
                float[] trendValues
        ) {
            this.periodLabel = periodLabel;
            this.totalReservations = totalReservations;
            this.totalIncome = totalIncome;
            this.attendanceRate = attendanceRate;
            this.expiredRate = expiredRate;
            this.paymentApprovalRate = paymentApprovalRate;
            this.enablementTime = enablementTime;
            this.growth = growth;
            this.monthlyAverage = monthlyAverage;
            this.axisLabels = axisLabels;
            this.trendValues = trendValues;
        }
    }

    private void open(Class<?> destination) {
        startActivity(new Intent(this, destination));
    }

    private void openAndFinish(Class<?> destination) {
        open(destination);
        finish();
    }
}
