package lu.kremi151.desk.api

interface DeskViewController<MovableT : TypedMovable<ID, ContextT>, ID, ContextT> {

    fun addMovable(movable: MovableT)

    fun removeMovable(movable: MovableT): Boolean
    fun removeMovables(predicate: (MovableT) -> Boolean): Int

    fun forEachMovable(block: (MovableT) -> Unit)
    fun <R> mapMovables(mapper: (MovableT) -> R): List<R>

    fun firstOrNull(predicate: (MovableT) -> Boolean): MovableT?

    fun moveToForeground(movable: MovableT, entirely: Boolean = false): Boolean
    fun moveToBackground(movable: MovableT, entirely: Boolean = false): Boolean

}
