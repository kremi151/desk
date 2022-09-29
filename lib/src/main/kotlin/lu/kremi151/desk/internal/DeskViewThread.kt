package lu.kremi151.desk.internal

import android.graphics.Canvas
import android.graphics.Color
import android.view.SurfaceHolder
import lu.kremi151.desk.datamodel.Movable
import java.util.concurrent.Semaphore

internal class DeskViewThread<MovableT : Movable>(
    private val surfaceHolder: SurfaceHolder,
    private val movables: MovableCollection<MovableT>,
): Thread("DeskView thread") {

    @Volatile
    private var running = true

    private val s = Semaphore(0)

    var backgroundColor: Int = Color.WHITE
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

    fun quit() {
        running = false
        invalidate()
    }

    fun invalidate() {
        s.release()
    }

}