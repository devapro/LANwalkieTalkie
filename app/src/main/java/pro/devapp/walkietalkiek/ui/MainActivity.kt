package pro.devapp.walkietalkiek.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.service.WalkieService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val serviceIntent = Intent(this, WalkieService::class.java)
        startService(serviceIntent)

        stop.setOnClickListener {
            stopService(serviceIntent)
        }
    }
}