package lu.kremi151.desk.api

import lu.kremi151.desk.internal.DefaultFormat

interface Format {

    fun fromViewPixels(px: Float): Float
    fun toViewPixels(px: Float): Float

    fun onLayoutChanged(newWidth: Float, newHeight: Float)

    companion object {
        @JvmStatic
        val DEFAULT: Format = DefaultFormat
    }

}
