package lu.kremi151.desk.sample

import android.graphics.Canvas
import android.graphics.Paint
import android.util.SizeF
import lu.kremi151.desk.api.Movable
import kotlin.math.min

class AspectRatioKeepingMovable(
    id: Long,
    private val unfocusedColor: Int,
    private val focusedColor: Int,
    private val aspectRatio: Float,
    height: Float,
    initialX: Float = 0.0f,
    initialY: Float = 0.0f,
): Movable(id) {
    private val paint = Paint().apply {
        this.color = unfocusedColor
        style = Paint.Style.STROKE
        strokeWidth = 10.0f
        textSize = 26.0f
    }

    private val referenceSize = SizeF(height * aspectRatio, height)

    override var width: Float = referenceSize.width
        private set

    override var height: Float = referenceSize.height
        private set

    override var x: Float = initialX
        private set

    override var y: Float = initialY
        private set

    override fun remeasure(desiredWidth: Float, desiredHeight: Float) {
        val scale = min(desiredWidth / referenceSize.width, desiredHeight / referenceSize.height)

        // We use min to avoid rounding errors in order to not exceed the given size bounds
        width = min(referenceSize.width * scale, desiredWidth)
        height = min(referenceSize.height * scale, desiredHeight)
    }

    override fun move(desiredX: Float, desiredY: Float) {
        x = desiredX
        y = desiredY
    }

    override fun draw(canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        canvas.drawRect(0.0f, 0.0f, width, height, paint)

        paint.style = Paint.Style.FILL
        val text = "Aspect ratio: $aspectRatio"
        val textWidth = paint.measureText(text)
        canvas.drawText(text, width - textWidth - 18.0f, height - 26.0f, paint)
    }

    override fun onFocus() {
        paint.color = focusedColor
    }

    override fun onBlur() {
        paint.color = unfocusedColor
    }

}