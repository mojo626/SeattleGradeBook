@file:Suppress("PropertyName", "SpellCheckingInspection")

package com.chrissytopher.source

import coil3.toUri
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class SourceData(
    var classes: List<Class>,
    var grade_level: String?,
    var student_name: String,
    var past_classes: List<PastClass>,
    var teachers: HashMap<String, String>,
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
    var reported_grade: String? = null,
    var reported_score: String? = null,
) {
    fun totalScores(): Int {
        return assignments_parsed.sumOf { it._assignmentsections.sumOf { it._assignmentscores.size } }
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

class ClassMeta(classData: Class, allowLessThanE: Boolean = false) {
    var totalPoints: Float = 0f
    var earnedPoints: Float = 0f
    var finalScore: Float? = null
    var grade: String? = null

    init {
        classData.assignments_parsed.forEach { assignment ->
            assignment._assignmentsections.forEach { section ->
                if (!section.iscountedinfinalgrade) return@forEach
                if (section._assignmentscores.isEmpty()) return@forEach
                val possiblePoints = section.totalpointvalue
                val newestScore = section._assignmentscores.minByOrNull {
                    LocalDateTime.parse(it.scoreentrydate)
                }

                if (newestScore?.scorepoints == null) return@forEach
                if (newestScore.isexempt) return@forEach
                var newestScorePoints: Float = newestScore.scorepoints ?: return@forEach
                newestScorePoints *= section.weight
                if (newestScorePoints < possiblePoints / 2f && !allowLessThanE) {
                    newestScorePoints = possiblePoints / 2f
                }
                earnedPoints += newestScorePoints
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
    
//
//    //constructor for adding new assignments to see new grade, score is list of pairs that are <points earned, points possible>
//    constructor(classData : Class, newScores : List<Pair<Float, Float>>, changedAssignments : List<ChangedAssignment>) {
//        classData.assignments_parsed.forEachIndexed { index, assignment ->
//            if (changedAssignments.getOrNull(index) == null) {
//                //@Ben what is it supposed to do in this case
//                return@forEachIndexed
//            }
//            if (changedAssignments[index].hidden) {
//                return@forEachIndexed
//            }
//            assignment._assignmentsections.forEach {section ->
//
//                if (!section.iscountedinfinalgrade) return@forEach
//                if (section._assignmentscores.isEmpty()) return@forEach
//                if (changedAssignments.getOrNull(index)?.receivedPointvalue == -1.0f) return@forEach
//                val possiblePoints = changedAssignments.getOrNull(index)?.totalPointValue ?: return@forEach
//                val newestScore = changedAssignments.getOrNull(index)
//                var newestScorePoints = newestScore?.receivedPointvalue ?: return@forEach
//
//                if (newestScorePoints < possiblePoints / 2f) {
//                    newestScorePoints = possiblePoints / 2f
//                }
//                earnedPoints += newestScorePoints
//                totalPoints += possiblePoints
//            }
//        }
//
//        newScores.forEach { score ->
//            earnedPoints += score.first
//            totalPoints += score.second
//        }
//
//        finalScore = runCatching { (earnedPoints/totalPoints * 1000f).roundToInt().toFloat() / 10f }.getOrNull()
//        finalScore?.roundToInt()?.let { roundedScore ->
//            grade = if (roundedScore > 92) {
//                "A"
//            } else if (roundedScore > 89) {
//                "A-"
//            } else if (roundedScore > 86) {
//                "B+"
//            } else if (roundedScore > 82) {
//                "B"
//            } else if (roundedScore > 79) {
//                "B-"
//            } else if (roundedScore > 76) {
//                "C+"
//            } else if (roundedScore > 72) {
//                "C"
//            } else if (roundedScore > 69) {
//                "C-"
//            } else if (roundedScore > 66) {
//                "D+"
//            } else if (roundedScore > 64) {
//                "D"
//            } else {
//                "E"
//            }
//        }
//    }

//    //Constructor to get grade without certian assignment
//    constructor(classData : Class, withoutScore : Int) {
//        classData.assignments_parsed.forEach { assignment ->
//            assignment._assignmentsections.forEach { section ->
//                if (!section.iscountedinfinalgrade) return@forEach
//                if (section._assignmentscores.isEmpty()) return@forEach
//                if (section._id == withoutScore) return@forEach
//                val possiblePoints = section.totalpointvalue
//                var newestScore = section._assignmentscores.minByOrNull {
//                    LocalDateTime.parse(it.scoreentrydate)
//                }?.scorepoints
//
//                if (newestScore == null) return@forEach
//                newestScore *= section.weight
//                if (newestScore < possiblePoints / 2f) {
//                    newestScore = possiblePoints / 2f
//                }
//                earnedPoints += newestScore
//                totalPoints += possiblePoints
//            }
//        }
//
//
//        finalScore = runCatching { (earnedPoints/totalPoints * 1000f).roundToInt().toFloat() / 10f }.getOrNull()
//        finalScore?.roundToInt()?.let { roundedScore ->
//            grade = if (roundedScore > 92) {
//                "A"
//            } else if (roundedScore > 89) {
//                "A-"
//            } else if (roundedScore > 86) {
//                "B+"
//            } else if (roundedScore > 82) {
//                "B"
//            } else if (roundedScore > 79) {
//                "B-"
//            } else if (roundedScore > 76) {
//                "C+"
//            } else if (roundedScore > 72) {
//                "C"
//            } else if (roundedScore > 69) {
//                "C-"
//            } else if (roundedScore > 66) {
//                "D+"
//            } else if (roundedScore > 64) {
//                "D"
//            } else {
//                "E"
//            }
//        }
//    }
//
//    constructor(classData : Class, changedAssignment: AssignmentSection, changedScore: AssignmentScore) {
//        classData.assignments_parsed.forEach { assignment ->
//            assignment._assignmentsections.forEach { it ->
//                var section = it
//                if (section._id == changedAssignment._id) {
//                    section = changedAssignment
//                }
//                if (!section.iscountedinfinalgrade) return@forEach
//                if (section._assignmentscores.isEmpty() && section._id != changedAssignment._id) return@forEach
//
//                val possiblePoints = section.totalpointvalue
//                var newestScore = if (section._id == changedAssignment._id) {
//                    changedScore.scorepoints
//                } else {
//                    section._assignmentscores.minByOrNull {
//                        LocalDateTime.parse(it.scoreentrydate)
//                    }?.scorepoints
//                }
//
//                if (newestScore == null) return@forEach
//                newestScore *= section.weight
//                if (newestScore < possiblePoints / 2f) {
//                    newestScore = possiblePoints / 2f
//                }
//                earnedPoints += newestScore
//                totalPoints += possiblePoints
//            }
//        }
//
//
//        finalScore = runCatching { (earnedPoints/totalPoints * 1000f).roundToInt().toFloat() / 10f }.getOrNull()
//        finalScore?.let { gradeForScore(it) }
//    }
}

fun gradeForScore(score: Float): String {
    val roundedScore = runCatching {
        score.roundToInt()
    }.getOrNull() ?: return "\uD83D\uDC80"
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

fun schoolFromClasses(sourceData: SourceData): List<String> = sourceData.classes.mapNotNull {
    it.url.toUri().query?.split("&")?.map { it.split("=") }?.find { it.first() == "schoolid" }?.getOrNull(1)
}