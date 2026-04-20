package com.example.bitbusters.views.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SuperadminTrendLineChartView extends View {

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float[] values = new float[]{10f, 14f, 18f, 16f, 21f, 23f, 24.5f, 26f};

    public SuperadminTrendLineChartView(Context context) {
        super(context);
        init();
    }

    public SuperadminTrendLineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuperadminTrendLineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setColor(Color.parseColor("#D9E2EC"));

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setColor(Color.parseColor("#7ACF58"));

        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStrokeWidth(3f);

        areaPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float left = dp(8);
        float top = dp(8);
        float right = w - dp(8);
        float bottom = h - dp(18);

        drawGrid(canvas, left, top, right, bottom);

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (float v : values) {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        float range = Math.max(1f, max - min);

        Path linePath = new Path();
        Path areaPath = new Path();

        float stepX = (right - left) / (values.length - 1f);

        for (int i = 0; i < values.length; i++) {
            float x = left + stepX * i;
            float norm = (values[i] - min) / range;
            float y = bottom - (norm * (bottom - top));

            if (i == 0) {
                linePath.moveTo(x, y);
                areaPath.moveTo(x, bottom);
                areaPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                areaPath.lineTo(x, y);
            }
        }

        areaPath.lineTo(right, bottom);
        areaPath.close();

        areaPaint.setShader(new LinearGradient(
                0f,
                top,
                0f,
                bottom,
                Color.parseColor("#667ACF58"),
                Color.parseColor("#107ACF58"),
                Shader.TileMode.CLAMP
        ));

        canvas.drawPath(areaPath, areaPaint);
        canvas.drawPath(linePath, linePaint);

        Paint pointStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointStroke.setStyle(Paint.Style.STROKE);
        pointStroke.setStrokeWidth(3f);
        pointStroke.setColor(Color.parseColor("#7ACF58"));

        for (int i = 0; i < values.length; i++) {
            float x = left + stepX * i;
            float norm = (values[i] - min) / range;
            float y = bottom - (norm * (bottom - top));
            canvas.drawCircle(x, y, dp(3), pointPaint);
            canvas.drawCircle(x, y, dp(3), pointStroke);
        }
    }

    private void drawGrid(Canvas canvas, float left, float top, float right, float bottom) {
        float section = (bottom - top) / 4f;
        for (int i = 0; i <= 4; i++) {
            float y = top + section * i;
            canvas.drawLine(left, y, right, y, gridPaint);
        }
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
