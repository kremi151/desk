package lu.kremi151.desk.internal

import lu.kremi151.desk.api.TypedMovable
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class MovableCollection<MovableT: TypedMovable<ID, ContextT>, ID, ContextT>(
    private val getContext: () -> ContextT?,
) {

    private val movables = mutableListOf<MovableT>()
    private val id2Movables = mutableMapOf<ID, MovableT>()
    private val listeners = mutableListOf<Listener<MovableT, ID, ContextT>>()

    private val lock = ReentrantLock()

    fun add(movable: MovableT) {
        check(!movable.bound) { "Movable is already bound to a DeskView" }
        lock.withLock {
            movables.add(movable.also {
                it.bound = true
                it.context = getContext()
            })
            id2Movables[movable.id] = movable
        }
        callListeners()
    }

    fun updateContexts() {
        lock.withLock {
            movables.forEach {
                it.context = getContext()
            }
        }
    }

    fun remove(movable: MovableT): Boolean {
        val removed = lock.withLock {
            val removed = movables.remove(movable)
            if (removed) {
                movable.bound = false
                movable.context = null
                id2Movables.remove(movable.id)
            }
            removed
        }
        if (removed) {
            callListeners()
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
            callListeners()
        }
        return counter
    }

    fun forEach(action: (MovableT) -> Unit) {
        lock.withLock {
            movables.forEach(action)
        }
    }

    fun <R> map(mapper: (MovableT) -> R): List<R> = lock.withLock {
        movables.map(mapper)
    }

    fun forEachReversed(action: (MovableT) -> Unit) {
        lock.withLock {
            val size = movables.size
            for (i in size-1 downTo 0) {
                action(movables[i])
            }
        }
    }

    fun firstOrNull(predicate: (MovableT) -> Boolean): MovableT? = lock.withLock {
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
            callListeners()
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
            callListeners()
        }
        return changed
    }

    fun addListener(listener: Listener<MovableT, ID, ContextT>) {
        synchronized(listener) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: Listener<MovableT, ID, ContextT>): Boolean {
        return synchronized(listener) {
            listeners.remove(listener)
        }
    }

    private fun callListeners() {
        synchronized(listeners) {
            listeners.forEach {
                it.onChanged(this)
            }
        }
    }

    interface Listener<MovableT: TypedMovable<ID, ContextT>, ID, ContextT> {
        fun onChanged(collection: MovableCollection<MovableT, ID, ContextT>)
    }

}
