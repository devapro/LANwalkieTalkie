package pro.devapp.walkietalkiek.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.VoiceRecorder
import pro.devapp.walkietalkiek.WalkieTalkieApp
import pro.devapp.walkietalkiek.databinding.ActivityMainBinding
import pro.devapp.walkietalkiek.service.WalkieService
import pro.devapp.walkietalkiek.ui.widgets.BottomButtons
import pro.devapp.walkietalkiek.utils.permission.Permission
import pro.devapp.walkietalkiek.utils.permission.UtilPermission
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var voiceRecorder: VoiceRecorder

    private val utilPermission = UtilPermission()
    private val compositeDisposable = CompositeDisposable()
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val serviceIntent = Intent(this, WalkieService::class.java)
        startService(serviceIntent)
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, WalkieService::class.java)
        utilPermission.checkOrRequestPermissions(this, object : UtilPermission.PermissionCallback(
            arrayOf(Permission.AUDIO_RECORD)
        ) {
            override fun onSuccessGrantedAll() {
                startVoiceRecorder()
            }
        })
        (application as WalkieTalkieApp).connectedDevicesRepository.getConnectedDevicesList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                viewBinding.clientsList.setItems(list)
            }
            .also {
                compositeDisposable.add(it)
            }

        (application as WalkieTalkieApp).chanelController.subjectAudioData
            .timeout(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                viewBinding.audioView.text = "---"
            }
            .retry()
            .subscribe {
                viewBinding.audioView.text = it.size.toString()
                viewBinding.waveView.setData(it, 8000)
            }
            .also {
                compositeDisposable.add(it)
            }

        findViewById<BottomButtons>(R.id.bottomButtons).buttonsClickSubject.subscribe {
            when (it) {
                BottomButtons.Buttons.MESSAGES -> {
                    (application as WalkieTalkieApp).chanelController.sendMessage(ByteBuffer.wrap("test ${Date().seconds}".toByteArray()))
                }
                BottomButtons.Buttons.SETTINGS -> {
                }
                BottomButtons.Buttons.EXIT -> {
                    stopService(serviceIntent)
                    finish()
                }
            }
        }
            .also {
                compositeDisposable.add(it)
            }

        val ipAddress = (application as WalkieTalkieApp).deviceInfoRepository.getCurrentIp()
        viewBinding.ip.text = ipAddress
    }

    override fun onStop() {
        super.onStop()
        voiceRecorder.destroy()
        compositeDisposable.clear()
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

        viewBinding.ppt.pushStateSubject.subscribe {
            if (it) {
                voiceRecorder.startRecord()
            } else {
                voiceRecorder.stopRecord()
            }
        }.also {
            compositeDisposable.add(it)
        }
    }
}