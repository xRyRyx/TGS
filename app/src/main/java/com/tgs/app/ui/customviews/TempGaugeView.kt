package com.tgs.app.ui.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class TempGaugeView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(10f, 0f, 0f, Color.GRAY)
    }

    private val startAngle = 135f
    private val sweepAngle = 270f
    private var temperature = 24.2f
    private var minTemp = -40f
    private var maxTemp = 125f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 50f
        val rect = RectF(padding, padding, width - padding, height - padding)

        val colors = intArrayOf(
            Color.parseColor("#87CEEB"),
            Color.parseColor("#5CB9A1"),
            Color.parseColor("#FFF48C"),
            Color.parseColor("#F4A460"),
            Color.parseColor("#F17381"),
            Color.parseColor("#87CEEB")
        )

        val colorPositions = floatArrayOf(
            0.0f, 0.15f, 0.35f, 0.6f, 0.85f, 1.0f
        )

        arcPaint.shader = SweepGradient(
            width / 2f, height / 2f,
            colors,
            colorPositions
        )

        canvas.save()
        canvas.rotate(135f, width / 2f, height / 2f)
        canvas.drawArc(rect, 0f, sweepAngle, false, arcPaint)
        canvas.restore()

        val angle = startAngle + ((temperature - minTemp) / (maxTemp - minTemp)) * sweepAngle
        val radius = (width - padding * 2) / 2f
        val cx = (width / 2f) + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
        val cy = (height / 2f) + radius * sin(Math.toRadians(angle.toDouble())).toFloat()

        canvas.drawCircle(cx, cy, 35f, indicatorPaint)
    }

    fun setTemperature(temp: Float) {
        temperature = temp
        invalidate()
    }
}