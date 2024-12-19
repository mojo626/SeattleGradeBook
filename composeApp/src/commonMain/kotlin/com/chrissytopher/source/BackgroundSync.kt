package com.chrissytopher.source

import com.liftric.kvault.KVault
import io.github.aakira.napier.Napier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun doBackgroundSync(kvault: KVault, json: Json, notificationSender: NotificationSender?, platform: Platform): Boolean {
    println("background sync started")
    kvault.string(USERNAME_KEY)?.let { username ->
        kvault.string(PASSWORD_KEY)?.let { password ->
            val quarter = getCurrentQuarter()
            platform.gradeSyncManager.getSourceData(username, password, quarter, true).getOrNullAndThrow()?.let { newSourceData ->
                val sourceData = kvault.string(SOURCE_DATA_KEY)?.let { json.decodeFromString<HashMap<String, SourceData>>(it) }
                kvault.set(SOURCE_DATA_KEY, json.encodeToString(HashMap(sourceData ?: HashMap()).apply {
                    set(quarter, newSourceData)
                }))
                val currentUpdates = kvault.string(CLASS_UPDATES_KEY)?.let { json.decodeFromString<List<String>>(it) } ?: listOf()
                val newAssignments = arrayListOf<Pair<AssignmentSection, Pair<Class, Class?>>>()
                val updatedClasses = newSourceData.classes.filter { newClass ->
                    val oldClass = sourceData?.get(quarter)?.classes?.find { it.name == newClass.name}
                    if (oldClass == null) {
                        newClass.assignments_parsed.flatMap { it._assignmentsections }.forEach {
                            newAssignments += Pair(it, Pair(newClass, null))
                        }
                        return@filter true
                    }
                    val newSections = newClass.assignments_parsed.flatMap { it._assignmentsections }
                    val oldSections = oldClass.assignments_parsed.flatMap { it._assignmentsections }
                    newSections.forEach { newSection ->
                        val oldSection = oldSections.find { it._id == newSection._id }
                        if (oldSection == null || oldSection._assignmentscores.size < newSection._assignmentscores.size) {
                            newAssignments += Pair(newSection, Pair(newClass, oldClass))
                        }
                    }
                    (oldClass.totalSections() != newClass.totalSections())
                }
                kvault.set(CLASS_UPDATES_KEY, json.encodeToString(currentUpdates + updatedClasses.map { it.name }))
                newAssignments.forEach { (newAssignment, classes) ->
                    val (newClass, oldClass) = classes
                    var sendNotification = false
                    if (kvault.bool(NEW_ASSIGNMENTS_NOTIFICATIONS_KEY) == true) {
                        sendNotification = true
                    }
                    if (kvault.bool(THRESHOLD_NOTIFICATIONS_KEY) == true) {
                        kvault.float(THRESHOLD_VALUE_NOTIFICATIONS_KEY)?.let { thresholdPoints ->
                            if (newAssignment.totalpointvalue > thresholdPoints) {
                                sendNotification = true
                            }
                        }
                    }
                    if (kvault.bool(LETTER_GRADE_CHANGES_NOTIFICATIONS_KEY) == true) {
                        val oldLetter = oldClass?.let { ClassMeta(it).grade }
                        val newLetter = ClassMeta(newClass).grade
                        if (oldLetter != newLetter) {
                            sendNotification = true
                        }
                    }
                    if (sendNotification) {
                        notificationSender?.sendNotification("New Assignment in ${newClass.name}", "${newAssignment.name} - Tap to view")
                    }
                }
                return true
            }
        }
    }
    Napier.d("failed")
    return false
}