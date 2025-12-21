package dev.nutrisport.data.domain

import dev.nutrisport.shared.domain.Order
import dev.nutrisport.shared.domain.OrderUiModel
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getCurrentUserId(): String?
    suspend fun createTheOrder(
        order: Order,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    )

    fun readOrdersFlow(): Flow<RequestState<List<OrderUiModel>>>
    fun readOrdersByOrderIdFlow(orderId: String): Flow<RequestState<OrderUiModel>>

    suspend fun deleteOrder(
        orderId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}