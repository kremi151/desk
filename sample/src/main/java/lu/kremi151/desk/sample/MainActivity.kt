package lu.kremi151.desk.sample

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import lu.kremi151.desk.view.DeskView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val deskView = findViewById<DeskView>(R.id.deskView)
        deskView.post {
            val controller = deskView.controller
            controller.addMovable(SampleMovable(
                id = 1L,
                color1 = Color.parseColor("#bada55"),
                color2 = Color.parseColor("#55daba"),
                width = 400.0f,
                height = 400.0f,
            ))
            controller.addMovable(
                movable = SampleMovable(
                    id = 2L,
                    color1 = Color.parseColor("#ba55da"),
                    color2 = Color.parseColor("#da55ba"),
                    width = 400.0f,
                    height = 400.0f,
                ),
                x = deskView.width - 400.0f,
            )
            controller.addMovable(
                movable = AspectRatioKeepingMovable(
                    id = 3L,
                    focusedColor = Color.parseColor("#00ff50"),
                    unfocusedColor = Color.parseColor("#ff0050"),
                    aspectRatio = 1.5f,
                    height = 400.0f,
                ),
                y = deskView.height - 400.0f,
            )
        }
    }
}