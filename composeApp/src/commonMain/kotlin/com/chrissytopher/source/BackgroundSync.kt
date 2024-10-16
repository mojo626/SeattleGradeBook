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
                    newClass.assignments_parsed.forEach { newAssignment ->
                        val oldAssignment = oldClass
                    }
                    (oldClass.totalSections() != newClass.totalSections())
                }
                kvault.set(CLASS_UPDATES_KEY, json.encodeToString(currentUpdates + updatedClasses.map { it.name }))
                Napier.d("sent notification")
                return true
            }
        }
    }
    Napier.d("failed")
    return false
}