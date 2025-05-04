package com.chrissytopher.source

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.painter.Painter
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.PlatformContext
import dev.chrisbanes.haze.HazeState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class AppViewModel(val dataStore: DataStore<Preferences>) : ViewModel() {
    val json = Json { ignoreUnknownKeys = true }
    abstract val platformContext: PlatformContext
    abstract val notificationSender: NotificationSender?

    @Composable
    abstract fun notificationsAllowed(): State<Boolean>
    abstract fun requestNotificationPermissions()

    val classForGradePage: MutableState<Class?> = mutableStateOf(null)
    val assignmentForPage: MutableState<AssignmentSection?> = mutableStateOf(null)
    val refreshedAlready = mutableStateOf(false)
    val homeScrollState = ScrollState(0)
    val hazeState = HazeState()
    val lastClassMeta: MutableState<List<ClassMeta>?> = mutableStateOf(null)
    val initializedFlows = MutableStateFlow(false)

    private lateinit var _sourceData: StateFlow<HashMap<String, SourceData>?>
    private lateinit var _selectedQuarter: StateFlow<String>
    private lateinit var _showMiddleName: StateFlow<Boolean>
    private lateinit var _updateClasses: StateFlow<HashMap<String, Boolean>>
    private lateinit var _hideMentorship: StateFlow<Boolean>
    private lateinit var _preferReported: StateFlow<Boolean>
    private lateinit var _currentTheme: StateFlow<ThemeVariant>
    private lateinit var _gradeColors: StateFlow<GradeColors>

    private lateinit var _notificationsEveryAssignment: StateFlow<Boolean>
    private lateinit var _notificationsLetterGradeChange: StateFlow<Boolean>
    private lateinit var _notificationsThreshold: StateFlow<Boolean>
    private lateinit var _notificationsPoints: StateFlow<Float>
    private lateinit var _username: StateFlow<String?>
    private lateinit var _password: StateFlow<String?>

    private val _schoolTitleImagePainter: MutableState<Painter?> = mutableStateOf(null)
    val schoolTitleImage: State<Painter?> = _schoolTitleImagePainter

    init {
        viewModelScope.launch {
            _sourceData = dataStore.data.map { it[SOURCE_DATA_PREFERENCE]?.let { gradeData ->
                runCatching { json.decodeFromString<HashMap<String, SourceData>>(gradeData) }.getOrNullAndThrow()
            } }.stateIn(viewModelScope)
            _selectedQuarter = dataStore.data.map { it[QUARTER_PREFERENCE] ?: getCurrentQuarter() }.stateIn(viewModelScope)
            _showMiddleName = dataStore.data.map { it[SHOW_MIDDLE_NAME_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _updateClasses = dataStore.data.map { it[CLASS_UPDATES_PREFERENCE]?.let {
                json.decodeFromString<HashMap<String, Boolean>>(it)
            } ?: hashMapOf() }.stateIn(viewModelScope)
            _hideMentorship = dataStore.data.map { it[HIDE_MENTORSHIP_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _preferReported = dataStore.data.map { it[PREFER_REPORTED_PREFERENCE] ?: true }.stateIn(viewModelScope)
            _currentTheme = dataStore.data.map { ThemeVariant.valueOf(it[THEME_PREFERENCE] ?: ThemeVariant.Dynamic.name) }.stateIn(viewModelScope)
            _gradeColors = dataStore.data.map { it[GRADE_COLORS_PREFERENCE]?.let {
                runCatching { Json.decodeFromString<GradeColors>(it) }.getOrNull() } ?: GradeColors.default()
            }.stateIn(viewModelScope)

            _notificationsEveryAssignment = dataStore.data.map { it[NEW_ASSIGNMENTS_NOTIFICATIONS_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _notificationsLetterGradeChange = dataStore.data.map { it[LETTER_GRADE_CHANGES_NOTIFICATIONS_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _notificationsThreshold = dataStore.data.map { it[THRESHOLD_NOTIFICATIONS_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _notificationsPoints = dataStore.data.map { it[THRESHOLD_VALUE_NOTIFICATIONS_PREFERENCE] ?: 100f }.stateIn(viewModelScope)
            _username = dataStore.data.map { it[USERNAME_PREFERENCE] }.stateIn(viewModelScope)
            _password = dataStore.data.map { it[PASSWORD_PREFERENCE] }.stateIn(viewModelScope)
            migrations()
            initializedFlows.value = true

            launch {
//                _schoolTitleImagePainter.value = ImageLoader(platformContext).execute(ImageRequest.Builder(platformContext).data(_sourceData.value?.getSchool()?.titleImageUrl).build()).image?.asPainter(platformContext)
            }
        }
    }

    @Composable
    fun sourceData(): State<HashMap<String, SourceData>?> = _sourceData.collectAsState()
    @Composable
    fun selectedQuarter(): State<String> = _selectedQuarter.collectAsState()
    @Composable
    fun updateClasses(): State<HashMap<String, Boolean>> = _updateClasses.collectAsState()
    @Composable
    fun hideMentorship(): State<Boolean> = _hideMentorship.collectAsState()
    @Composable
    fun showMiddleName() = _showMiddleName.collectAsState()
    @Composable
    fun preferReported(): State<Boolean> = _preferReported.collectAsState()
    @Composable
    fun currentTheme(): State<ThemeVariant> = _currentTheme.collectAsState()
    @Composable
    fun gradeColors(): State<GradeColors> = _gradeColors.collectAsState()
    @Composable
    fun username(): State<String?> = _username.collectAsState()

    @Composable
    fun notificationsEveryAssignment(): State<Boolean> = _notificationsEveryAssignment.collectAsState()
    @Composable
    fun notificationsLetterGradeChange(): State<Boolean> = _notificationsLetterGradeChange.collectAsState()
    @Composable
    fun notificationsThreshold(): State<Boolean> = _notificationsThreshold.collectAsState()
    @Composable
    fun notificationsPoints(): State<Float> = _notificationsPoints.collectAsState()

    val refreshingInProgress = mutableStateOf(false)
    val refreshSuccess: MutableState<Boolean?> = mutableStateOf(null)
    fun refresh() {
        _username.value?.let { username ->
            _password.value?.let { password ->
                viewModelScope.launch {
                    refreshingInProgress.value = true
                    val newSourceData = gradeSyncManager.getSourceData(username, password, _selectedQuarter.value, false).getOrNullAndThrow()
                    if (newSourceData != null && !(newSourceData.classes.isEmpty() && newSourceData.past_classes.isEmpty())) {
                        if (_selectedQuarter.value == getCurrentQuarter()) {
                            val currentUpdates = _updateClasses.value
                            val updatedClasses = newSourceData.classes.filter { newClass ->
                                val oldClass = _sourceData.value?.get(_selectedQuarter.value)?.classes?.find { it.name == newClass.name} ?: return@filter false
                                (oldClass.totalScores() != newClass.totalScores())
                            }
                            val updatedClassesMap = hashMapOf(*updatedClasses.map { Pair(it.name, true) }.toTypedArray())
                            dataStore.edit {
                                it[CLASS_UPDATES_PREFERENCE] = json.encodeToString<Map<String, Boolean>>(currentUpdates + updatedClassesMap)
                            }
                        }
                        dataStore.edit {
                            it[SOURCE_DATA_PREFERENCE] = json.encodeToString(HashMap(_sourceData.value ?: HashMap()).apply {
                                set(_selectedQuarter.value, newSourceData)
                            })
                        }
                        refreshSuccess.value = true
                    } else {
                        refreshSuccess.value = false
                    }
                    refreshedAlready.value = true
                    refreshingInProgress.value = false
                    delay(500)
                    refreshSuccess.value = null
                }
            }
        }
    }

    fun changeLogin(username : String, password : String, sourceData: HashMap<String, SourceData>) = viewModelScope.launch {
        dataStore.edit {
            it[USERNAME_PREFERENCE] = username
            it[PASSWORD_PREFERENCE] = password
            it[SOURCE_DATA_PREFERENCE] = json.encodeToString(sourceData)
        }
    }

    fun setSourceData(sourceData: HashMap<String, SourceData>) = viewModelScope.launch {
        dataStore.edit {
            it[SOURCE_DATA_PREFERENCE] = json.encodeToString(sourceData)
        }
    }

    fun setSelectedQuarter(quarter: String) = viewModelScope.launch {
        dataStore.edit {
            if (quarter == getCurrentQuarter()) {
                it.remove(QUARTER_PREFERENCE)
            } else {
                it[QUARTER_PREFERENCE] = quarter
            }
        }
//        if (getCurrentQuarter() == _selectedQuarter.value) {
//            val classNames = _sourceData.value?.get(quarter)?.classes?.map { it.name }
//            val updateClassesNew = _updateClasses.value.filter { classNames?.contains(it.key) == true }
//            viewModelScope.launch { dataStore.edit {
//                it[CLASS_UPDATES_PREFERENCE] = json.encodeToString(updateClassesNew)
//            } }
//        } else {
//            viewModelScope.launch { dataStore.edit {
//                it[CLASS_UPDATES_PREFERENCE] = json.encodeToString<HashMap<String, Boolean>>(hashMapOf())
//            } }
//        }
        refresh()
    }

    fun setCurrentTheme(themeVariant: ThemeVariant) = viewModelScope.launch {
        dataStore.edit {
            it[THEME_PREFERENCE] = themeVariant.toString()
        }
    }

    fun setGradeColors(gradeColors: GradeColors) = viewModelScope.launch {
        dataStore.edit {
            it[GRADE_COLORS_PREFERENCE] = json.encodeToString(gradeColors)
        }
    }

    fun setHideMentorship(hideMentorship: Boolean) = viewModelScope.launch {
        dataStore.edit {
            it[HIDE_MENTORSHIP_PREFERENCE] = hideMentorship
        }
    }

    fun setShowMiddleName(showMiddleName: Boolean) = viewModelScope.launch {
        dataStore.edit {
            it[SHOW_MIDDLE_NAME_PREFERENCE] = showMiddleName
        }
    }

    fun setPreferReported(preferReported: Boolean) = viewModelScope.launch {
        dataStore.edit {
            it[PREFER_REPORTED_PREFERENCE] = preferReported
        }
    }

    fun markClassRead(readClass: Class) = viewModelScope.launch {
        var updateClasses: Map<String, Boolean> = _updateClasses.value
        readClass.name.let { currentName -> updateClasses = updateClasses.filter { it.key != currentName } }
        dataStore.edit {
            it[CLASS_UPDATES_PREFERENCE] = json.encodeToString(updateClasses)
        }
    }

    fun setNotificationsEveryAssignment(everyAssignment: Boolean) = viewModelScope.launch {
        dataStore.edit {
            it[NEW_ASSIGNMENTS_NOTIFICATIONS_PREFERENCE] = everyAssignment
        }
    }

    fun setNotificationsLetterGradeChanged(letterGradeChanged: Boolean) = viewModelScope.launch {
        dataStore.edit {
            it[LETTER_GRADE_CHANGES_NOTIFICATIONS_PREFERENCE] = letterGradeChanged
        }
    }

    fun setNotificationsThreshold(threshold: Boolean) = viewModelScope.launch {
        dataStore.edit {
            it[THRESHOLD_NOTIFICATIONS_PREFERENCE] = threshold
        }
    }

    fun setNotificationsThresholdPoints(points: Float) = viewModelScope.launch {
        dataStore.edit {
            it[THRESHOLD_VALUE_NOTIFICATIONS_PREFERENCE] = points
        }
    }

    fun logOut() = viewModelScope.launch {
        dataStore.edit {
            it.remove(USERNAME_PREFERENCE)
            it.remove(PASSWORD_PREFERENCE)
            it.remove(SOURCE_DATA_PREFERENCE)
        }
    }

    val gradeSyncManager by lazy { GradeSyncManager(this) }
    protected abstract fun getSourceData(username: String, password: String, quarter: String, loadPfp: Boolean): Result<SourceData>
    class GradeSyncManager(private val viewModel: AppViewModel) {
        private var deferredResult: Deferred<Result<SourceData>>? = null

        suspend fun getSourceData(username: String, password: String, quarter: String, loadPfp: Boolean): Result<SourceData> {
            deferredResult?.let {
                if (it.isActive) {
                    Napier.d("deferring")
                    return@getSourceData it.await()
                }
            }
            Napier.d("running new call")
            val newDeferred = CoroutineScope(Dispatchers.Default).async {
                viewModel.getSourceData(username, password, quarter, loadPfp)
            }
            deferredResult = newDeferred
            return newDeferred.await()
        }
    }

    open suspend fun migrations() {}
}

fun HashMap<String, SourceData>.getSchool(): School? = get(getCurrentQuarter())?.let { schoolFromClasses(it) }?.firstOrNull()?.let { id -> School.entries.find { it.id == id } }