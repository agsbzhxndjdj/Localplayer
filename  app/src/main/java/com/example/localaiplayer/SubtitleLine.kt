package com.example.localaiplayer

enum class Gender {
    UNKNOWN,
    MALE,
    FEMALE
}

data class SubtitleLine(
    val id: Int,
    val startMs: Long,
    val endMs: Long,
    var text: String,
    var sourceText: String = "",
    var language: String = "und",
    var speakerGender: Gender = Gender.UNKNOWN,
    var editedByUser: Boolean = false
)
