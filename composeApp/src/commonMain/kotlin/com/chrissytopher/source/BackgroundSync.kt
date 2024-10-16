package com.chrissytopher.source

import com.liftric.kvault.KVault
import io.github.aakira.napier.Napier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun doBackgroundSync(kvault: KVault, json: Json, notificationSender: NotificationSender?): Boolean {
    println("background sync started")
    kvault.string(USERNAME_KEY)?.let { username ->
        kvault.string(PASSWORD_KEY)?.let { password ->
            getSourceData(username, password).getOrNullAndThrow()?.let { newSourceData ->
                val sourceData = kvault.string(SOURCE_DATA_KEY)?.let { json.decodeFromString<SourceData>(it) }
                kvault.set(SOURCE_DATA_KEY, json.encodeToString(newSourceData))
                val currentUpdates = kvault.string(CLASS_UPDATES_KEY)?.let { json.decodeFromString<List<String>>(it) } ?: listOf()
                val newAssignments = arrayListOf<AssignmentSection>()
                val updatedClasses = newSourceData.classes.filter { newClass ->
                    val oldClass = sourceData?.classes?.find { it.name == newClass.name} ?: return@filter true
                    val newSections = newClass.assignments_parsed.flatMap { it._assignmentsections }
                    val oldSections = oldClass.assignments_parsed.flatMap { it._assignmentsections }
                    newSections.forEach { newSection ->
                        val oldSection = oldSections.find { it._id == newSection._id }
                        if (oldSection == null || oldSection._assignmentscores.size != newSection._assignmentscores.size) {
                            newAssignments += newSection
                        }
                    }
                    (oldClass.totalSections() != newClass.totalSections())
                }
                kvault.set(CLASS_UPDATES_KEY, json.encodeToString(currentUpdates + updatedClasses.map { it.name }))
                var sendNotification = false
                if (kvault.bool(NEW_ASSIGNMENTS_NOTIFICATIONS_KEY) == true) {
                    sendNotification = true
                }
                if (kvault.bool(THRESHOLD_NOTIFICATIONS_KEY) == true) {
                    kvault.float(THRESHOLD_VALUE_NOTIFICATIONS_KEY)?.let { thresholdPoints ->
                        if ((newAssignments.maxOfOrNull { it.totalpointvalue } ?: 0f) > thresholdPoints) {
                            sendNotification = true
                        }
                    }
                }
                if (kvault.bool(LETTER_GRADE_CHANGES_NOTIFICATIONS_KEY) == true) {
                    val oldLetters = sourceData?.classes?.map {
                        ClassMeta(it).grade
                    }
                    val newLetters = newSourceData.classes.map {
                        ClassMeta(it).grade
                    }
                    if (oldLetters != newLetters) {
                        sendNotification = true
                    }
                }
                if (sendNotification) {
                    notificationSender?.sendNotification(NEW_ASSIGNMENTS_TITLE, NEW_ASSIGNMENTS_BODY)
                }
                return true
            }
        }
    }
    Napier.d("failed")
    return false
}