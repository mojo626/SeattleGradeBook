@file:Suppress("PropertyName", "SpellCheckingInspection")

package com.chrissytopher.source

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.roundToInt

@Serializable
data class Class(
    var assignments_parsed: List<Assignment>,
    var frn: String,
    var store_code: String,
    var url: String,
    var name: String,
)

@Serializable
data class Assignment (
    var _assignmentsections: List<AssignmentSection>,
)

@Serializable
data class AssignmentSection (
    var _id: Int,
    var _name: String,
    var assignmentsectionid: Int,
    var duedate: String,
    var iscountedinfinalgrade: Boolean,
    var isscorespublish: Boolean,
    var isscoringneeded: Boolean,
    var name: String,
    var scoreentrypoints: Float,
    var scoretype: String,
    var sectionsdcid: Int,
    var totalpointvalue: Float,
    var weight: Float,
    var _assignmentscores: List<AssignmentScore>,
)

@Serializable
data class AssignmentScore (
    var _name: String,
    var actualscoreentered: String?,
    var actualscorekind: String?,
    var authoredbyuc: Boolean,
    var isabsent: Boolean,
    var iscollected: Boolean,
    var isexempt: Boolean,
    var isincomplete: Boolean,
    var islate: Boolean,
    var ismissing: Boolean,
    var scoreentrydate: String,
    var scorelettergrade: String?,
    var scorepercent: Float?,
    var scorepoints: Float?,
    var studentsdcid: Int,
    var whenmodified: String,
)

class ClassMeta(classData: Class) {
    var totalPoints: Float = 0f
    var earnedPoints: Float = 0f
    var finalScore: Float
    var grade: String

    init {
        classData.assignments_parsed.forEach { assignment ->
            assignment._assignmentsections.forEach { section ->
                val possiblePoints = section.totalpointvalue
                val finalScore = max(section._assignmentscores.minByOrNull {
                    LocalDateTime.parse(it.scoreentrydate)
                }?.scorepercent ?: 0f, 50f)/100f * possiblePoints
                earnedPoints += finalScore
                totalPoints += possiblePoints
            }
        }
        finalScore = (earnedPoints/totalPoints * 1000f).roundToInt().toFloat() / 10f
        val roundedScore = finalScore.roundToInt()
        grade = if (roundedScore > 92) {
            "A"
        } else if (roundedScore > 89) {
            "A-"
        } else if (roundedScore > 86) {
            "B+"
        } else if (roundedScore > 82) {
            "B"
        } else if (roundedScore > 79) {
            "B-"
        } else if (roundedScore > 76) {
            "C+"
        } else if (roundedScore > 72) {
            "C"
        } else if (roundedScore > 69) {
            "C-"
        } else if (roundedScore > 66) {
            "D+"
        } else if (roundedScore > 64) {
            "D"
        } else {
            "E"
        }
    }
}