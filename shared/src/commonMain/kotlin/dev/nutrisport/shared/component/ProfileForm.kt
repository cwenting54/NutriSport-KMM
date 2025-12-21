package dev.nutrisport.shared.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.component.dialog.CountryPickerDialog
import dev.nutrisport.shared.domain.Country

@Composable
fun ProfileForm(
    modifier: Modifier = Modifier,
    country: Country,
    onCountrySelect: (Country) -> Unit,
    firstName: String = "",
    onFirstNameChange: (String) -> Unit = {},
    lastName: String = "",
    onLastNameChange: (String) -> Unit = {},
    email: String = "",
    city: String?,
    onCityChange: (String) -> Unit = {},
    postalCode: Int?,
    onPostalCodeChange: (Int?) -> Unit = {},
    address: String?,
    onAddressChange: (String) -> Unit = {},
    phoneNumber: String?,
    onPhoneNumberChange: (String) -> Unit = {},
) {

    var showCountryDialog by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = showCountryDialog
    ) {
        CountryPickerDialog(
            country = country,
            onDismiss = { showCountryDialog = false },
            onConfirmClick = { selectCountry ->
                onCountrySelect(selectCountry)
                showCountryDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CustomTextField(
            title = "名字",
            value = firstName,
            onValueChange = onFirstNameChange,
            placeholder = "名字",
            error = firstName.length !in 3..50,
        )
        CustomTextField(
            title = "姓氏",
            value = lastName,
            onValueChange = onLastNameChange,
            placeholder = "姓氏",
            error = lastName.length !in 3..50,
        )

        CustomTextField(
            title = "電子信箱",
            value = email,
            onValueChange = {},
            placeholder = "Email",
            enabled = false,
        )
        CustomTextField(
            title = "城市",
            value = city ?: "",
            onValueChange = onCityChange,
            placeholder = "城市",
            error = city?.length !in 3..50,
        )
        CustomTextField(
            title = "郵遞區號",
            value = postalCode?.toString() ?: "",
            onValueChange = { onPostalCodeChange(it.toIntOrNull()) },
            placeholder = "郵遞區號",
            error = postalCode == null || postalCode.toString().length !in 3..8,
            keyboardOption = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
        CustomTextField(
            title = "地址",
            value = address ?: "",
            onValueChange = onAddressChange,
            placeholder = "地址",
            error = address?.length !in 3..50,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlertTextField(
                title = "",
                text = "+${country.dialCode}",
                icon = country.flag,
                onClick = {
                    showCountryDialog = true
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            CustomTextField(
                title = "號碼",
                value = phoneNumber ?: "",
                onValueChange = onPhoneNumberChange,
                placeholder = "Phone Number",
                error = phoneNumber.toString().length !in 5..30,
            )
        }

    }
}