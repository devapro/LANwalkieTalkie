package pro.devapp.walkietalkiek.service.voice

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import pro.devapp.walkietalkiek.core.mvi.CoroutineContextProvider
import pro.devapp.walkietalkiek.serivce.network.ChanelController
import timber.log.Timber
import java.lang.Byte
import java.lang.Short
import java.nio.ByteBuffer
import kotlin.ByteArray
import kotlin.Int
import kotlin.apply
import kotlin.arrayOf
import kotlin.let

class VoiceRecorder(
    private val chanelController: ChanelController,
    private val coroutineContextProvider: CoroutineContextProvider
) {
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO

    private val coroutineScope = coroutineContextProvider.createScope(
        coroutineContextProvider.io.limitedParallelism(1)
    )

    private var audioRecord: AudioRecord? = null
    private var readBufferSize = 8192

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun create() {
        Timber.Forest.i("create")
        val minRate = getMinRate()
        minRate?.let  {
            Timber.Forest.i("minRate: $it")
            val frameSize =
                it * (Short.SIZE / Byte.SIZE) / 2 and Int.MAX_VALUE - 1
            var bufferSize = (frameSize * 4)
            val minBufferSize = getMinBufferSize(it)
            if (bufferSize < minBufferSize) bufferSize = minBufferSize
            Timber.Forest.i("internal audio buffer size: $bufferSize")
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                it,
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

        }
    }

    fun destroy() {
        Timber.Forest.i("destroy")
        coroutineScope.cancel()
        audioRecord?.apply {
            release()
        }
    }

    fun startRecord() {
        Timber.Forest.i("startRecord")
        audioRecord?.apply {
            startRecording()
        }
        startReading()
    }

    fun stopRecord() {
        Timber.Forest.i("stopRecord")
        audioRecord?.apply { stop() }
    }

    private fun startReading() {
        Timber.Forest.i("startReading")
        coroutineScope.launch {
            while (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.apply {
                    val bytes = ByteArray(readBufferSize)
                    val readCount = read(bytes, 0, readBufferSize)
                    if (readCount > 0) {
                        chanelController.sendMessage(ByteBuffer.wrap(bytes))
                    }
                    Timber.Forest.i("read $readCount")
                }
            }
        }
    }

    private fun getMinRate(): Int? {
        val rates = arrayOf(8000, 11025, 16000, 22050, 44100)
        rates.forEach {
            val minBufferSize = getMinBufferSize(it)
            if (minBufferSize != AudioRecord.ERROR &&
                minBufferSize != AudioRecord.ERROR_BAD_VALUE
            ) {
                return it
            }
        }
        return null
    }

    private fun getMinBufferSize(rate: Int): Int {
        return AudioRecord.getMinBufferSize(
            rate, channelConfig, AudioFormat.ENCODING_PCM_16BIT
        )
    }
}