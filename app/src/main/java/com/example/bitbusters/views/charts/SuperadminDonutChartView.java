package com.example.bitbusters.views.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SuperadminDonutChartView extends View {

    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private final float[] values = new float[]{68f, 24f, 8f};
    private final int[] colors = new int[]{
            Color.parseColor("#7ACF58"),
            Color.parseColor("#FBBF24"),
            Color.parseColor("#EF4444")
    };

    public SuperadminDonutChartView(Context context) {
        super(context);
        init();
    }

    public SuperadminDonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuperadminDonutChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        segmentPaint.setStyle(Paint.Style.STROKE);
        segmentPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height);
        float ringThickness = size * 0.22f;
        float pad = ringThickness / 2f + 2f;

        arcBounds.set((width - size) / 2f + pad, (height - size) / 2f + pad,
                (width + size) / 2f - pad, (height + size) / 2f - pad);

        segmentPaint.setStrokeWidth(ringThickness);

        float total = 0f;
        for (float value : values) {
            total += value;
        }

        float startAngle = -90f;
        for (int i = 0; i < values.length; i++) {
            float sweep = (values[i] / total) * 360f;
            segmentPaint.setColor(colors[i]);
            canvas.drawArc(arcBounds, startAngle, sweep - 1.5f, false, segmentPaint);
            startAngle += sweep;
        }
    }
}
