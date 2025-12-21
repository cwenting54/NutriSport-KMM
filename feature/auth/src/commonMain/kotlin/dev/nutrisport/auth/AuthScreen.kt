package dev.nutrisport.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import dev.nutrisport.auth.component.GoogleButton
import dev.nutrisport.shared.Alpha
import dev.nutrisport.shared.BebasNeueFont
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    navigateToHome: () -> Unit = { }
) {
    val viewModel = koinViewModel<AuthViewModel>()
    var loadingState by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "NUTRISPORT",
                    textAlign = TextAlign.Center,
                    fontFamily = BebasNeueFont(),
                    fontSize = FontSize.EXTRA_LARGE,
                    color = TextSecondary
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(Alpha.HALF),
                    text = "Sign in to continue",
                    textAlign = TextAlign.Center,
                    fontSize = FontSize.EXTRA_REGULAR,
                    color = TextPrimary
                )
            }
            GoogleButtonUiContainerFirebase(
                linkAccount = false,
                onResult = { result ->
                    result.onSuccess { user ->

                        viewModel.createCustomer(
                            user = user,
                            onSuccess = navigateToHome,
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error, duration = SnackbarDuration.Short)
                                }
                            }
                        )
                        loadingState = false
                    }.onFailure { error ->
                        scope.launch {
                            if (error.message?.contains("A network error") == true) {
                                snackbarHostState.showSnackbar("Internet connection unavailable.", duration = SnackbarDuration.Short)
                            } else if (error.message?.contains("Idtoken is null") == true) {
                                snackbarHostState.showSnackbar("Sing in cancelled.", duration = SnackbarDuration.Short)
                            } else {
                                snackbarHostState.showSnackbar(error.message ?: "Unknown error", duration = SnackbarDuration.Short)
                            }
                        }
                        loadingState = false
                    }

                }
            ) {
                GoogleButton(
                    loading = loadingState,
                    onClick = {
                        loadingState = true
                        this@GoogleButtonUiContainerFirebase.onClick()
                    }
                )
            }
        }

    }
}
