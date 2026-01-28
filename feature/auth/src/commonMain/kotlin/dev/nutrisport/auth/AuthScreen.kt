package dev.nutrisport.auth

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    navigateToHome: () -> Unit = { }
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMsg by viewModel.snackbarMsg.collectAsState()

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.onSnackbarShown()
        }
    }

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
                            onSuccess = navigateToHome
                        )
                    }.onFailure { error ->
                        viewModel.handleSignInError(error)
                    }

                }
            ) {
                GoogleButton(
                    loading = isLoading,
                    onClick = {
                        this@GoogleButtonUiContainerFirebase.onClick()
                    }
                )
            }
        }

    }
}
