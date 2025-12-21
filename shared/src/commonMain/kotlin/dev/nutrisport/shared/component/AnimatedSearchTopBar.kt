package dev.nutrisport.shared.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nutrisport.shared.BebasNeueFont
import dev.nutrisport.shared.BorderIdle
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.SurfaceLighter
import dev.nutrisport.shared.TextPrimary
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedSearchTopBar(
    modifier: Modifier = Modifier,
    title: String,
    searchQuery: String,
    searchBarVisible: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchVisibilityChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
) {
    AnimatedContent(
        targetState = searchBarVisible,
        label = "AnimatedSearchTopBarTransition"
    ) { visible ->
        if (visible) {
            SearchBar(
                modifier = modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
                inputField = {
                    SearchBarDefaults.InputField(
                        modifier = Modifier.fillMaxWidth(),
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        expanded = false,
                        onExpandedChange = {},
                        onSearch = {},
                        placeholder = {
                            Text(
                                text = "Search here",
                                fontSize = FontSize.EXTRA_REGULAR,
                                color = TextPrimary
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                modifier = Modifier.size(20.dp),
                                onClick = {
                                    if (searchQuery.isNotEmpty()) onSearchQueryChange("")
                                    else onSearchVisibilityChange(false)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(Resources.Icon.Close),
                                    contentDescription = "Close icon"
                                )
                            }
                        }
                    )
                },
                colors = SearchBarColors(
                    containerColor = SurfaceLighter,
                    dividerColor = BorderIdle
                ),
                expanded = false,
                onExpandedChange = {},
                content = {}
            )
        } else {
            TopAppBar(
                modifier = modifier,
                title = {
                    Text(
                        text = title,
                        fontFamily = BebasNeueFont(),
                        fontSize = FontSize.EXTRA_MEDIUM,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = "Back Arrow icon",
                            tint = IconPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onSearchVisibilityChange(true) }) {
                        Icon(
                            painter = painterResource(Resources.Icon.Search),
                            contentDescription = "Search icon",
                            tint = IconPrimary
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
    }
}
