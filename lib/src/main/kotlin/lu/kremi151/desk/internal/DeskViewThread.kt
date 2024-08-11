package lu.kremi151.desk.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import lu.kremi151.desk.BuildConfig
import lu.kremi151.desk.api.DeskViewContext
import lu.kremi151.desk.api.DeskViewLayer
import lu.kremi151.desk.api.DrawErrorHandling
import lu.kremi151.desk.api.Format
import lu.kremi151.desk.api.TypedMovable
import lu.kremi151.desk.config.DeskViewConfig
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Semaphore

internal class DeskViewThread<MovableT : TypedMovable<ID, ContextT>, ID, ContextT: DeskViewContext>(
    private val surfaceHolder: SurfaceHolder,
    private val movables: MovableCollection<MovableT, ID, ContextT>,
    private val underlays: CopyOnWriteArrayList<DeskViewLayer>,
    private val overlays: CopyOnWriteArrayList<DeskViewLayer>,
    initialWidth: Int,
    initialHeight: Int,
    config: DeskViewConfig,
): Thread("DeskView thread") {

    companion object {
        private const val DEBUG_PAINT_FONT_SIZE = 28.0f
    }

    @Volatile
    private var running = true

    @Volatile
    var surfaceSize = Size(initialWidth, initialHeight)

    private val s = Semaphore(0)

    var config: DeskViewConfig = config
        set(value) {
            field = value
            invalidate()
        }

    var format: Format = DefaultFormat
        set(value) {
            field = value
            invalidate()
        }

    private val listener = object : MovableCollection.Listener<MovableT, ID, ContextT> {
        override fun onChanged(collection: MovableCollection<MovableT, ID, ContextT>) {
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
        } ?: return

        canvas.drawColor(config.backgroundColor)

        preDraw(canvas)
        draw(canvas)
        postDraw(canvas)

        surfaceHolder.unlockCanvasAndPost(canvas)
    }

    private fun preDraw(canvas: Canvas) {
        underlays.forEach {
            it.draw(canvas)
        }
    }

    private fun draw(canvas: Canvas) {
        val translation = config.translation
        with(format) {
            val scale = toViewPixels(1.0f)
            movables.forEach {
                synchronized(it) {
                    canvas.save()
                    canvas.translate(
                        toViewPixels(it.x + translation.x),
                        toViewPixels(it.y + translation.y),
                    )
                    canvas.scale(scale, scale)
                    it.drawWithErrorHandling(canvas)
                    canvas.restore()
                }
            }
        }
    }

    private fun MovableT.drawWithErrorHandling(canvas: Canvas) {
        try {
            draw(canvas)
        } catch (e: Exception) {
            reportDrawingError(this, e)
            val handling = onDrawError(e)
            applyErrorHandling(this, handling, canvas)
        }
    }

    private fun applyErrorHandling(movableT: MovableT, handling: DrawErrorHandling, canvas: Canvas) {
        when(handling) {
            is DrawErrorHandling.Retry -> movableT.drawWithRetries(canvas, handling.attempts, handling.then)
            is DrawErrorHandling.Ignore -> ignoreDrawError(handling.invalidate)
        }
    }

    private fun MovableT.drawWithRetries(canvas: Canvas,
                                         attempts: Int,
                                         fallback: DrawErrorHandling) {
        for (i in 1 until attempts) {
            try {
                draw(canvas)
                return
            } catch (e: Exception) {
                reportDrawingError(this, e)
            }
        }
        applyErrorHandling(this, fallback, canvas)
    }

    private fun ignoreDrawError(invalidateOnError: Boolean) {
        if (invalidateOnError) {
            s.release()
        }
    }

    private fun reportDrawingError(movable: MovableT, e: Exception) {
        Log.e(javaClass.simpleName, "Draw error for movable ${movable.id}", e)
    }

    private fun postDraw(canvas: Canvas) {
        overlays.forEach {
            it.draw(canvas)
        }
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
