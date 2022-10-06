package lu.kremi151.desk.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.SurfaceHolder
import lu.kremi151.desk.BuildConfig
import lu.kremi151.desk.api.Movable
import lu.kremi151.desk.config.DeskViewConfig
import java.util.concurrent.Semaphore

internal class DeskViewThread<MovableT : Movable>(
    private val surfaceHolder: SurfaceHolder,
    private val movables: MovableCollection<MovableT>,
    config: DeskViewConfig,
): Thread("DeskView thread") {

    companion object {
        private const val DEBUG_PAINT_FONT_SIZE = 28.0f
    }

    @Volatile
    private var running = true

    private val s = Semaphore(0)

    var config: DeskViewConfig = config
        set(value) {
            field = value
            invalidate()
        }

    private val listener = object : MovableCollection.Listener<MovableT> {
        override fun onChanged(collection: MovableCollection<MovableT>) {
            invalidate()
        }
    };

    override fun run() {
        movables.addListener(listener)
        try {
            while (running) {
                if (surfaceHolder.surface.isValid) {
                    acquireAndDrawCanvas()
                }
                s.acquire()
            }
        } finally {
            movables.removeListener(listener)
        }
    }

    private fun acquireAndDrawCanvas() {
        val config = config
        val canvas = if (config.hardwareAccelerated
            && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // TODO: Test it (#1)
            surfaceHolder.lockHardwareCanvas()
        } else {
            surfaceHolder.lockCanvas()
        }

        canvas.drawColor(config.backgroundColor)

        draw(canvas)
        postDraw(canvas)

        surfaceHolder.unlockCanvasAndPost(canvas)
    }

    private fun draw(canvas: Canvas) {
        movables.forEach {
            canvas.save()
            canvas.translate(it.x, it.y)
            it.draw(canvas)
            canvas.restore()
        }
    }

    private fun postDraw(canvas: Canvas) {
        if (BuildConfig.DEBUG && config.debugMode) {
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = DEBUG_PAINT_FONT_SIZE
            }
            val text = "Last re-draw: ${System.currentTimeMillis()}"
            canvas.drawText(text, 0f, canvas.height.toFloat() - DEBUG_PAINT_FONT_SIZE, paint)
        }
    }

    fun quit() {
        running = false
        invalidate()
    }

    fun invalidate() {
        s.release()
    }

}
