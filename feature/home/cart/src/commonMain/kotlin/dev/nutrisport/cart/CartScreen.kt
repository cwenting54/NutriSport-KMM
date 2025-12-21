package dev.nutrisport.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import dev.nutrisport.cart.component.CartItemCard
import dev.nutrisport.shared.BebasNeueFont
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.component.PrimaryButton
import dev.nutrisport.shared.util.DisplayResult
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navigateToCheckout: (totalAmount: String) -> Unit
) {
    val viewModel = koinViewModel<CartViewModel>()
    val cartItemsWithProducts by viewModel.cartItemsWithProducts.collectAsState(RequestState.Loading)
    val totalAmount by viewModel.totalAmountFlow.collectAsState(RequestState.Loading)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            snackbarHostState.currentSnackbarData?.dismiss()
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Surface,
        bottomBar = {
            if (cartItemsWithProducts.isSuccess() && cartItemsWithProducts.getSuccessData().isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$ ${if (totalAmount.isSuccess()) totalAmount.getSuccessData().formatAmount() else "0.00"}",
                        fontSize = FontSize.EXTRA_MEDIUM,
                        color = TextPrimary,
                        fontFamily = BebasNeueFont(),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        text = "前往付款",
                        icon = null,
                        enabled = true,
                        onClick = {
                            navigateToCheckout(
                                totalAmount.getSuccessData().toString()
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        cartItemsWithProducts.DisplayResult(
            onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
            onSuccess = { data ->
                if (data.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = innerPadding.calculateBottomPadding()
                            )
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = data,
                            key = { it.first.id }
                        ) { pair ->
                            CartItemCard(
                                cartItem = pair.first,
                                product = pair.second,
                                flavor = pair.first.flavor,
                                onMinusClick = { quantity ->
                                    viewModel.updateCartItemQuantity(
                                        id = pair.first.id,
                                        quantity = quantity,
                                        onSuccess = {},
                                        onError = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(it)
                                            }
                                        }
                                    )
                                },
                                onPlusClick = { quantity ->
                                    viewModel.updateCartItemQuantity(
                                        id = pair.first.id,
                                        quantity = quantity,
                                        onSuccess = {},
                                        onError = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(it)
                                            }
                                        }
                                    )
                                },
                                onDeleteClick = {
                                    viewModel.deleteCartItem(
                                        id = pair.first.id,
                                        onSuccess = {},
                                        onError = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(it)
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                } else {
                    InfoCard(
                        image = Resources.Image.ShoppingCart,
                        title = "你的購物車中還沒有任何商品",
                        subtitle = "快去選購一些喜歡的商品吧！"
                    )
                }
            },
            onError = { message ->
                InfoCard(
                    image = Resources.Image.Cat,
                    title = "Oops!",
                    subtitle = message
                )
            },
            transitionSpec = fadeIn() togetherWith fadeOut()
        )
    }
}

private fun Double.formatAmount(): String {
    val str = this.toString()
    val parts = str.split('.')
    val integerPart = parts[0]
    val fractionalPart = if (parts.size > 1) parts[1] else ""
    
    val paddedFractional = fractionalPart.padEnd(2, '0').take(2)
    return "$integerPart.$paddedFractional"
}