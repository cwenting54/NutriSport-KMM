package dev.nutrisport.home.domain

import dev.nutrisport.shared.Resources
import org.jetbrains.compose.resources.DrawableResource

enum class DrawerItem(
    val title: String,
    val icon: DrawableResource
) {
    Profile(
        title = "個人資訊",
        icon = Resources.Icon.Person
    ),

    OrderList(
        title = "訂單紀錄",
        icon = Resources.Icon.Book
    ),

    Favorites(
        title = "按讚好物",
        icon = Resources.Icon.Favorite
    ),

    Contact(
        title = "聯絡我們",
        icon = Resources.Icon.Edit
    ),

    SignOut(
        title = "登出",
        icon = Resources.Icon.SignOut
    ),

    Admin(
        title = "後台管理",
        icon = Resources.Icon.Unlock
    ),

}