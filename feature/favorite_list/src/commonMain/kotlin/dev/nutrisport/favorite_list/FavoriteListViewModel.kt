package dev.nutrisport.favorite_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nutrisport.data.domain.CustomerRepository
import dev.nutrisport.shared.domain.ProductCategory
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteListViewModel(
    private val customerRepository: CustomerRepository,
): ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ProductCategory?>(null)
    val selectedCategory: StateFlow<ProductCategory?> = _selectedCategory.asStateFlow()

    val favoriteList =
        combine(
            customerRepository.readFavoriteProductsFlow(),
            _searchQuery,
            _selectedCategory
        ) { productsState, query, category ->

            when {
                productsState.isSuccess() -> {
                    val data = productsState.getSuccessDataOrNull().orEmpty()

                    val filtered = data.filter { product ->
                        val matchQuery =
                            query.isBlank() || product.title.contains(query, ignoreCase = true)

                        val matchCategory =
                            category == null ||
                                    category.toString().isBlank() ||
                                    ProductCategory.fromString(product.category) == category
                        matchQuery && matchCategory
                    }

                    RequestState.Success(filtered)
                }

                productsState.isError() -> {
                    RequestState.Error(productsState.getErrorMessage())
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

    fun updateSelectedCategory(value: ProductCategory?) {
        _selectedCategory.value = if (value == _selectedCategory.value) null else value
    }

    fun updateFavoriteProduct (
        productId: String,
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