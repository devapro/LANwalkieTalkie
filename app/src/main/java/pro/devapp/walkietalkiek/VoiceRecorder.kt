package pro.devapp.walkietalkiek

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import timber.log.Timber
import java.util.concurrent.Executors

class VoiceRecorder(private val recordListener: (bytes: ByteArray) -> Unit) {
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val executorService = Executors.newSingleThreadExecutor()

    private var audioRecord: AudioRecord? = null
    private var readBufferSize = 8192

    fun create() {
        Timber.i("create")
        val minRate = getMinRate()
        minRate?.let {
            Timber.i("minRate: $it")
            val frameSize =
                it * (java.lang.Short.SIZE / java.lang.Byte.SIZE) / 2 and Int.MAX_VALUE - 1
            var bufferSize = (frameSize * 4)
            val minBufferSize = getMinBufferSize(it)
            if (bufferSize < minBufferSize) bufferSize = minBufferSize
            Timber.i("internal audio buffer size: $bufferSize")
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
        Timber.i("destroy")
        executorService.shutdown()
        audioRecord?.apply {
            release()
        }
    }

    fun startRecord() {
        Timber.i("startRecord")
        audioRecord?.apply {
            startRecording()
        }
        startReading()
    }

    fun stopRecord() {
        Timber.i("stopRecord")
        audioRecord?.apply { stop() }
    }

    private fun startReading() {
        Timber.i("startReading")
        executorService.execute {
            while (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.apply {
                    val bytes = ByteArray(readBufferSize)
                    val readCount = read(bytes, 0, readBufferSize)
                    if (readCount > 0) {
                        recordListener(bytes)
                    }
                    Timber.i("read $readCount")
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