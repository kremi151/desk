package lu.kremi151.desk.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import lu.kremi151.desk.R
import lu.kremi151.desk.api.DeskViewLayer
import lu.kremi151.desk.api.Format
import lu.kremi151.desk.api.Movable
import lu.kremi151.desk.config.DeskViewConfig
import lu.kremi151.desk.internal.DeskViewThread
import lu.kremi151.desk.internal.MovableCollection
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

@Suppress("TooManyFunctions")
open class TypedDeskView<MovableT : Movable> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : SurfaceView(context, attrs, defStyleAttr) {

    companion object {
        private const val SCALE_FACTOR_MINIMUM = 0.1f
        private const val SCALE_FACTOR_MAXIMUM = 5.0f

        private const val DEFAULT_SWIPE_THRESHOLD_MS = 100
    }

    private var thread: DeskViewThread<MovableT>? = null
    private val movables = MovableCollection<MovableT>()
    private val underlays = CopyOnWriteArrayList<DeskViewLayer>()
    private val overlays = CopyOnWriteArrayList<DeskViewLayer>()

    var format: Format = Format.DEFAULT
        set(value) {
            if (mWidth > 0 && mHeight > 0) {
                value.onLayoutChanged(mWidth.toFloat(), mHeight.toFloat())
            }
            field = value
            thread?.format = format
            invalidate()
        }

    private val surfaceHolderCallback = object : SurfaceHolder.Callback2 {

        override fun surfaceCreated(holder: SurfaceHolder) {
            resume()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            mWidth = width
            mHeight = height
            thread?.surfaceSize = Size(width, height)

            underlays.forEach { it.onSizeChanged(width, height) }
            overlays.forEach { it.onSizeChanged(width, height) }
            this@TypedDeskView.format.onLayoutChanged(width.toFloat(), height.toFloat())

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
    private var mTouchStarted: Long = 0L
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
            swipeThreshold = typedArray.getInt(
                R.styleable.TypedDeskView_deskView_swipeThreshold,
                DEFAULT_SWIPE_THRESHOLD_MS,
            ),
        )
        this.config = config
        typedArray.recycle()
    }

    override fun invalidate() {
        super.invalidate()
        thread?.invalidate()
    }

    private fun findMovableByViewPos(x: Float, y: Float): MovableT? = with(format) {
        val formatX = fromViewPixels(x)
        val formatY = fromViewPixels(y)
        movables.findLast { m ->
            m.x <= formatX && m.y <= formatY && m.x + m.width >= formatX && m.y + m.height >= formatY
        }
    }

    private val scaleListener = object : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            val movable = findMovableByViewPos(detector.focusX, detector.focusY)
            mActiveMovable = movable
            return movable != null
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = max(SCALE_FACTOR_MINIMUM, min(SCALE_FACTOR_MAXIMUM, detector.scaleFactor))

            val activeMovable = mActiveMovable ?: return true
            if (activeMovable.locked) {
                return true
            }

            val oldWidth = activeMovable.width
            val oldHeight = activeMovable.height
            var newWidth = oldWidth * scaleFactor
            var newHeight = oldHeight * scaleFactor

            if (config.containMovables) {
                with(format) {
                    val vWidth = fromViewPixels(mWidth.toFloat())
                    val vHeight = fromViewPixels(mHeight.toFloat())
                    if (newWidth > vWidth) {
                        newWidth = vWidth
                    }
                    if (newHeight > vHeight) {
                        newHeight = vHeight
                    }
                }
            }

            activeMovable.remeasure(newWidth, newHeight)
            with(activeMovable) {
                check(width <= newWidth) { "New width $width must not be larger than available $newWidth" }
                check(height <= newHeight) { "New height $height must not be larger than available $newHeight" }
                newWidth = width
                newHeight = height
            }

            activeMovable.x += (oldWidth - newWidth) / 2f
            activeMovable.y += (oldHeight - newHeight) / 2f
            if (config.containMovables) {
                with(format) {
                    val vWidth = fromViewPixels(mWidth.toFloat())
                    val vHeight = fromViewPixels(mHeight.toFloat())
                    if (activeMovable.x + newWidth > vWidth) {
                        activeMovable.x = vWidth - activeMovable.width
                    } else if (activeMovable.x < 0.0f) {
                        activeMovable.x = 0.0f
                    }
                    if (activeMovable.y + newHeight > vHeight) {
                        activeMovable.y = vHeight - activeMovable.height
                    } else if (activeMovable.y < 0.0f) {
                        activeMovable.y = 0.0f
                    }
                }
            }

            invalidate()

            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            mActiveMovable = null
        }
    }
    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleDetector.onTouchEvent(event)
        if (mScaleDetector.isInProgress) {
            return true
        }

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> handleActionDown(event)
            MotionEvent.ACTION_MOVE -> handleActionMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> handleActionUpOrCancel()
            MotionEvent.ACTION_POINTER_UP -> handleActionPointerUp(event)
        }
        return true
    }

    private fun handleActionDown(event: MotionEvent) {
        mTouchStarted = SystemClock.uptimeMillis()
        var shouldInvalidate = false

        event.actionIndex.also { pointerIndex ->
            // Remember where we started (for dragging)
            mLastTouchX = event.getX(pointerIndex)
            mLastTouchY = event.getY(pointerIndex)

            val currentActiveMovable = mActiveMovable
            val activeMovable = findMovableByViewPos(mLastTouchX, mLastTouchY)
            if (currentActiveMovable != null && activeMovable?.id != currentActiveMovable.id) {
                currentActiveMovable.onBlur()
                shouldInvalidate = true
            }
            if (activeMovable != null) {
                with(format) {
                    mInitialPosX = toViewPixels(activeMovable.x)
                    mInitialPosY = toViewPixels(activeMovable.y)
                    if (currentActiveMovable == null || activeMovable.id != currentActiveMovable.id) {
                        activeMovable.onFocus()
                    }
                    activeMovable.onTapped(
                        fromViewPixels(mLastTouchX - mInitialPosX),
                        fromViewPixels(mLastTouchY - mInitialPosY),
                    )
                }
                shouldInvalidate = true
            }
            mActiveMovable = activeMovable
        }

        if (shouldInvalidate) {
            invalidate()
        }

        // Save the ID of this pointer (for dragging)
        mActivePointerId = event.getPointerId(0)
    }

    private fun handleActionMove(event: MotionEvent) {
        // Find the index of the active pointer and fetch its position
        val (x: Float, y: Float) =
            event.findPointerIndex(mActivePointerId).let { pointerIndex ->
                // Calculate the distance moved
                event.getX(pointerIndex) to event.getY(pointerIndex)
            }

        if (SystemClock.uptimeMillis() - mTouchStarted < config.swipeThreshold) {
            return
        }

        val activeMovable = mActiveMovable
        if (activeMovable?.locked == false) {
            with(format) {
                val newX = toViewPixels(activeMovable.x) + x - mLastTouchX
                val newY = toViewPixels(activeMovable.y) + y - mLastTouchY

                if (config.containMovables) {
                    moveContained(activeMovable, newX, newY)
                } else {
                    activeMovable.x = fromViewPixels(newX)
                    activeMovable.y = fromViewPixels(newY)
                }
                invalidate()
            }
        }

        // Remember this touch position for the next move event
        mLastTouchX = x
        mLastTouchY = y
    }

    private fun handleActionUpOrCancel() {
        mActivePointerId = MotionEvent.INVALID_POINTER_ID
        val activeMovable = mActiveMovable
        if (activeMovable != null && (mInitialPosX != activeMovable.x || mInitialPosY != activeMovable.y)) {
            activeMovable.onMoved(activeMovable.x, activeMovable.y)
        }
    }

    private fun handleActionPointerUp(event: MotionEvent) {
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

    private fun moveContained(movable: MovableT, newViewX: Float, newViewY: Float) {
        with(format) {
            val newX = fromViewPixels(newViewX)
            val newY = fromViewPixels(newViewY)
            val vWidth = fromViewPixels(mWidth.toFloat())
            val vHeight = fromViewPixels(mHeight.toFloat())

            movable.x = if (newX < 0.0f) {
                0.0f
            } else if (newX > vWidth - movable.width) {
                vWidth - movable.width
            } else {
                newX
            }

            movable.y = if (newY < 0.0f) {
                0.0f
            } else if (newY > vHeight - movable.height) {
                vHeight - movable.height
            } else {
                newY
            }
        }
    }

    val focusedMovable: MovableT?
        get() = mActiveMovable

    fun addMovable(movable: MovableT, x: Float = 0.0f, y: Float = 0.0f) {
        movables.add(movable.apply {
            this.x = x
            this.y = y
            onMoved(x, y)
        })
    }

    fun removeMovable(movable: MovableT): Boolean {
        return movables.remove(movable)
    }

    fun removeMovables(predicate: (MovableT) -> Boolean): Int {
        return movables.removeIf(predicate)
    }

    fun forEachMovable(block: (MovableT) -> Unit) {
        movables.forEach(block)
    }

    fun addUnderlay(underlay: DeskViewLayer) {
        underlay.onSizeChanged(mWidth, mHeight)
        underlays.add(underlay)
    }

    fun removeUnderlay(underlay: DeskViewLayer): Boolean {
        return underlays.remove(underlay)
    }

    fun addOverlay(overlay: DeskViewLayer) {
        overlay.onSizeChanged(mWidth, mHeight)
        overlays.add(overlay)
    }

    fun removeOverlay(overlay: DeskViewLayer): Boolean {
        return overlays.remove(overlay)
    }

    fun moveToForeground(movable: MovableT, entirely: Boolean = false) {
        if (movables.moveToForeground(movable, entirely)) {
            invalidate()
        }
    }

    fun moveToBackground(movable: MovableT, entirely: Boolean = false) {
        if (movables.moveToBackground(movable, entirely)) {
            invalidate()
        }
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
            underlays = underlays,
            overlays = overlays,
            initialWidth = mWidth,
            initialHeight = mHeight,
            config = config,
        ).also {
            it.format = format
            it.start()
        }
    }

}
