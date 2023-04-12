package lu.kremi151.desk.api

import android.graphics.Canvas

abstract class TypedMovable<ID, ContextT>(internal val id: ID) {

    internal var bound = false

    var context: ContextT? = null
        internal set

    abstract val x: Float
    abstract val y: Float

    abstract val width: Float
    abstract val height: Float

    open val locked: Boolean get() = false

    abstract fun move(desiredX: Float, desiredY: Float)

    abstract fun remeasure(desiredWidth: Float, desiredHeight: Float)

    abstract fun draw(canvas: Canvas)

    /**
     * Called when the [Movable] has been moved based on user interaction
     *
     * @param x     The new x position
     * @param y     The new y position
     * @param prevX The previous x position
     * @param prevY The previous y position
     */
    open fun onMoved(x: Float, y: Float, prevX: Float, prevY: Float) {
        // No-op by default
    }

    /**
     * Called when the [Movable] has been resized based on user interaction
     *
     * @param width      The new width
     * @param height     The new height
     * @param prevWidth  The previous width
     * @param prevHeight The previous height
     */
    open fun onResized(width: Float, height: Float, prevWidth: Float, prevHeight: Float) {
        // No-op by default
    }

    /**
     * Called when the [Movable] has been moved and resizes simultaneously based on user interaction
     *
     * @param x          The new x position
     * @param y          The new y position
     * @param width      The new width
     * @param height     The new height
     * @param prevX      The previous x position
     * @param prevY      The previous y position
     * @param prevWidth  The previous width
     * @param prevHeight The previous height
     */
    open fun onMovedAndResized(x: Float, y: Float, width: Float, height: Float, prevX: Float, prevY: Float, prevWidth: Float, prevHeight: Float) {
        onMoved(x, y, prevX, prevY)
        onResized(width, height, prevWidth, prevHeight)
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
