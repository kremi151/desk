package lu.kremi151.desk.view

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import lu.kremi151.desk.datamodel.Movable
import lu.kremi151.desk.internal.DeskViewThread
import lu.kremi151.desk.internal.MovableCollection

open class TypedDeskView<MovableT : Movable> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : SurfaceView(context, attrs, defStyleAttr) {

    private var thread: DeskViewThread<MovableT>? = null
    private val movables = MovableCollection<MovableT>()

    private val surfaceHolderCallback = object : SurfaceHolder.Callback2 {

        override fun surfaceCreated(holder: SurfaceHolder) {
            resume()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            invalidate()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            pause()
        }

        override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
            invalidate()
        }

    }

    init {
        holder.addCallback(surfaceHolderCallback)
    }

    override fun invalidate() {
        super.invalidate()
        thread?.invalidate()
    }

    fun addMovable(movable: MovableT) {
        movables.add(movable)
    }

    fun removeMovable(movable: MovableT): Boolean {
        return movables.remove(movable)
    }

    private fun pause() {
        val thread = thread ?: return
        thread.quit()
        try {
            thread.join()
        } catch (e: InterruptedException) {}
        this.thread = null
    }

    private fun resume() {
        thread = DeskViewThread(holder, movables).also {
            it.start()
        }
    }

}