package dev.nutrisport.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.nutrisport.shared.BebasNeueFont
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
import dev.nutrisport.shared.util.DisplayResult
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    totalAmount: Double,
    navigateBack: () -> Unit,
    navigateToPaymentCompleted: (isSuccess: Boolean?, error: String?) -> Unit,
) {
    val viewModel = koinViewModel<CheckoutViewModel>()
    val requestState = viewModel.requestState
    val screenState = viewModel.screenState
    val isFormValid = viewModel.isFormValid

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showOrdererEditScreen by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = showOrdererEditScreen,
        modifier = Modifier.zIndex(1f)
    ) {
        OrdererEditScreen(
            navigateBack = {
                showOrdererEditScreen = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            snackbarHostState.currentSnackbarData?.dismiss()
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = SurfaceLighter,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "結帳",
                        fontSize = FontSize.MEDIUM,
                        fontWeight = FontWeight.Bold,
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
        }
    ) { padding ->
        requestState.DisplayResult(
            onLoading = {
                LoadingCard(modifier = Modifier.fillMaxSize())
            },
            onSuccess = { cartItemsWithProducts ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        )
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ReceiverCard(
                            info = screenState,
                            modifier = Modifier.padding(top = 16.dp),
                            onEditClick = {
                                showOrdererEditScreen = true
                            }
                        )

                        // 商品總覽卡
                        ProductSummaryCard(
                            title = "訂購商品",
                            order = cartItemsWithProducts,
                        )
                        //付款總金額
                        TotalAmountCard(
                            amount = totalAmount
                        )
                    }


                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton(
                            text = "PayPal付款",
                            icon = Resources.Image.PaypalLogo,
                            enabled = isFormValid,
                            onClick = {
                                viewModel.payWithPaypal(
                                    onSuccess = {},
                                    onError = { message ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    }
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = "貨到付款",
                            icon = Resources.Icon.ShoppingCart,
                            secondary = true,
                            enabled = isFormValid,
                            onClick = {
                                viewModel.payOnDelivery(
                                    onSuccess = {
                                        navigateToPaymentCompleted(true, null)
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
private fun ReceiverCard(
    info: CheckoutScreenState,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
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
            // 收件資訊
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "收件資訊",
                    fontSize = FontSize.EXTRA_REGULAR,
                    fontWeight = FontWeight.Medium
                )
                EditButton(
                    onEditClick = onEditClick
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = info.firstName + info.lastName,
                fontSize = FontSize.REGULAR,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "電話：(+${info.phoneNumber?.dialCode}) ${info.phoneNumber?.number}",
                fontSize = FontSize.REGULAR,
                color = GrayDarker
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "地址：${info.address ?: ""}",
                fontSize = FontSize.REGULAR,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun TotalAmountCard(
    amount: Double,
    modifier: Modifier = Modifier,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "最終付款金額",
                    fontSize = FontSize.EXTRA_REGULAR,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "付款總金額",
                    fontSize = FontSize.REGULAR,
                )
                Text(
                    text = "$${amount}",
                    fontSize = FontSize.MEDIUM,
                    fontWeight = FontWeight.Bold
                )
            }


        }
    }
}

@Composable
fun EditButton(
    onEditClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFFF7A3C),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                onEditClick()
            }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "編輯",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFFF7A3C)
        )
    }
}