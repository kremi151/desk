package lu.kremi151.desk.internal

import lu.kremi151.desk.datamodel.Movable
import java.util.*

internal class MovableCollection<MovableT: Movable> {

    private val movables = mutableListOf<MovableState<MovableT>>()
    private val listeners = mutableListOf<Listener<MovableT>>()
    private val idToState = mutableMapOf<UUID, MovableState<MovableT>>()

    fun add(movable: MovableT) {
        if (movable.id != null) {
            throw IllegalStateException("Movable is already bound to a DeskView")
        }
        val id = UUID.randomUUID()
        synchronized(movables) {
            MovableState(
                movable = movable,
            ).let {
                movables.add(it)
                idToState[id] = it
            }
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

    fun forEach(action: (MovableState<MovableT>) -> Unit) {
        synchronized(movables) {
            movables.forEach(action)
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