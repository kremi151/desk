package lu.kremi151.desk.api

interface DeskViewController<MovableT : TypedMovable<ID>, ID> {

    fun addMovable(movable: MovableT, x: Float = 0.0f, y: Float = 0.0f)

    fun removeMovable(movable: MovableT): Boolean
    fun removeMovables(predicate: (MovableT) -> Boolean): Int

    fun forEachMovable(block: (MovableT) -> Unit)

    fun firstOrNull(predicate: (MovableT) -> Boolean): MovableT?

    fun move(id: ID, x: Float, y: Float): Boolean

    fun moveToForeground(movable: MovableT, entirely: Boolean = false): Boolean
    fun moveToBackground(movable: MovableT, entirely: Boolean = false): Boolean

}