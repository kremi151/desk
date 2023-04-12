package lu.kremi151.desk.api

abstract class SimpleMovable(
    id: Long,
    width: Float,
    height: Float,
    x: Float = 0.0f,
    y: Float = 0.0f,
): Movable(id) {

    final override var width: Float = width
        private set

    final override var height: Float = height
        private set

    final override var x: Float = x
        private set

    final override var y: Float = y
        private set

    override fun remeasure(desiredWidth: Float, desiredHeight: Float) {
        width = desiredWidth
        height = desiredHeight
    }

    override fun move(desiredX: Float, desiredY: Float) {
        x = desiredX
        y = desiredY
    }

}
