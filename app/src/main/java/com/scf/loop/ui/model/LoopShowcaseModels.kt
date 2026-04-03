package com.scf.loop.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class InspirationCardUiModel(
    val id: String,
    val title: String,
    val tags: List<String>,
    val likes: String,
    val height: Dp,
    val beforeColor: Color,
    val afterColor: Color
)

data class WardrobeItemUiModel(
    val id: String,
    val name: String,
    val category: String,
    val tags: List<String>,
    val status: String,
    val coverColor: Color
)

enum class PlanetStatKind {
    Water,
    Carbon
}

data class PlanetStatUiModel(
    val kind: PlanetStatKind,
    val value: String,
    val label: String,
    val tint: Color,
    val background: Color
)

data class ProfileWorkUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val gradientStart: Color,
    val gradientEnd: Color
)

object LoopShowcaseContent {
    val inspirationCards = listOf(
        InspirationCardUiModel(
            id = "shirt-halter",
            title = "旧男士衬衫 -> 夏日挂脖上衣",
            tags = listOf("#日常改造", "#零废弃"),
            likes = "1.2k",
            height = 238.dp,
            beforeColor = Color(0xFFD8E5F4),
            afterColor = Color(0xFF8FB5E8)
        ),
        InspirationCardUiModel(
            id = "denim-skirt",
            title = "闲置牛仔裤 -> Y2K 百褶短裙",
            tags = listOf("#Y2K", "#进阶"),
            likes = "892",
            height = 208.dp,
            beforeColor = Color(0xFFCFE6F7),
            afterColor = Color(0xFF72A9DF)
        ),
        InspirationCardUiModel(
            id = "blazer-vest",
            title = "过时西装外套 -> 解构风收腰马甲",
            tags = listOf("#高级感", "#解构"),
            likes = "2.4k",
            height = 258.dp,
            beforeColor = Color(0xFFD6D8DD),
            afterColor = Color(0xFF818792)
        ),
        InspirationCardUiModel(
            id = "knit-tote",
            title = "起球针织衫 -> 拼色环保托特包",
            tags = listOf("#实用", "#10分钟"),
            likes = "456",
            height = 182.dp,
            beforeColor = Color(0xFFF5D7BF),
            afterColor = Color(0xFFE8A96A)
        ),
        InspirationCardUiModel(
            id = "tee-crop",
            title = "超大号 T 恤 -> 抽绳短上衣",
            tags = listOf("#基础款", "#免缝纫"),
            likes = "3.1k",
            height = 222.dp,
            beforeColor = Color(0xFFD7F0DB),
            afterColor = Color(0xFF7CCB93)
        ),
        InspirationCardUiModel(
            id = "dress-set",
            title = "旧碎花长裙 -> 法式两件套",
            tags = listOf("#浪漫", "#夏日"),
            likes = "1.8k",
            height = 240.dp,
            beforeColor = Color(0xFFF3D8E5),
            afterColor = Color(0xFFE5A1BF)
        )
    )

    val wardrobeCategories = listOf("全部", "上装", "下装", "裙装", "配饰")

    val wardrobeItems = listOf(
        WardrobeItemUiModel(
            id = "striped-knit",
            name = "条纹针织衫",
            category = "上装",
            tags = listOf("纯棉", "轻微起球"),
            status = "待改造",
            coverColor = Color(0xFFDDE4FF)
        ),
        WardrobeItemUiModel(
            id = "straight-jeans",
            name = "直筒牛仔裤",
            category = "下装",
            tags = listOf("丹宁", "膝盖磨损"),
            status = "寻找灵感",
            coverColor = Color(0xFFD4E6FB)
        ),
        WardrobeItemUiModel(
            id = "white-tee",
            name = "白色基础 T 恤",
            category = "上装",
            tags = listOf("莫代尔", "领口发黄"),
            status = "改造中",
            coverColor = Color(0xFFF0F1F3)
        ),
        WardrobeItemUiModel(
            id = "floral-dress",
            name = "碎花雪纺裙",
            category = "裙装",
            tags = listOf("聚酯纤维", "过时款式"),
            status = "待改造",
            coverColor = Color(0xFFF7D9E5)
        )
    )

    val planetStats = listOf(
        PlanetStatUiModel(
            kind = PlanetStatKind.Water,
            value = "4,500 L",
            label = "节约水资源",
            tint = Color(0xFF3A88C9),
            background = Color(0xFFE7F2FB)
        ),
        PlanetStatUiModel(
            kind = PlanetStatKind.Carbon,
            value = "12.5 kg",
            label = "减少碳排放",
            tint = Color(0xFF5D6C73),
            background = Color(0xFFEDEFF1)
        )
    )

    val fallbackProfileWorks = listOf(
        ProfileWorkUiModel(
            id = "work-1",
            title = "抽绳短上衣",
            subtitle = "日常焕新",
            gradientStart = Color(0xFFE8DDF8),
            gradientEnd = Color(0xFFBCAAEF)
        ),
        ProfileWorkUiModel(
            id = "work-2",
            title = "丹宁百褶裙",
            subtitle = "进阶改造",
            gradientStart = Color(0xFFD9F2E0),
            gradientEnd = Color(0xFF8ECDA1)
        ),
        ProfileWorkUiModel(
            id = "work-3",
            title = "法式两件套",
            subtitle = "夏日灵感",
            gradientStart = Color(0xFFFCE1D7),
            gradientEnd = Color(0xFFF1A681)
        )
    )

    val savedCollection = listOf(
        ProfileWorkUiModel(
            id = "saved-1",
            title = "环保托特包",
            subtitle = "收藏灵感",
            gradientStart = Color(0xFFF8E9D7),
            gradientEnd = Color(0xFFE9BF7A)
        ),
        ProfileWorkUiModel(
            id = "saved-2",
            title = "解构收腰马甲",
            subtitle = "收藏灵感",
            gradientStart = Color(0xFFE2E5EB),
            gradientEnd = Color(0xFFADB3BF)
        ),
        ProfileWorkUiModel(
            id = "saved-3",
            title = "拼色针织围巾",
            subtitle = "收藏灵感",
            gradientStart = Color(0xFFDDECF7),
            gradientEnd = Color(0xFF9AC4E2)
        )
    )
}
