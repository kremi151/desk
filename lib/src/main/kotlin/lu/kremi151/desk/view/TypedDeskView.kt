package lu.kremi151.desk.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import lu.kremi151.desk.R
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

    var debugMode: Boolean = false
        set(value) {
            field = value
            thread?.debugMode = value
        }

    private var mBackgroundColor: Int = Color.WHITE
        set(value) {
            field = value
            thread?.backgroundColor = value
        }

    final override fun setBackgroundColor(color: Int) {
        mBackgroundColor = color
    }

    init {
        holder.addCallback(surfaceHolderCallback)

        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.TypedDeskView, defStyleAttr, R.style.DeskView)
        debugMode = typedArray.getBoolean(R.styleable.TypedDeskView_deskView_debugMode, false)
        setBackgroundColor(typedArray.getColor(R.styleable.TypedDeskView_deskView_backgroundColor, Color.WHITE))
        typedArray.recycle()
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
        thread = DeskViewThread(
            surfaceHolder = holder,
            movables = movables,
            backgroundColor = mBackgroundColor,
            debugMode = debugMode,
        ).also {
            it.start()
        }
    }

}