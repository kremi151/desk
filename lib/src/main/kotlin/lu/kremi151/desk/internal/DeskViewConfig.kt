package lu.kremi151.desk.internal

import android.graphics.Color

data class DeskViewConfig(
    val backgroundColor: Int = Color.WHITE,
    val debugMode: Boolean = false,
    val hardwareAccelerated: Boolean = false,
)
