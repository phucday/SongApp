package com.example.testrun.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;

public class WaveformSeekBar extends AppCompatSeekBar {
    private Paint paintCompleted;
    private Paint paintRemaining;
    private float[] waveform;
    private float cornerRadius;
    private float barSpacing;

    public WaveformSeekBar(Context context) {
        super(context);
        init(context);
    }

    public WaveformSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WaveformSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
//        setThumb(null);
        setProgressDrawable(null);
        paintCompleted = new Paint();
        paintCompleted.setColor(Color.DKGRAY); // Màu của phần đã hoàn thành
        paintCompleted.setStyle(Paint.Style.FILL);
        paintCompleted.setAntiAlias(true);

        paintRemaining = new Paint();
        paintRemaining.setColor(Color.GRAY); // Màu của phần chưa hoàn thành
        paintRemaining.setStyle(Paint.Style.FILL);
        paintRemaining.setAntiAlias(true);

        cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
        barSpacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (waveform != null && waveform.length > 0) {
            float barWidth = ((float) getWidth() - (waveform.length - 1) * barSpacing) / waveform.length;
            float centerY = getHeight() / 2f;
            int progressIndex = (int) (((float) getProgress() / getMax()) * waveform.length);

            for (int i = 0; i < waveform.length; i++) {
                float barHeight = waveform[i] * getHeight();
                float left = i * (barWidth + barSpacing);
                float top = centerY - barHeight / 2;
                float right = left + barWidth;
                float bottom = centerY + barHeight / 2;
                Paint paint = i <= progressIndex ? paintCompleted : paintRemaining;
                canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, paint);
            }
        }
    }

    public void setWaveform(float[] waveform) {
        this.waveform = waveform;
        invalidate();
    }
}
