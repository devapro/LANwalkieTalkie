package pro.devapp.walkietalkiek.ui

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.VoiceRecorder
import pro.devapp.walkietalkiek.WalkieTalkieApp
import pro.devapp.walkietalkiek.service.SocketClient
import pro.devapp.walkietalkiek.service.WalkieService
import pro.devapp.walkietalkiek.utils.permission.Permission
import pro.devapp.walkietalkiek.utils.permission.UtilPermission
import java.nio.ByteBuffer
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var voiceRecorder: VoiceRecorder

    private val utilPermission = UtilPermission()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceIntent = Intent(this, WalkieService::class.java)
        startService(serviceIntent)

        exit.setOnClickListener {
            stopService(serviceIntent)
            finish()
        }

        send.setOnClickListener {
            (application as WalkieTalkieApp).chanelController.sendMessage(ByteBuffer.wrap("test ${Date().seconds}".toByteArray()))
        }

    }

    override fun onStart() {
        super.onStart()
        utilPermission.checkOrRequestPermissions(this, object : UtilPermission.PermissionCallback(
            arrayOf(Permission.AUDIO_RECORD)
        ) {
            override fun onSuccessGrantedAll() {
                startVoiceRecorder()
            }
        })
        (application as WalkieTalkieApp).chanelController.actionListener =
            object : SocketClient.ActionListener {
                override fun onClientListUpdated(clients: List<String>) {
                    clientsView.post {
                        clientsView.text = clients.size.toString()
                    }
                }

                override fun onClientSendMessage(client: String) {
                    if (activeClient.text == "---") {
                        activeClient.post {
                            activeClient.text = client
                        }
                        activeClient.postDelayed({
                            activeClient.text = "---"
                        }, 1000)
                    }
                }
            }
    }

    override fun onStop() {
        super.onStop()
        voiceRecorder.destroy()
        (application as WalkieTalkieApp).chanelController.actionListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceRecorder.destroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        utilPermission.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startVoiceRecorder() {
        voiceRecorder = VoiceRecorder() {
            (application as WalkieTalkieApp).chanelController.sendMessage(ByteBuffer.wrap(it))
        }
        voiceRecorder.create()

        talk.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    voiceRecorder.startRecord()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    voiceRecorder.stopRecord()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }
}