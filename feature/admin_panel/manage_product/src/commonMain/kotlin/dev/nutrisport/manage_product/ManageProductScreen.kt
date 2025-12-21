package dev.nutrisport.manage_product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.nutrisport.manage_product.util.PhotoPicker
import dev.nutrisport.shared.BebasNeueFont
import dev.nutrisport.shared.BorderIdle
import dev.nutrisport.shared.BtnPrimary
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.SurfaceDarker
import dev.nutrisport.shared.SurfaceLighter
import dev.nutrisport.shared.SurfaceSecondary
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.TextSecondary
import dev.nutrisport.shared.component.AlertTextField
import dev.nutrisport.shared.component.CustomTextField
import dev.nutrisport.shared.component.ErrorCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.component.PrimaryButton
import dev.nutrisport.shared.component.dialog.CategoriesDialog
import dev.nutrisport.shared.domain.ProductCategory
import dev.nutrisport.shared.util.DisplayResult
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductScreen(
    id: String?,
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<ManageProductViewModel>()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showCategoriesDialog by remember { mutableStateOf(false) }
    var dropdownMenuOpened by remember { mutableStateOf(false) }

    val screenState = viewModel.screenState
    val isFormValid = viewModel.isFormValid
    val thumbnailUploaderState = viewModel.thumbnailUploaderState

    val photoPicker = koinInject<PhotoPicker>()

    photoPicker.InitializePhotoPicker(
        onImageSelect = { file ->
            viewModel.uploadThumbnailToStorage(
                file = file,
                onSuccess = {
                    scope.launch {
                        snackbarHostState.showSnackbar(message = "Thumbnail uploaded successfully!")
                    }
                },
            )

        }
    )

    AnimatedVisibility(
        visible = showCategoriesDialog
    ) {
        CategoriesDialog(
            category = screenState.category,
            onDismiss = { showCategoriesDialog = false },
            onConfirmClick = { selectedCategory ->
                viewModel.updateCategory(selectedCategory)
                showCategoriesDialog = false
            }
        )
    }

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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (id == null) "New Product"
                        else "Edit Product",
                        fontFamily = BebasNeueFont(),
                        fontSize = FontSize.LARGE,
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
                    id.takeIf { it != null }?.let {
                        Box {
                            IconButton(onClick = { dropdownMenuOpened = true }) {
                                Icon(
                                    painter = painterResource(Resources.Icon.VerticalMenu),
                                    contentDescription = "Vertical menu icon",
                                    tint = IconPrimary
                                )
                            }
                            DropdownMenu(
                                containerColor = Surface,
                                expanded = dropdownMenuOpened,
                                onDismissRequest = { dropdownMenuOpened = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            modifier = Modifier.size(14.dp),
                                            painter = painterResource(Resources.Icon.Delete),
                                            contentDescription = "Delete icon",
                                            tint = IconPrimary
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = "Delete",
                                            color = TextPrimary,
                                            fontSize = FontSize.REGULAR
                                        )
                                    },
                                    onClick = {
                                        dropdownMenuOpened = false
                                        viewModel.deleteProduct(
                                            onSuccess = navigateBack,
                                            onError = { message ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(message)
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
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
        },

        ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .padding(horizontal = 24.dp)
                .padding(
                    top = 12.dp,
                    bottom = 24.dp
                )
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "商品照片",
                        fontSize = FontSize.REGULAR,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 14.dp)
                    )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = BorderIdle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(SurfaceLighter)
                        .clickable(
                            enabled = thumbnailUploaderState.isIdle()
                        ) {
                            photoPicker.open()
                        },
                    contentAlignment = Alignment.Center
                ) {

                        thumbnailUploaderState.DisplayResult(
                            onIdle = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(Resources.Icon.Plus),
                                    contentDescription = "Plus icon",
                                    tint = IconPrimary
                                )
                            },
                            onLoading = {
                                LoadingCard(modifier = Modifier.fillMaxSize())
                            },
                            onSuccess = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    AsyncImage(
                                        modifier = Modifier.fillMaxSize(),
                                        model = ImageRequest.Builder(
                                            LocalPlatformContext.current
                                        ).data(screenState.thumbnail)
                                            .crossfade(enable = true)
                                            .build(),
                                        contentDescription = "Product thumbnail image",
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .padding(top = 12.dp, end = 12.dp)
                                            .background(BtnPrimary)
                                            .clickable {
                                                viewModel.deleteThumbnailFromStorage(
                                                    onSuccess = {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar("Thumbnail removed successfully.")
                                                        }
                                                    },
                                                    onError = { message ->
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(message)
                                                        }
                                                    }
                                                )
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(14.dp),
                                            painter = painterResource(Resources.Icon.Delete),
                                            contentDescription = "Delete icon"
                                        )
                                    }
                                }
                            },
                            onError = { message ->
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    ErrorCard(message = message)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TextButton(
                                        onClick = {
                                            viewModel.updateThumbnailUploaderState(RequestState.Idle)
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = TextSecondary
                                        )
                                    ) {
                                        Text(
                                            text = "Try again",
                                            fontSize = FontSize.SMALL,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        )
                    }


                }
                CustomTextField(
                    title = "標題",
                    value = screenState.title,
                    onValueChange = viewModel::updateTitle,
                    placeholder = "標題"
                )
                CustomTextField(
                    title = "",
                    value = screenState.description,
                    onValueChange = viewModel::updateDescription,
                    placeholder = "商品內容描述",
                    expanded = true,
                    modifier = Modifier.height(120.dp)
                )
                AlertTextField(
                    modifier = Modifier.fillMaxWidth(),
                    title = "",
                    text = screenState.category.title,
                    onClick = {
                        showCategoriesDialog = true
                    }
                )
                AnimatedVisibility(
                    visible = screenState.category != ProductCategory.Accessories
                ) {
                    Column {
                        CustomTextField(
                            title = "重量",
                            value = "${screenState.weight ?: ""}",
                            onValueChange = { viewModel.updateWeight(it.toIntOrNull() ?: 0) },
                            placeholder = "重量 (g)",
                            keyboardOption = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CustomTextField(
                            title = "口味",
                            value = screenState.flavors,
                            onValueChange = viewModel::updateFlavors,
                            placeholder = "口味"
                        )
                    }
                }
                CustomTextField(
                    title = "價格",
                    value = screenState.price?.toString() ?: "",
                    onValueChange = { value ->
                        if (value.isEmpty() || value.toDoubleOrNull() != null) {
                            viewModel.updatePrice(value.toDoubleOrNull() ?: 0.0)
                        }
                    },
                    placeholder = "價格"
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 12.dp),
                            text = "是否新上架",
                            fontSize = FontSize.REGULAR,
                            color = TextPrimary
                        )
                        Switch(
                            checked = screenState.isNew,
                            onCheckedChange = viewModel::updateNew,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = SurfaceSecondary,
                                uncheckedTrackColor = SurfaceDarker,
                                checkedThumbColor = Surface,
                                uncheckedThumbColor = Surface,
                                checkedBorderColor = SurfaceSecondary,
                                uncheckedBorderColor = SurfaceDarker
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 12.dp),
                            text = "是否熱銷",
                            fontSize = FontSize.REGULAR,
                            color = TextPrimary
                        )
                        Switch(
                            checked = screenState.isPopular,
                            onCheckedChange = viewModel::updatePopular,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = SurfaceSecondary,
                                uncheckedTrackColor = SurfaceDarker,
                                checkedThumbColor = Surface,
                                uncheckedThumbColor = Surface,
                                checkedBorderColor = SurfaceSecondary,
                                uncheckedBorderColor = SurfaceDarker
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 12.dp),
                            text = "是否有折扣",
                            fontSize = FontSize.REGULAR,
                            color = TextPrimary
                        )
                        Switch(
                            checked = screenState.isDiscounted,
                            onCheckedChange = viewModel::updateDiscounted,
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = SurfaceSecondary,
                                uncheckedTrackColor = SurfaceDarker,
                                checkedThumbColor = Surface,
                                uncheckedThumbColor = Surface,
                                checkedBorderColor = SurfaceSecondary,
                                uncheckedBorderColor = SurfaceDarker
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            PrimaryButton(
                text = if (id == null) "Add new product" else "Update",
                icon = if (id == null) Resources.Icon.Plus else Resources.Icon.Checkmark,
                enabled = isFormValid,
                onClick = {
                    if (id != null) {
                        viewModel.updateProduct(
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = "Product successfully updated!")
                                }
                            },
                            onError = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = message)
                                }
                            }
                        )
                    } else {
                        viewModel.createNewProduct(
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = "Product successfully added!")
                                }
                            },
                            onError = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = message)
                                }
                            }
                        )
                    }
                }
            )

        }
    }
}