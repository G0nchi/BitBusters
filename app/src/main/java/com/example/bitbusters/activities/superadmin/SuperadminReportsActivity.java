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
import com.example.bitbusters.data.SuperadminMetricsRepository;
import com.example.bitbusters.data.SuperadminMetricsSnapshot;
import com.example.bitbusters.utils.MoneyParser;
import com.example.bitbusters.utils.PeriodoUtils;
import com.example.bitbusters.views.charts.SuperadminDonutChartView;
import com.example.bitbusters.views.charts.SuperadminTrendLineChartView;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SuperadminReportsActivity extends AppCompatActivity {

    private static final int PERIOD_MONTH = 0;
    private static final int PERIOD_QUARTER = 1;
    private static final int PERIOD_YEAR = 2;

    private static final int MAX_COMPANIAS_MOSTRADAS = 4;
    private static final double UMBRAL_ALERTA_VENCIDAS = 10.0;

    private final SuperadminMetricsRepository metricsRepository = new SuperadminMetricsRepository();
    private SuperadminMetricsSnapshot snapshot;
    private int currentPeriod = PERIOD_MONTH;

    private TextView reportsPeriodText;
    private TextView reservationsValueText;
    private TextView paymentsValueText;
    private TextView cancelacionValueText;
    private TextView expiredValueText;
    private TextView habilitacionValueText;
    private TextView donutTotalText;
    private TextView donutVigentesText;
    private TextView donutVencidasText;
    private TextView trendXAxisText;
    private TextView growthValueText;
    private TextView monthlyAvgValueText;
    private SuperadminTrendLineChartView monthlyTrendChart;
    private SuperadminDonutChartView salesDonutChart;

    private View reportsAlertCard;
    private TextView reportsAlertBodyText;

    private View[] companyHeaders;
    private View[] companyBars;
    private TextView[] companyNames;
    private TextView[] companyPercents;
    private View[] companyFills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersiveMode.apply(this);
        setContentView(R.layout.activity_superadmin_reports);

        bindInsets();
        setupClicks();
        cargarMetricasReales();
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
        cancelacionValueText = findViewById(R.id.reportsCancelacionValueText);
        expiredValueText = findViewById(R.id.reportsExpiredValueText);
        habilitacionValueText = findViewById(R.id.reportsHabilitacionValueText);
        donutTotalText = findViewById(R.id.reportsDonutTotalText);
        donutVigentesText = findViewById(R.id.reportsDonutVigentesText);
        donutVencidasText = findViewById(R.id.reportsDonutVencidasText);
        trendXAxisText = findViewById(R.id.reportsTrendXAxisText);
        growthValueText = findViewById(R.id.reportsGrowthValueText);
        monthlyAvgValueText = findViewById(R.id.reportsMonthlyAvgValueText);
        monthlyTrendChart = findViewById(R.id.monthlyTrendChart);
        salesDonutChart = findViewById(R.id.salesDonutChart);
        reportsAlertCard = findViewById(R.id.reportsAlertCard);
        reportsAlertBodyText = findViewById(R.id.reportsAlertBodyText);

        companyHeaders = new View[]{
                findViewById(R.id.companyRow1Header), findViewById(R.id.companyRow2Header),
                findViewById(R.id.companyRow3Header), findViewById(R.id.companyRow4Header)
        };
        companyBars = new View[]{
                findViewById(R.id.companyRow1Bar), findViewById(R.id.companyRow2Bar),
                findViewById(R.id.companyRow3Bar), findViewById(R.id.companyRow4Bar)
        };
        companyNames = new TextView[]{
                findViewById(R.id.companyRow1Name), findViewById(R.id.companyRow2Name),
                findViewById(R.id.companyRow3Name), findViewById(R.id.companyRow4Name)
        };
        companyPercents = new TextView[]{
                findViewById(R.id.companyRow1Percent), findViewById(R.id.companyRow2Percent),
                findViewById(R.id.companyRow3Percent), findViewById(R.id.companyRow4Percent)
        };
        companyFills = new View[]{
                findViewById(R.id.companyRow1Fill), findViewById(R.id.companyRow2Fill),
                findViewById(R.id.companyRow3Fill), findViewById(R.id.companyRow4Fill)
        };

        View reportsPeriodChip = findViewById(R.id.reportsPeriodChip);
        View navHome = findViewById(R.id.navHome);
        View navUsers = findViewById(R.id.navUsers);
        View navApprovals = findViewById(R.id.navApprovals);
        View navLogs = findViewById(R.id.navLogs);

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

    private void cargarMetricasReales() {
        metricsRepository.loadSnapshot(loaded -> {
            if (isFinishing() || isDestroyed()) return;
            snapshot = loaded;
            applyPeriodData(currentPeriod);
        });
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
        currentPeriod = period;
        if (snapshot == null) {
            return;
        }

        Date ahora = PeriodoUtils.ahora();
        Date inicioActual;
        Date inicioAnterior;
        int bucketsCount;
        int mesesPorBucket;
        String periodLabel;

        switch (period) {
            case PERIOD_QUARTER:
                inicioActual = PeriodoUtils.inicioDeTrimestre(0);
                inicioAnterior = PeriodoUtils.inicioDeTrimestre(-1);
                bucketsCount = 4;
                mesesPorBucket = 3;
                periodLabel = getString(R.string.sa_reports_period_quarter);
                break;
            case PERIOD_YEAR:
                inicioActual = PeriodoUtils.inicioDeAnio(0);
                inicioAnterior = PeriodoUtils.inicioDeAnio(-1);
                bucketsCount = 5;
                mesesPorBucket = 12;
                periodLabel = getString(R.string.sa_reports_period_year);
                break;
            default:
                inicioActual = PeriodoUtils.inicioDeMes(0);
                inicioAnterior = PeriodoUtils.inicioDeMes(-1);
                bucketsCount = 9;
                mesesPorBucket = 1;
                periodLabel = getString(R.string.sa_reports_period_month);
                break;
        }

        if (reportsPeriodText != null) reportsPeriodText.setText(periodLabel);

        int reservas = snapshot.reservasEnRango(inicioActual, ahora);
        Double ingresos = snapshot.ingresosEnRango(inicioActual, ahora);
        Double vencidasRate = snapshot.tasaVencidasEnRango(inicioActual, ahora);
        Double cancelacionRate = snapshot.tasaCancelacionCitasEnRango(inicioActual, ahora);
        Double habilitacionRate = snapshot.tasaHabilitacionAsesoresEnRango(inicioActual, ahora);

        setText(reservationsValueText, String.valueOf(reservas));
        setText(donutTotalText, String.valueOf(reservas));
        setText(paymentsValueText, ingresos != null ? MoneyParser.formatCompacto(ingresos) : "S/ 0");
        setText(expiredValueText, formatPorcentaje(vencidasRate));
        setText(cancelacionValueText, formatPorcentaje(cancelacionRate));
        setText(habilitacionValueText, formatPorcentaje(habilitacionRate));

        bindDonut(vencidasRate);
        bindTendencia(bucketsCount, mesesPorBucket);
        bindCrecimiento(inicioActual, inicioAnterior);
        bindReparto(inicioActual, ahora);
        bindAlerta(inicioActual, ahora);
    }

    private void bindDonut(Double vencidasRate) {
        double vencidas = vencidasRate != null ? vencidasRate : 0.0;
        double vigentes = 100.0 - vencidas;
        setText(donutVigentesText, String.format(Locale.getDefault(), "%.0f%%", vigentes));
        setText(donutVencidasText, String.format(Locale.getDefault(), "%.0f%%", vencidas));
        if (salesDonutChart != null) {
            salesDonutChart.setData(
                    new float[]{(float) Math.max(vigentes, 0.01), (float) Math.max(vencidas, 0.01)},
                    new int[]{android.graphics.Color.parseColor("#7ACF58"), android.graphics.Color.parseColor("#EF4444")}
            );
        }
    }

    private void bindTendencia(int bucketsCount, int mesesPorBucket) {
        float[] valores = snapshot.tendenciaPorBuckets(bucketsCount, mesesPorBucket);
        String[] etiquetas = snapshot.etiquetasBuckets(bucketsCount, mesesPorBucket);

        if (monthlyTrendChart != null) {
            monthlyTrendChart.setValues(valores);
        }
        if (trendXAxisText != null) {
            trendXAxisText.setText(String.join("     ", etiquetas));
        }

        float suma = 0f;
        for (float v : valores) suma += v;
        float promedio = valores.length > 0 ? suma / valores.length : 0f;
        setText(monthlyAvgValueText, String.format(Locale.getDefault(), "%.0f", promedio));
    }

    private void bindCrecimiento(Date inicioActual, Date inicioAnterior) {
        int actual = snapshot.reservasEnRango(inicioActual, PeriodoUtils.ahora());
        int anterior = snapshot.reservasEnRango(inicioAnterior, inicioActual);
        if (anterior > 0) {
            double variacion = ((actual - anterior) * 100.0) / anterior;
            String signo = variacion >= 0 ? "+" : "";
            setText(growthValueText, String.format(Locale.getDefault(), "%s%.0f%%", signo, variacion));
        } else {
            setText(growthValueText, "—");
        }
    }

    private void bindReparto(Date from, Date to) {
        List<SuperadminMetricsSnapshot.CompanyShare> reparto =
                snapshot.repartoPorInmobiliariaEnRango(from, to, MAX_COMPANIAS_MOSTRADAS);

        for (int i = 0; i < companyHeaders.length; i++) {
            if (i < reparto.size()) {
                SuperadminMetricsSnapshot.CompanyShare share = reparto.get(i);
                setVisible(companyHeaders[i], true);
                setVisible(companyBars[i], true);
                setText(companyNames[i], share.nombre);
                setText(companyPercents[i], String.format(Locale.getDefault(), "%.0f%%", share.porcentaje));
                float fillWeight = (float) Math.max(share.porcentaje, 0.5);
                setWeight(companyFills[i], fillWeight);
                if (companyBars[i] instanceof android.widget.LinearLayout) {
                    android.widget.LinearLayout bar = (android.widget.LinearLayout) companyBars[i];
                    if (bar.getChildCount() > 1) {
                        setWeight(bar.getChildAt(1), 100f - fillWeight);
                    }
                }
            } else {
                setVisible(companyHeaders[i], false);
                setVisible(companyBars[i], false);
            }
        }
    }

    private void bindAlerta(Date from, Date to) {
        Map<String, Double> tasasPorInmobiliaria = snapshot.tasaVencidasPorInmobiliariaEnRango(from, to);
        String peorInmobiliaria = null;
        double peorTasa = 0;
        for (Map.Entry<String, Double> entry : tasasPorInmobiliaria.entrySet()) {
            if (entry.getValue() > peorTasa) {
                peorTasa = entry.getValue();
                peorInmobiliaria = entry.getKey();
            }
        }

        if (reportsAlertCard == null) return;
        if (peorInmobiliaria != null && peorTasa > UMBRAL_ALERTA_VENCIDAS) {
            reportsAlertCard.setVisibility(View.VISIBLE);
            setText(reportsAlertBodyText, getString(R.string.sa_reports_alert_body_dynamic, peorInmobiliaria, peorTasa));
        } else {
            reportsAlertCard.setVisibility(View.GONE);
        }
    }

    private String formatPorcentaje(Double value) {
        return value != null ? String.format(Locale.getDefault(), "%.0f%%", value) : "N/D";
    }

    private void setText(TextView view, String value) {
        if (view != null) view.setText(value);
    }

    private void setVisible(View view, boolean visible) {
        if (view != null) view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setWeight(View view, float weight) {
        if (view == null || !(view.getLayoutParams() instanceof android.widget.LinearLayout.LayoutParams)) return;
        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) view.getLayoutParams();
        params.weight = weight;
        view.setLayoutParams(params);
    }

    private void open(Class<?> destination) {
        startActivity(new Intent(this, destination));
    }

    private void openAndFinish(Class<?> destination) {
        open(destination);
        finish();
    }
}
