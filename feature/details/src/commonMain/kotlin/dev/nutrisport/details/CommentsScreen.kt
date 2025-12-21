package dev.nutrisport.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nutrisport.details.component.CommentsList
import dev.nutrisport.details.domain.CommentFilter
import dev.nutrisport.shared.BebasNeueFont
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.IconSecondary
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.TextSecondary
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.domain.CommentItem
import dev.nutrisport.shared.util.DisplayResult
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun CommentsScreen(
    navigateBack: () -> Unit,
    viewModel: DetailsViewModel
) {
    val filteredComments by viewModel.filteredComments.collectAsStateWithLifecycle()
    val commentFilter by viewModel.commentFilter.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
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
                        text = "商品評價",
                        fontFamily = BebasNeueFont(),
                        fontSize = FontSize.EXTRA_MEDIUM,
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
                actions = {
                    Box {
                        Icon(
                            painter = painterResource(Resources.Icon.Filter),
                            contentDescription = "Filter icon",
                            tint = IconSecondary,
                            modifier = Modifier
                                .clickable { expanded = true }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            containerColor = Surface,
                            tonalElevation = 4.dp
                        ) {
                            CommentFilter.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.filter,
                                            color = if (option == commentFilter)
                                                TextSecondary
                                            else
                                                TextPrimary
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateFilter(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Surface,
                    scrolledContainerColor = Surface,
                    navigationIconContentColor = IconPrimary,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = IconPrimary
                )
            )
        },
    ) { innerPadding ->
        filteredComments.DisplayResult(
            transitionSpec = null,
            onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
            onSuccess = { commentsList ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    CommentsList(
                        comments = commentsList,
                        onLikeClick = { commentId ->
                            viewModel.likeComment(
                                commentId = commentId,
                                onError = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "更新失敗，請稍後再試"
                                        )
                                    }
                                }
                            )
                        }
                    )
                }
            },
            onError = { message ->
                InfoCard(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    image = Resources.Image.Cat,
                    title = "Oops!",
                    subtitle = message
                )
            }
        )

    }
}