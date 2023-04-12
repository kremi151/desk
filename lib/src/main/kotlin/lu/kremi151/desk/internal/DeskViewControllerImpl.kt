package lu.kremi151.desk.internal

import lu.kremi151.desk.api.DeskViewController
import lu.kremi151.desk.api.TypedMovable

internal class DeskViewControllerImpl<MovableT : TypedMovable<ID, ContextT>, ID, ContextT>(
    private val movables: MovableCollection<MovableT, ID, ContextT>,
): DeskViewController<MovableT, ID, ContextT> {

    override fun addMovable(movable: MovableT) {
        movables.add(movable)
    }

    override fun removeMovable(movable: MovableT): Boolean {
        return movables.remove(movable)
    }

    override fun removeMovables(predicate: (MovableT) -> Boolean): Int {
        return movables.removeIf(predicate)
    }

    override fun forEachMovable(block: (MovableT) -> Unit) {
        movables.forEach(block)
    }

    override fun firstOrNull(predicate: (MovableT) -> Boolean): MovableT? {
        return movables.firstOrNull(predicate)
    }

    override fun moveToForeground(movable: MovableT, entirely: Boolean): Boolean {
        return movables.moveToForeground(movable, entirely)
    }

    override fun moveToBackground(movable: MovableT, entirely: Boolean): Boolean {
        return movables.moveToBackground(movable, entirely)
    }

}