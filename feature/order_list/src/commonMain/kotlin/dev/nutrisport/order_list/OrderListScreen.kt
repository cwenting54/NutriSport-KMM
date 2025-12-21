package dev.nutrisport.order_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nutrisport.shared.component.OrderProductItem
import dev.nutrisport.shared.BebasNeueFont
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.SurfaceLighter
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.TextRed
import dev.nutrisport.shared.TextThird
import dev.nutrisport.shared.White
import dev.nutrisport.shared.component.AnimatedSearchTopBar
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.domain.CartItemUiModel
import dev.nutrisport.shared.domain.OrderUiModel
import dev.nutrisport.shared.domain.ShipStatus
import dev.nutrisport.shared.util.DisplayResult
import dev.nutrisport.shared.util.toFormattedString
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OrderListScreen(
    navigateToOderDetail: (String) -> Unit,
    navigateBack: () -> Unit,
    viewModel: OrderViewModel = koinViewModel()
) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val orders by viewModel.orders.collectAsStateWithLifecycle()


    Scaffold(
        containerColor = SurfaceLighter,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            snackbarHostState.currentSnackbarData?.dismiss()
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AnimatedSearchTopBar(
                title = "訂單列表",
                searchQuery = "",
                searchBarVisible = false,
                onSearchQueryChange = {},
                onSearchVisibilityChange = { },
                onNavigateBack = navigateBack
            )
        }
    ) { innerPadding ->
        orders.DisplayResult(
            onLoading = {
                LoadingCard(modifier = Modifier.fillMaxSize())
            },
            onSuccess = { orderList ->
                if (orderList.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = innerPadding.calculateBottomPadding()
                            ),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(
                            items = orderList
                                .sortedByDescending { it.createAt },
                            key = { order -> order.id }
                        ) { order ->
                            OrderCard(
                                order = order,
                                onShowOrderDetail = navigateToOderDetail,
                                onAddToCartClick = { item ->
                                    viewModel.addItemToCart(
                                        item = item,
                                        onSuccess = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(message = "成功加入購物車！")
                                            }
                                        },
                                        onError = { message ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(message)
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                } else {
                    InfoCard(
                        image = Resources.Image.Cat,
                        title = "你尚未有任何訂單紀錄",
                        subtitle = ""
                    )
                }

            },
            onError = { message ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    InfoCard(
                        title = "Oops!",
                        subtitle = message,
                        image = Resources.Image.Cat
                    )
                }
            }
        )

    }
}

@Composable
fun OrderCard(
    order: OrderUiModel,
    modifier: Modifier = Modifier,
    onShowOrderDetail: (String) -> Unit = {},
    onAddToCartClick: (CartItemUiModel) -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable {
                onShowOrderDetail(order.id)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            // 上方：訂購日期 & 訂單編號
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = order.createAt.toFormattedString("yyyy.MM.dd")+" 訂購",
                    fontSize = FontSize.EXTRA_REGULAR,
                    fontFamily = BebasNeueFont(),
                )
                Text(
                    text = "訂單編號：${order.id}",
                    fontSize = FontSize.SMALL,
                    color = TextThird,
                    textAlign = TextAlign.End,
                    modifier = Modifier.widthIn(max = 200.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // 商品主體區 (圖片 + 文案)
            OrderProductSection(
                order = order,
                onAddToCartClick = onAddToCartClick
            )

            Spacer(Modifier.height(8.dp))

            // 底部：狀態 / 配送資訊 & 價格 / 詳情
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = order.completedAt?.let {
                        it.toFormattedString("yyyy.MM.dd") + " 訂單完成"
                    } ?: order.shipStatus.title,
                    fontSize = FontSize.EXTRA_REGULAR,
                    fontFamily = BebasNeueFont(),
                    color = if (order.shipStatus == ShipStatus.Completed)
                        TextPrimary
                    else
                        TextRed
                )


                Row(
                    modifier = Modifier.clickable {
                        onShowOrderDetail(order.id)
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "查看詳情",
                        fontSize = 14.sp,
                        color = Color(0xFFF57C00) // 橘色
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "›",
                        fontSize = 18.sp,
                        color = Color(0xFFF57C00)
                    )
                }


            }
        }
    }
}

@Composable
fun OrderProductSection(
    order: OrderUiModel,
    onAddToCartClick: (CartItemUiModel) -> Unit = {}
) {
    // 是否顯示全部商品
    var showAllProducts by rememberSaveable(order.id) { mutableStateOf(false) }

    // 決定這次要畫出來的商品清單
    val productsToShow = if (showAllProducts) {
        order.items
    } else {
        order.items.take(3)
    }

    // 商品主體區 (圖片 + 文案)
    productsToShow.forEach { product ->
        OrderProductItem(
            cartItem = product,
            onAddToCartClick = { onAddToCartClick(product) }
        )
        Spacer(Modifier.height(8.dp))
    }

    Spacer(Modifier.height(8.dp))

    // 只有在「尚未展開」且 商品數量大於 3 筆 的情況下，才顯示「檢視其他商品」
    if (!showAllProducts && order.items.size > 3) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showAllProducts = true
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "檢視其他商品",
                fontSize = FontSize.REGULAR,
                color = Color(0xFF777777)
            )
            Icon(
                painter = painterResource(Resources.Icon.DownArrow),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color(0xFF777777)
            )
        }
    }
}
