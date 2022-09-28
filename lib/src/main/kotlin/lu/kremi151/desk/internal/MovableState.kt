package lu.kremi151.desk.internal

import lu.kremi151.desk.datamodel.Movable

internal data class MovableState<MovableT : Movable>(
    val movable: MovableT,
    var x: Float = 0.0f,
    var y: Float = 0.0f,
)