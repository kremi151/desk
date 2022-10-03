package lu.kremi151.desk.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import lu.kremi151.desk.R
import lu.kremi151.desk.datamodel.Movable
import lu.kremi151.desk.internal.DeskViewConfig
import lu.kremi151.desk.internal.DeskViewThread
import lu.kremi151.desk.internal.MovableCollection
import kotlin.math.max
import kotlin.math.min

open class TypedDeskView<MovableT : Movable> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : SurfaceView(context, attrs, defStyleAttr) {

    companion object {
        private const val SCALE_FACTOR_MINIMUM = 0.1f
        private const val SCALE_FACTOR_MAXIMUM = 5.0f
    }

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

    var config: DeskViewConfig = DeskViewConfig()
        set(value) {
            field = value
            thread?.config = value
        }

    private var mInitialPosX: Float = 0f
    private var mInitialPosY: Float = 0f
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f
    private var mActivePointerId: Int = MotionEvent.INVALID_POINTER_ID
    private var mActiveMovable: MovableT? = null

    private var mWidth: Int = 0
    private var mHeight: Int = 0

    final override fun setBackgroundColor(color: Int) {
        config = config.copy(backgroundColor = color)
    }

    init {
        holder.addCallback(surfaceHolderCallback)

        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.TypedDeskView, defStyleAttr, R.style.DeskView)

        val config = DeskViewConfig(
            debugMode = typedArray.getBoolean(R.styleable.TypedDeskView_deskView_debugMode, false),
            hardwareAccelerated = typedArray.getBoolean(R.styleable.TypedDeskView_deskView_hardwareAccelerated, false),
            containMovables = typedArray.getBoolean(R.styleable.TypedDeskView_deskView_containMovables, false),
            backgroundColor = typedArray.getColor(R.styleable.TypedDeskView_deskView_backgroundColor, Color.WHITE),
        )
        this.config = config
        typedArray.recycle()
    }

    override fun invalidate() {
        super.invalidate()
        thread?.invalidate()
    }

    private fun findMovableByPos(x: Float, y: Float): MovableT? = movables.findLast { m ->
        m.x <= x && m.y <= y && m.x + m.width >= x && m.y + m.height >= y
    }

    private val scaleListener = object : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            val movable = findMovableByPos(detector.focusX, detector.focusY)
            mActiveMovable = movable
            return movable != null
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = max(SCALE_FACTOR_MINIMUM, min(SCALE_FACTOR_MAXIMUM, detector.scaleFactor))
            mActiveMovable?.let {
                val oldWidth = it.width
                val oldHeight = it.height
                var newWidth = oldWidth * scaleFactor
                var newHeight = oldHeight * scaleFactor

                if (config.containMovables) {
                    if (newWidth > mWidth) {
                        newWidth = mWidth.toFloat()
                    }
                    if (newHeight > mHeight) {
                        newHeight = mHeight.toFloat()
                    }
                }

                it.remeasure(newWidth, newHeight)
                check(it.width <= newWidth) { "New width ${it.width} must not be larger than available $newWidth" }
                check(it.height <= newHeight) { "New height ${it.height} must not be larger than available $newHeight" }
                newWidth = it.width
                newHeight = it.height

                it.x += (oldWidth - newWidth) / 2f
                it.y += (oldHeight - newHeight) / 2f
                if (it.x + newWidth > mWidth) {
                    it.x = mWidth - it.width
                } else if (it.x < 0.0f) {
                    it.x = 0.0f
                }
                if (it.y + newHeight > mHeight) {
                    it.y = mHeight - it.height
                } else if (it.y < 0.0f) {
                    it.y = 0.0f
                }

                invalidate()
            }

            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            mActiveMovable = null
        }
    }
    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)

    @SuppressLint("ClickableViewAccessibility")
    @Suppress("ComplexMethod")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleDetector.onTouchEvent(event)
        if (mScaleDetector.isInProgress) {
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                event.actionIndex.also { pointerIndex ->
                    // Remember where we started (for dragging)
                    mLastTouchX = event.getX(pointerIndex)
                    mLastTouchY = event.getY(pointerIndex)
                    mActiveMovable = findMovableByPos(mLastTouchX, mLastTouchY)?.also { m ->
                        mInitialPosX = m.x
                        mInitialPosY = m.y
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

                mActiveMovable?.let {
                    val newX = it.x + x - mLastTouchX
                    val newY = it.y + y - mLastTouchY

                    if (config.containMovables) {
                        moveContained(it, newX, newY)
                    } else {
                        it.x = newX
                        it.y = newY
                    }
                    invalidate()
                }

                // Remember this touch position for the next move event
                mLastTouchX = x
                mLastTouchY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
                val activeMovable = mActiveMovable
                if (activeMovable != null && (mInitialPosX != activeMovable.x || mInitialPosY != activeMovable.y)) {
                    activeMovable.onMoved(activeMovable.x, activeMovable.y)
                }
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
    }

    private fun moveContained(movable: MovableT, newX: Float, newY: Float) {
        movable.x = if (newX < 0.0f) {
            0.0f
        } else if (newX > mWidth - movable.width) {
            mWidth - movable.width
        } else {
            newX
        }

        movable.y = if (newY < 0.0f) {
            0.0f
        } else if (newY > mHeight - movable.height) {
            mHeight - movable.height
        } else {
            newY
        }
    }

    fun addMovable(movable: MovableT, x: Float = 0.0f, y: Float = 0.0f) {
        movables.add(movable.apply {
            this.x = x
            this.y = y
        })
    }

    fun removeMovable(movable: MovableT): Boolean {
        return movables.remove(movable)
    }

    private fun pause() {
        val thread = thread ?: return
        thread.quit()
        try {
            thread.join()
        } catch (ignore: InterruptedException) {}
        this.thread = null
    }

    private fun resume() {
        thread = DeskViewThread(
            surfaceHolder = holder,
            movables = movables,
            config = config,
        ).also {
            it.start()
        }
    }

}
