package dev.nutrisport.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.nutrisport.cart.CartScreen
import dev.nutrisport.categories.CategoriesScreen
import dev.nutrisport.home.component.BottomBar
import dev.nutrisport.home.component.CustomDrawer
import dev.nutrisport.home.domain.BottomBarDestination
import dev.nutrisport.home.domain.CustomDrawerState
import dev.nutrisport.home.domain.isOpened
import dev.nutrisport.home.domain.opposite
import dev.nutrisport.products_overview.ProductsOverviewScreen
import dev.nutrisport.shared.Alpha
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.SurfaceLighter
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.domain.ProductType
import dev.nutrisport.shared.navigation.Screen
import dev.nutrisport.shared.util.getScreenWidth
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import dev.nutrisport.shared.BebasNeueFont
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGraphScreen(
    navigateToAuth: () -> Unit,
    navigateToProfile: () -> Unit,
    navigateToOrderList: () -> Unit,
    navigateToFavoriteList: () -> Unit,
    navigateToAdminPanel: () -> Unit,
    navigateToDetails: (String) -> Unit,
    navigateToProductMore: (ProductType) -> Unit,
    navigateToCategorySearch: (String) -> Unit,
    navigateToCheckout: (String) -> Unit,
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState()
    val selectedDestination by remember {
        derivedStateOf {
            val route = currentRoute.value?.destination?.route.toString()
            when {
                route.contains(BottomBarDestination.ProductOverview.toString()) -> BottomBarDestination.ProductOverview
                route.contains(BottomBarDestination.Cart.toString()) -> BottomBarDestination.Cart
                route.contains(BottomBarDestination.Categories.toString()) -> BottomBarDestination.Categories
                else -> BottomBarDestination.ProductOverview
            }
        }
    }

    val screenWidth = remember { getScreenWidth() }
    var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }

    val offsetValue by remember { derivedStateOf { (screenWidth / 1.5).dp } }
    val animatedOffset by animateDpAsState(
        targetValue = if (drawerState.isOpened()) offsetValue else 0.dp
    )

    val animatedBackground by animateColorAsState(
        targetValue = if (drawerState.isOpened()) SurfaceLighter else Surface
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (drawerState.isOpened()) 0.9f else 1f
    )

    val animatedRadius by animateDpAsState(
        targetValue = if (drawerState.isOpened()) 20.dp else 0.dp
    )

    val viewModel = koinViewModel<HomeGraphViewmodel>()
    val customer by viewModel.customer.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBackground)
            .navigationBarsPadding()
    ) {
        CustomDrawer(
            customer = customer,
            onProfilerClick = navigateToProfile,
            onOrderListClick = navigateToOrderList,
            onFavoriteListClick = navigateToFavoriteList,
            onContactUsClick = {},
            onSignOutClick = {
                viewModel.signOut(
                    onSuccess = {
                        navigateToAuth()
                    },
                    onError = { error ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message = error)
                        }
                    }
                )
            },
            onAdminPanelClick = navigateToAdminPanel
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(animatedRadius))
                .offset(x = animatedOffset)
                .scale(scale = animatedScale)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(size = animatedRadius),
                    ambientColor = Color.Black.copy(alpha = Alpha.DISABLED),
                    spotColor = Color.Black.copy(alpha = Alpha.DISABLED)
                )
        ) {
            Scaffold(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    snackbarHostState.currentSnackbarData?.dismiss()
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                containerColor = Surface,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            AnimatedContent(
                                targetState = selectedDestination
                            ) { destination ->
                                Text(
                                    text = destination.title.uppercase(),
                                    fontSize = if (destination == BottomBarDestination.ProductOverview) FontSize.LARGE else FontSize.EXTRA_MEDIUM,
                                    fontWeight = if (destination == BottomBarDestination.ProductOverview) FontWeight.Normal else FontWeight.Bold,
                                    fontFamily = if (destination == BottomBarDestination.ProductOverview) BebasNeueFont() else null,
                                    color = TextPrimary
                                )
                            }
                        },
                        navigationIcon = {
                            AnimatedContent(
                                targetState = drawerState
                            ) { drawer ->
                                if (drawer.isOpened()) {
                                    IconButton(onClick = {
                                        drawerState = drawerState.opposite()
                                    }) {
                                        Icon(
                                            painter = painterResource(Resources.Icon.Close),
                                            contentDescription = "Close icon",
                                            tint = IconPrimary,
                                        )
                                    }
                                } else {
                                    IconButton(onClick = {
                                        drawerState = drawerState.opposite()
                                    }) {
                                        Icon(
                                            painter = painterResource(Resources.Icon.Menu),
                                            contentDescription = "Menu icon",
                                            tint = IconPrimary,
                                        )
                                    }
                                }
                            }

                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Surface,
                            scrolledContainerColor = Surface,
                            navigationIconContentColor = IconPrimary,
                            titleContentColor = TextPrimary,
                            actionIconContentColor = IconPrimary
                        )
                    )
                },
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        BottomBar(
                            customer = customer,
                            selected = selectedDestination,
                            onSelect = { destination ->
                                navController.navigate(destination.screen) {
                                    launchSingleTop = true
                                    popUpTo<Screen.ProductOverview> {
                                        saveState = true
                                        inclusive = false
                                    }
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.ProductOverview
                    ) {
                        composable<Screen.ProductOverview> {
                            ProductsOverviewScreen(
                                navigateToDetails = { productId ->
                                    navigateToDetails(productId)
                                },
                                navigateToProductMore = navigateToProductMore
                            )
                        }
                        composable<Screen.Cart> {
                            CartScreen(
                                navigateToCheckout = navigateToCheckout
                            )
                        }
                        composable<Screen.Categories> {
                            CategoriesScreen(
                                navigateToCategorySearch = navigateToCategorySearch
                            )
                        }
                    }

                }
            }
        }
    }

}