package lu.kremi151.desk.extensions

import lu.kremi151.desk.api.Movable
import org.junit.Assert

private data class Pos(val x: Float, val y: Float)

fun Movable.assertPos(expectedX: Float, expectedY: Float) {
    Assert.assertEquals("Position is incorrect", Pos(expectedX, expectedY), Pos(x, y))
}
