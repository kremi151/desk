package lu.kremi151.desk.internal

import lu.kremi151.desk.api.Movable
import java.util.*

internal class MovableCollection<MovableT: Movable> {

    private val movables = mutableListOf<MovableT>()
    private val listeners = mutableListOf<Listener<MovableT>>()

    fun add(movable: MovableT) {
        check(movable.id == null) { "Movable is already bound to a DeskView" }
        synchronized(movables) {
            movables.add(movable.also { it.id = UUID.randomUUID() })
        }
        synchronized(listeners) {
            listeners.forEach {
                it.onChanged(this)
            }
        }
    }

    fun remove(movable: MovableT): Boolean {
        val removed = synchronized(movables) {
            movables.remove(movable)
        }
        if (removed) {
            movable.id = null
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

    fun moveToForeground(movable: MovableT, entirely: Boolean): Boolean {
        synchronized(movables) {
            val index = movables.indexOf(movable)
            if (index < 0 || index >= movables.size - 1) {
                return false
            }
            movables.removeAt(index).let {
                if (entirely) {
                    movables.add(it)
                } else {
                    movables.add(index + 1, it)
                }
            }
            return true
        }
    }

    fun moveToBackground(movable: MovableT, entirely: Boolean): Boolean {
        synchronized(movables) {
            val index = movables.indexOf(movable)
            if (index <= 0) {
                return false
            }
            movables.removeAt(index).let {
                if (entirely) {
                    movables.add(0, it)
                } else {
                    movables.add(index - 1, it)
                }
            }
            return true
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
