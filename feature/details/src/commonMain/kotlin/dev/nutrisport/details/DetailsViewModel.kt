package dev.nutrisport.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nutrisport.data.domain.CommentRepository
import dev.nutrisport.data.domain.CustomerRepository
import dev.nutrisport.data.domain.ProductRepository
import dev.nutrisport.details.domain.CommentFilter
import dev.nutrisport.shared.domain.CartItem
import dev.nutrisport.shared.domain.CommentItem
import dev.nutrisport.shared.domain.Product
import dev.nutrisport.shared.util.RequestState
import dev.nutrisport.shared.util.RequestState.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val commentRepository: CommentRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val productId = savedStateHandle.get<String>("id") ?: ""

    val product = productRepository.readProductByIdFlow(
        id = productId
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RequestState.Loading
    )

    val similarProducts = productRepository.readSimilarProducts(
        id = productId
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RequestState.Loading
    )

    val isFavoriteProduct =
        customerRepository.isFavoriteProductFlow(
            productId = productId,
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RequestState.Loading
        )

    // 目前選擇的篩選條件（預設：最新評價）
    private val _commentFilter = MutableStateFlow(CommentFilter.NEWEST)
    val commentFilter: StateFlow<CommentFilter> = _commentFilter.asStateFlow()

    fun updateFilter(filter: CommentFilter) {
        _commentFilter.value = filter
    }

    // 原始的評論資料（不套篩選）
    val comments =
        commentRepository.readCommentsByProductIdFlow(productId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RequestState.Loading
            )

    // 套用目前篩選條件後，要給 UI 用的評論列表
    val filteredComments: StateFlow<RequestState<List<CommentItem>>> =
        combine(
            comments,
            _commentFilter
        ) { state: RequestState<List<CommentItem>>, filter: CommentFilter ->
            val result: RequestState<List<CommentItem>> =
                when (state) {
                    is RequestState.Loading -> state

                    is RequestState.Error -> state

                    is Success -> {
                        val list = state.data

                        val sorted = when (filter) {
                            CommentFilter.NEWEST -> {
                                // 最新評價：createdAt 新 → 舊
                                list.sortedByDescending { it.createdAt }
                            }
                            CommentFilter.RATING_HIGH -> {
                                // 評價由高到低
                                list.sortedByDescending { it.rating }
                            }
                            CommentFilter.RATING_LOW -> {
                                // 評價由低到高
                                list.sortedBy { it.rating }
                            }
                            CommentFilter.HAS_CONTENT -> {
                                // 有評論內容，先篩選再照時間新 → 舊
                                list
                                    .filter { it.content.isNotBlank() }
                                    .sortedByDescending { it.createdAt }
                            }
                        }

                        Success(sorted)
                    }

                    RequestState.Idle -> state
                }
            result
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = RequestState.Loading
            )

    fun likeComment(
        commentId: String,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            commentRepository.updateCommentsLike(
                commentId = commentId,
                onSuccess = {},
                onError = onError
            )
        }
    }

    var selectedFlavor: String? by mutableStateOf(null)
        private set

    var quantity by mutableStateOf(1)
        private set

    fun updateFlavor(value: String) {
        selectedFlavor = value
    }

    fun updateQuantity(value: Int) {
        quantity = value
    }

    fun addItemToCart(
        product: Product,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            customerRepository.addItemToCart(
                cartItem = CartItem(
                    productId = productId,
                    weight = product.weight,
                    price = product.price,
                    flavor = selectedFlavor,
                    quantity = quantity
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    fun updateFavoriteProduct (
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            customerRepository.updateFavoriteProduct(
                productId = productId,
                onSuccess = onSuccess,
                onError = {
                    onError(it)
                }
            )
        }
    }




}