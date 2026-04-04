package com.scf.secondbloom.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String,
    val showInBottomBar: Boolean
) {
    data object Inspiration : Screen(
        route = "inspiration",
        title = "灵感空间",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        contentDescription = "灵感空间页面",
        showInBottomBar = true
    )

    data object InspirationDetail : Screen(
        route = "inspiration_detail/{itemId}",
        title = "灵感详情",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        contentDescription = "灵感详情页面",
        showInBottomBar = false
    ) {
        const val itemIdArg = "itemId"

        fun createRoute(itemId: String): String = "inspiration_detail/$itemId"
    }

    data object Wardrobe : Screen(
        route = "wardrobe",
        title = "数字衣橱",
        selectedIcon = Icons.Filled.Checkroom,
        unselectedIcon = Icons.Outlined.Checkroom,
        contentDescription = "数字衣橱页面",
        showInBottomBar = true
    )

    data object Planet : Screen(
        route = "planet",
        title = "可持续星球",
        selectedIcon = Icons.Filled.Public,
        unselectedIcon = Icons.Outlined.Public,
        contentDescription = "可持续星球页面",
        showInBottomBar = true
    )

    data object Profile : Screen(
        route = "profile",
        title = "我的主页",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "我的主页页面",
        showInBottomBar = true
    )

    data object Auth : Screen(
        route = "auth",
        title = "登录",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "登录页面",
        showInBottomBar = false
    )

    data object Account : Screen(
        route = "account",
        title = "账号设置",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        contentDescription = "账号设置页面",
        showInBottomBar = false
    )

    data object CameraRecognition : Screen(
        route = "camera_recognition",
        title = "上传识别",
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline,
        contentDescription = "上传识别页面",
        showInBottomBar = false
    )

    data object Plan : Screen(
        route = "plan",
        title = "制衣方案",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        contentDescription = "制衣方案页面",
        showInBottomBar = false
    )

    data object PreviewEditor : Screen(
        route = "preview_editor/{planId}",
        title = "真图编辑",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        contentDescription = "真图编辑页面",
        showInBottomBar = false
    ) {
        const val planIdArg = "planId"

        fun createRoute(planId: String): String = "preview_editor/$planId"
    }

    data object PreviewResult : Screen(
        route = "preview_result/{planId}",
        title = "最终效果图",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        contentDescription = "最终效果图页面",
        showInBottomBar = false
    ) {
        const val planIdArg = "planId"

        fun createRoute(planId: String): String = "preview_result/$planId"
    }

    companion object {
        fun topLevelItems(): List<Screen> = listOf(Inspiration, Wardrobe, Planet, Profile)

        fun flowItems(): List<Screen> = listOf(
            InspirationDetail,
            CameraRecognition,
            Plan,
            PreviewEditor,
            PreviewResult,
            Auth,
            Account
        )

        fun allItems(): List<Screen> = topLevelItems() + flowItems()
    }
}
