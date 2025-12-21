package dev.nutrisport.shared.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.nutrisport.shared.BorderIdle
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.Orange
import dev.nutrisport.shared.TextThird
import dev.nutrisport.shared.domain.CartItemUiModel

@Composable
fun OrderProductItem(
    cartItem: CartItemUiModel,
    modifier: Modifier = Modifier,
    onAddToCartClick: (() -> Unit)? = null,
    navigateToDetail: ((String) -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        AsyncImage(
            modifier = Modifier
                .size(110.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(size = 12.dp))
                .border(
                    width = 1.dp,
                    color = BorderIdle,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .let { base ->
                    if (navigateToDetail != null) {
                        base.clickable { navigateToDetail(cartItem.productId) }
                    } else {
                        base
                    }
                },
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(cartItem.thumbnail)
                .crossfade(enable = true)
                .build(),
            contentDescription = "Product thumbnail image",
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .height(110.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = cartItem.productTitle,
                    fontSize = FontSize.EXTRA_REGULAR,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (cartItem.flavor != null || cartItem.weight != null) {
                        Text(
                            text = listOfNotNull(
                                cartItem.weight?.let { "${it}g" },
                                cartItem.flavor
                            ).joinToString(", "),
                            fontSize = FontSize.REGULAR,
                            color = TextThird
                        )
                    }

                    Text(
                        text = "x${cartItem.quantity}",
                        fontSize = FontSize.REGULAR,
                        color = TextThird
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = if (onAddToCartClick == null)
                    Arrangement.End
                else
                    Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${cartItem.price}",
                    fontSize = FontSize.EXTRA_REGULAR,
                )
                if (onAddToCartClick != null) {
                    AddToCartBoxButton(
                        onClick = onAddToCartClick
                    )
                }
            }
        }
    }
}

@Composable
fun AddToCartBoxButton(
    text: String = "加入購物車",
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = Orange,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = FontSize.SMALL,
            color = Orange
        )
    }
}