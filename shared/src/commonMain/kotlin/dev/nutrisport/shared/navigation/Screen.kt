package dev.nutrisport.shared.navigation

import dev.nutrisport.shared.domain.ProductType
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Auth : Screen()

    @Serializable
    data object HomeGraph : Screen()
    @Serializable
    data object ProductOverview : Screen()
    @Serializable
    data object Cart : Screen()
    @Serializable
    data object Categories : Screen()
    @Serializable
    data object Profile : Screen()
    @Serializable
    data object OrderGraph : Screen()

    @Serializable
    data object FavoriteList : Screen()
    @Serializable
    data object AdminPanel : Screen()
    @Serializable
    data class ManageProduct(
        val id: String? = null,
    ) : Screen()

    @Serializable
    data class Details(
        val id: String,
    ) : Screen()
    @Serializable
    data class CategorySearch(
        val category: String,
    ) : Screen()

    @Serializable
    data class Checkout(
        val totalAmount: String,
    ) : Screen()

    @Serializable
    data class PaymentComplete(
        val isSuccess: Boolean? = null,
        val error: String? = null,
        val token: String? = null,
    ): Screen()

    @Serializable
    data class ProductMore(
        val productType: String,
    ): Screen()

    @Serializable
    data object OrderList : Screen()
    @Serializable
    data class OrderDetail(val orderId: String) : Screen()

    @Serializable
    data class ProductRating(val orderId: String) : Screen()
}