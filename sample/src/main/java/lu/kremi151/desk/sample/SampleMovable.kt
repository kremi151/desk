package lu.kremi151.desk.sample

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import lu.kremi151.desk.datamodel.Movable

class SampleMovable: Movable() {

    private val paint1 = Paint().apply {
        color = Color.parseColor("#bada55")
        style = Paint.Style.FILL
    }
    private val paint2 = Paint().apply {
        color = Color.parseColor("#55daba")
        style = Paint.Style.FILL
    }

    override var width: Float = 400.0f
        private set

    override var height: Float = 400.0f
        private set

    override fun remeasure(desiredWidth: Float, desiredHeight: Float) {
        width = desiredWidth
        height = desiredHeight
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