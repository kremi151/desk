package lu.kremi151.desk.config

import android.graphics.Color

data class DeskViewConfig(
    val backgroundColor: Int = Color.WHITE,
    val debugMode: Boolean = false,
    val hardwareAccelerated: Boolean = false,
    val containMovables: Boolean = false,
    val swipeThreshold: Int = 100,
)
