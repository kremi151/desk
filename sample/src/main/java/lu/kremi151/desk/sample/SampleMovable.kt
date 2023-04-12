package lu.kremi151.desk.sample

import android.graphics.Canvas
import android.graphics.Paint
import lu.kremi151.desk.api.SimpleMovable

class SampleMovable(
    id: Long,
    color1: Int,
    color2: Int,
    width: Float,
    height: Float,
    initialX: Float = 0.0f,
    initialY: Float = 0.0f,
): SimpleMovable(id, width, height, initialX, initialY) {

    private val paint1 = Paint().apply {
        color = color1
        style = Paint.Style.FILL
    }
    private val paint2 = Paint().apply {
        color = color2
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        val hw = width / 2f
        val hh = height / 2f
        canvas.drawRect(0f, 0f, hw, hh, paint1)
        canvas.drawRect(hw, hh, width, height, paint1)
        canvas.drawRect(hw, 0f, width, hh, paint2)
        canvas.drawRect(0f, hh, hw, height, paint2)
    }
}