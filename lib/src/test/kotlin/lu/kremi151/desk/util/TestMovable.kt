package lu.kremi151.desk.util

import android.graphics.Canvas
import lu.kremi151.desk.api.Movable

class TestMovable(
    id: Long,
    private var mWidth: Float,
    private var mHeight: Float,
    private var mX: Float,
    private var mY: Float,
): Movable(id) {

    var focusedCounter = 0
    var tappedX: Float = -1.0f
    var tappedY: Float = -1.0f

    override val width: Float
        get() = mWidth
    override val height: Float
        get() = mHeight
    override val x: Float
        get() = mX
    override val y: Float
        get() = mY

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

    override fun move(desiredX: Float, desiredY: Float) {
        mX = desiredX
        mY = desiredY
    }

    @Suppress("EmptyFunctionBlock")
    override fun draw(canvas: Canvas) {}
}
