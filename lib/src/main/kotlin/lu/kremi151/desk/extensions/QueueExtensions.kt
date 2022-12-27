package lu.kremi151.desk.extensions

import java.util.*

internal fun <E> Queue<E>.consumeEach(consume: (E) -> Unit) {
    var e: E? = poll()
    while (e != null) {
        consume(e)
        e = poll()
    }
}
