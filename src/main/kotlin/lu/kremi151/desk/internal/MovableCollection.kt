package lu.kremi151.desk.internal

import lu.kremi151.desk.datamodel.Movable
import java.util.*

class MovableCollection<MovableT: Movable> {

    private val movables = mutableListOf<MovableState<MovableT>>()
    private val listeners = mutableListOf<Listener<MovableT>>()
    private val idToState = mutableMapOf<UUID, MovableState<MovableT>>()

    fun add(movable: MovableT) {
        if (movable.id != null) {
            throw IllegalStateException("Movable is already bound to a DeskView")
        }
        val id = UUID.randomUUID()
        movable.id = id
        MovableState(
            movable = movable,
        ).let {
            movables.add(it)
            idToState[id] = it
        }
        listeners.forEach {
            it.onChanged(this)
        }
    }

    fun remove(movable: MovableT): Boolean {
        val toRemove = movable.id?.let { id ->
            idToState.remove(id)
        } ?: return false
        val removed = movables.remove(toRemove)
        if (removed) {
            listeners.forEach {
                it.onChanged(this)
            }
        }
        return removed
    }

    interface Listener<MovableT: Movable> {
        fun onChanged(collection: MovableCollection<MovableT>)
    }

}