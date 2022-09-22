package lu.kremi151.desk.datamodel

import android.graphics.Canvas

interface Movable {

    val width: Float
    val height: Float

    fun remeasure(desiredWidth: Float, desiredHeight: Float)

    fun draw(canvas: Canvas)

    fun onMoved(x: Float, y: Float) {
        // No-op by default
    }

}
