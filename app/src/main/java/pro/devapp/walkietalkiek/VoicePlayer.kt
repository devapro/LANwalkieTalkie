package pro.devapp.walkietalkiek

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import timber.log.Timber
import java.util.concurrent.Executors

class VoicePlayer {
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private var audioTrack: AudioTrack? = null
    private val executorAudioReader = Executors.newFixedThreadPool(1)
    private var bufferSize = 0

    fun create() {
        val minRate = getMinRate()
        minRate?.let { sampleRate ->
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            bufferSize = sampleRate * (java.lang.Short.SIZE / java.lang.Byte.SIZE) * 4
            if (bufferSize < minBufferSize) bufferSize = minBufferSize
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize / 4,
                AudioTrack.MODE_STREAM
            ).apply {
                play()
            }
        }
    }

    fun play(bytes: ByteArray) {
        Timber.i("play ${bytes.size} - ${bytes[0]} ${bytes[1]}")
        audioTrack?.apply {
            write(bytes, 0, bytes.size)
            Timber.i("write ${bytes.size}")
//            if (frame++ == 0){
//                audioTrack?.play()
//                audioTrack?.stop()
//            }
        }
    }

    fun startPlay() {

    }

    fun stopPlay() {
        executorAudioReader.shutdown()
        audioTrack?.apply {
            stop()
            release()
        }
    }


    private fun getMinRate(): Int? {
        val rates = arrayOf(8000, 11025, 16000, 22050, 44100)
        rates.forEach {
            val minBufferSize = AudioRecord.getMinBufferSize(
                it, channelConfig, AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBufferSize != AudioRecord.ERROR &&
                minBufferSize != AudioRecord.ERROR_BAD_VALUE
            ) {
                return it
            }
        }
        return null
    }
}