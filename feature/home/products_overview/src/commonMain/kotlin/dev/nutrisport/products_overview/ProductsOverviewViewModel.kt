package dev.nutrisport.products_overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nutrisport.data.domain.ProductRepository
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProductsOverviewViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    val products = combine(
        productRepository.readNewProducts(),
        productRepository.readDiscountedProducts(),
        productRepository.readPopularProducts(),
    ) { new, discounted, popular ->
        when {
            new.isSuccess() && discounted.isSuccess() && popular.isSuccess() -> {
                RequestState.Success(new.getSuccessData() + discounted.getSuccessData() + popular.getSuccessData())
            }
            new.isError() -> new
            discounted.isError() -> discounted
            else -> RequestState.Loading
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RequestState.Loading
    )

}