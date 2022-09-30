package lu.kremi151.desk.sample

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import lu.kremi151.desk.view.DeskView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listOf(
            SampleMovable(
                Color.parseColor("#bada55"),
                Color.parseColor("#55daba"),
            ),
            SampleMovable(
                Color.parseColor("#ba55da"),
                Color.parseColor("#da55ba"),
            ),
        ).forEach {
            findViewById<DeskView>(R.id.deskView).addMovable(it)
        }
    }
}