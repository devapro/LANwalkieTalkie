package pro.devapp.walkietalkiek.reducers

import android.Manifest
import pro.devapp.walkietalkiek.PermissionState
import pro.devapp.walkietalkiek.core.mvi.Reducer
import pro.devapp.walkietalkiek.model.MainScreenAction
import pro.devapp.walkietalkiek.model.MainScreenEvent
import pro.devapp.walkietalkiek.model.MainScreenState

internal class CheckPermissionsReducer(
    private val permissionState: PermissionState
): Reducer<MainScreenAction.CheckPermissions, MainScreenState, MainScreenAction, MainScreenEvent> {

    override val actionClass = MainScreenAction.CheckPermissions::class

    override suspend fun reduce(
        action: MainScreenAction.CheckPermissions,
        getState: () -> MainScreenState
    ): Reducer.Result<MainScreenState, MainScreenAction, MainScreenEvent?> {
        val isNotificationPermissionGranted = permissionState.isNotificationPermissionGranted()
        val isAudioRecordPermissionGranted = permissionState.isAudioRecordPermissionGranted()
        val requestPermissions = mutableListOf<String>()
        if (isNotificationPermissionGranted.not()) {
            requestPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (isAudioRecordPermissionGranted.not()) {
            requestPermissions.add(Manifest.permission.RECORD_AUDIO)
        }
        return if (isNotificationPermissionGranted.not()
            || isAudioRecordPermissionGranted.not()
        ) {
            Reducer.Result(
                state = getState().copy(
                    requiredPermissions = requestPermissions,
                ),
                action = null,
                event = MainScreenEvent.RequestPermissions(
                    permissions = requestPermissions
                )
            )
        } else {
            Reducer.Result(
                state = getState().copy(
                    requiredPermissions = emptyList()
                ),
                action = null,
                event = MainScreenEvent.StartService
            )
        }
    }

}