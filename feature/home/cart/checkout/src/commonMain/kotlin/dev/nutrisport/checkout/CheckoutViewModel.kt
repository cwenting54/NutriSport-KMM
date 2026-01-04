package dev.nutrisport.checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nutrisport.checkout.domain.Amount
import dev.nutrisport.checkout.domain.PaypalApi
import dev.nutrisport.checkout.domain.ShippingAddress
import dev.nutrisport.data.domain.CustomerRepository
import dev.nutrisport.data.domain.OrderRepository
import dev.nutrisport.data.domain.ProductRepository
import dev.nutrisport.shared.domain.CartItem
import dev.nutrisport.shared.domain.CartItemUiModel
import dev.nutrisport.shared.domain.ConsigneeInfo
import dev.nutrisport.shared.domain.Country
import dev.nutrisport.shared.domain.Customer
import dev.nutrisport.shared.domain.Order
import dev.nutrisport.shared.domain.PayMethod
import dev.nutrisport.shared.domain.PhoneNumber
import dev.nutrisport.shared.domain.Product
import dev.nutrisport.shared.util.PreferencesRepository
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class CheckoutScreenState(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val city: String? = null,
    val postalCode: Int? = null,
    val address: String? = null,
    val country: Country = Country.Serbia,
    val phoneNumber: PhoneNumber? = null,
    val cart: List<CartItem> = emptyList(),
)

class CheckoutViewModel(
    private val customerRepository: CustomerRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val savedStateHandle: SavedStateHandle,
    private val paypalApi: PaypalApi
) : ViewModel() {

    var screenState: CheckoutScreenState by mutableStateOf(CheckoutScreenState())
        private set

    var requestState: RequestState<List<CartItemUiModel>> by mutableStateOf(RequestState.Loading)
        private set

    val isFormValid: Boolean
        get() = with(screenState) {
            firstName.length in 3..50 &&
                    lastName.length in 3..50 &&
                    (city?.length ?: 0) in 3..50 &&
                    (postalCode?.toString()?.length ?: 0) in 3..8 &&
                    (address?.length ?: 0) in 3..50 &&
                    (phoneNumber?.number?.length ?: 0) in 5..30
        }

    init {
        fetchPaypalToken()
        observeData()
    }

    private fun fetchPaypalToken() {
        viewModelScope.launch {
            paypalApi.fetchAccessToken(
                onSuccess = { token -> println("TOKEN RECEIVED: $token") },
                onError = { message -> println("PayPal Error: $message") }
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch {
            customerRepository.readCustomerFlow()
                .flatMapLatest { customerState ->
                    when (customerState) {
                        is RequestState.Success -> observeProducts(customerState.data)
                        is RequestState.Error ->
                            flowOf(CombinedState.Error(customerState.message))
                        RequestState.Loading, RequestState.Idle ->
                            flowOf(CombinedState.Loading)
                    }
                }
                .collect { state ->
                    render(state)
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProducts(customer: Customer): Flow<CombinedState> {
        val productIds = customer.cart.map { it.productId }.toSet()

        if (productIds.isEmpty()) {
            return flowOf(
                CombinedState.Success(customer, emptyList())
            )
        }

        return productRepository.readProductsByIdsFlow(productIds.toList())
            .map { productState ->
                when (productState) {
                    is RequestState.Success ->
                        CombinedState.Success(customer, productState.data)

                    is RequestState.Error ->
                        CombinedState.Error(productState.message)

                    RequestState.Loading,
                    RequestState.Idle ->
                        CombinedState.Loading
                }
            }
            .catch { e ->
                emit(CombinedState.Error(e.message ?: "Unknown Error"))
            }

    }

    private fun render(state: CombinedState) {
        when (state) {
            is CombinedState.Loading -> {
                requestState = RequestState.Loading
            }

            is CombinedState.Error -> {
                requestState = RequestState.Error(state.message)
            }

            is CombinedState.Success -> {
                updateScreenStateIfNeeded(state.customer)
                requestState = RequestState.Success(
                    buildCartUiModels(state.customer, state.products)
                )
            }
        }
    }


    private fun updateScreenStateIfNeeded(customer: Customer) {
        if (screenState.email.isNotBlank()) return

        screenState = CheckoutScreenState(
            id = customer.id,
            firstName = customer.firstName,
            lastName = customer.lastName,
            email = customer.email,
            city = customer.city,
            postalCode = customer.postalCode,
            address = customer.address,
            phoneNumber = customer.phoneNumber,
            country = Country.entries.firstOrNull {
                it.dialCode == customer.phoneNumber?.dialCode
            } ?: Country.Taiwan,
            cart = customer.cart
        )
    }

    private fun buildCartUiModels(
        customer: Customer,
        products: List<Product>
    ): List<CartItemUiModel> =
        customer.cart.mapNotNull { cartItem ->
            val product = products.find { it.id == cartItem.productId } ?: return@mapNotNull null

            CartItemUiModel(
                id = product.id,
                productId = product.id,
                productTitle = product.title,
                thumbnail = product.thumbnail,
                price = cartItem.price,
                weight = cartItem.weight,
                flavor = cartItem.flavor,
                quantity = cartItem.quantity
            )
        }

    fun updateFirstName(value: String) {
        screenState = screenState.copy(firstName = value)
    }

    fun updateLastName(value: String) {
        screenState = screenState.copy(lastName = value)
    }

    fun updateCity(value: String) {
        screenState = screenState.copy(city = value)
    }

    fun updatePostalCode(value: Int?) {
        screenState = screenState.copy(postalCode = value)
    }

    fun updateAddress(value: String) {
        screenState = screenState.copy(address = value)
    }

    fun updateCountry(value: Country) {
        screenState = screenState.copy(
            country = value,
            phoneNumber = screenState.phoneNumber?.copy(
                dialCode = value.dialCode
            )
        )
    }

    fun updatePhoneNumber(value: String) {
        screenState = screenState.copy(
            phoneNumber = PhoneNumber(
                dialCode = screenState.country.dialCode,
                number = value
            )
        )
    }


    fun payOnDelivery(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        saveConsigneeInfo(
            onError = onError
        )

        createTheOrder(
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun createTheOrder(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            orderRepository.createTheOrder(
                order = Order(
                    customerId = screenState.id,
                    items = screenState.cart,
                    totalAmount = savedStateHandle.get<String>("totalAmount")?.toDoubleOrNull()
                        ?: 0.0,
                    payMethod = PayMethod.CashOnDelivery.title,
                    consignee = screenState.firstName + screenState.lastName,
                    address = screenState.postalCode.toString() + " " + screenState.city + (screenState.address
                        ?: ""),
                    phone = screenState.phoneNumber ?: PhoneNumber(screenState.country.dialCode, "")
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    fun payWithPaypal(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val totalAmount = savedStateHandle.get<String>("totalAmount")
        if (totalAmount == null) {
            onError("Total amount is not available.")
            return
        }
        saveConsigneeInfo(
            onError = onError
        )
        viewModelScope.launch {
            paypalApi.beginCheckout(
                amount = Amount(
                    currencyCode = "TWD",
                    value = totalAmount.toDouble().roundToInt().toString()
                ),
                fullName = "${screenState.firstName}${screenState.lastName}",
                shippingAddress = ShippingAddress(
                    addressLine1 = screenState.address ?: "Unknown address",
                    city = screenState.city ?: "Unknown city",
                    state = screenState.country.name,
                    postalCode = screenState.postalCode.toString(),
                    countryCode = screenState.country.code
                ),
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }

    fun saveConsigneeInfo(
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            val consigneeInfo = ConsigneeInfo(
                name = "${screenState.firstName}${screenState.lastName}",
                phone = screenState.phoneNumber ?: PhoneNumber(screenState.country.dialCode, ""),
                city = screenState.city ?: "",
                postalCode = screenState.postalCode?.toString() ?: "",
                address = screenState.address ?: ""
            )
            customerRepository.updateConsigneeInfo(
                customerId = screenState.id,
                consigneeInfo = consigneeInfo,
                onSuccess = {},
                onError = onError
            )
        }

        PreferencesRepository.saveConsigneeInfo(
            name = "${screenState.firstName}${screenState.lastName}",
            dialCode = screenState.country.dialCode,
            phone = screenState.phoneNumber?.number ?: "",
            city = screenState.city ?: "",
            postalCode = screenState.postalCode?.toString() ?: "",
            address = screenState.address ?: ""
        )
    }

}

sealed interface CombinedState {
    object Loading : CombinedState
    data class Error(val message: String) : CombinedState
    data class Success(
        val customer: Customer,
        val products: List<Product>
    ) : CombinedState
}
