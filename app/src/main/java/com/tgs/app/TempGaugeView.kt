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
    private var maxTemp = 40f       // Adjust as needed

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Define the arc bounds
        val padding = 50f
        val rect = RectF(padding, padding, width - padding, height - padding)

        // Gradient for rainbow arc
        arcPaint.shader = SweepGradient(
            width / 2f, height / 2f,
            intArrayOf(Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED),
            floatArrayOf(0.0f, 0.3f, 0.7f, 1.0f) // Color stops
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
        canvas.drawCircle(cx, cy, 15f, indicatorPaint)
    }

    // Function to update temperature dynamically
    fun setTemperature(temp: Float) {
        temperature = temp
        invalidate() // Redraw view
    }
}