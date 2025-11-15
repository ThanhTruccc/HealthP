package com.example.healthprofile;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

public class HealthGaugeView extends View {

    private Paint arcPaint;
    private Paint arcBackgroundPaint;
    private Paint needlePaint;
    private Paint needleShadowPaint;
    private Paint centerCirclePaint;
    private Paint centerCircleBorderPaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint descriptionPaint;
    private RectF arcRect;

    private int healthScore = 0;
    private int targetScore = 76;
    private float needleAngle = -90; // Start at leftmost position
    private ValueAnimator scoreAnimator;

    // Color zones - Updated for better visual appeal
    private static final int COLOR_CRITICAL = 0xFFE74C3C;    // Red (0-49)
    private static final int COLOR_LOW = 0xFFFF6B6B;         // Light Red (50-69)
    private static final int COLOR_MEDIUM = 0xFFFFA726;      // Orange (70-79)
    private static final int COLOR_GOOD = 0xFF66BB6A;        // Green (80-89)
    private static final int COLOR_EXCELLENT = 0xFF4CAF50;   // Dark Green (90-100)

    // Background arc color
    private static final int COLOR_BACKGROUND = 0xFFE0E0E0;

    public HealthGaugeView(Context context) {
        super(context);
        init();
    }

    public HealthGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Arc background paint
        arcBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcBackgroundPaint.setStyle(Paint.Style.STROKE);
        arcBackgroundPaint.setStrokeWidth(30f);
        arcBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        arcBackgroundPaint.setColor(COLOR_BACKGROUND);

        // Arc paint for colored segments
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(30f);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Needle paint with shadow effect
        needleShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needleShadowPaint.setColor(0x40000000); // Semi-transparent black
        needleShadowPaint.setStrokeWidth(10f);
        needleShadowPaint.setStrokeCap(Paint.Cap.ROUND);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(0xFF2C3E50);
        needlePaint.setStrokeWidth(6f);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);

        // Center circle with border
        centerCircleBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerCircleBorderPaint.setColor(0xFF2C3E50);
        centerCircleBorderPaint.setStyle(Paint.Style.FILL);

        centerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerCirclePaint.setColor(Color.WHITE);
        centerCirclePaint.setStyle(Paint.Style.FILL);

        // Score text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF2C3E50);
        textPaint.setTextSize(80f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // Label text paint
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(0xFF757575);
        labelPaint.setTextSize(28f);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        // Description text paint
        descriptionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        descriptionPaint.setColor(0xFF9E9E9E);
        descriptionPaint.setTextSize(24f);
        descriptionPaint.setTextAlign(Paint.Align.CENTER);

        arcRect = new RectF();
    }

    public void setHealthScore(int score) {
        // Clamp score between 0-100
        score = Math.max(0, Math.min(100, score));
        this.targetScore = score;
        animateScore(score);
    }

    private void animateScore(int targetScore) {
        if (scoreAnimator != null && scoreAnimator.isRunning()) {
            scoreAnimator.cancel();
        }

        scoreAnimator = ValueAnimator.ofInt(healthScore, targetScore);
        scoreAnimator.setDuration(1500);
        scoreAnimator.setInterpolator(new DecelerateInterpolator());
        scoreAnimator.addUpdateListener(animation -> {
            healthScore = (int) animation.getAnimatedValue();
            needleAngle = -90 + (healthScore * 180f / 100f);
            invalidate();
        });
        scoreAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = (int) (height * 0.65f); // Move center down a bit
        int radius = Math.min(width, height) / 2 - 80;

        // Set arc bounds
        arcRect.set(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius);

        // Draw background arc (full semicircle)
        canvas.drawArc(arcRect, 180, 180, false, arcBackgroundPaint);

        // Draw colored arc segments based on score zones
        drawColoredArcs(canvas);

        // Draw tick marks
        drawTickMarks(canvas, centerX, centerY, radius);

        // Draw zone labels
        drawZoneLabels(canvas, centerX, centerY, radius);

        // Draw needle shadow
        drawNeedle(canvas, centerX, centerY, radius - 20, needleShadowPaint, 3);

        // Draw needle
        drawNeedle(canvas, centerX, centerY, radius - 20, needlePaint, 0);

        // Draw center circles
        canvas.drawCircle(centerX, centerY, 22, centerCircleBorderPaint);
        canvas.drawCircle(centerX, centerY, 18, centerCirclePaint);

        // Draw score at the bottom center
        drawScoreDisplay(canvas, centerX, centerY, radius);
    }

    private void drawColoredArcs(Canvas canvas) {
        // Calculate how much arc to fill based on current score
        float currentSweep = healthScore * 180f / 100f;

        // Draw each zone with appropriate color
        float[] zoneBreakpoints = {0, 50, 70, 80, 90, 100};
        int[] zoneColors = {COLOR_CRITICAL, COLOR_LOW, COLOR_MEDIUM, COLOR_GOOD, COLOR_EXCELLENT};

        for (int i = 0; i < zoneColors.length; i++) {
            float zoneStart = zoneBreakpoints[i] * 180f / 100f;
            float zoneEnd = zoneBreakpoints[i + 1] * 180f / 100f;

            if (currentSweep > zoneStart) {
                float sweepAngle = Math.min(currentSweep - zoneStart, zoneEnd - zoneStart);
                arcPaint.setColor(zoneColors[i]);
                canvas.drawArc(arcRect, 180 + zoneStart, sweepAngle, false, arcPaint);
            }
        }
    }

    private void drawTickMarks(Canvas canvas, int centerX, int centerY, int radius) {
        Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStrokeWidth(2f);
        tickPaint.setColor(0xFFBDBDBD);

        // Draw major ticks at 0, 25, 50, 75, 100
        for (int i = 0; i <= 100; i += 25) {
            float angle = 180 + (i * 180f / 100f);
            double angleRad = Math.toRadians(angle);

            float innerRadius = radius - 15;
            float outerRadius = radius - 5;

            if (i % 50 == 0) {
                tickPaint.setStrokeWidth(3f);
                innerRadius = radius - 20;
            } else {
                tickPaint.setStrokeWidth(2f);
            }

            float x1 = centerX + (float) (innerRadius * Math.cos(angleRad));
            float y1 = centerY + (float) (innerRadius * Math.sin(angleRad));
            float x2 = centerX + (float) (outerRadius * Math.cos(angleRad));
            float y2 = centerY + (float) (outerRadius * Math.sin(angleRad));

            canvas.drawLine(x1, y1, x2, y2, tickPaint);
        }
    }

    private void drawZoneLabels(Canvas canvas, int centerX, int centerY, int radius) {
        Paint zonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zonePaint.setTextSize(20f);
        zonePaint.setTextAlign(Paint.Align.CENTER);

        String[] labels = {"0", "50", "100"};
        float[] scores = {0, 50, 100};

        for (int i = 0; i < labels.length; i++) {
            float angle = 180 + (scores[i] * 180f / 100f);
            double angleRad = Math.toRadians(angle);

            float labelRadius = radius + 35;
            float x = centerX + (float) (labelRadius * Math.cos(angleRad));
            float y = centerY + (float) (labelRadius * Math.sin(angleRad)) + 8;

            zonePaint.setColor(0xFF757575);
            canvas.drawText(labels[i], x, y, zonePaint);
        }
    }

    private void drawNeedle(Canvas canvas, int centerX, int centerY, int length, Paint paint, float offset) {
        double angleRad = Math.toRadians(needleAngle);
        float needleEndX = centerX + (float) (length * Math.cos(angleRad)) + offset;
        float needleEndY = centerY + (float) (length * Math.sin(angleRad)) + offset;

        canvas.drawLine(centerX + offset, centerY + offset, needleEndX, needleEndY, paint);
    }

    private void drawScoreDisplay(Canvas canvas, int centerX, int centerY, int radius) {
        // Draw score number
        float scoreY = centerY + radius + 80;
        canvas.drawText(String.valueOf(healthScore), centerX, scoreY, textPaint);

        // Draw "Health Score" label
        labelPaint.setTextSize(24f);
        canvas.drawText("Health Score", centerX, scoreY + 35, labelPaint);

        // Draw health status description
        String status = getHealthStatus(healthScore);
        int statusColor = getStatusColor(healthScore);
        descriptionPaint.setColor(statusColor);
        descriptionPaint.setFakeBoldText(true);
        canvas.drawText(status, centerX, scoreY + 65, descriptionPaint);
    }

    private String getHealthStatus(int score) {
        if (score < 50) return "Critical - Need Attention";
        if (score < 70) return "Poor - Needs Improvement";
        if (score < 80) return "Fair - Keep Working";
        if (score < 90) return "Good - Well Done!";
        return "Excellent - Outstanding!";
    }

    private int getStatusColor(int score) {
        if (score < 50) return COLOR_CRITICAL;
        if (score < 70) return COLOR_LOW;
        if (score < 80) return COLOR_MEDIUM;
        if (score < 90) return COLOR_GOOD;
        return COLOR_EXCELLENT;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (scoreAnimator != null && scoreAnimator.isRunning()) {
            scoreAnimator.cancel();
        }
    }
}