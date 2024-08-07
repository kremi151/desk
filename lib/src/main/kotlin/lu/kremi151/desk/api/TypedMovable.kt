package lu.kremi151.desk.api

import android.graphics.Canvas

abstract class TypedMovable<ID, ContextT: DeskViewContext>(internal val id: ID) {

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
    @Suppress("LongParameterList")
    open fun onMovedAndResized(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        prevX: Float,
        prevY: Float,
        prevWidth: Float,
        prevHeight: Float,
    ) {
        onMoved(x, y, prevX, prevY)
        onResized(width, height, prevWidth, prevHeight)
    }

    /**
     * Callback for when the movable has been tapped by the user
     * @param x The x coordinate, relative to the movable's position
     * @param y the y coordinate, relative to the movable's position
     * @return true if the movable can be dragged along any swipe gesture, false otherwise
     */
    open fun onTapped(x: Float, y: Float): Boolean {
        // No-op by default
        return true
    }

    /**
     * Callback for when the movable is about to begin scaling due to user interaction
     * @param focusX The x coordinate of the focus point, relative to the movable's position
     * @param focusY the y coordinate of the focus point, relative to the movable's position
     * @return true if the movable can be scaled using pinch gesture, false otherwise
     */
    open fun onScaleStart(focusX: Float, focusY: Float): Boolean {
        // No-op by default
        return true
    }

    /**
     * Callback for when movable finishes scaling due to user interaction.
     * Note that this will only be called if [onScaleStart] returns true.
     */
    open fun onScaleEnd() {
        // No-op by default
    }

    open fun onFocus() {
        // No-op by default
    }

    open fun onBlur() {
        // No-op by default
    }

}
