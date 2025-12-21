package dev.nutrisport.products_overview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nutrisport.products_overview.component.MainProductCard
import dev.nutrisport.shared.Alpha
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.TextSecondary
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.component.ProductFavorCard
import dev.nutrisport.shared.domain.Product
import dev.nutrisport.shared.domain.ProductType
import dev.nutrisport.shared.util.DisplayResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProductsOverviewScreen(
    navigateToDetails: (String) -> Unit,
    navigateToProductMore: (ProductType) -> Unit
) {
    val viewModel = koinViewModel<ProductsOverviewViewModel>()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val centeredIndex: Int? by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                kotlin.math.abs(itemCenter - viewportCenter)
            }?.index
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        products.DisplayResult(
            onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
            onSuccess = { productList ->
                AnimatedContent(
                    targetState = productList.distinctBy { it.id }
                ) { products ->
                    if (products.isNotEmpty()) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                state = listState,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                itemsIndexed(
                                    items = products.filter { it.isNew }
                                        .sortedBy { it.createdAt }
                                        .take(6),
                                    key = { index, item -> item.id }
                                ) { index, product ->
                                    val isLarge = index == centeredIndex
                                    val animatedScale by animateFloatAsState(
                                        targetValue = if (isLarge) 1f else 0.8f,
                                        animationSpec = tween(300)
                                    )
                                    MainProductCard(
                                        modifier = Modifier
                                            .scale(animatedScale)
                                            .height(250.dp)
                                            .fillParentMaxWidth(0.6f),
                                        product = product,
                                        isLarge = isLarge,
                                        onClick = {
                                            navigateToDetails(product.id)
                                        }
                                    )

                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            ProductDisplayRow(
                                title = "特惠商品",
                                filterType = ProductType.Discounted,
                                products = products,
                                navigateToDetails = navigateToDetails,
                                navigateToProductMore = navigateToProductMore
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ProductDisplayRow(
                                title = "熱銷商品",
                                filterType = ProductType.Popular,
                                products = products,
                                navigateToDetails = navigateToDetails,
                                navigateToProductMore = navigateToProductMore
                            )
                        }
                    } else {
                        InfoCard(
                            image = Resources.Image.Cat,
                            title = "Nothing here",
                            subtitle = "Empty product list."
                        )
                    }

                }

            },
            onError = { message ->
                InfoCard(
                    image = Resources.Image.Cat,
                    title = "Oops!",
                    subtitle = message
                )
            }
        )
    }
}

@Composable
fun ProductDisplayRow(
    title: String,
    products: List<Product>,
    filterType: ProductType,
    navigateToDetails: (String) -> Unit,
    navigateToProductMore: (ProductType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.alpha(Alpha.HALF),
            text = title,
            fontSize = FontSize.EXTRA_REGULAR,
            color = TextPrimary
        )
        Text(
            text = "查看更多",
            color = TextSecondary,
            fontSize = FontSize.EXTRA_REGULAR,
            modifier = Modifier.clickable{
                navigateToProductMore(filterType)
            }
        )
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        items(
            items = products.filter { if (filterType == ProductType.Discounted) it.isDiscounted else it.isPopular }
                .sortedBy { it.createdAt },
            key = { item -> item.id }
        ) { product ->
            ProductFavorCard(
                modifier = Modifier
                    .width(163.dp),
                product = product,
                onNavigateToDetails = { navigateToDetails(product.id) }
            )
        }
    }
}