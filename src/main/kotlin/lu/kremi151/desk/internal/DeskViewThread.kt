package lu.kremi151.desk.internal

import android.graphics.Canvas
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.SurfaceHolder
import lu.kremi151.desk.datamodel.Movable

internal class DeskViewThread<MovableT : Movable>(
    private val surfaceHolder: SurfaceHolder,
    private val movables: MovableCollection<MovableT>,
): HandlerThread("DeskView handler") {

    private lateinit var mHandler: Handler

    override fun onLooperPrepared() {
        val queue = Looper.myQueue()
        queue.addIdleHandler {
            if (surfaceHolder.surface.isValid) {
                val canvas = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    // TODO: Test it
                    surfaceHolder.lockHardwareCanvas()
                } else {
                    surfaceHolder.lockCanvas()
                }

                draw(canvas)

                surfaceHolder.unlockCanvasAndPost(canvas)
            }
            true
        }

        mHandler = Handler(looper)
        movables.handler = mHandler
    }

    override fun run() {
        super.run()

        // Detach handler
        movables.handler = null
    }

    private fun draw(canvas: Canvas) {
        // TODO: Draw
    }

}