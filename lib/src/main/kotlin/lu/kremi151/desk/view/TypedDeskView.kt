package lu.kremi151.desk.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import lu.kremi151.desk.R
import lu.kremi151.desk.datamodel.Movable
import lu.kremi151.desk.internal.DeskViewThread
import lu.kremi151.desk.internal.MovableCollection
import lu.kremi151.desk.internal.MovableState

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

    private var mPosX: Float = 0f
    private var mPosY: Float = 0f
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f
    private var mActivePointerId: Int = MotionEvent.INVALID_POINTER_ID
    private var mActiveMovable: MovableState<MovableT>? = null

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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                event.actionIndex.also { pointerIndex ->
                    // Remember where we started (for dragging)
                    mLastTouchX = event.getX(pointerIndex)
                    mLastTouchY = event.getY(pointerIndex)
                    mActiveMovable = movables.findFirstState { m ->
                        m.x <= mLastTouchX
                                && m.y <= mLastTouchY
                                && m.x + m.movable.width >= mLastTouchX
                                && m.y + m.movable.height >= mLastTouchY
                    }?.also { m ->
                        mPosX = m.x
                        mPosY = m.y
                    }
                }

                // Save the ID of this pointer (for dragging)
                mActivePointerId = event.getPointerId(0)
            }

            MotionEvent.ACTION_MOVE -> {
                // Find the index of the active pointer and fetch its position
                val (x: Float, y: Float) =
                    event.findPointerIndex(mActivePointerId).let { pointerIndex ->
                        // Calculate the distance moved
                        event.getX(pointerIndex) to event.getY(pointerIndex)
                    }

                mPosX += x - mLastTouchX
                mPosY += y - mLastTouchY

                mActiveMovable?.let {
                    it.x = mPosX
                    it.y = mPosY
                }

                invalidate()

                // Remember this touch position for the next move event
                mLastTouchX = x
                mLastTouchY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                event.actionIndex.also { pointerIndex ->
                    event.getPointerId(pointerIndex)
                        .takeIf { it == mActivePointerId }
                        ?.run {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            mLastTouchX = event.getX(newPointerIndex)
                            mLastTouchY = event.getY(newPointerIndex)
                            mActivePointerId = event.getPointerId(newPointerIndex)
                        }
                }
            }
        }
        return true
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