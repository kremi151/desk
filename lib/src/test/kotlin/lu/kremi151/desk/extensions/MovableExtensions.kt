package lu.kremi151.desk.extensions

import lu.kremi151.desk.api.Movable
import lu.kremi151.desk.util.TestMovable
import org.junit.Assert

private data class Pos(val x: Float, val y: Float)

fun Movable.assertPos(expectedX: Float, expectedY: Float) {
    Assert.assertEquals("Position is incorrect", Pos(expectedX, expectedY), Pos(x, y))
}

fun TestMovable.assertTapped(expectedX: Float, expectedY: Float) {
    Assert.assertEquals("Tapped coordinates is incorrect", Pos(expectedX, expectedY), Pos(tappedX, tappedY))
}
