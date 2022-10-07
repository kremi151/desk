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

    @Before
    fun before() {
        mockContext = RuntimeEnvironment.getApplication()
        deskView = DeskView(mockContext)

        motionEventDownTime = 0L

        deskView.right = 800
        deskView.bottom = 1200

        deskView.config = deskView.config.copy(
            // Disable swipe threshold by default in tests
            swipeThreshold = 0,
        )

        movable1 = TestMovable(400.0f, 400.0f).also {
            deskView.addMovable(
                movable = it,
                x = 100.0f,
                y = 100.0f,
            )
        }
        movable2 = TestMovable(400.0f, 400.0f).also {
            deskView.addMovable(
                movable = it,
                x = 300.0f,
                y = 300.0f,
            )
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
        assertFalse(movable1.focused)
        assertFalse(movable2.focused)

        deskView.onTouchEvent(makeTouch(99.9f, 99.9f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(99.9f, 99.9f, MotionEvent.ACTION_UP))
        assertNull(deskView.focusedMovable)
        assertFalse(movable1.focused)
        assertFalse(movable2.focused)

        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_UP))
        assertEquals(movable1, deskView.focusedMovable)
        assertTrue(movable1.focused)
        assertFalse(movable2.focused)

        deskView.onTouchEvent(makeTouch(400f, 400f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(400f, 400f, MotionEvent.ACTION_UP))
        assertEquals(movable2, deskView.focusedMovable)
        assertFalse(movable1.focused)
        assertTrue(movable2.focused)

        deskView.onTouchEvent(makeTouch(700.1f, 700.1f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(700.1f, 700.1f, MotionEvent.ACTION_UP))
        assertNull(deskView.focusedMovable)
        assertFalse(movable1.focused)
        assertFalse(movable2.focused)
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
