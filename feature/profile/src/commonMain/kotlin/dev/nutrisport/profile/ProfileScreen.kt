package dev.nutrisport.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.component.PrimaryButton
import dev.nutrisport.shared.component.ProfileForm
import dev.nutrisport.shared.util.DisplayResult
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val screenReady = viewModel.screenReady
    val screenState = viewModel.screenState
    val isFormValid = viewModel.isFormValid

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            snackbarHostState.currentSnackbarData?.dismiss()
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "個人資料",
                        fontSize = FontSize.EXTRA_MEDIUM,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = "Close icon",
                            tint = IconPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Surface,
                    scrolledContainerColor = Surface,
                    navigationIconContentColor = IconPrimary,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = IconPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 24.dp)
                .imePadding()
        ) {
            screenReady.DisplayResult(
                onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
                onSuccess = {
                    Column(
                        modifier = Modifier.fillMaxSize()
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
                        Spacer(modifier = Modifier.height(12.dp))
                        PrimaryButton(
                            text = "更新",
                            icon = Resources.Icon.Checkmark,
                            enabled = isFormValid,
                            onClick = {
                                viewModel.updateCustomer(
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Successfully updated!")
                                        }
                                    },
                                    onError = { message ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    }
                                )
                            }
                        )
                    }
                },
                onError = { message ->
                    InfoCard(
                        image = Resources.Image.Cat,
                        title = "Oops!",
                        subtitle = message
                    )

                }
            )

        }
    }


}