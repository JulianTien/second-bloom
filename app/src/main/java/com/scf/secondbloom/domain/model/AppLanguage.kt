package com.scf.secondbloom.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage(
    val wireValue: String,
    val englishLabel: String,
    val chineseLabel: String,
) {
    ENGLISH(
        wireValue = "en",
        englishLabel = "English",
        chineseLabel = "英文",
    ),
    CHINESE(
        wireValue = "zh",
        englishLabel = "Chinese",
        chineseLabel = "中文",
    );

    fun displayLabel(displayLanguage: AppLanguage): String =
        if (displayLanguage == ENGLISH) englishLabel else chineseLabel

    companion object {
        fun fromWire(value: String?): AppLanguage = when ((value ?: "").trim().lowercase()) {
            "zh", "zh-cn", "zh_hans" -> CHINESE
            else -> ENGLISH
        }
    }
}

