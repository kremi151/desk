package lu.kremi151.desk.api

import lu.kremi151.desk.internal.DefaultFormat

/**
 * Base context type of a [lu.kremi151.desk.view.DeskView]
 */
open class DeskViewContext {

    /**
     * The current width of the desk view
     */
    var viewWidth: Int = 0
        internal set

    /**
     * The current height of the desk view
     */
    var viewHeight: Int = 0
        internal set

    /**
     * The currently used format
     */
    var format: Format = DefaultFormat
        internal set

}