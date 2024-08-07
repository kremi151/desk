package lu.kremi151.desk.view

import android.content.Context
import android.util.AttributeSet
import lu.kremi151.desk.api.DeskViewContext
import lu.kremi151.desk.api.Movable

open class DeskView @JvmOverloads constructor(
    context: Context, attrs:
    AttributeSet? = null,
    defStyleAttr: Int = 0,
) : TypedDeskView<Movable, Long, DeskViewContext>(context, attrs, defStyleAttr)
