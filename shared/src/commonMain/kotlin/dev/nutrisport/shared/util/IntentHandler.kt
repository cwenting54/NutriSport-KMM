package dev.nutrisport.shared.util

import dev.nutrisport.shared.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class IntentHandler {
    private val _navigateTo = MutableStateFlow<Screen?>(null)
    val navigateTo: StateFlow<Screen?> = _navigateTo.asStateFlow()

    fun navigateToPaymentCompleted(
        isSuccess: Boolean?,
        error: String?,
        token: String? = null
    ) {
        _navigateTo.value = Screen.PaymentComplete(
            isSuccess = isSuccess,
            error = error,
            token =token
        )
    }

    fun resetNavigation() {
        _navigateTo.value = null
    }
}

class IntentHandlerHelper: KoinComponent {
   private val intentHandler: IntentHandler by inject()
//
//    fun navigateToPaymentCompleted(
//        isSuccess: Boolean?,
//        error: String?,
//        token: String? = null
//    ) {
//        intentHandler.navigateToPaymentCompleted(
//            isSuccess = isSuccess,
//            error = error,
//            token =token
//        )
//    }

}