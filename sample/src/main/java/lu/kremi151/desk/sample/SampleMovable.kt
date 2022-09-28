package lu.kremi151.desk.sample

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import lu.kremi151.desk.datamodel.Movable

class SampleMovable: Movable() {

    private val paint = Paint().apply {
        color = Color.parseColor("#bada55")
        style = Paint.Style.FILL
    }

    override val width: Float
        get() = 200.0f

    override val height: Float
        get() = 200.0f

    override fun remeasure(desiredWidth: Float, desiredHeight: Float) {
        // TODO: Dynamic sizing
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width, height, paint)
    }
}