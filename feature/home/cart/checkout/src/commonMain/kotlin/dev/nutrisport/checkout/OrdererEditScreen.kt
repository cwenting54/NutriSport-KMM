package dev.nutrisport.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nutrisport.shared.BebasNeueFont
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.component.PrimaryButton
import dev.nutrisport.shared.component.ProfileForm
import dev.nutrisport.shared.util.RequestState
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdererEditScreen(
    navigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<CheckoutViewModel>()
    val requestState = viewModel.requestState
    val screenState = viewModel.screenState
    val isFormValid = viewModel.isFormValid


    Scaffold(
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "收件資訊編輯",
                        fontFamily = BebasNeueFont(),
                        fontSize = FontSize.EXTRA_MEDIUM,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = "Back arrow icon",
                            tint = IconPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    scrolledContainerColor = Surface,
                    navigationIconContentColor = IconPrimary,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = IconPrimary
                )
            )
        },
        bottomBar = {
            Row (
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryButton(
                    text = "完成",
                    enabled = isFormValid,
                    onClick = navigateBack
                )
            }
        }
    ) { padding ->
        when(requestState) {
            RequestState.Loading -> {
                LoadingCard(modifier = Modifier.fillMaxSize())
            }
            is RequestState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        )
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileForm(
                        modifier = Modifier.weight(1f),
                        country = screenState.country,
                        onCountrySelect = viewModel::updateCountry,
                        firstName = screenState.firstName,
                        onFirstNameChange = viewModel::updateFirstName,
                        lastName = screenState.lastName,
                        onLastNameChange = viewModel::updateLastName,
                        email = screenState.email,
                        city = screenState.city,
                        onCityChange = viewModel::updateCity,
                        postalCode = screenState.postalCode,
                        onPostalCodeChange = viewModel::updatePostalCode,
                        address = screenState.address,
                        onAddressChange = viewModel::updateAddress,
                        phoneNumber = screenState.phoneNumber?.number,
                        onPhoneNumberChange = viewModel::updatePhoneNumber,
                    )
                }
            }
            is RequestState.Error -> {
                InfoCard(
                    image = Resources.Image.Cat,
                    title = "Oops!",
                    subtitle = requestState.message
                )
            }
            RequestState.Idle -> {}
        }

    }
}