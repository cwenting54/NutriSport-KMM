package dev.nutrisport.shared.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.domain.CartItemUiModel

@Composable
fun ProductSummaryCard(
    order: List<CartItemUiModel>,
    title: String,
    modifier: Modifier = Modifier,
    onAddToCartClick: ((CartItemUiModel) -> Unit)? = null,
    navigateToDetail: ((String) -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = title,
                fontSize = FontSize.EXTRA_REGULAR,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            // 商品主體區 (圖片 + 文案)
            order.forEach { order ->
                OrderProductItem(
                    cartItem = order,
                    onAddToCartClick = onAddToCartClick?.let { click ->
                        { click(order) }
                    },
                    navigateToDetail = navigateToDetail
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}