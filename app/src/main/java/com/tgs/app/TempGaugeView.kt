package com.tgs.app

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class TempGaugeView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f // Adjust for thickness
        strokeCap = Paint.Cap.ROUND
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(10f, 0f, 0f, Color.GRAY)
    }

    private val startAngle = 135f  // Start from lower-left
    private val sweepAngle = 270f  // Sweep across top to lower-right
    private var temperature = 24.2f // Default temperature
    private var minTemp = 10f       // Adjust as needed
    private var maxTemp = 100f       // Adjust as needed

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Define the arc bounds
        val padding = 50f
        val rect = RectF(padding, padding, width - padding, height - padding)

        val colors = intArrayOf(
            Color.parseColor("#87CEEB"), // Light Blue (Cold)
            Color.parseColor("#5CB9A1"), // Teal
            Color.parseColor("#FFF48C"), // Yellow (Neutral)
            Color.parseColor("#F4A460"), // Sandy Brown
            Color.parseColor("#F17381"), // Reddish-Pink (Hot)
            Color.parseColor("#87CEEB")  // Repeat Blue for smooth blend
        )

        val colorPositions = floatArrayOf(
            0.0f, 0.15f, 0.35f, 0.6f, 0.85f, 1.0f // Added last blue at 1.0
        )

        // Apply SweepGradient with the unique colors
        arcPaint.shader = SweepGradient(
            width / 2f, height / 2f,
            colors,
            colorPositions
        )

        // Rotate canvas so gradient starts correctly
        canvas.save()
        canvas.rotate(135f, width / 2f, height / 2f)
        canvas.drawArc(rect, 0f, sweepAngle, false, arcPaint)
        canvas.restore()

        // Calculate indicator position
        val angle = startAngle + ((temperature - minTemp) / (maxTemp - minTemp)) * sweepAngle
        val radius = (width - padding * 2) / 2f
        val cx = (width / 2f) + radius * cos(Math.toRadians(angle.toDouble())).toFloat()
        val cy = (height / 2f) + radius * sin(Math.toRadians(angle.toDouble())).toFloat()

        // Draw indicator circle
        canvas.drawCircle(cx, cy, 35f, indicatorPaint)
    }

    // Function to update temperature dynamically
    fun setTemperature(temp: Float) {
        temperature = temp
        invalidate() // Redraw view
    }
}