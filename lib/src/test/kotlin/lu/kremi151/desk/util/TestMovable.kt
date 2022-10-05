package lu.kremi151.desk.util

import android.graphics.Canvas
import lu.kremi151.desk.api.Movable

class TestMovable(
    private var mWidth: Float,
    private var mHeight: Float,
): Movable() {

    var focused: Boolean = false

    override val width: Float
        get() = mWidth
    override val height: Float
        get() = mHeight

    override fun remeasure(desiredWidth: Float, desiredHeight: Float) {
        mWidth = desiredWidth
        mHeight = desiredHeight
    }

    override fun onFocus() {
        focused = true
    }

    override fun onBlur() {
        focused = false
    }

    override fun draw(canvas: Canvas) {}
}