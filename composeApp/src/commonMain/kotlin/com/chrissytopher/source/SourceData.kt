@file:Suppress("PropertyName", "SpellCheckingInspection")

package com.chrissytopher.source

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import com.chrynan.uri.core.Uri
import com.chrynan.uri.core.fromStringOrNull
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class SourceData(
    var classes: List<Class>,
    var grade_level: String?,
    var student_name: String,
    var past_classes: List<PastClass>,
)

@Serializable
data class Class(
    var assignments_parsed: List<Assignment>,
    var frn: String,
    var store_code: String,
    var url: String,
    var name: String,
    var teacher_name: String,
    var teacher_contact: String,
) {
    fun totalSections(): Int {
        return assignments_parsed.sumOf { it._assignmentsections.size }
    }
}

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

@Serializable
data class PastClass (
    var date_completed: String,
    var grade_level: String,
    var school: String,
    var course_id: String,
    var course_name: String,
    var credit_earned: Float,
    var credit_attempted: Float,
    var grade: String,
)

class ClassMeta {
    var totalPoints: Float = 0f
    var earnedPoints: Float = 0f
    var finalScore: Float? = null
    var grade: String? = null

    constructor(classData : Class) {
        classData.assignments_parsed.forEach { assignment ->
            assignment._assignmentsections.forEach { section ->
                if (!section.iscountedinfinalgrade) return@forEach
                if (section._assignmentscores.isEmpty()) return@forEach
                val possiblePoints = section.totalpointvalue
                var newestScore = section._assignmentscores.minByOrNull {
                    LocalDateTime.parse(it.scoreentrydate)
                }?.scorepoints
                
                if (newestScore == null) return@forEach
                newestScore *= section.weight
                if (newestScore < possiblePoints / 2f) {
                    newestScore = possiblePoints / 2f
                }
                earnedPoints += newestScore
                totalPoints += possiblePoints
            }
        }
        finalScore = runCatching { (earnedPoints/totalPoints * 1000f).roundToInt().toFloat() / 10f }.getOrNull()
        finalScore?.roundToInt()?.let { roundedScore ->
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
    

    //constructor for adding new assignments to see new grade, score is list of pairs that are <points earned, points possible>
    constructor(classData : Class, newScores : List<Pair<Float, Float>>, changedAssignments : List<ChangedAssignment>) {
        classData.assignments_parsed.forEachIndexed() { index, assignment ->
            if (changedAssignments[index].hidden) {
                return@forEachIndexed
            }
            assignment._assignmentsections.forEach {section ->

                if (!section.iscountedinfinalgrade) return@forEach
                if (section._assignmentscores.isEmpty()) return@forEach
                if (changedAssignments[index].receivedPointvalue == -1.0f) return@forEach
                val possiblePoints = changedAssignments[index].totalPointValue
                var newestScore = changedAssignments[index].receivedPointvalue

                if (newestScore < possiblePoints / 2f) {
                    newestScore = possiblePoints / 2f
                }
                earnedPoints += newestScore
                totalPoints += possiblePoints
            }
        }

        newScores.forEach { score ->
            earnedPoints += score.first
            totalPoints += score.second
        }

        finalScore = runCatching { (earnedPoints/totalPoints * 1000f).roundToInt().toFloat() / 10f }.getOrNull()
        finalScore?.roundToInt()?.let { roundedScore ->
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

    //Constructor to get grade without certian assignment
    constructor(classData : Class, withoutScore : Int) {
        classData.assignments_parsed.forEach { assignment ->
            assignment._assignmentsections.forEach { section ->
                if (!section.iscountedinfinalgrade) return@forEach
                if (section._assignmentscores.isEmpty()) return@forEach
                if (section._id == withoutScore) return@forEach
                val possiblePoints = section.totalpointvalue
                var newestScore = section._assignmentscores.minByOrNull {
                    LocalDateTime.parse(it.scoreentrydate)
                }?.scorepoints

                if (newestScore == null) return@forEach
                newestScore *= section.weight
                if (newestScore < possiblePoints / 2f) {
                    newestScore = possiblePoints / 2f
                }
                earnedPoints += newestScore
                totalPoints += possiblePoints
            }
        }


        finalScore = runCatching { (earnedPoints/totalPoints * 1000f).roundToInt().toFloat() / 10f }.getOrNull()
        finalScore?.roundToInt()?.let { roundedScore ->
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

    constructor(classData : Class, changedAssignment: AssignmentSection, changedScore: AssignmentScore) {
        classData.assignments_parsed.forEach { assignment ->
            assignment._assignmentsections.forEach { it ->
                var section = it
                if (section._id == changedAssignment._id) {
                    section = changedAssignment
                }
                if (!section.iscountedinfinalgrade) return@forEach
                if (section._assignmentscores.isEmpty() && section._id != changedAssignment._id) return@forEach

                val possiblePoints = section.totalpointvalue
                var newestScore = if (section._id == changedAssignment._id) {
                    changedScore.scorepoints
                } else {
                    section._assignmentscores.minByOrNull {
                        LocalDateTime.parse(it.scoreentrydate)
                    }?.scorepoints
                }

                if (newestScore == null) return@forEach
                newestScore *= section.weight
                if (newestScore < possiblePoints / 2f) {
                    newestScore = possiblePoints / 2f
                }
                earnedPoints += newestScore
                totalPoints += possiblePoints
            }
        }


        finalScore = runCatching { (earnedPoints/totalPoints * 1000f).roundToInt().toFloat() / 10f }.getOrNull()
        finalScore?.let { gradeForScore(it) }
    }
}

fun gradeForScore(score: Float): String {
    val roundedScore = score.roundToInt()
    return if (roundedScore > 92) {
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

@Composable
fun rememberSchoolFromClasses(sourceData: SourceData): List<String> {
    return key(sourceData) {remember { sourceData.classes.mapNotNull {
        Uri.fromStringOrNull(it.url)?.query?.split("&")?.map { it.split("=") }?.find { it.first() == "schoolid" }?.getOrNull(1)
    } } }
}