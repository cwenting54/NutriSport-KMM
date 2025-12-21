package dev.nutrisport.home.domain

import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.navigation.Screen
import org.jetbrains.compose.resources.DrawableResource

enum class BottomBarDestination(
    val icon: DrawableResource,
    val title: String,
    val screen: Screen
) {
    ProductOverview(
        icon = Resources.Icon.Home,
        title = "Nutri Sport",
        screen = Screen.ProductOverview
    ),
    Cart(
        icon = Resources.Icon.ShoppingCart,
        title = "購物車",
        screen = Screen.Cart
    ),
    Categories(
        icon = Resources.Icon.Categories,
        title = "商品類別",
        screen = Screen.Categories
    ),
}