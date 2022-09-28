package lu.kremi151.desk.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import lu.kremi151.desk.view.DeskView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<DeskView>(R.id.deskView).addMovable(SampleMovable())
    }
}