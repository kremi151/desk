package lu.kremi151.desk.api

import android.graphics.Canvas

interface DeskViewOverlay {

    fun drawOverlay(canvas: Canvas, width: Int, height: Int)

}
