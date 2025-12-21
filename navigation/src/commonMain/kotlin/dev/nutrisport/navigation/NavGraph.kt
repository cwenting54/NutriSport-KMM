package dev.nutrisport.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.nutrisport.admin_panel.AdminPanelScreen
import dev.nutrisport.auth.AuthScreen
import dev.nutrisport.category_search.CategorySearchScreen
import dev.nutrisport.checkout.CheckoutScreen
import dev.nutrisport.details.DetailsScreen
import dev.nutrisport.favorite_list.FavoriteListScreen
import dev.nutrisport.home.HomeGraphScreen
import dev.nutrisport.manage_product.ManageProductScreen
import dev.nutrisport.order_list.navigation.orderGraph
import dev.nutrisport.payment_complete.PaymentCompletedScreen
import dev.nutrisport.products_more.ProductMoreScreen
import dev.nutrisport.profile.ProfileScreen
import dev.nutrisport.shared.domain.ProductCategory
import dev.nutrisport.shared.navigation.Screen
import dev.nutrisport.shared.util.PreferencesRepository

@Composable
fun SetupNavGraph(startDestination: Screen = Screen.Auth) {
    val navController = rememberNavController()
//    val intentHandler = koinInject<IntentHandler>()
//    val navigateTo by intentHandler.navigateTo.collectAsState()
//
//    LaunchedEffect(navigateTo) {
//        navigateTo?.let { paymentCompleted ->
//            navController.navigate(paymentCompleted)
//            intentHandler.resetNavigation()
//        }
//    }
    val preferencesData by PreferencesRepository.readPayPalDataFlow()
        .collectAsState(initial = null)

    LaunchedEffect(preferencesData) {
        preferencesData?.let { paymentComplete ->
            if (paymentComplete.token != null ) {
                navController.navigate(paymentComplete)
                PreferencesRepository.reset()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Auth> {
            AuthScreen(
                navigateToHome = {
                    navController.navigate(Screen.HomeGraph) {
                        popUpTo<Screen.Auth> { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.HomeGraph> {
            HomeGraphScreen(
                navigateToAuth = {
                    navController.navigate(Screen.Auth) {
                        popUpTo<Screen.HomeGraph> { inclusive = true }
                    }
                },
                navigateToProfile = {
                    navController.navigate(Screen.Profile)
                },
                navigateToOrderList = {
                    navController.navigate(Screen.OrderList)
                },
                navigateToFavoriteList = {
                    navController.navigate(Screen.FavoriteList)
                },
                navigateToAdminPanel = {
                    navController.navigate(Screen.AdminPanel)
                },
                navigateToDetails = { productId ->
                    navController.navigate(Screen.Details(id = productId))
                },
                navigateToProductMore = { productType ->
                    navController.navigate(Screen.ProductMore(productType = productType.title))
                },
                navigateToCategorySearch = {category ->
                    navController.navigate(Screen.CategorySearch(category = category))
                },
                navigateToCheckout = {totalAmount ->
                    navController.navigate(Screen.Checkout(totalAmount = totalAmount))
                }
            )
        }
        composable<Screen.Profile> {
            ProfileScreen(
                navigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable<Screen.FavoriteList> {
            FavoriteListScreen(
                navigateBack = {
                    navController.navigateUp()
                },
                navigateToDetails = { productId ->
                    navController.navigate(Screen.Details(id = productId))
                },
            )
        }
        composable<Screen.AdminPanel> {
            AdminPanelScreen(
                navigateBack = {
                    navController.navigateUp()
                },
                navigateToManageProduct = { id ->
                    navController.navigate(Screen.ManageProduct(id = id))
                }
            )
        }
        composable<Screen.ManageProduct> {
            val id = it.toRoute<Screen.ManageProduct>().id
            ManageProductScreen(
                id = id,
                navigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable<Screen.Details> {
            DetailsScreen(
                navigateBack = {
                    navController.navigateUp()
                },
                navigateToDetails = { productId ->
                    navController.navigate(Screen.Details(id = productId))
                }
            )
        }
        composable<Screen.CategorySearch> {
            val category = ProductCategory.valueOf(it.toRoute<Screen.CategorySearch>().category)
            CategorySearchScreen(
                category = category,
                navigateToDetails = { id ->
                    navController.navigate(Screen.Details(id))
                },
                navigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable<Screen.Checkout> {
            val totalAmount = it.toRoute<Screen.Checkout>().totalAmount
            CheckoutScreen(
                totalAmount = totalAmount.toDoubleOrNull() ?: 0.0,
                navigateBack = {
                    navController.navigateUp()
                },
                navigateToPaymentCompleted = { isSuccess, error ->
                    navController.navigate(Screen.PaymentComplete(isSuccess, error))
                }
            )
        }
        composable<Screen.PaymentComplete> {
            PaymentCompletedScreen(
                navigateBack = {
                    navController.navigate(Screen.HomeGraph) {
                        launchSingleTop = true
                        // Clear backstack completely
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.ProductMore> {
            val productType = it.toRoute<Screen.ProductMore>().productType
            ProductMoreScreen(
                productType = productType,
                navigateBack = {
                    navController.navigateUp()
                },
                navigateToDetails = { productId ->
                    navController.navigate(Screen.Details(id = productId))
                }
            )
        }

        orderGraph(navController)

    }
}