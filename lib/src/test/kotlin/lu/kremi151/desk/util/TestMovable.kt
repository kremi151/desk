package lu.kremi151.desk.util

import android.graphics.Canvas
import lu.kremi151.desk.api.Movable

class TestMovable(
    private var mWidth: Float,
    private var mHeight: Float,
): Movable() {

    var focusedCounter = 0
    var tappedX: Float = -1.0f
    var tappedY: Float = -1.0f
    var movedX: Float = -1.0f
    var movedY: Float = -1.0f

    override val width: Float
        get() = mWidth
    override val height: Float
        get() = mHeight

    override fun remeasure(desiredWidth: Float, desiredHeight: Float) {
        mWidth = desiredWidth
        mHeight = desiredHeight
    }

    override fun onFocus() {
        focusedCounter++
    }

    override fun onBlur() {
        focusedCounter = 0
    }

    override fun onTapped(x: Float, y: Float) {
        tappedX = x
        tappedY = y
    }

    override fun onMoved(x: Float, y: Float) {
        movedX = x
        movedY = y
    }

    @Suppress("EmptyFunctionBlock")
    override fun draw(canvas: Canvas) {}
}
