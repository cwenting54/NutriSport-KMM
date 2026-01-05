package dev.nutrisport.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.nutrisport.details.component.CommentsList
import dev.nutrisport.details.component.FlavorChip
import dev.nutrisport.shared.Alpha
import dev.nutrisport.shared.BebasNeueFont
import dev.nutrisport.shared.BorderIdle
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.IconSecondary
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.RobotoCondensedFont
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.SurfaceLighter
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.TextSecondary
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.component.PrimaryButton
import dev.nutrisport.shared.component.ProductFavorCard
import dev.nutrisport.shared.component.QuantityCounter
import dev.nutrisport.shared.domain.CommentItem
import dev.nutrisport.shared.domain.Product
import dev.nutrisport.shared.domain.ProductCategory
import dev.nutrisport.shared.domain.QuantityCounterSize
import dev.nutrisport.shared.util.DisplayResult
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.round
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun DetailsScreen(
    navigateBack: () -> Unit,
    navigateToDetails: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel = koinViewModel<DetailsViewModel>()
    val product by viewModel.product.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val similarProducts by viewModel.similarProducts.collectAsState()
    val selectedFlavor = viewModel.selectedFlavor
    val quantity = viewModel.quantity
    var showProductOptionBottomSheet by remember { mutableStateOf(false) }
    var showCommentScreen by remember { mutableStateOf(false) }
    val isFavorite by viewModel.isFavoriteProduct.collectAsState()

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize().zIndex(1f),
        visible = showCommentScreen,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        CommentsScreen(
            viewModel = viewModel,
            navigateBack = {
                showCommentScreen = false
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
            TopAppBar(
                title = {
                    Text(
                        text = "商品詳情",
                        fontFamily = BebasNeueFont(),
                        fontSize = FontSize.EXTRA_MEDIUM,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
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
                    AnimatedContent(
                        targetState = isFavorite,
                        label = "favoriteAnimation"
                    ) { isFav ->
                        when(isFav) {
                            is RequestState.Success -> {
                                val data = isFav.data
                                Icon(
                                    painter = painterResource(
                                        if (data) Resources.Icon.Favorite
                                        else Resources.Icon.OutlineFavorite
                                    ),
                                    contentDescription = "Favorite icon",
                                    tint = IconSecondary,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .clickable {
                                        viewModel.updateFavoriteProduct(
                                            onSuccess = {},
                                            onError = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("收藏失敗，請稍後再試")
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                            else -> {
                                Icon(
                                    painter = painterResource(Resources.Icon.OutlineFavorite),
                                    contentDescription = "Favorite icon",
                                    tint = IconSecondary,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .clickable {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "收藏失敗，請稍後再試"
                                            )
                                        }
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
        }
    ) { innerPadding ->

        product.DisplayResult(
            onLoading = {
                LoadingCard(modifier = Modifier.fillMaxSize())
            },
            onSuccess = { selectedProduct ->
                // 加入購物車的選單
                if (showProductOptionBottomSheet) {
                    ProductOptionBottomSheet(
                        selectedProduct = selectedProduct,
                        selectedFlavor = selectedFlavor,
                        quantity = quantity,
                        onFlavorSelected = viewModel::updateFlavor,
                        onQuantityChange = viewModel::updateQuantity,
                        onAddToCart = {
                            viewModel.addItemToCart(
                                product = selectedProduct,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(message = "成功加入購物車！")
                                    }
                                },
                                onError = { message ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(message)
                                    }
                                }
                            )
                            showProductOptionBottomSheet = false
                        },
                        onDismiss = { showProductOptionBottomSheet = false }
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                            .padding(top = 22.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(size = 12.dp))
                                .border(
                                    width = 1.dp,
                                    color = BorderIdle,
                                    shape = RoundedCornerShape(size = 12.dp)
                                ),
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(selectedProduct.thumbnail)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Product thumbnail",
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AnimatedContent(
                                targetState = selectedProduct.category
                            ) { category ->
                                if (ProductCategory.valueOf(category) == ProductCategory.Accessories) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(14.dp),
                                            painter = painterResource(Resources.Icon.Weight),
                                            contentDescription = "Weight icon"
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${selectedProduct.weight} g",
                                            fontSize = FontSize.REGULAR,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "$${selectedProduct.price}",
                                fontSize = FontSize.MEDIUM,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = selectedProduct.title,
                            fontSize = FontSize.EXTRA_MEDIUM,
                            fontWeight = FontWeight.Medium,
                            fontFamily = RobotoCondensedFont(),
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = selectedProduct.description,
                            fontSize = FontSize.REGULAR,
                            lineHeight = FontSize.REGULAR * 1.3,
                            color = TextPrimary
                        )
                        // TODO 評價
                        comments.DisplayResult(
                            transitionSpec = null,
                            onLoading = {},
                            onSuccess = { commentsList ->
                                Column {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = commentsList.calculateAverageRating().toString(),
                                                fontSize = FontSize.EXTRA_MEDIUM,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                            )
                                            Icon(
                                                painter = painterResource(Resources.Icon.FilledStar),
                                                contentDescription = "Star icon",
                                                tint = IconSecondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "評價 (${commentsList.size})",
                                                fontSize = FontSize.EXTRA_REGULAR,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { showCommentScreen = true }
                                        ) {
                                            Text(
                                                text = "查看更多",
                                                fontSize = FontSize.REGULAR,
                                                color = TextPrimary,
                                                modifier = Modifier.alpha(Alpha.HALF)
                                            )
                                            Icon(
                                                painter = painterResource(Resources.Icon.ForwardArrow),
                                                contentDescription = "Star icon",
                                                tint = IconPrimary,
                                                modifier = Modifier.size(16.dp).alpha(Alpha.HALF)
                                            )
                                        }

                                    }
                                    CommentsList(
                                        comments = commentsList.take(3),
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
                            onError = {}
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        similarProducts.DisplayResult(
                            onLoading = {},
                            onSuccess = { similarProductList ->
                                // 相似商品
                                if (similarProductList.isNotEmpty())
                                SimilarProductDisplayRow(
                                    title = "相似商品",
                                    products = similarProductList,
                                    navigateToDetails = navigateToDetails,
                                )
                            },
                            onError = {}
                        )

                    }
                    Column(
                        modifier = Modifier
                            .background(
                                if (selectedProduct.flavors?.isNotEmpty() == true) SurfaceLighter
                                else Surface
                            )
                            .padding(24.dp)
                    ) {

                        PrimaryButton(
                            icon = Resources.Image.ShoppingCart,
                            text = "加入購物車",
                            enabled = true,
                            onClick = {
                                showProductOptionBottomSheet = true
                            }
                        )
                    }
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


@Composable
fun SimilarProductDisplayRow(
    title: String,
    products: List<Product>,
    navigateToDetails: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = FontSize.EXTRA_MEDIUM,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(
                items = products.filter { it.isNew }
                    .sortedBy { it.createdAt }
                    .take(6),
                key = { item -> item.id }
            ) { product ->
                ProductFavorCard(
                    modifier = Modifier
                        .width(163.dp),
                    product = product,
                    onNavigateToDetails = { navigateToDetails(product.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductOptionBottomSheet(
    modifier: Modifier = Modifier,
    selectedProduct: Product,
    selectedFlavor: String?,
    onFlavorSelected: (String) -> Unit = {},
    quantity: Int = 1,
    onQuantityChange: (Int) -> Unit = {},
    onAddToCart: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (selectedProduct.flavors
                    ?.any { it.isNotBlank() } == true) {
                Text("口味", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    selectedProduct.flavors?.forEach { flavor ->
                        FlavorChip(
                            flavor = flavor,
                            isSelected = selectedFlavor == flavor,
                            onSelectFlavor = onFlavorSelected
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("數量", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                QuantityCounter(
                    size = QuantityCounterSize.Large,
                    value = quantity,
                    onMinusClick = onQuantityChange,
                    onPlusClick = onQuantityChange
                )
            }

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                icon = Resources.Image.ShoppingCart,
                text = "加入購物車",
                enabled = if (selectedProduct.flavors
                        ?.any { it.isNotBlank() } == true)
                    selectedFlavor != null
                else true,
                onClick = {
                    onAddToCart()
                }
            )
        }
    }
}

fun List<CommentItem>.calculateAverageRating(): Double {
    if (isEmpty()) return 0.0

    val validRatings = this.map { it.rating }.filter { it > 0 }
    if (validRatings.isEmpty()) return 0.0

    val avg = validRatings.sum().toDouble() / validRatings.size

    // 四捨五入到小數點一位
    return (round(avg * 10) / 10)
}
