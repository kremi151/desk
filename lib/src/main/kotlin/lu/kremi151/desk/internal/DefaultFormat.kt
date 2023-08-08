package lu.kremi151.desk.internal

import lu.kremi151.desk.api.Format

internal object DefaultFormat: Format {

    override fun fromViewPixels(px: Float): Float = px

    override fun toViewPixels(px: Float): Float = px

    override fun onLayoutChanged(newWidth: Float, newHeight: Float) {
        // No-op
    }

}
