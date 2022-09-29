package lu.kremi151.desk.datamodel

import android.graphics.Canvas
import java.util.*

abstract class Movable {

    internal var id: UUID? = null

    var x: Float = 0.0f
        internal set
    var y: Float = 0.0f
        internal set

    abstract val width: Float
    abstract val height: Float

    open val locked: Boolean get() = false

    abstract fun remeasure(desiredWidth: Float, desiredHeight: Float)

    abstract fun draw(canvas: Canvas)

    open fun onMoved(x: Float, y: Float) {
        // No-op by default
    }

    open fun onFocus() {
        // No-op by default
    }

    open fun onBlur() {
        // No-op by default
    }

}
