package pro.devapp.walkietalkiek.service.voice

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import pro.devapp.walkietalkiek.serivce.network.SocketServer
import timber.log.Timber
import java.lang.Byte
import java.lang.Short
import kotlin.ByteArray
import kotlin.Int
import kotlin.apply
import kotlin.arrayOf
import kotlin.let

class VoicePlayer(
    private val socketServer: SocketServer
) {
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private var audioTrack: AudioTrack? = null
    private var bufferSize = 0

    private val _voiceDataFlow = MutableSharedFlow<ByteArray>(
        replay = 1,
        extraBufferCapacity = 10
    )
    val voiceDataFlow: SharedFlow<ByteArray>
        get() = _voiceDataFlow

    fun create() {
        val minRate = getMinRate()
        minRate?.let { sampleRate ->
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            bufferSize = sampleRate * (Short.SIZE / Byte.SIZE) * 4
            if (bufferSize < minBufferSize) bufferSize = minBufferSize
            audioTrack =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize / 4,
                        AudioTrack.MODE_STREAM,
                        AudioTrack.WRITE_NON_BLOCKING
                    )
                } else {
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize / 4,
                        AudioTrack.MODE_STREAM
                    )
                }
            audioTrack?.apply {
                play()
            }
        }
        socketServer.dataListener = { bytes ->
            play(bytes)
        }
    }

    private fun play(bytes: ByteArray) {
        Timber.Forest.i("play ${bytes.size} - ${bytes[0]} ${bytes[1]}")
        if (audioTrack?.playState == AudioTrack.PLAYSTATE_STOPPED) {
            Timber.Forest.w("PLAYER STOPPED!!!")
        }
        audioTrack?.write(bytes, 0, bytes.size)
        _voiceDataFlow.tryEmit(bytes)
    }

    fun shutdown() {
        socketServer.dataListener = null
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