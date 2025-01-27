package com.chrissytopher.source

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class GradeColors(
    val _AColor: ULong,
    val _BColor: ULong,
    val _CColor: ULong,
    val _DColor: ULong,
    val _EColor: ULong,
) {
    val AColor: Color
        get() = Color(_AColor)
    val BColor: Color
        get() = Color(_BColor)
    val CColor: Color
        get() = Color(_CColor)
    val DColor: Color
        get() = Color(_DColor)
    val EColor: Color
        get() = Color(_EColor)

    constructor(AColor: Color, BColor: Color, CColor: Color, DColor: Color, EColor: Color, jvmMarker: String = "") : this(AColor.value, BColor.value, CColor.value, DColor.value, EColor.value)

    companion object {
        fun default(): GradeColors {
            return GradeColors(
                Color(0xFF5ab52c),
                Color(0xff20abdc),
                Color(0xffdcc927),
                Color(0xffe76918),
                Color(0xffee2323),
            )
        }

        fun georgeMode(): GradeColors {
            return GradeColors(
                Color(0xff428820),
                Color(0xff1d92ba),
                Color(0xffdcc927),
                Color(0xffe76918),
                Color(0xffee2323),
            )
        }

        fun fionaMode(): GradeColors {
            return GradeColors(
                Color(0xffc178f5),
                Color(0xfff578ef),
                Color(0xffff7aa2),
                Color(0xffff7a7a),
                Color(0xfffa2f2f),
            )
        }
    }

    fun gradeColor(letter: String): Color? {
        return when(letter) {
            "A" -> AColor
            "B" -> BColor
            "C" -> CColor
            "D" -> DColor
            "E" -> EColor
            else -> null
        }
    }
}