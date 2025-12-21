package dev.nutrisport.products_more

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.component.AnimatedSearchTopBar
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.component.ProductFavorCard
import dev.nutrisport.shared.domain.ProductType
import dev.nutrisport.shared.util.DisplayResult
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductMoreScreen(
    productType: String = ProductType.Discounted.title,
    navigateBack: () -> Unit,
    navigateToDetails: (String) -> Unit
) {

    val viewModel = koinViewModel<ProductMoreViewModel>()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var searchBarVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Surface,
        topBar = {
            AnimatedSearchTopBar(
                title = productType,
                searchQuery = searchQuery,
                searchBarVisible = searchBarVisible,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSearchVisibilityChange = { searchBarVisible = it },
                onNavigateBack = navigateBack
            )
        }
    ) { innerPadding ->
        products.DisplayResult(
            onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
            onSuccess = { productList ->
                Column(
                    modifier = Modifier
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                ) {
                    AnimatedContent(
                        targetState = productList.distinctBy { it.id }
                    ) { products ->
                        if (products.isNotEmpty()) {
                            LazyVerticalGrid(
                                modifier = Modifier
                                    .padding(12.dp),
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = products
                                        .filter { it.isDiscounted }
                                        .sortedBy { it.createdAt },
                                    key = { it.id }
                                ) { product ->
                                    ProductFavorCard(
                                        product = product,
                                        onNavigateToDetails = { navigateToDetails(product.id) }
                                    )
                                }
                            }
                        } else {
                            InfoCard(
                                image = Resources.Image.Cat,
                                title = "Nothing here",
                                subtitle = "Empty product list."
                            )
                        }

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