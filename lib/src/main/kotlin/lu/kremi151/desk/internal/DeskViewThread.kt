package lu.kremi151.desk.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.SurfaceHolder
import lu.kremi151.desk.BuildConfig
import lu.kremi151.desk.datamodel.Movable
import java.util.concurrent.Semaphore

internal class DeskViewThread<MovableT : Movable>(
    private val surfaceHolder: SurfaceHolder,
    private val movables: MovableCollection<MovableT>,
    backgroundColor: Int,
    debugMode: Boolean,
): Thread("DeskView thread") {

    @Volatile
    private var running = true

    private val s = Semaphore(0)

    var backgroundColor: Int = backgroundColor
        set(value) {
            field = value
            invalidate()
        }

    var debugMode: Boolean = debugMode
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
                    val canvas = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        // TODO: Test it
                        surfaceHolder.lockHardwareCanvas()
                    } else {
                        surfaceHolder.lockCanvas()
                    }

                    canvas.drawColor(backgroundColor)

                    draw(canvas)
                    postDraw(canvas)

                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
                s.acquire()
            }
        } finally {
            movables.removeListener(listener)
        }
    }

    private fun draw(canvas: Canvas) {
        movables.forEach {
            canvas.save()
            canvas.translate(it.x, it.y)
            it.movable.draw(canvas)
            canvas.restore()
        }
    }

    private fun postDraw(canvas: Canvas) {
        if (BuildConfig.DEBUG && debugMode) {
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 28.0f
            }
            val text = "Last re-draw: ${System.currentTimeMillis()}"
            canvas.drawText(text, 0f, canvas.height.toFloat() - 28.0f, paint)
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