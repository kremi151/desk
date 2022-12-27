package lu.kremi151.desk.internal

import lu.kremi151.desk.api.TypedMovable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class MovableCollection<MovableT: TypedMovable<ID>, ID> {

    private val movables = mutableListOf<MovableT>()
    private val id2Movables = mutableMapOf<ID, MovableT>()
    private val listeners = mutableListOf<Listener<MovableT, ID>>()

    private val lock = ReentrantLock()

    fun add(movable: MovableT) {
        check(!movable.bound) { "Movable is already bound to a DeskView" }
        lock.withLock {
            movables.add(movable.also { it.bound = true })
            id2Movables[movable.id] = movable
        }
        synchronized(listeners) {
            listeners.forEach {
                it.onChanged(this)
            }
        }
    }

    fun remove(movable: MovableT): Boolean {
        val removed = lock.withLock {
            val removed = movables.remove(movable)
            if (removed) {
                movable.bound = false
                id2Movables.remove(movable.id)
            }
            removed
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

    fun removeIf(predicate: (MovableT) -> Boolean): Int {
        var counter = 0
        lock.withLock {
            val it = movables.iterator()
            while (it.hasNext()) {
                val m = it.next()
                if (predicate(m)) {
                    it.remove()
                    m.bound = false
                    id2Movables.remove(m.id)
                    counter++
                }
            }
        }
        if (counter > 0) {
            synchronized(listeners) {
                listeners.forEach {
                    it.onChanged(this)
                }
            }
        }
        return counter
    }

    fun forEach(action: (MovableT) -> Unit) {
        lock.withLock {
            movables.forEach(action)
        }
    }

    fun forEachReversed(action: (MovableT) -> Unit) {
        lock.withLock {
            val size = movables.size
            for (i in size-1 downTo 0) {
                action(movables[i])
            }
        }
    }

    fun findFirst(predicate: (MovableT) -> Boolean): MovableT? = lock.withLock {
        movables.firstOrNull(predicate)
    }

    fun findById(id: ID): MovableT? = lock.withLock {
        id2Movables[id]
    }

    fun findLast(predicate: (MovableT) -> Boolean): MovableT? = lock.withLock {
        movables.lastOrNull(predicate)
    }

    fun moveToForeground(movable: MovableT, entirely: Boolean): Boolean {
        val changed = lock.withLock {
            val index = movables.indexOf(movable)
            if (index < 0 || index >= movables.size - 1) {
                false
            } else {
                movables.removeAt(index).let {
                    if (entirely) {
                        movables.add(it)
                    } else {
                        movables.add(index + 1, it)
                    }
                }
                true
            }
        }
        if (changed) {
            synchronized(listeners) {
                listeners.forEach {
                    it.onChanged(this)
                }
            }
        }
        return changed
    }

    fun moveToBackground(movable: MovableT, entirely: Boolean): Boolean {
        val changed = lock.withLock {
            val index = movables.indexOf(movable)
            if (index <= 0) {
                false
            } else {
                movables.removeAt(index).let {
                    if (entirely) {
                        movables.add(0, it)
                    } else {
                        movables.add(index - 1, it)
                    }
                }
                true
            }
        }
        if (changed) {
            synchronized(listeners) {
                listeners.forEach {
                    it.onChanged(this)
                }
            }
        }
        return changed
    }

    fun move(id: ID, x: Float, y: Float): Boolean {
        val changed = lock.withLock {
            val movable = id2Movables[id]
            if (movable == null) {
                false
            } else {
                movable.x = x
                movable.y = y
                true
            }
        }
        if (changed) {
            synchronized(listeners) {
                listeners.forEach {
                    it.onChanged(this)
                }
            }
        }
        return changed
    }

    fun addListener(listener: Listener<MovableT, ID>) {
        synchronized(listener) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: Listener<MovableT, ID>): Boolean {
        return synchronized(listener) {
            listeners.remove(listener)
        }
    }

    interface Listener<MovableT: TypedMovable<ID>, ID> {
        fun onChanged(collection: MovableCollection<MovableT, ID>)
    }

}
