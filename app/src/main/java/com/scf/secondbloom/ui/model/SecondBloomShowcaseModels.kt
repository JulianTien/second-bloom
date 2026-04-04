package com.scf.secondbloom.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.scf.secondbloom.domain.model.AppLanguage
import com.scf.secondbloom.domain.model.InspirationEngagementRecord
import com.scf.secondbloom.domain.model.PublishedRemodelRecord

data class InspirationCardUiModel(
    val id: String,
    val title: String,
    val description: String,
    val authorName: String,
    val tags: List<String>,
    val likeCount: Int,
    val commentsCount: Int,
    val height: Dp,
    val beforeColor: Color,
    val afterColor: Color,
    val beforeImageUrl: String? = null,
    val afterImageUrl: String? = null,
    val likedByViewer: Boolean = false,
    val bookmarkedByViewer: Boolean = false,
    val publishedByViewer: Boolean = false,
    val publishedPlanId: String? = null,
    val showFavoriteIcon: Boolean = true,
    val actionLabel: String? = null
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

object SecondBloomShowcaseContent {
    fun inspirationCards(
        language: AppLanguage,
        engagements: List<InspirationEngagementRecord> = emptyList()
    ) = mergeEngagement(
        cards = listOf(
        InspirationCardUiModel(
            id = "shirt-halter",
            title = localized(language, "Old men's shirt -> summer halter top", "旧男士衬衫 -> 夏日挂脖上衣"),
            description = localized(language, "Turn a structured oversized shirt into a breezy halter silhouette with a cleaner summer neckline.", "把版型偏正式的宽松衬衫改成更轻盈的挂脖廓形，保留夏天更好穿的清爽领口。"),
            authorName = localized(language, "Second Bloom Studio", "Second Bloom 编辑部"),
            tags = listOf(
                localized(language, "#Daily refresh", "#日常改造"),
                localized(language, "#Zero waste", "#零废弃")
            ),
            likeCount = 1200,
            commentsCount = 18,
            height = 238.dp,
            beforeColor = Color(0xFFD8E5F4),
            afterColor = Color(0xFF8FB5E8)
        ),
        InspirationCardUiModel(
            id = "denim-skirt",
            title = localized(language, "Unused jeans -> Y2K pleated mini skirt", "闲置牛仔裤 -> Y2K 百褶短裙"),
            description = localized(language, "Reuse the strongest denim panels to build a playful pleated mini with a sharper Y2K proportion.", "利用牛仔裤保存最好的布片，重组成更利落的 Y2K 百褶短裙比例。"),
            authorName = localized(language, "Second Bloom Studio", "Second Bloom 编辑部"),
            tags = listOf("#Y2K", localized(language, "#Advanced", "#进阶")),
            likeCount = 892,
            commentsCount = 9,
            height = 208.dp,
            beforeColor = Color(0xFFCFE6F7),
            afterColor = Color(0xFF72A9DF)
        ),
        InspirationCardUiModel(
            id = "blazer-vest",
            title = localized(language, "Outdated blazer -> deconstructed fitted vest", "过时西装外套 -> 解构风收腰马甲"),
            description = localized(language, "Keep the tailored shoulder mood, but cut away bulk so the final shape feels sharper and more fashion-forward.", "保留西装肩线的利落感，同时去掉多余体量，让成衣更贴身也更有设计感。"),
            authorName = localized(language, "Second Bloom Studio", "Second Bloom 编辑部"),
            tags = listOf(
                localized(language, "#Elevated", "#高级感"),
                localized(language, "#Deconstructed", "#解构")
            ),
            likeCount = 2400,
            commentsCount = 27,
            height = 258.dp,
            beforeColor = Color(0xFFD6D8DD),
            afterColor = Color(0xFF818792)
        ),
        InspirationCardUiModel(
            id = "knit-tote",
            title = localized(language, "Pilled knitwear -> color-block eco tote", "起球针织衫 -> 拼色环保托特包"),
            description = localized(language, "Skip garment repair entirely and redirect soft worn knit panels into a textured everyday tote.", "不再勉强修复起球针织衫，直接把柔软布片改造成更有质感的日常托特包。"),
            authorName = localized(language, "Second Bloom Studio", "Second Bloom 编辑部"),
            tags = listOf(
                localized(language, "#Practical", "#实用"),
                localized(language, "#10 mins", "#10分钟")
            ),
            likeCount = 456,
            commentsCount = 6,
            height = 182.dp,
            beforeColor = Color(0xFFF5D7BF),
            afterColor = Color(0xFFE8A96A)
        ),
        InspirationCardUiModel(
            id = "tee-crop",
            title = localized(language, "Oversized tee -> drawstring crop top", "超大号 T 恤 -> 抽绳短上衣"),
            description = localized(language, "A fast remake that keeps the original jersey softness while using drawstrings to create a more styled waistline.", "这是一种快速改法，保留 T 恤本身的柔软感，同时用抽绳做出更有造型的腰部线条。"),
            authorName = localized(language, "Second Bloom Studio", "Second Bloom 编辑部"),
            tags = listOf(
                localized(language, "#Basics", "#基础款"),
                localized(language, "#No sewing", "#免缝纫")
            ),
            likeCount = 3100,
            commentsCount = 42,
            height = 222.dp,
            beforeColor = Color(0xFFD7F0DB),
            afterColor = Color(0xFF7CCB93)
        ),
        InspirationCardUiModel(
            id = "dress-set",
            title = localized(language, "Old floral dress -> French two-piece set", "旧碎花长裙 -> 法式两件套"),
            description = localized(language, "Split a full-length floral dress into an easier, lighter matching set that still keeps the romantic print story.", "把一条完整长裙拆成更轻盈的法式两件套，同时保留碎花图案的浪漫气质。"),
            authorName = localized(language, "Second Bloom Studio", "Second Bloom 编辑部"),
            tags = listOf(
                localized(language, "#Romantic", "#浪漫"),
                localized(language, "#Summer", "#夏日")
            ),
            likeCount = 1800,
            commentsCount = 21,
            height = 240.dp,
            beforeColor = Color(0xFFF3D8E5),
            afterColor = Color(0xFFE5A1BF)
        )
    ),
        engagements = engagements
    )

    fun wardrobeCategories(language: AppLanguage) = listOf(
        localized(language, "All", "全部"),
        localized(language, "Tops", "上装"),
        localized(language, "Bottoms", "下装"),
        localized(language, "Dresses", "裙装"),
        localized(language, "Accessories", "配饰")
    )

    fun wardrobeItems(language: AppLanguage) = listOf(
        WardrobeItemUiModel(
            id = "striped-knit",
            name = localized(language, "Striped knit top", "条纹针织衫"),
            category = localized(language, "Tops", "上装"),
            tags = listOf(localized(language, "Cotton", "纯棉"), localized(language, "Light pilling", "轻微起球")),
            status = localized(language, "Awaiting remodel", "待改造"),
            coverColor = Color(0xFFDDE4FF)
        ),
        WardrobeItemUiModel(
            id = "straight-jeans",
            name = localized(language, "Straight-leg jeans", "直筒牛仔裤"),
            category = localized(language, "Bottoms", "下装"),
            tags = listOf(localized(language, "Denim", "丹宁"), localized(language, "Knee wear", "膝盖磨损")),
            status = localized(language, "Looking for ideas", "寻找灵感"),
            coverColor = Color(0xFFD4E6FB)
        ),
        WardrobeItemUiModel(
            id = "white-tee",
            name = localized(language, "Basic white tee", "白色基础 T 恤"),
            category = localized(language, "Tops", "上装"),
            tags = listOf(localized(language, "Modal", "莫代尔"), localized(language, "Yellowed neckline", "领口发黄")),
            status = localized(language, "In progress", "改造中"),
            coverColor = Color(0xFFF0F1F3)
        ),
        WardrobeItemUiModel(
            id = "floral-dress",
            name = localized(language, "Floral chiffon dress", "碎花雪纺裙"),
            category = localized(language, "Dresses", "裙装"),
            tags = listOf(localized(language, "Polyester", "聚酯纤维"), localized(language, "Outdated silhouette", "过时款式")),
            status = localized(language, "Awaiting remodel", "待改造"),
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

    fun fallbackProfileWorks(language: AppLanguage) = listOf(
        ProfileWorkUiModel(
            id = "work-1",
            title = localized(language, "Drawstring crop top", "抽绳短上衣"),
            subtitle = localized(language, "Daily refresh", "日常焕新"),
            gradientStart = Color(0xFFE8DDF8),
            gradientEnd = Color(0xFFBCAAEF)
        ),
        ProfileWorkUiModel(
            id = "work-2",
            title = localized(language, "Denim pleated skirt", "丹宁百褶裙"),
            subtitle = localized(language, "Advanced remake", "进阶改造"),
            gradientStart = Color(0xFFD9F2E0),
            gradientEnd = Color(0xFF8ECDA1)
        ),
        ProfileWorkUiModel(
            id = "work-3",
            title = localized(language, "French two-piece set", "法式两件套"),
            subtitle = localized(language, "Summer mood", "夏日灵感"),
            gradientStart = Color(0xFFFCE1D7),
            gradientEnd = Color(0xFFF1A681)
        )
    )

    fun savedCollection(language: AppLanguage) = listOf(
        ProfileWorkUiModel(
            id = "saved-1",
            title = localized(language, "Eco tote bag", "环保托特包"),
            subtitle = localized(language, "Saved idea", "收藏灵感"),
            gradientStart = Color(0xFFF8E9D7),
            gradientEnd = Color(0xFFE9BF7A)
        ),
        ProfileWorkUiModel(
            id = "saved-2",
            title = localized(language, "Deconstructed fitted vest", "解构收腰马甲"),
            subtitle = localized(language, "Saved idea", "收藏灵感"),
            gradientStart = Color(0xFFE2E5EB),
            gradientEnd = Color(0xFFADB3BF)
        ),
        ProfileWorkUiModel(
            id = "saved-3",
            title = localized(language, "Color-block knit scarf", "拼色针织围巾"),
            subtitle = localized(language, "Saved idea", "收藏灵感"),
            gradientStart = Color(0xFFDDECF7),
            gradientEnd = Color(0xFF9AC4E2)
        )
    )

    fun publishedInspirationCards(
        records: List<PublishedRemodelRecord>,
        language: AppLanguage,
        engagements: List<InspirationEngagementRecord> = emptyList()
    ): List<InspirationCardUiModel> = mergeEngagement(records.mapIndexed { index, record ->
        InspirationCardUiModel(
            id = "published-${record.recordId}",
            title = record.selectedPlan.title,
            description = record.selectedPlan.summary,
            authorName = localized(language, "You", "你"),
            tags = listOf(
                localized(language, "#Published by you", "#由你发布"),
                localized(
                    language,
                    when (record.intent) {
                        com.scf.secondbloom.domain.model.RemodelIntent.DAILY -> "#Daily refresh"
                        com.scf.secondbloom.domain.model.RemodelIntent.OCCASION -> "#Occasion upgrade"
                        com.scf.secondbloom.domain.model.RemodelIntent.DIY -> "#Creative DIY"
                        com.scf.secondbloom.domain.model.RemodelIntent.SIZE_ADJUSTMENT -> "#Size adjustment"
                    },
                    when (record.intent) {
                        com.scf.secondbloom.domain.model.RemodelIntent.DAILY -> "#日常改造"
                        com.scf.secondbloom.domain.model.RemodelIntent.OCCASION -> "#场合升级"
                        com.scf.secondbloom.domain.model.RemodelIntent.DIY -> "#创意DIY"
                        com.scf.secondbloom.domain.model.RemodelIntent.SIZE_ADJUSTMENT -> "#尺码调整"
                    }
                )
            ),
            likeCount = 0,
            commentsCount = 0,
            height = if (index % 2 == 0) 248.dp else 222.dp,
            beforeColor = Color(0xFFD7E3F4),
            afterColor = Color(0xFF9BC8AE),
            beforeImageUrl = record.previewResult.beforeImage?.url,
            afterImageUrl = record.previewResult.afterImage?.url,
            publishedByViewer = true,
            publishedPlanId = record.selectedPlan.planId,
            showFavoriteIcon = false,
            actionLabel = localized(language, "View your published look", "查看你发布的作品")
        )
    }, engagements)

    fun allInspirationCards(
        records: List<PublishedRemodelRecord>,
        engagements: List<InspirationEngagementRecord>,
        language: AppLanguage
    ): List<InspirationCardUiModel> =
        publishedInspirationCards(records, language, engagements) + inspirationCards(language, engagements)

    fun inspirationCardById(
        itemId: String,
        records: List<PublishedRemodelRecord>,
        engagements: List<InspirationEngagementRecord>,
        language: AppLanguage
    ): InspirationCardUiModel? =
        allInspirationCards(records, engagements, language).firstOrNull { it.id == itemId }

    private fun mergeEngagement(
        cards: List<InspirationCardUiModel>,
        engagements: List<InspirationEngagementRecord>
    ): List<InspirationCardUiModel> {
        val byId = engagements.associateBy { it.itemId }
        return cards.map { card ->
            val engagement = byId[card.id]
            if (engagement == null) {
                card
            } else {
                card.copy(
                    likeCount = engagement.likeCount,
                    commentsCount = engagement.comments.size,
                    likedByViewer = engagement.liked,
                    bookmarkedByViewer = engagement.bookmarked
                )
            }
        }
    }
}

private fun localized(language: AppLanguage, english: String, chinese: String): String =
    if (language == AppLanguage.ENGLISH) english else chinese
