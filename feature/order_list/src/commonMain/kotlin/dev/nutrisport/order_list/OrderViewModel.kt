package dev.nutrisport.order_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nutrisport.data.domain.CommentRepository
import dev.nutrisport.data.domain.CustomerRepository
import dev.nutrisport.data.domain.OrderRepository
import dev.nutrisport.shared.domain.CartItem
import dev.nutrisport.shared.domain.CartItemUiModel
import dev.nutrisport.shared.domain.CommentUiModel
import dev.nutrisport.shared.domain.Comments
import dev.nutrisport.shared.domain.OrderUiModel
import dev.nutrisport.shared.domain.ProductInfo
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.map
import kotlin.collections.orEmpty

class OrderViewModel(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val commentRepository: CommentRepository,
): ViewModel() {

    private val _currentOrderId = MutableStateFlow("")
    val currentOrderId: StateFlow<String> = _currentOrderId.asStateFlow()

    private val _commentDrafts = MutableStateFlow<List<CommentUiModel>>(emptyList())
    val commentDrafts: StateFlow<List<CommentUiModel>> = _commentDrafts

    fun setOrderId(orderId: String) {
        _currentOrderId.value = orderId
    }

    val orders = orderRepository.readOrdersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RequestState.Loading
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedOrder =
        currentOrderId
            .filter { it.isNotEmpty() }
            .distinctUntilChanged()
            .flatMapLatest { orderId ->
                orderRepository.readOrdersByOrderIdFlow(orderId = orderId)
            }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RequestState.Loading
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val comments =  currentOrderId
        .filter { it.isNotEmpty() }
        .distinctUntilChanged()
        .flatMapLatest { orderId ->
            commentRepository.readCommentsFlowByOrderId(orderId = orderId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RequestState.Loading
        )


    fun addItemToCart(
        item: CartItemUiModel,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            customerRepository.addItemToCart(
                cartItem = CartItem(
                    productId = item.productId,
                    weight = item.weight,
                    price = item.price,
                    flavor = item.flavor,
                    quantity = item.quantity
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    fun initCommentDraftsFromOrder(comments: List<CommentUiModel>) {
        val commentsList: List<CommentUiModel> = comments.ifEmpty {
            val order = (selectedOrder.value as? RequestState.Success)?.data
            val items = order?.items.orEmpty()
            items.map { item ->
                CommentUiModel(
                    id = item.id,
                    customerId = order?.customerId.orEmpty(),
                    orderId = order?.id.orEmpty(),
                    productInfo = ProductInfo(
                        id = item.productId,
                        name = item.productTitle,
                        thumbnail = item.thumbnail
                    ),
                    rate = 5,
                    thumbUpCount = 0,
                    description = null,
                    createdAt = 0L,
                    updateAt = 0L
                )
            }
        }

        _commentDrafts.value = commentsList
    }

    fun updateDraftRating(productId: String, rate: Int) {
        _commentDrafts.update { list ->
            list.map { c ->
                if (c.productInfo.id == productId) c.copy(rate = rate) else c
            }
        }
    }

    fun updateDraftDescription(productId: String, text: String) {
        _commentDrafts.update { list ->
            list.map { c ->
                if (c.productInfo.id == productId) c.copy(description = text) else c
            }
        }
    }

    fun updateComment(
        commentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val comment =  _commentDrafts.value.find { it.id == commentId }?.let { ui ->
                Comments(
                    id = ui.id,
                    customerId = ui.customerId,
                    productId = ui.productInfo.id,
                    orderId = ui.orderId,
                    rate = ui.rate,
                    description = ui.description,
                    thumbUpCount = ui.thumbUpCount,
                    createdAt = ui.createdAt
                )
            } ?: return@launch

            commentRepository.updateComment(
                comment = comment,
                onSuccess = onSuccess,
                onError = onError,
            )
        }
    }

    fun submitComments(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val commentEntities: List<Comments> =  _commentDrafts.value.map { ui ->
                    Comments(
                        customerId = ui.customerId,
                        productId = ui.productInfo.id,
                        orderId = ui.orderId,
                        rate = ui.rate,
                        description = ui.description
                    )
                }

                commentRepository.createComment(
                    comment = commentEntities,
                    onSuccess = onSuccess,
                    onError = onError
                )
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add comments.")
            }
        }
    }


    fun deleteOrder(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val orderId = _currentOrderId.value.ifEmpty { return@launch }
            orderRepository.deleteOrder(
                orderId = orderId,
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }
}