package lu.kremi151.desk

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import lu.kremi151.desk.util.TestMovable
import lu.kremi151.desk.view.DeskView
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DeskViewTest {

    private lateinit var mockContext: Context

    private lateinit var deskView: DeskView

    private lateinit var movable1: TestMovable
    private lateinit var movable2: TestMovable

    @Before
    fun before() {
        mockContext = RuntimeEnvironment.getApplication()
        deskView = DeskView(mockContext)

        deskView.right = 800
        deskView.bottom = 1200

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
    fun testMovableSelection() {
        assertEquals(800, deskView.width)
        assertEquals(1200, deskView.height)

        assertNull(deskView.focusedMovable)

        deskView.onTouchEvent(makeTouch(99.9f, 99.9f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(99.9f, 99.9f, MotionEvent.ACTION_UP))
        assertNull(deskView.focusedMovable)

        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_UP))
        assertEquals(movable1, deskView.focusedMovable)

        deskView.onTouchEvent(makeTouch(400f, 400f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(400f, 400f, MotionEvent.ACTION_UP))
        assertEquals(movable2, deskView.focusedMovable)

        deskView.onTouchEvent(makeTouch(700.1f, 700.1f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(700.1f, 700.1f, MotionEvent.ACTION_UP))
        assertNull(deskView.focusedMovable)
    }

    @Test
    fun testFocusAndBlurCallback() {
        assertFalse(movable1.focused)
        assertFalse(movable2.focused)

        deskView.onTouchEvent(makeTouch(99.9f, 99.9f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(99.9f, 99.9f, MotionEvent.ACTION_UP))
        assertFalse(movable1.focused)
        assertFalse(movable2.focused)

        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(200f, 200f, MotionEvent.ACTION_UP))
        assertTrue(movable1.focused)
        assertFalse(movable2.focused)

        deskView.onTouchEvent(makeTouch(400f, 400f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(400f, 400f, MotionEvent.ACTION_UP))
        assertFalse(movable1.focused)
        assertTrue(movable2.focused)

        deskView.onTouchEvent(makeTouch(700.1f, 700.1f, MotionEvent.ACTION_DOWN))
        deskView.onTouchEvent(makeTouch(700.1f, 700.1f, MotionEvent.ACTION_UP))
        assertFalse(movable1.focused)
        assertFalse(movable2.focused)
    }

    private fun makeTouch(x: Float, y: Float, event: Int) = MotionEvent.obtain(
        SystemClock.uptimeMillis(),
        SystemClock.uptimeMillis() + 100L,
        event,
        x,
        y,
        0,
    )

}