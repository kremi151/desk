package lu.kremi151.desk.api

import android.graphics.Canvas

abstract class TypedMovable<ID>(internal val id: ID) {

    internal var bound = false

    var x: Float = 0.0f
        internal set
    var y: Float = 0.0f
        internal set

    abstract val width: Float
    abstract val height: Float

    open val locked: Boolean get() = false

    abstract fun remeasure(desiredWidth: Float, desiredHeight: Float)

    abstract fun draw(canvas: Canvas)

    open fun onMoved(x: Float, y: Float, interaction: Boolean) {
        // No-op by default
    }

    open fun onResized(width: Float, height: Float, prevWidth: Float, prevHeight: Float, interaction: Boolean) {
        // No-op by default
    }

    open fun onMovedAndResized(x: Float, y: Float, width: Float, height: Float, prevWidth: Float, prevHeight: Float, interaction: Boolean) {
        onMoved(x, y, interaction)
        onResized(width, height, prevWidth, prevHeight, interaction)
    }

    open fun onTapped(x: Float, y: Float) {
        // No-op by default
    }

    open fun onFocus() {
        // No-op by default
    }

    open fun onBlur() {
        // No-op by default
    }

}
