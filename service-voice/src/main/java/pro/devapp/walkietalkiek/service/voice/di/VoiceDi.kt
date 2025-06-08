package pro.devapp.walkietalkiek.service.voice.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import pro.devapp.walkietalkiek.service.voice.VoicePlayer
import pro.devapp.walkietalkiek.service.voice.VoiceRecorder

fun Module.registerVoiceDi() {
    singleOf(::VoiceRecorder)
    singleOf(::VoicePlayer)
}