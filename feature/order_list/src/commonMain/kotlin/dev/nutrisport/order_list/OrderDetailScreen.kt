package dev.nutrisport.order_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nutrisport.shared.component.OrderProductItem
import dev.nutrisport.shared.BebasNeueFont
import dev.nutrisport.shared.BorderIdle
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.GrayDarker
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.SurfaceLighter
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.TextSecondary
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.component.PrimaryButton
import dev.nutrisport.shared.component.ProductSummaryCard
import dev.nutrisport.shared.component.dialog.AlertDialog
import dev.nutrisport.shared.domain.CartItemUiModel
import dev.nutrisport.shared.domain.OrderUiModel
import dev.nutrisport.shared.domain.ShipStatus
import dev.nutrisport.shared.util.DisplayResult
import dev.nutrisport.shared.util.rememberClipboardManager
import dev.nutrisport.shared.util.toFormattedString
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    modifier: Modifier = Modifier,
    navigateToDetail: (String) -> Unit,
    navigateToRating: (String) -> Unit,
    navigateBack: () -> Unit,
    viewModel: OrderViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteOrderDialog by remember { mutableStateOf(false) }
    val order by viewModel.selectedOrder.collectAsStateWithLifecycle()


    AnimatedVisibility(
        visible = showDeleteOrderDialog
    ) {
        AlertDialog(
            title = "刪除訂單紀錄",
            message = "您確定要刪除此筆訂單紀錄嗎？",
            onDismiss = {
                showDeleteOrderDialog = false
            },
            onConfirmClick = {
                showDeleteOrderDialog = false
                viewModel.deleteOrder(
                    onSuccess = {
                        navigateBack()
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
            TopAppBar(
                title = {
                    Text(
                        text = "訂單詳情",
                        fontFamily = BebasNeueFont(),
                        fontSize = FontSize.EXTRA_MEDIUM,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = "Back arrow icon",
                            tint = IconPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    scrolledContainerColor = Surface,
                    navigationIconContentColor = IconPrimary,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = IconPrimary
                )
            )
        },
    ) { innerPadding ->
        order.DisplayResult(
            onLoading = {
                LoadingCard(modifier = Modifier.fillMaxSize())
            },
            onSuccess = { order ->
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val orderStatus = when(order.shipStatus) {
                        ShipStatus.Shipping -> "運送中"
                        ShipStatus.Pending -> "待出貨"
                        ShipStatus.Delivered -> "已送達"
                        ShipStatus.Completed -> "您的訂單已完成"
                        else -> "待出貨"
                    }
                    Text(
                        text = orderStatus,
                        color = TextSecondary,
                        fontSize = FontSize.EXTRA_MEDIUM,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp)
                    )

                    // 付款 / 收件資訊卡
                    PaymentAndReceiverCard(order = order)


                    // 商品總覽卡
                    ProductSummaryCard(
                        title = "商品總覽",
                        order = order.items,
                        navigateToDetail = navigateToDetail,
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

                    // 訂單編號與時間卡
                    OrderInfoCard(order = order)
                    if (order.shipStatus == ShipStatus.Completed) {
                        Column {
                            PrimaryButton(
                                text = "評價",
                                enabled = true,
                                onClick = {
                                    navigateToRating(order.id)
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            PrimaryButton(
                                text = "刪除訂單紀錄",
                                secondary = true,
                                enabled = true,
                                onClick = {
                                    showDeleteOrderDialog = true
                                }
                            )
                        }
                    }
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
private fun PaymentAndReceiverCard(
    order: OrderUiModel,
    modifier: Modifier = Modifier
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
            // 付款資訊
            Text(
                text = "付款資訊",
                fontSize = FontSize.EXTRA_REGULAR,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "付款總金額", fontSize = FontSize.REGULAR)
                    Spacer(Modifier.height(8.dp))
                    Text(text = "付款方式", fontSize = FontSize.REGULAR)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$ ${order.totalAmount}",
                        fontSize = FontSize.REGULAR,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = order.payMethod.title,
                        fontSize = FontSize.REGULAR,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(
                color = BorderIdle,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // 收件資訊
            Text(
                text = "收件資訊",
                fontSize =  FontSize.EXTRA_REGULAR,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Resources.Icon.MapPin),
                    contentDescription = null,
                    tint = IconPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = order.consignee,
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "(+${order.phone.dialCode}) ${order.phone.number}",
                    fontSize = FontSize.REGULAR,
                    color = GrayDarker
                )
            }
            Text(
                text = order.address,
                fontSize = FontSize.REGULAR,
                color = TextPrimary,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Composable
private fun OrderInfoCard(
    order: OrderUiModel,
    modifier: Modifier = Modifier
) {
    val clipboard = rememberClipboardManager()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "訂單編號",
                    fontSize = FontSize.EXTRA_REGULAR,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.clickable { clipboard.copy(order.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = order.id,
                        fontSize = FontSize.REGULAR
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(Resources.Icon.Copy),
                        contentDescription = "複製",
                        tint = IconPrimary,
                        modifier = Modifier
                            .size(24.dp)

                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OrderInfoRow(label = "訂單成立時間", value = order.createAt.toFormattedString("yyyy-MM-dd HH:mm"))
            Spacer(Modifier.height(8.dp))
            order.paidAt?.let {
                OrderInfoRow(label = "完成付款時間", value = order.paidAt!!.toFormattedString("yyyy-MM-dd HH:mm"))
                Spacer(Modifier.height(8.dp))
            }
            order.completedAt?.let {
                OrderInfoRow(label = "訂單完成時間", value = order.completedAt!!.toFormattedString("yyyy-MM-dd HH:mm"))
                Spacer(Modifier.height(8.dp))
            }

        }
    }
}

@Composable
private fun OrderInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = FontSize.REGULAR
        )
        Text(
            text = value,
            fontSize = FontSize.REGULAR,
            color = GrayDarker
        )
    }
}
