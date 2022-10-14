package lu.kremi151.desk.api

import android.graphics.Canvas

interface DeskViewLayer {

    fun onSizeChanged(width: Int, height: Int) {
        // No-op by default
    }

    fun draw(canvas: Canvas)

}
