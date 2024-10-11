@file:Suppress("PropertyName", "SpellCheckingInspection")

package com.chrissytopher.source

import kotlinx.serialization.Serializable

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