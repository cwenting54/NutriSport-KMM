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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
            val customerFlow = customerRepository.readCustomerFlow()

            val productsFlow = customerFlow.flatMapLatest { customerState ->
                if (customerState.isSuccess()) {
                    val productIds =
                        customerState.getSuccessData().cart.map { it.productId }.toSet()
                    if (productIds.isNotEmpty()) {
                        productRepository.readProductsByIdsFlow(productIds.toList())
                    } else {
                        flowOf(RequestState.Success(emptyList()))
                    }
                } else if (customerState.isError()) {
                    flowOf(RequestState.Error(customerState.getErrorMessage()))
                } else {
                    flowOf(RequestState.Loading)
                }
            }

            combine(customerFlow, productsFlow) { customerState, productsState ->
                handleDataCombine(customerState, productsState)
            }.collect()
        }
    }

    private fun handleDataCombine(
        customerState: RequestState<Customer>,
        productsState: RequestState<List<Product>>
    ) {
        if (customerState.isError()) {
            requestState = RequestState.Error(customerState.getErrorMessage())
            return
        }
        if (productsState.isError()) {
            requestState = RequestState.Error(productsState.getErrorMessage())
            return
        }
        if (!customerState.isSuccess() || !productsState.isSuccess()) {
            requestState = RequestState.Loading
            return
        }

        val customer = customerState.getSuccessData()
        val products = productsState.getSuccessData()

        if (screenState.email.isBlank()) {
            screenState = CheckoutScreenState(
                id = customer.id,
                firstName = customer.firstName,
                lastName = customer.lastName,
                email = customer.email,
                city = customer.city,
                postalCode = customer.postalCode,
                address = customer.address,
                phoneNumber = customer.phoneNumber,
                country = Country.entries.firstOrNull { it.dialCode == customer.phoneNumber?.dialCode }
                    ?: Country.Taiwan,
                cart = customer.cart
            )
        }

        val uiModels = customer.cart.mapNotNull { cartItem ->
            val product = products.find { it.id == cartItem.productId }

            product?.let {
                CartItemUiModel(
                    id = it.id,
                    productId = it.id,
                    productTitle = it.title,
                    thumbnail = it.thumbnail,
                    price = cartItem.price,
                    weight = cartItem.weight,
                    flavor = cartItem.flavor,
                    quantity = cartItem.quantity
                )
            }
        }

        requestState = RequestState.Success(uiModels)
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