package com.chrissytopher.source

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.PlatformContext
import com.liftric.kvault.KVault
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.launch
import kotlinx.io.files.Path

class AndroidAppViewModel(private val applicationContext: Context) : AppViewModel(createDataStore(applicationContext), Path(applicationContext.filesDir.absolutePath)) {
    val kVault = KVault(applicationContext)
    override val platformContext: PlatformContext = applicationContext
    val permissionsController: PermissionsController = PermissionsController(applicationContext)
    override val notificationSender = AndroidNotificationSender(applicationContext)

//    override fun getSourceData(username: String, password: String, quarter: String, loadPfp: Boolean): Result<SourceData> = runCatching {
//        json.decodeFromString(SourceApi.getSourceData(username, password, applicationContext.filesDir.absolutePath, quarter, loadPfp))
//    }

    companion object {
        fun factory(applicationContext: Context) : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AndroidAppViewModel(
                    applicationContext,
                )
            }
        }
    }

    private suspend fun migrateFromKVault() {
        if (kVault.bool(MIGRATED_TO_DATASTORE_KEY) == true) return
        dataStore.edit { preferences ->
            kVault.string(SOURCE_DATA_KEY)?.let { preferences[SOURCE_DATA_PREFERENCE] = it }
            kVault.string(QUARTER_KEY)?.let { preferences[QUARTER_PREFERENCE] = it }
            kVault.bool(SHOW_MIDDLE_NAME_KEY)?.let { preferences[SHOW_MIDDLE_NAME_PREFERENCE] = it }
            //just clear the updates, I don't trust it anymore anyway
//            kVault.string(CLASS_UPDATES_KEY)?.let { preferences[CLASS_UPDATES_PREFERENCE] = it }
            kVault.bool(HIDE_MENTORSHIP_KEY)?.let { preferences[HIDE_MENTORSHIP_PREFERENCE] = it }
            kVault.bool(PREFER_REPORTED_KEY)?.let { preferences[PREFER_REPORTED_PREFERENCE] = it }
            kVault.string("THEME")?.let { preferences[THEME_PREFERENCE] = it }
            kVault.string(GRADE_COLORS_KEY)?.let { preferences[GRADE_COLORS_PREFERENCE] = it }
            kVault.bool(NEW_ASSIGNMENTS_NOTIFICATIONS_KEY)?.let { preferences[NEW_ASSIGNMENTS_NOTIFICATIONS_PREFERENCE] = it }
            kVault.bool(LETTER_GRADE_CHANGES_NOTIFICATIONS_KEY)?.let { preferences[LETTER_GRADE_CHANGES_NOTIFICATIONS_PREFERENCE] = it }
            kVault.bool(THRESHOLD_NOTIFICATIONS_KEY)?.let { preferences[THRESHOLD_NOTIFICATIONS_PREFERENCE] = it }
            kVault.float(THRESHOLD_VALUE_NOTIFICATIONS_KEY)?.let { preferences[THRESHOLD_VALUE_NOTIFICATIONS_PREFERENCE] = it }
            kVault.string(USERNAME_KEY)?.let { preferences[USERNAME_PREFERENCE] = it }
            kVault.string(PASSWORD_KEY)?.let { preferences[PASSWORD_PREFERENCE] = it }
            kVault.set(MIGRATED_TO_DATASTORE_KEY, true)
        }
    }

    override suspend fun migrations() {
        super.migrations()
        migrateFromKVault()
    }

    private val _notificationsAllowed = mutableStateOf(false)
    init {
        viewModelScope.launch {
            _notificationsAllowed.value = permissionsController.isPermissionGranted(Permission.REMOTE_NOTIFICATION)
        }
    }

    @Composable
    override fun notificationsAllowed(): State<Boolean> {
        return _notificationsAllowed
    }

    override fun requestNotificationPermissions() {
        viewModelScope.launch {
            runCatching { permissionsController.providePermission(Permission.REMOTE_NOTIFICATION) }.exceptionOrNull()?.printStackTrace()
            _notificationsAllowed.value = permissionsController.isPermissionGranted(Permission.REMOTE_NOTIFICATION)
        }
    }
}