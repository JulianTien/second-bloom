package com.scf.secondbloom.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.scf.secondbloom.domain.model.AppLanguage
import com.scf.secondbloom.domain.model.DemoScenario
import com.scf.secondbloom.domain.model.PreviewEditFidelity
import com.scf.secondbloom.domain.model.PreviewEditLength
import com.scf.secondbloom.domain.model.PreviewEditNeckline
import com.scf.secondbloom.domain.model.PreviewEditSilhouette
import com.scf.secondbloom.domain.model.PreviewEditSleeve
import com.scf.secondbloom.domain.model.RemodelDifficulty
import com.scf.secondbloom.domain.model.RemodelIntent
import com.scf.secondbloom.navigation.Screen

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

fun localized(language: AppLanguage, english: String, chinese: String): String =
    if (language == AppLanguage.ENGLISH) english else chinese

fun RemodelIntent.localizedLabel(language: AppLanguage): String =
    localized(language, englishLabel, chineseLabel)

fun RemodelDifficulty.localizedLabel(language: AppLanguage): String =
    localized(language, englishLabel, chineseLabel)

fun PreviewEditSilhouette.localizedLabel(language: AppLanguage): String =
    localized(language, englishLabel, chineseLabel)

fun PreviewEditLength.localizedLabel(language: AppLanguage): String =
    localized(language, englishLabel, chineseLabel)

fun PreviewEditNeckline.localizedLabel(language: AppLanguage): String =
    localized(language, englishLabel, chineseLabel)

fun PreviewEditSleeve.localizedLabel(language: AppLanguage): String =
    localized(language, englishLabel, chineseLabel)

fun PreviewEditFidelity.localizedLabel(language: AppLanguage): String =
    localized(language, englishLabel, chineseLabel)

fun DemoScenario.localizedTitle(language: AppLanguage): String =
    localized(language, englishTitle, chineseTitle)

fun DemoScenario.localizedDescription(language: AppLanguage): String =
    localized(language, englishDescription, chineseDescription)

fun DemoScenario.localizedExpectedOutcome(language: AppLanguage): String =
    localized(language, englishExpectedOutcome, chineseExpectedOutcome)

fun Screen.localizedTitle(language: AppLanguage): String = when (this) {
    Screen.Inspiration -> localized(language, "Inspiration", "灵感空间")
    Screen.InspirationDetail -> localized(language, "Inspiration Detail", "灵感详情")
    Screen.Wardrobe -> localized(language, "Wardrobe", "数字衣橱")
    Screen.Planet -> localized(language, "Planet", "可持续星球")
    Screen.Profile -> localized(language, "Profile", "我的主页")
    Screen.Account -> localized(language, "Account", "账号中心")
    Screen.CameraRecognition -> localized(language, "Upload", "上传识别")
    Screen.Plan -> localized(language, "Plans", "制衣方案")
    Screen.PreviewEditor -> localized(language, "Editor", "真图编辑")
    Screen.PreviewResult -> localized(language, "Final Result", "最终效果图")
    Screen.Auth -> localized(language, "Sign in", "登录")
}

fun Screen.localizedContentDescription(language: AppLanguage): String = when (this) {
    Screen.Inspiration -> localized(language, "Inspiration screen", "灵感空间页面")
    Screen.InspirationDetail -> localized(language, "Inspiration detail screen", "灵感详情页面")
    Screen.Wardrobe -> localized(language, "Wardrobe screen", "数字衣橱页面")
    Screen.Planet -> localized(language, "Planet screen", "可持续星球页面")
    Screen.Profile -> localized(language, "Profile screen", "我的主页页面")
    Screen.Account -> localized(language, "Account screen", "账号中心页面")
    Screen.CameraRecognition -> localized(language, "Upload screen", "上传识别页面")
    Screen.Plan -> localized(language, "Plan screen", "制衣方案页面")
    Screen.PreviewEditor -> localized(language, "Image editing screen", "真图编辑页面")
    Screen.PreviewResult -> localized(language, "Final result screen", "最终效果图页面")
    Screen.Auth -> localized(language, "Authentication screen", "登录页面")
}
