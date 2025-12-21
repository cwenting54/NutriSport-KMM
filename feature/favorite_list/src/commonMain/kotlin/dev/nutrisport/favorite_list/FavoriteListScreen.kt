package dev.nutrisport.favorite_list

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.SurfaceLighter
import dev.nutrisport.shared.component.AnimatedSearchTopBar
import dev.nutrisport.shared.component.Chip
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.component.ProductFavorCard
import dev.nutrisport.shared.domain.ProductCategory
import dev.nutrisport.shared.util.DisplayResult
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoriteListScreen(
    navigateBack: () -> Unit,
    navigateToDetails: (String) -> Unit
) {
    val viewModel = koinViewModel<FavoriteListViewModel>()
    val favoriteProducts by viewModel.favoriteList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var searchBarVisible by mutableStateOf(false)

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
        containerColor = SurfaceLighter,
        topBar = {
            AnimatedSearchTopBar(
                title = "按讚好物",
                searchQuery = searchQuery,
                searchBarVisible = searchBarVisible,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSearchVisibilityChange = { searchBarVisible = it },
                onNavigateBack = navigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            CategoryFilter(
                items = ProductCategory.entries,
                selectedCategory = selectedCategory,
                onChipClick = viewModel::updateSelectedCategory
            )
            Spacer(modifier = Modifier.height(8.dp))
            favoriteProducts.DisplayResult(
                onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
                onSuccess = { productList ->

                    AnimatedContent(
                        targetState = productList.distinctBy { it.id }
                    ) { products ->

                        if (productList.isNotEmpty()) {
                            LazyVerticalGrid(
                                modifier = Modifier
                                    .fillMaxSize(),
                                columns = GridCells.Adaptive(minSize = 160.dp),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 16.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(productList, key = { it.id }) { product ->
                                    ProductFavorCard(
                                        modifier = Modifier,
                                        product = product,
                                        isShowFavoriteIcon = true,
                                        isFavorite = true,
                                        onNavigateToDetails = navigateToDetails,
                                        onFavoriteClick = { id ->
                                            viewModel.updateFavoriteProduct(
                                                productId = id,
                                                onSuccess = {},
                                                onError = {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "收藏失敗，請稍後再試"
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        } else {
                            InfoCard(
                                image = Resources.Image.Cat,
                                title = "你尚未收藏任何商品",
                                subtitle = ""
                            )
                        }

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

@Composable
fun CategoryFilter(
    items: List<ProductCategory>,
    selectedCategory: ProductCategory?,
    modifier: Modifier = Modifier,
    onChipClick: (ProductCategory) -> Unit = {}
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(items) { label ->
            Chip(
                text = label.title,
                isSelected = selectedCategory?.let { it == label } ?: false ,
                onClick = { onChipClick(label) }
            )
        }
    }
}
