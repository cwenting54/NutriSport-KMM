package dev.nutrisport.data.domain

import dev.nutrisport.shared.domain.CommentItem
import dev.nutrisport.shared.domain.CommentUiModel
import dev.nutrisport.shared.domain.Comments
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getCurrentUserId(): String?

    suspend fun createComment(
        comment: List<Comments>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun updateComment(
        comment: Comments,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    suspend fun updateCommentsLike(
        commentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    fun readCommentsByProductIdFlow(
        productId: String
    ): Flow<RequestState<List<CommentItem>>>

    fun readCommentsFlowByOrderId(
        orderId: String
    ): Flow<RequestState<List<CommentUiModel>>>
}