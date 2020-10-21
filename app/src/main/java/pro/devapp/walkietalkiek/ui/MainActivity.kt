package pro.devapp.walkietalkiek.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import pro.devapp.walkietalkiek.R
import pro.devapp.walkietalkiek.VoiceRecorder
import pro.devapp.walkietalkiek.WalkieService
import pro.devapp.walkietalkiek.WalkieTalkieApp
import pro.devapp.walkietalkiek.ui.dialogs.SettingsDialog
import pro.devapp.walkietalkiek.ui.dialogs.messages.MessagesDialog
import pro.devapp.walkietalkiek.ui.widgets.BottomButtons
import pro.devapp.walkietalkiek.utils.permission.Permission
import pro.devapp.walkietalkiek.utils.permission.UtilPermission
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var utilPermission: UtilPermission

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var voiceRecorder: VoiceRecorder? = null
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (application as WalkieTalkieApp).applicationComponent.inject(this)

        val serviceIntent = Intent(this, WalkieService::class.java)
        startService(serviceIntent)
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, WalkieService::class.java)
        bindService(serviceIntent, serviceConnection, BIND_IMPORTANT)
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
                clientsList.setItems(list)
            }
            .also {
                compositeDisposable.add(it)
            }

        findViewById<BottomButtons>(R.id.bottomButtons).buttonsClickSubject.subscribe {
            when (it) {
                BottomButtons.Buttons.MESSAGES -> {
                    MessagesDialog().show(supportFragmentManager, "MessagesDialog")
                }
                BottomButtons.Buttons.SETTINGS -> {
                    SettingsDialog().show(supportFragmentManager, "SettingsDialog")
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
        ip.text = ipAddress
    }

    override fun onStop() {
        super.onStop()
        voiceRecorder?.destroy()
        compositeDisposable.clear()
        unbindService(serviceConnection)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        utilPermission.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun sendMessage(byteBuffer: ByteBuffer) {
        serviceConnection.serviceInterface?.sendMessage(byteBuffer)
    }

    private fun startVoiceRecorder() {
        voiceRecorder = VoiceRecorder() {
            sendMessage(ByteBuffer.wrap(it))
        }
        voiceRecorder?.create()

        ppt.pushStateSubject.subscribe {
            if (it) {
                voiceRecorder?.startRecord()
            } else {
                voiceRecorder?.stopRecord()
            }
        }.also {
            compositeDisposable.add(it)
        }
    }

    private fun initWaveView() {
        serviceConnection.serviceInterface?.getAudioData()?.apply {
            timeout(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    audioView.text = "---"
                }
                .retry()
                .subscribe {
                    audioView.text = it.size.toString()
                    waveView.setData(it, 8000)
                }
                .also {
                    compositeDisposable.add(it)
                }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        var serviceInterface: WalkieService.MBinder? = null
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceInterface = service as WalkieService.MBinder
            initWaveView()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceInterface = null
        }
    }
}