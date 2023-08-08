package lu.kremi151.desk

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import lu.kremi151.desk.extensions.assertPos
import lu.kremi151.desk.util.TestMovable
import lu.kremi151.desk.view.DeskView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowSystemClock
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class DeskViewTest {

    private lateinit var mockContext: Context

    private lateinit var deskView: DeskView

    private lateinit var movable1: TestMovable
    private lateinit var movable2: TestMovable

    private var motionEventDownTime = 0L
    private var invalidates = 0

    @Before
    fun before() {
        mockContext = RuntimeEnvironment.getApplication()
        deskView = object : DeskView(mockContext) {
            override fun invalidate() {
                super.invalidate()
                invalidates++
            }
        }

        motionEventDownTime = 0L
        invalidates = 0

        deskView.right = 800
        deskView.bottom = 1200

        deskView.config = deskView.config.copy(
            // Disable swipe threshold by default in tests
            swipeThreshold = 0,
        )

        movable1 = TestMovable(1, mWidth = 400.0f, mHeight = 400.0f, mX= 100.0f, mY = 100.0f).also {
            deskView.controller.addMovable(it)
        }
        movable2 = TestMovable(2, mWidth = 400.0f, mHeight = 400.0f, mX = 300.0f, mY = 300.0f).also {
            deskView.controller.addMovable(it)
        }
    }

    @Test
    fun testViewSize() {
        assertEquals(800, deskView.width)
        assertEquals(1200, deskView.height)
    }

    @Test
    fun testMovableSelectionAndFocus() {
        assertNull(deskView.focusedMovable)
        assertEquals(0, movable1.focusedCounter)
        assertEquals(0, movable2.focusedCounter)
        assertEquals(0, invalidates)

        deskView.onTouchEvent(makeTouch(99.9f, 99.9f, MotionEvent.ACTION_DOWN))
        assertEquals(0, invalidates)
        deskView.onTouchEvent(makeTouch(99.9f, 99.9f, MotionEvent.ACTION_UP))
        assertNull(deskView.focusedMovable)
        assertEquals(0, movable1.focusedCounter)
        assertEquals(0, movable2.focusedCounter)

        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_DOWN))
        assertEquals(1, invalidates)
        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_UP))
        assertEquals(movable1, deskView.focusedMovable)
        assertEquals(1, movable1.focusedCounter)
        assertEquals(0, movable2.focusedCounter)

        deskView.onTouchEvent(makeTouch(400f, 400f, MotionEvent.ACTION_DOWN))
        assertEquals(2, invalidates)
        deskView.onTouchEvent(makeTouch(400f, 400f, MotionEvent.ACTION_UP))
        assertEquals(movable2, deskView.focusedMovable)
        assertEquals(0, movable1.focusedCounter) //
        assertEquals(1, movable2.focusedCounter)

        deskView.onTouchEvent(makeTouch(450f, 450f, MotionEvent.ACTION_DOWN))
        assertEquals(3, invalidates)
        deskView.onTouchEvent(makeTouch(450f, 450f, MotionEvent.ACTION_UP))
        assertEquals(movable2, deskView.focusedMovable)
        assertEquals(0, movable1.focusedCounter)
        assertEquals(1, movable2.focusedCounter)

        deskView.onTouchEvent(makeTouch(700.1f, 700.1f, MotionEvent.ACTION_DOWN))
        assertEquals(4, invalidates)
        deskView.onTouchEvent(makeTouch(700.1f, 700.1f, MotionEvent.ACTION_UP))
        assertNull(deskView.focusedMovable)
        assertEquals(0, movable1.focusedCounter)
        assertEquals(0, movable2.focusedCounter)

        deskView.onTouchEvent(makeTouch(750f, 750f, MotionEvent.ACTION_DOWN))
        assertEquals(4, invalidates)
    }

    @Test
    fun testMovingMovable() {
        movable1.assertPos(100.0f, 100.0f)
        movable2.assertPos(300.0f, 300.0f)

        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(210f, 205f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(220f, 210f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(230f, 215f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(240f, 220f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(235f, 210f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(230f, 200f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(225f, 190f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(220f, 180f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(220f, 180f, MotionEvent.ACTION_UP))

        movable1.assertPos(120.0f, 80.0f)
        movable2.assertPos(300.0f, 300.0f)

        deskView.onTouchEvent(makeTouch(350f, 350f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(340f, 345f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(330f, 340f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(320f, 335f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(310f, 330f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(310f, 330f, MotionEvent.ACTION_UP))

        movable1.assertPos(120.0f, 80.0f)
        movable2.assertPos(260.0f, 280.0f)
    }

    @Test
    fun testMovingMovableWithSwipeThreshold() {
        deskView.config = deskView.config.copy(
            swipeThreshold = 200,
        )

        movable1.assertPos(100.0f, 100.0f)

        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_DOWN))
        ShadowSystemClock.advanceBy(45L, TimeUnit.MILLISECONDS)
        deskView.onTouchEvent(makeTouch(210f, 205f, MotionEvent.ACTION_MOVE))
        movable1.assertPos(100.0f, 100.0f)
        ShadowSystemClock.advanceBy(45L, TimeUnit.MILLISECONDS)
        deskView.onTouchEvent(makeTouch(220f, 210f, MotionEvent.ACTION_MOVE))
        movable1.assertPos(100.0f, 100.0f)
        ShadowSystemClock.advanceBy(45L, TimeUnit.MILLISECONDS)
        deskView.onTouchEvent(makeTouch(230f, 215f, MotionEvent.ACTION_MOVE))
        movable1.assertPos(100.0f, 100.0f)
        ShadowSystemClock.advanceBy(45L, TimeUnit.MILLISECONDS)
        deskView.onTouchEvent(makeTouch(230f, 215f, MotionEvent.ACTION_UP))
        movable1.assertPos(100.0f, 100.0f)

        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_DOWN))
        ShadowSystemClock.advanceBy(75L, TimeUnit.MILLISECONDS)
        deskView.onTouchEvent(makeTouch(210f, 205f, MotionEvent.ACTION_MOVE))
        movable1.assertPos(100.0f, 100.0f)
        ShadowSystemClock.advanceBy(75L, TimeUnit.MILLISECONDS)
        deskView.onTouchEvent(makeTouch(220f, 210f, MotionEvent.ACTION_MOVE))
        movable1.assertPos(100.0f, 100.0f)
        ShadowSystemClock.advanceBy(75L, TimeUnit.MILLISECONDS)
        deskView.onTouchEvent(makeTouch(230f, 215f, MotionEvent.ACTION_MOVE))
        movable1.assertPos(130.0f, 115.0f)
        ShadowSystemClock.advanceBy(75L, TimeUnit.MILLISECONDS)
        deskView.onTouchEvent(makeTouch(230f, 215f, MotionEvent.ACTION_UP))
        movable1.assertPos(130.0f, 115.0f)
    }

    @Test
    fun testOnTappedCallback() {
        deskView.onTouchEvent(makeTouch(175f, 175f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(175f, 175f, MotionEvent.ACTION_UP))
        assertEquals(75.0f, movable1.tappedX)
        assertEquals(75.0f, movable1.tappedY)

        deskView.onTouchEvent(makeTouch(193.3f, 100.11000061f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(193.3f, 100.11000061f, MotionEvent.ACTION_UP))
        assertEquals(93.3f, movable1.tappedX)
        assertEquals(0.11000061f, movable1.tappedY)
    }

    @Test
    fun testInitialOnMovedCalled() {
        val movable = TestMovable(99, mWidth = 420f, mHeight = 420f, mX = 123.0f, mY = 386.0f)
        deskView.controller.addMovable(movable)

        assertEquals(123.0f, movable.x)
        assertEquals(386.0f, movable.y)
    }

    @Test
    fun testIgnoringTouchEvents() {
        movable1.assertPos(100.0f, 100.0f)
        movable2.assertPos(300.0f, 300.0f)
        assertFalse(deskView.config.ignoreTouchEvents)

        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(210f, 205f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(220f, 210f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(230f, 215f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(240f, 220f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(235f, 210f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(230f, 200f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(225f, 190f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(220f, 180f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(220f, 180f, MotionEvent.ACTION_UP))

        movable1.assertPos(120.0f, 80.0f)
        movable2.assertPos(300.0f, 300.0f)

        deskView.config = deskView.config.copy(ignoreTouchEvents = true)
        assertTrue(deskView.config.ignoreTouchEvents)

        deskView.onTouchEvent(makeTouch(350f, 350f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(340f, 345f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(330f, 340f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(320f, 335f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(310f, 330f, MotionEvent.ACTION_MOVE))
        deskView.onTouchEvent(makeTouch(310f, 330f, MotionEvent.ACTION_UP))

        movable1.assertPos(120.0f, 80.0f)
        movable2.assertPos(300.0f, 300.0f)
    }

    private fun makeTouch(x: Float, y: Float, event: Int): MotionEvent {
        if (event == MotionEvent.ACTION_DOWN) {
            motionEventDownTime = SystemClock.uptimeMillis()
        }
        return MotionEvent.obtain(
            motionEventDownTime,
            SystemClock.uptimeMillis(),
            event,
            x,
            y,
            0,
        )
    }

}
