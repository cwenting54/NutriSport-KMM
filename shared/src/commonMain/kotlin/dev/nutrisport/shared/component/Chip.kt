package dev.nutrisport.shared.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nutrisport.shared.BorderSecondary
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.Orange
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.TextSecondary
import dev.nutrisport.shared.TextWhite

@Composable
fun Chip(
    text: String,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .background(if(isSelected) Orange else Surface, RoundedCornerShape(14.dp))
            .border(BorderStroke(1.dp, BorderSecondary), shape = RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = FontSize.EXTRA_REGULAR,
            color = if(isSelected) TextWhite else TextSecondary,
        )
    }
}
