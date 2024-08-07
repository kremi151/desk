package lu.kremi151.desk.api

@Suppress("UnnecessaryAbstractClass")
abstract class Movable(id: Long): TypedMovable<Long, DeskViewContext>(id)
