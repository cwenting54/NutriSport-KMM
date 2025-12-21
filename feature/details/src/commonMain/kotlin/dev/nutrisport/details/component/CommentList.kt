package dev.nutrisport.details.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nutrisport.shared.Alpha
import dev.nutrisport.shared.BorderIdle
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.Orange
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.domain.CommentItem
import org.jetbrains.compose.resources.painterResource

@Composable
fun CommentsList(
    comments: List<CommentItem>,
    modifier: Modifier = Modifier,
    onLikeClick: (String) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        comments.forEach { review ->
            Spacer(modifier = Modifier.height(12.dp))
            CommentItem(
                comment = review,
                onLikeClick = onLikeClick,
            )
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentItem,
    modifier: Modifier = Modifier,
    onLikeClick: (String) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = maskEmail(comment.customerAccount),
                color = TextPrimary,
                fontSize = FontSize.REGULAR,
                modifier = Modifier.alpha(Alpha.HALF)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp).clickable { onLikeClick(comment.id) }
            ) {
                Icon(
                    painter = painterResource(Resources.Icon.ThumbUp),
                    contentDescription = "Like",
                    tint = IconPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = " (${comment.likes})",
                    color = IconPrimary,
                    fontSize = FontSize.REGULAR
                )
            }

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Stars row
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { idx ->
                    Icon(
                        painter = painterResource(Resources.Icon.FilledStar),
                        contentDescription = null,
                        tint = if (idx < comment.rating) Orange else Orange.copy(alpha = 0f),
                        modifier = Modifier.size(18.dp)
                    )
                    if (idx < 4) Spacer(modifier = Modifier.width(4.dp))
                }
            }

            Text(
                text = comment.formatedCreatedAt,
                color = TextPrimary,
                fontSize = FontSize.REGULAR
            )

        }
    }

    Text(
        text = comment.content,
        modifier = Modifier
            .fillMaxWidth(),
        color = TextPrimary,
        fontSize = FontSize.REGULAR,
        lineHeight = FontSize.REGULAR * 1.5,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(
        color = BorderIdle,
        thickness = 1.dp,
        modifier = Modifier.fillMaxWidth()
    )
}

fun maskEmail(email: String): String {
    val parts = email.split("@")
    if (parts.size != 2) return email

    val name = parts[0]

    return when {
        name.length <= 2 -> {
            name.first() + "*".repeat(5)
        }
        else -> {
            val first = name.first()
            val last = name.last()
            first + "*".repeat(5) + last
        }
    }
}
