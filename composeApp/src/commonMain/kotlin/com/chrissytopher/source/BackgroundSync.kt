package com.chrissytopher.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlin.collections.map
import kotlin.collections.orEmpty

typealias GetSourceDataLambda = suspend (String, String, String, Boolean) -> Result<SourceData>

suspend fun doBackgroundSync(
    username: String,
    password: String,
    sourceData: HashMap<String, SourceData>?,
    currentUpdates: HashMap<Int, Boolean>,
    thresholdNotifications: Boolean,
    thresholdNotificationsValue: Float,
    newAssignmentNotifications: Boolean,
    letterGradeChangedNotifications: Boolean,
    preferReported: Boolean,
    notificationSender: NotificationSender?,
    getSourceData: GetSourceDataLambda
): Pair<HashMap<String, SourceData>, Map<Int, Boolean>>? {
    println("background sync started")
//    currentData[USERNAME_PREFERENCE]?.let { username ->
//        currentData[PASSWORD_PREFERENCE]?.let { password ->
            val quarter = getCurrentQuarter()
            getSourceData(username, password, quarter, true).getOrNullAndThrow()?.let { newSourceData ->
//                val sourceData = currentData[SOURCE_DATA_PREFERENCE]?.let { json.decodeFromString<HashMap<String, SourceData>>(it) }
//                dataStore.edit {
//                    it[SOURCE_DATA_PREFERENCE] = json.encodeToString()
//                }
                val newSourceDataMap = HashMap(sourceData ?: HashMap()).apply {
                    set(quarter, newSourceData)
                }
//                val currentUpdates = currentData[CLASS_UPDATES_PREFERENCE]?.let { json.decodeFromString<HashMap<String, Boolean>>(it) } ?: hashMapOf()
                val newAssignments = arrayListOf<Pair<AssignmentSection, Class>>()
                val updatedClasses = newSourceData.classes.filter { newClass ->
                    val oldClass = sourceData?.get(quarter)?.classes?.find { it.name == newClass.name}
                    if (oldClass == null) {
                        newClass.assignments_parsed.flatMap { it._assignmentsections }.forEach {
                            newAssignments += Pair(it, newClass)
                        }
                        return@filter true
                    }
                    val newSections = newClass.assignments_parsed.flatMap { it._assignmentsections }
                    val oldSections = oldClass.assignments_parsed.flatMap { it._assignmentsections }
                    newSections.forEach { newSection ->
                        val oldSection = oldSections.find { it._id == newSection._id }
                        if (oldSection == null || oldSection._assignmentscores.size < newSection._assignmentscores.size) {
                            newAssignments += Pair(newSection, newClass)
                        }
                    }
                    (oldClass.totalScores() != newClass.totalScores())
                }
//                dataStore.edit {
//                    it[CLASS_UPDATES_PREFERENCE] = json.encodeToString(currentUpdates + hashMapOf(*updatedClasses.map { Pair(it.name, true) }.toTypedArray()))
//                }
                val newAssignmentUpdates = currentUpdates + hashMapOf(*newAssignments.map { Pair(it.first._id, true) }.toTypedArray())
                newAssignments.forEach { (newAssignment, newClass) ->
                    if (thresholdNotifications && thresholdNotificationsValue <= newAssignment.totalpointvalue) {
                        notificationSender?.sendNotification("New Assignment in ${newClass.name}", "${newAssignment.name}, worth ${newAssignment.totalpointvalue} points - Tap to view")
                    } else if (newAssignmentNotifications) {
                        notificationSender?.sendNotification("New Assignment in ${newClass.name}", "${newAssignment.name} - Tap to view")
                    }
                }
                if (letterGradeChangedNotifications) {
                    for (oldClass in sourceData?.get(quarter)?.classes ?: listOf()) {
                        newSourceData.classes.find { it.name == oldClass.name }?.let { newClass ->
                            val oldGrade = if (preferReported) {
                                oldClass.reported_grade ?: ClassMeta(oldClass).grade
                            } else {
                                val oldMeta = ClassMeta(oldClass)
                                oldMeta.grade
                            }
                            val newGrade = if (preferReported) {
                                newClass.reported_grade ?: ClassMeta(newClass).grade
                            } else {
                                val newMeta = ClassMeta(newClass)
                                newMeta.grade
                            }
                            if (oldGrade != newGrade && newGrade != null) {
                                notificationSender?.sendNotification("Letter grade changed", "${newClass.name} grade changed to $newGrade - Tap to view")
                            }
                        }
                    }
                }
                return Pair(newSourceDataMap, newAssignmentUpdates)
            }
//        }
//    }
    Napier.w("background sync failed")
    return null
}

suspend fun backgroundSyncDatastore(dataStore: DataStore<Preferences>, json: Json, notificationSender: NotificationSender?, runBackgroundSync: GetSourceDataLambda): Boolean {
    val preferences = dataStore.data.first()
    val username = preferences[USERNAME_PREFERENCE] ?: return false
    val password = preferences[PASSWORD_PREFERENCE] ?: return false
    val sourceData = preferences[SOURCE_DATA_PREFERENCE]?.let { json.decodeFromString<HashMap<String, SourceData>>(it) }
    val currentUpdates = preferences[ASSIGNMENT_UPDATES_PREFERENCE]?.let { json.decodeFromString<HashMap<Int, Boolean>>(it) } ?: hashMapOf()
    val thresholdNotifications = preferences[THRESHOLD_NOTIFICATIONS_PREFERENCE] ?: false
    val thresholdNotificationsValue = preferences[THRESHOLD_VALUE_NOTIFICATIONS_PREFERENCE] ?: 100f
    val newAssignmentNotifications = preferences[NEW_ASSIGNMENTS_NOTIFICATIONS_PREFERENCE] ?: false
    val letterGradeChangedNotifications = preferences[LETTER_GRADE_CHANGES_NOTIFICATIONS_PREFERENCE] ?: false
    val preferReported = preferences[PREFER_REPORTED_PREFERENCE] ?: true
    val result = doBackgroundSync(username, password, sourceData, currentUpdates, thresholdNotifications, thresholdNotificationsValue, newAssignmentNotifications, letterGradeChangedNotifications, preferReported, notificationSender, runBackgroundSync) ?: return false
    dataStore.edit {
        it[SOURCE_DATA_PREFERENCE] = json.encodeToString(result.first)
        it[ASSIGNMENT_UPDATES_PREFERENCE] = json.encodeToString(result.second)
    }
    return true
}