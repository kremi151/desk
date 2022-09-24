package lu.kremi151.desk.view

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import lu.kremi151.desk.datamodel.Movable

class TypedDeskView<MovableT : Movable> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs) {

    private var running = false
    private var thread: Thread? = null

    private val surfaceHolderCallback = object : SurfaceHolder.Callback2 {

        override fun surfaceCreated(holder: SurfaceHolder) {
            resume()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            pause()
        }

        override fun surfaceRedrawNeeded(holder: SurfaceHolder) {}

    }

    init {
        holder.addCallback(surfaceHolderCallback)
    }

    private fun pause() {
        running = false
        try {
            thread?.join()
        } catch (e: InterruptedException) {}
    }

    private fun resume() {
        running = true
        thread = Thread(drawCallback).also {
            it.start()
        }
    }

    private val drawCallback = Runnable {
        val h = holder
        while (running) {
            if (!h.surface.isValid) {
                continue
            }
            val canvas = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // TODO: Test it
                h.lockHardwareCanvas()
            } else {
                h.lockCanvas()
            }

            // TODO: Draw

            h.unlockCanvasAndPost(canvas)
        }
    }

}