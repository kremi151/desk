package lu.kremi151.desk.extensions

import lu.kremi151.desk.api.Movable
import org.junit.Assert

fun Movable.assertPos(expectedX: Float, expectedY: Float) {
    Assert.assertEquals("X position is incorrect", expectedX, x)
    Assert.assertEquals("Y position is incorrect", expectedY, y)
}
