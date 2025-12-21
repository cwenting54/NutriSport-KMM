package dev.nutrisport.shared.util

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.observable.makeObservable
import dev.nutrisport.shared.domain.ConsigneeInfo
import dev.nutrisport.shared.domain.PhoneNumber
import dev.nutrisport.shared.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(ExperimentalSettingsApi::class)
object PreferencesRepository {
    private val settings: ObservableSettings = Settings().makeObservable()

    private const val IS_SUCCESS = "isSuccess_paypal"
    private const val ERROR = "error_paypal"
    private const val TOKEN = "token_paypal"

    fun savePayPalData(
        isSuccess: Boolean?,
        error: String?,
        token: String?,
    ) {
        isSuccess?.let { settings.putBoolean(IS_SUCCESS, it) }
        error?.let { settings.putString(ERROR, it) }
        token?.let { settings.putString(TOKEN, it) }
    }

    fun readPayPalDataFlow(): Flow<Screen.PaymentComplete?> = callbackFlow {
        while (true) {
            val paymentProcessed = Screen.PaymentComplete(
                isSuccess = settings.getBooleanOrNull(IS_SUCCESS),
                error = settings.getStringOrNull(ERROR),
                token = settings.getStringOrNull(TOKEN)
            )
            send(paymentProcessed)
            delay(1000) // Check for updates every second
        }
    }

    fun reset() {
        settings.remove(IS_SUCCESS)
        settings.remove(ERROR)
        settings.remove(TOKEN)
    }

    private const val CONSIGNEE_NAME = "consignee_name"
    private const val CONSIGNEE_DIAL_CODE = "consignee_dial_code"
    private const val CONSIGNEE_PHONE = "consignee_phone"
    private const val CONSIGNEE_CITY = "consignee_city"
    private const val CONSIGNEE_POSTAL_CODE = "consignee_postal_code"
    private const val CONSIGNEE_ADDRESS = "consignee_address"

    fun saveConsigneeInfo(
        name: String,
        dialCode: Int,
        phone: String,
        city: String,
        postalCode: String,
        address: String
    ) {
        settings.putString(CONSIGNEE_NAME, name)
        settings.putInt(CONSIGNEE_DIAL_CODE, dialCode)
        settings.putString(CONSIGNEE_PHONE, phone)
        settings.putString(CONSIGNEE_CITY, city)
        settings.putString(CONSIGNEE_POSTAL_CODE, postalCode)
        settings.putString(CONSIGNEE_ADDRESS, address)
    }

    fun readConsigneeInfo(): ConsigneeInfo {
        return ConsigneeInfo(
            name = settings.getString(CONSIGNEE_NAME, ""),
            phone = PhoneNumber(settings.getInt(CONSIGNEE_DIAL_CODE, 886), settings.getString(CONSIGNEE_PHONE, "")),
            city = settings.getString(CONSIGNEE_CITY, ""),
            postalCode = settings.getString(CONSIGNEE_POSTAL_CODE, ""),
            address = settings.getString(CONSIGNEE_ADDRESS, "")
        )
    }
}

