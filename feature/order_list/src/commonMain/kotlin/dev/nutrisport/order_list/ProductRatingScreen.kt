package dev.nutrisport.order_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.nutrisport.shared.BebasNeueFont
import dev.nutrisport.shared.BorderIdle
import dev.nutrisport.shared.FontSize
import dev.nutrisport.shared.IconPrimary
import dev.nutrisport.shared.Resources
import dev.nutrisport.shared.Surface
import dev.nutrisport.shared.SurfaceLighter
import dev.nutrisport.shared.TextPrimary
import dev.nutrisport.shared.component.CustomTextField
import dev.nutrisport.shared.component.InfoCard
import dev.nutrisport.shared.component.LoadingCard
import dev.nutrisport.shared.component.PrimaryButton
import dev.nutrisport.shared.component.dialog.AlertDialog
import dev.nutrisport.shared.domain.CommentUiModel
import dev.nutrisport.shared.util.DisplayResult
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductRatingScreen(
    navigateBack: () -> Unit = {},
    viewModel: OrderViewModel = koinViewModel()
) {

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val drafts by viewModel.commentDrafts.collectAsStateWithLifecycle()
    var editingCommentId by remember { mutableStateOf<String?>(null) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                snackbarHostState.currentSnackbarData?.dismiss()
            },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "評價商品",
                        fontFamily = BebasNeueFont(),
                        fontSize = FontSize.LARGE,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (editingCommentId != null) {
                            showLeaveDialog = true
                        } else {
                            navigateBack()
                        }
                    }) {
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
            val comments = (comments as? RequestState.Success)?.data
            AnimatedVisibility(
                visible = comments.isNullOrEmpty()
            ) {
                PrimaryButton(
                    text = "完成",
                    enabled = true,
                    onClick = {
                        viewModel.submitComments(
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("評價商品成功！")
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
        containerColor = SurfaceLighter,
    ) { innerPadding ->
        comments.DisplayResult(
            onLoading = {
                LoadingCard(modifier = Modifier.fillMaxSize())
            },
            onSuccess = { comments ->
                viewModel.initCommentDraftsFromOrder(comments)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(drafts, key = { it.productInfo.id }) { comment ->
                        val isEditing = editingCommentId == comment.id
                        ReviewCardItem(
                            comment = comment,
                            isEditing = isEditing,
                            isComments = comments.isNotEmpty(),
                            onEditClick = {
                                editingCommentId = comment.id
                            },
                            onDoneClick = {
                                editingCommentId = null
                                viewModel.updateComment(
                                    commentId = comment.id,
                                    onSuccess = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("更新成功！")
                                        }
                                    },
                                    onError = { message ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    }
                                )
                            },
                            onRatingChange = { newRate ->
                                viewModel.updateDraftRating(comment.productInfo.id, newRate)
                            },
                            onCommentChange = { newText ->
                                viewModel.updateDraftDescription(comment.productInfo.id, newText)
                            }
                        )
                    }
                }
            },
            onError = { message ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    InfoCard(
                        title = "Oops!",
                        subtitle = message,
                        image = Resources.Image.Cat
                    )
                }
            }
        )


        AnimatedVisibility(
            visible = showLeaveDialog
        ) {
            AlertDialog(
                title = "尚未儲存的變更",
                message = "目前的評論修改尚未儲存，確定要離開嗎？",
                onDismiss = {
                    showLeaveDialog = false
                },
                onConfirmClick = {
                    showLeaveDialog = false
                    navigateBack()
                }
            )
        }
    }
}

@Composable
fun ReviewCardItem(
    comment: CommentUiModel,
    isComments: Boolean = false,
    isEditing: Boolean = false,
    onEditClick: () -> Unit,
    onDoneClick: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var rating by remember(comment.productInfo.id) { mutableStateOf(comment.rate) }
    var commentText by remember(comment.productInfo.id) { mutableStateOf(comment.description.orEmpty()) }

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 上方：商品圖 + 名稱 + 編輯按鈕（僅在 isComments = true 顯示）
            ReviewProductHeader(
                productName = comment.productInfo.name,
                productImageUrl = comment.productInfo.thumbnail,
                isComments = isComments,
                isEditing = isEditing,
                onEditClick = onEditClick,
                onDoneClick = onDoneClick
            )

            Spacer(Modifier.height(8.dp))

            // ============================
            //  已有評論模式 (isComments = true)
            // ============================
            if (isComments && !isEditing) {

                Text(
                    text = "評價此商品",
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))

                RatingStars(
                    rating = comment.rate,
                    onRatingSelected = {},
                    enabled = false
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "評論此商品",
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = comment.description ?: "",
                    fontSize = 14.sp,
                    color = Color(0xFF444444),
                    lineHeight = 20.sp
                )
            } else {
                // ============================
                //  填寫評論模式 (isComments = false)
                // ============================

                Text(
                    text = "評價此商品",
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))

                RatingStars(
                    rating = rating,
                    onRatingSelected = { new ->
                        rating = new
                        onRatingChange(new)
                    },
                    enabled = true
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "評論此商品",
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))

                CustomTextField(
                    title = "",
                    value = commentText,
                    onValueChange = { new ->
                        commentText = new
                        onCommentChange(new)
                    },
                    placeholder = "分享您的購物體驗並幫助大家做出更好的選擇～",
                    expanded = true,
                    modifier = Modifier.height(120.dp)
                )
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun ReviewProductHeader(
    productName: String,
    productImageUrl: String,
    isComments: Boolean = false,
    isEditing: Boolean = false,
    onEditClick: () -> Unit,
    onDoneClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 商品圖
        AsyncImage(
            modifier = Modifier
                .size(50.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(size = 12.dp))
                .border(
                    width = 1.dp,
                    color = BorderIdle,
                    shape = RoundedCornerShape(size = 12.dp)
                ),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(productImageUrl)
                .crossfade(enable = true)
                .build(),
            contentDescription = "Product thumbnail image",
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = productName,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.width(8.dp))
        if (isComments) {
            //「編輯」按鈕
            EditOrDoneButton(
                isEditing = isEditing,
                onEditClick = onEditClick,
                onDoneClick = onDoneClick
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp).border(1.dp, BorderIdle))
}

@Composable
fun RatingStars(
    rating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    max: Int = 5,
    enabled: Boolean = true
) {
    Row(modifier = modifier) {
        for (i in 1..max) {
            val tint =
                if (i <= rating) Color(0xFFE8A144) else Color(0xFFDDDDDD)

            Icon(
                painter = painterResource(Resources.Icon.FilledStar), // 換成你的星星 icon
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(28.dp)
                    .let {
                        if (enabled) {
                            it.clickable { onRatingSelected(i) }
                        } else it
                    }
            )

            if (i < max) Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
fun EditOrDoneButton(
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    val backgroundColor =
        if (isEditing) Color(0xFFFF7A3C) else Color.Transparent

    val contentColor =
        if (isEditing) Color.White else Color(0xFFFF7A3C)

    val borderColor =
        if (isEditing) Color(0x00FFFFFF) else Color(0xFFFF7A3C)

    Box(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                if (isEditing) onDoneClick() else onEditClick()
            }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isEditing) "完成" else "編輯",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

