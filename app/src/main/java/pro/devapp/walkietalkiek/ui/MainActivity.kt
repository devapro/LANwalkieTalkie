package pro.devapp.walkietalkiek.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.WalkieTalkieApp
import pro.devapp.walkietalkiek.service.WalkieService
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val serviceIntent = Intent(this, WalkieService::class.java)
        startService(serviceIntent)

        send.setOnClickListener {
            (application as WalkieTalkieApp).chanelController.sendMessage(ByteBuffer.wrap("test".toByteArray()))
        }

        stop.setOnClickListener {
            stopService(serviceIntent)
        }
    }
}