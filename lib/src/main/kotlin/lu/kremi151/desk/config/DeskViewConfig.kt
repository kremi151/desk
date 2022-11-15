package lu.kremi151.desk.config

import android.graphics.Color
import android.graphics.PointF

data class DeskViewConfig(
    val backgroundColor: Int = Color.WHITE,
    val debugMode: Boolean = false,
    val hardwareAccelerated: Boolean = false,
    val containMovables: Boolean = false,
    val ignoreTouchEvents: Boolean = false,
    val swipeThreshold: Int = 100,
    val translation: PointF = PointF(0f, 0f),
)
