package dev.nutrisport.data.domain

import dev.gitlive.firebase.auth.FirebaseUser
import dev.nutrisport.shared.domain.CartItem
import dev.nutrisport.shared.domain.CommentItem
import dev.nutrisport.shared.domain.CommentUiModel
import dev.nutrisport.shared.domain.Comments
import dev.nutrisport.shared.domain.ConsigneeInfo
import dev.nutrisport.shared.domain.Customer
import dev.nutrisport.shared.domain.Product
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getCurrentUserId(): String?
    suspend fun createCustomer(
        user: FirebaseUser?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    fun readCustomerFlow(): Flow<RequestState<Customer>>
    suspend fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun updateConsigneeInfo(
        customerId: String,
        consigneeInfo: ConsigneeInfo,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun addItemToCart(
        cartItem: CartItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun updateCartItemQuantity(
        id: String,
        quantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun deleteCartItem(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    suspend fun deleteAllCartItems(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun updateFavoriteProduct(
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    fun readFavoriteProductsFlow(): Flow<RequestState<List<Product>>>
    fun isFavoriteProductFlow(
        productId: String
    ): Flow<RequestState<Boolean>>



    suspend fun signOut(): RequestState<Unit>
}