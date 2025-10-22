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
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
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
import kotlinx.io.files.Path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

abstract class AppViewModel(val dataStore: DataStore<Preferences>, downloadDir: Path) : ViewModel() {
    val json = json()
    val sourceApi = sourceApi(downloadDir, json)
    abstract val platformContext: PlatformContext
    abstract val notificationSender: NotificationSender?

    @Composable
    abstract fun notificationsAllowed(): State<Boolean>
    abstract fun requestNotificationPermissions()

    val classForGradePage: MutableStateFlow<Class?> = MutableStateFlow(null)
    val refreshedAlready = mutableStateOf(false)
    val homeScrollState = ScrollState(0)
    val hazeState = HazeState()
    val lastClassMeta: MutableState<List<ClassMeta>?> = mutableStateOf(null)
    val initializedFlows = MutableStateFlow(false)

    private lateinit var _sourceData: StateFlow<HashMap<String, SourceData>?>
    private lateinit var _selectedQuarter: StateFlow<String>
    private lateinit var _showMiddleName: StateFlow<Boolean>
    private lateinit var _scrollHomeScreen: StateFlow<Boolean>
    lateinit var _implementPluey: StateFlow<Boolean>
//    private lateinit var _updateClasses: StateFlow<HashMap<String, Boolean>>
private lateinit var _updateAssignments: StateFlow<HashMap<Int, Boolean>>
    private lateinit var _hideMentorship: StateFlow<Boolean>
    private lateinit var _preferReported: StateFlow<Boolean>
    private lateinit var _currentTheme: StateFlow<ThemeVariant>
    private lateinit var _gradeColors: StateFlow<GradeColors>
    private lateinit var _autoSync: StateFlow<Boolean>

    private lateinit var _notificationsEveryAssignment: StateFlow<Boolean>
    private lateinit var _notificationsLetterGradeChange: StateFlow<Boolean>
    private lateinit var _notificationsThreshold: StateFlow<Boolean>
    private lateinit var _notificationsPoints: StateFlow<Float>
    lateinit var _username: StateFlow<String?>
    private lateinit var _password: StateFlow<String?>

    private val _schoolTitleImagePainter: MutableState<Painter?> = mutableStateOf(null)
    val schoolTitleImage: State<Painter?> = _schoolTitleImagePainter

    private var _congraduationPageContent = MutableStateFlow<String?>(null)
    val congraduationsPageContent = _congraduationPageContent.asStateFlow()

    val displayNameState = MutableStateFlow("")
    val currentClassNameState = MutableStateFlow("")
    val gpaTypeSelectionState = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            _sourceData = dataStore.data.map { it[SOURCE_DATA_PREFERENCE]?.let { gradeData ->
                runCatching { json.decodeFromString<HashMap<String, SourceData>>(gradeData) }.getOrNullAndThrow()
            } }.stateIn(viewModelScope)
            _selectedQuarter = dataStore.data.map { it[QUARTER_PREFERENCE] ?: getCurrentQuarter() }.stateIn(viewModelScope)
            _showMiddleName = dataStore.data.map { it[SHOW_MIDDLE_NAME_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _scrollHomeScreen = dataStore.data.map { it[SCROLL_HOME_SCREEN_PREFERENCE] ?: true }.stateIn(viewModelScope)
            _implementPluey = dataStore.data.map { it[IMPLEMENT_PLUEY_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _updateAssignments = dataStore.data.map { it[ASSIGNMENT_UPDATES_PREFERENCE]?.let {
                runCatching { json.decodeFromString<HashMap<Int, Boolean>>(it) }.getOrNullAndThrow()
            } ?: hashMapOf() }.stateIn(viewModelScope)
            _hideMentorship = dataStore.data.map { it[HIDE_MENTORSHIP_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _preferReported = dataStore.data.map { it[PREFER_REPORTED_PREFERENCE] ?: true }.stateIn(viewModelScope)
            _currentTheme = dataStore.data.map { ThemeVariant.valueOf(it[THEME_PREFERENCE] ?: ThemeVariant.Dynamic.name) }.stateIn(viewModelScope)
            _gradeColors = dataStore.data.map { it[GRADE_COLORS_PREFERENCE]?.let {
                runCatching { Json.decodeFromString<GradeColors>(it) }.getOrNull() } ?: GradeColors.default()
            }.stateIn(viewModelScope)
            _autoSync = dataStore.data.map { it[AUTO_SYNC_PREFERENCE] ?: true }.stateIn(viewModelScope)

            _notificationsEveryAssignment = dataStore.data.map { it[NEW_ASSIGNMENTS_NOTIFICATIONS_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _notificationsLetterGradeChange = dataStore.data.map { it[LETTER_GRADE_CHANGES_NOTIFICATIONS_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _notificationsThreshold = dataStore.data.map { it[THRESHOLD_NOTIFICATIONS_PREFERENCE] ?: false }.stateIn(viewModelScope)
            _notificationsPoints = dataStore.data.map { it[THRESHOLD_VALUE_NOTIFICATIONS_PREFERENCE] ?: 100f }.stateIn(viewModelScope)
            _username = dataStore.data.map { it[USERNAME_PREFERENCE] }.stateIn(viewModelScope)
            _password = dataStore.data.map { it[PASSWORD_PREFERENCE] }.stateIn(viewModelScope)
            migrations()

            viewModelScope.launch {
                combine(_sourceData, _showMiddleName, _selectedQuarter) { it }.collect { (sourceDataAny, showMiddleNameAny, selectedQuarterAny) ->
                    val sourceData = sourceDataAny as HashMap<String, SourceData>?
                    val showMiddleName = showMiddleNameAny as Boolean
                    val selectedQuarter = selectedQuarterAny as String
                    var displayName = sourceData?.get(selectedQuarter)?.student_name ?: ""
                    if (!showMiddleName) {
                        val names = displayName.split(" ")
                        displayName = "${names.first()} ${names.last()}"
                    }
                    displayNameState.value = displayName
                }
            }
            viewModelScope.launch {
                classForGradePage.collect {
                    currentClassNameState.value = it?.name ?: ""
                }
            }

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
    fun updatedAssignments(): State<HashMap<Int, Boolean>> = _updateAssignments.collectAsState()
    @Composable
    fun hideMentorship(): State<Boolean> = _hideMentorship.collectAsState()
    @Composable
    fun showMiddleName() = _showMiddleName.collectAsState()
    @Composable
    fun implementPluey() = _implementPluey.collectAsState()
    @Composable
    fun scrollHomeScreen() = _scrollHomeScreen.collectAsState()
    @Composable
    fun preferReported(): State<Boolean> = _preferReported.collectAsState()
    @Composable
    fun currentTheme(): State<ThemeVariant> = _currentTheme.collectAsState()
    @Composable
    fun gradeColors(): State<GradeColors> = _gradeColors.collectAsState()
    @Composable
    fun autoSync(): State<Boolean> = _autoSync.collectAsState()
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
                            val currentUpdates = _updateAssignments.value
                            val oldAssignments = hashMapOf<Int, Assignment>(*_sourceData.value?.get(_selectedQuarter.value)?.classes.orEmpty().map { it.assignments_parsed }.flatten().mapNotNull { assignment -> assignment._assignmentsections.firstOrNull()?.let { Pair(it._id, assignment) } }.toTypedArray())
                            val allNewAssignments = newSourceData.classes.map { it.assignments_parsed }.flatten()
                            val updatedAssignments = allNewAssignments.filter {
                                it._assignmentsections.size > (it._assignmentsections.firstOrNull()?.let { oldAssignments[it._id]?._assignmentsections?.size } ?: 0)
                            }
                            //I'm explicitly unwrapping the section because the previous step implicitly filters assignments with no sections
                            val updatedAssignmentsMap = hashMapOf(*updatedAssignments.map { Pair(it._assignmentsections.firstOrNull()!!._id, true) }.toTypedArray())
                            dataStore.edit {
                                it[ASSIGNMENT_UPDATES_PREFERENCE] = json.encodeToString<Map<Int, Boolean>>(currentUpdates + updatedAssignmentsMap)
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

    suspend fun loadCongraduationsContent() = runCatching {
        val pageContentString = sourceApi.httpClient.get(PAGE_CONTENT_URL).bodyAsText()
        _congraduationPageContent.value = pageContentString
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

    fun setImplementPluey(implementPluey: Boolean) = viewModelScope.launch {
        dataStore.edit {
            it[IMPLEMENT_PLUEY_PREFERENCE] = implementPluey
        }
    }

    fun setScrollHomeScreen(scrollHomeScreen: Boolean) = viewModelScope.launch {
        dataStore.edit {
            it[SCROLL_HOME_SCREEN_PREFERENCE] = scrollHomeScreen
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

    fun setAutoSync(autoSync: Boolean) = viewModelScope.launch {
        dataStore.edit {
            it[AUTO_SYNC_PREFERENCE] = autoSync
        }
    }

    fun markClassRead(readClass: Class) = viewModelScope.launch {
        var updateAssignments: Map<Int, Boolean> = _updateAssignments.value
        val readAssignments = hashMapOf(*readClass.assignments_parsed.mapNotNull { it._assignmentsections.firstOrNull()?._id?.let { Pair(it, Unit) } }.toTypedArray())
        updateAssignments = updateAssignments.filter { !readAssignments.contains(it.key) }
        dataStore.edit {
            it[ASSIGNMENT_UPDATES_PREFERENCE] = json.encodeToString(updateAssignments)
        }
    }

    fun _setUpdatedAssignments(updateAssignments: Map<Int, Boolean>) = viewModelScope.launch {
        dataStore.edit {
            it[ASSIGNMENT_UPDATES_PREFERENCE] = json.encodeToString(updateAssignments)
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
    class GradeSyncManager(private val viewModel: AppViewModel) {
        private var deferredResult: Deferred<Result<SourceData>>? = null

        suspend fun getSourceData(username: String, password: String, quarter: String, loadPfpSynchronously: Boolean): Result<SourceData> {
            deferredResult?.let {
                if (it.isActive) {
                    Napier.d("deferring")
                    return@getSourceData it.await()
                }
            }
            Napier.d("running new call")
            val newDeferred = CoroutineScope(Dispatchers.Default).async {
                viewModel.sourceApi.getSourceData(username, password, quarter, true, loadPfpSynchronously, null)
            }
            deferredResult = newDeferred
            return newDeferred.await()
        }
    }

    open suspend fun migrations() {}
}

fun HashMap<String, SourceData>.getSchool(): School? = get(getCurrentQuarter())?.let { schoolFromClasses(it) }?.firstOrNull()?.let { id -> School.entries.find { it.id == id } }

@OptIn(ExperimentalSerializationApi::class)
fun json() = Json {
    ignoreUnknownKeys = true
    allowTrailingComma = true
    explicitNulls = false
}

fun sourceApi(downloadDir: Path, json: Json = json()) =
    SourceApi(HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpCookies)
    }, json, downloadDir)
