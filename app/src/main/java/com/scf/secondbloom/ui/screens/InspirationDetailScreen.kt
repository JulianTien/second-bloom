package com.scf.secondbloom.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.scf.secondbloom.domain.model.RemodelUiState
import com.scf.secondbloom.ui.i18n.LocalAppLanguage
import com.scf.secondbloom.ui.i18n.localized
import com.scf.secondbloom.ui.model.InspirationCardUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InspirationDetailScreen(
    state: RemodelUiState,
    item: InspirationCardUiModel?,
    onBack: () -> Unit,
    onToggleLike: (String, Int) -> Unit,
    onToggleBookmark: (String, Int) -> Unit,
    onAddComment: (String, Int, String) -> Unit,
    onOpenPublishedResult: (String) -> Unit,
    onOpenRemodelFlow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    var commentDraft by rememberSaveable(item?.id) { mutableStateOf("") }
    val comments = item?.let { state.inspirationEngagementFor(it.id)?.comments }.orEmpty()

    BackHandler(onBack = onBack)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = localized(language, "Back", "返回")
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = localized(language, "Inspiration detail", "灵感详情"),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = localized(language, "Browse the full makeover story, then like, save, or join the conversation.", "完整查看改造灵感，并进行点赞、收藏和评论互动。"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (item == null) {
            item {
                DetailCard(
                    title = localized(language, "Post unavailable", "当前帖子不可用"),
                    description = localized(language, "This inspiration item could not be found. Return to the feed and open it again.", "当前灵感内容未找到，请返回灵感流后重新进入。")
                ) {
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text(localized(language, "Back to feed", "返回灵感流"))
                    }
                }
            }
            return@LazyColumn
        }

        item {
            DetailCard(
                title = item.title,
                description = item.description
            ) {
                Text(
                    text = item.authorName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            DetailCard(
                title = localized(language, "Before / After", "改造前后"),
                description = localized(language, "Scroll through the original look and the makeover result together.", "把原始衣物与改造结果放在一起查看。")
            ) {
                InspirationDetailImage(label = localized(language, "Before", "改造前"), imageUrl = item.beforeImageUrl, fallbackColor = item.beforeColor)
                InspirationDetailImage(label = localized(language, "After", "改造后"), imageUrl = item.afterImageUrl, fallbackColor = item.afterColor)
            }
        }

        item {
            DetailCard(
                title = localized(language, "Interaction", "互动"),
                description = localized(language, "Like this idea, save it for later, or leave a comment for the maker.", "可以点赞这条灵感、收藏备用，或者直接给作者留言。")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onToggleLike(item.id, item.likeCount) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (item.likedByViewer) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = " ${localized(language, "Like", "点赞")} ${formatCount(item.likeCount)}",
                            maxLines = 1
                        )
                    }
                    Button(
                        onClick = { onToggleBookmark(item.id, item.likeCount) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (item.bookmarkedByViewer) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (item.bookmarkedByViewer) {
                                localized(language, "Saved", "已收藏")
                            } else {
                                localized(language, "Save", "收藏")
                            }
                        )
                    }
                }

                if (item.publishedPlanId != null) {
                    Button(
                        onClick = { onOpenPublishedResult(item.publishedPlanId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(localized(language, "Open final result", "打开最终效果图"))
                    }
                } else {
                    Button(
                        onClick = onOpenRemodelFlow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(localized(language, "Try this remake", "尝试同款改造"))
                    }
                }
            }
        }

        item {
            DetailCard(
                title = localized(language, "Comments", "评论区"),
                description = localized(language, "Share feedback, ask about the remake, or leave styling ideas for this post.", "可以交流改造想法、提问细节，或者留下你的搭配建议。")
            ) {
                OutlinedTextField(
                    value = commentDraft,
                    onValueChange = { commentDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text(localized(language, "Write a comment", "写下你的评论")) }
                )
                Button(
                    onClick = {
                        onAddComment(item.id, item.likeCount, commentDraft)
                        commentDraft = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = commentDraft.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ModeComment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(" ${localized(language, "Post comment", "发布评论")}")
                }

                if (comments.isEmpty()) {
                    Text(
                        text = localized(language, "No comments yet. Be the first one to leave feedback.", "还没有评论，成为第一个留言的人吧。"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    comments.forEach { comment ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = comment.authorName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = comment.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun InspirationDetailImage(
    label: String,
    imageUrl: String?,
    fallbackColor: androidx.compose.ui.graphics.Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            if (imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(fallbackColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }
        }
    }
}

private fun formatCount(count: Int): String =
    when {
        count >= 1000 -> String.format("%.1fk", count / 1000f)
        else -> count.toString()
    }
