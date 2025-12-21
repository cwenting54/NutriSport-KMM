package dev.nutrisport.products_more

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nutrisport.data.domain.ProductRepository
import dev.nutrisport.shared.domain.ProductType
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProductMoreViewModel(
    private val productRepository: ProductRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val productType =
        savedStateHandle.get<String>("productType") ?: ProductType.Discounted.title


    private var _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val rawProducts =
        when (productType) {
            ProductType.Discounted.title -> productRepository.readDiscountedProducts()
            ProductType.Popular.title    -> productRepository.readPopularProducts()
            ProductType.Newest.title     -> productRepository.readNewProducts()
            else -> productRepository.readDiscountedProducts()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RequestState.Loading
        )

    val products =
        combine(
            rawProducts,
            _searchQuery
        ) { state, query ->
            when {
                state.isSuccess() -> {
                    val data = state.getSuccessDataOrNull().orEmpty()

                    val filtered = if (query.isBlank()) {
                        data
                    } else {
                        data.filter { product ->
                            product.title.contains(query, ignoreCase = true)
                        }
                    }

                    RequestState.Success(filtered)
                }

                state.isError() -> {
                    RequestState.Error(state.getErrorMessage())
                }

                else -> {
                    RequestState.Loading
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RequestState.Loading
        )


    fun updateSearchQuery(value: String) {
        _searchQuery.value = value
    }
}