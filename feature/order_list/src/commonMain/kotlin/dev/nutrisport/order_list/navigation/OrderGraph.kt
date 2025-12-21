package dev.nutrisport.order_list.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import dev.nutrisport.order_list.OrderDetailScreen
import dev.nutrisport.order_list.OrderListScreen
import dev.nutrisport.order_list.OrderViewModel
import dev.nutrisport.order_list.ProductRatingScreen
import dev.nutrisport.shared.navigation.Screen
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.orderGraph(navController: NavHostController) {

    navigation<Screen.OrderGraph>(
        startDestination = Screen.OrderList
    ) {

        // 1) 訂單列表
        composable<Screen.OrderList> { backStackEntry ->
            val orderViewModel: OrderViewModel = rememberSharedOrderViewModel(
                navController = navController,
                backStackEntry = backStackEntry
            )

            OrderListScreen(
                viewModel = orderViewModel,
                navigateToOderDetail = { orderId ->
                    navController.navigate(Screen.OrderDetail(orderId = orderId))
                },
                navigateBack = { navController.navigateUp() }
            )
        }

        // 2) 訂單明細
        composable<Screen.OrderDetail> { backStackEntry ->
            val orderViewModel: OrderViewModel = rememberSharedOrderViewModel(
                navController = navController,
                backStackEntry = backStackEntry
            )

            val route = backStackEntry.toRoute<Screen.OrderDetail>()
            val orderId = route.orderId

            LaunchedEffect(orderId) {
                orderViewModel.setOrderId(orderId)
            }

            OrderDetailScreen(
                viewModel = orderViewModel,
                navigateBack = { navController.navigateUp() },
                navigateToDetail = { id ->
                    navController.navigate(Screen.Details(id = id))
                },
                navigateToRating = { id ->
                    navController.navigate(Screen.ProductRating(orderId = id))
                }
            )
        }

        // 3) 商品評價畫面
        composable<Screen.ProductRating> { backStackEntry ->
            val orderViewModel: OrderViewModel = rememberSharedOrderViewModel(
                navController = navController,
                backStackEntry = backStackEntry
            )
            val route = backStackEntry.toRoute<Screen.OrderDetail>()
            val orderId = route.orderId

            LaunchedEffect(orderId) {
                orderViewModel.setOrderId(orderId)
            }

            ProductRatingScreen(
                viewModel = orderViewModel,
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}

@Composable
private fun rememberSharedOrderViewModel(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry
): OrderViewModel {

    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry<Screen.OrderGraph>()
    }

    return koinViewModel(
        viewModelStoreOwner = parentEntry
    )
}

