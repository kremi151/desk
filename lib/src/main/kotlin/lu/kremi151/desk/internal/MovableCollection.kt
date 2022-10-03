package lu.kremi151.desk.internal

import lu.kremi151.desk.datamodel.Movable
import java.util.*

internal class MovableCollection<MovableT: Movable> {

    private val movables = mutableListOf<MovableT>()
    private val listeners = mutableListOf<Listener<MovableT>>()
    private val idToState = mutableMapOf<UUID, MovableT>()

    fun add(movable: MovableT) {
        check(movable.id == null) { "Movable is already bound to a DeskView" }
        val id = UUID.randomUUID()
        synchronized(movables) {
            movables.add(movable)
            idToState[id] = movable
        }
        synchronized(listeners) {
            listeners.forEach {
                it.onChanged(this)
            }
        }
    }

    fun remove(movable: MovableT): Boolean {
        val toRemove = movable.id?.let { id ->
            idToState.remove(id)
        } ?: return false
        val removed = synchronized(movables) {
            movables.remove(toRemove)
        }
        if (removed) {
            synchronized(listeners) {
                listeners.forEach {
                    it.onChanged(this)
                }
            }
        }
        return removed
    }

    fun forEach(action: (MovableT) -> Unit) {
        synchronized(movables) {
            movables.forEach(action)
        }
    }

    fun forEachReversed(action: (MovableT) -> Unit) {
        synchronized(movables) {
            val size = movables.size
            for (i in size-1 downTo 0) {
                action(movables[i])
            }
        }
    }

    fun findFirst(predicate: (MovableT) -> Boolean): MovableT? {
        synchronized(movables) {
            return movables.firstOrNull(predicate)
        }
    }

    fun findLast(predicate: (MovableT) -> Boolean): MovableT? {
        synchronized(movables) {
            return movables.lastOrNull(predicate)
        }
    }

    fun addListener(listener: Listener<MovableT>) {
        synchronized(listener) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: Listener<MovableT>): Boolean {
        return synchronized(listener) {
            listeners.remove(listener)
        }
    }

    interface Listener<MovableT: Movable> {
        fun onChanged(collection: MovableCollection<MovableT>)
    }

}
