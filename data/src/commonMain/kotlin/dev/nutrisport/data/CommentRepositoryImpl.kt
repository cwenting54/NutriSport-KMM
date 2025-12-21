package dev.nutrisport.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import dev.nutrisport.data.domain.CommentRepository
import dev.nutrisport.shared.domain.CommentItem
import dev.nutrisport.shared.domain.CommentLike
import dev.nutrisport.shared.domain.CommentUiModel
import dev.nutrisport.shared.domain.Comments
import dev.nutrisport.shared.domain.ProductInfo
import dev.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CommentRepositoryImpl : CommentRepository {
    override fun getCurrentUserId(): String? {
        return Firebase.auth.currentUser?.uid
    }

    override suspend fun createComment(
        comment: List<Comments>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserId = getCurrentUserId()

        if (currentUserId.isNullOrEmpty()) {
            onError("User is not available.")
            return
        }

        try {

            val firestore = Firebase.firestore
            val batch = firestore.batch()
            val commentsCollection = firestore.collection("comments")

            comment.forEach { item ->
                val docRef = commentsCollection.document(item.id)

                batch.set(docRef, item)
            }

            batch.commit()
            onSuccess()

        } catch (e: Exception) {
            onError("Unexpected error: ${e.message}")
        }
    }

    override suspend fun updateComment(
        comment: Comments,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserId = getCurrentUserId()

        if (currentUserId.isNullOrEmpty()) {
            onError("User is not available.")
            return
        }

        try {
            val firestore = Firebase.firestore
            val commentsCollection = firestore.collection("comments")
            val commentData = firestore.collection("comments").document(comment.id).get()
            if (commentData.exists) {
                commentsCollection.document(comment.id).update(
                    "rate" to comment.rate,
                    "description" to comment.description,
                    "updateAt" to comment.updateAt
                )
                onSuccess()
            } else {
                onError("Comment is not exist.")
            }

        } catch (e: Exception) {
            onError("Unexpected error: ${e.message}")
        }


    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateCommentsLike(
        commentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUserId = getCurrentUserId()

        if (currentUserId.isNullOrEmpty()) {
            onError("User is not available.")
            return
        }

        try {
            val firestore = Firebase.firestore
            val commentsRef = firestore.collection("comments").document(commentId)

            // 對「同一 user + 同一 comment」只允許一筆按讚紀錄
            val likeDocId = "${commentId}_$currentUserId"
            val likeRef = firestore
                .collection("commentsLike")
                .document(likeDocId)


            // 1) 取 comment 資料
            val commentSnap = commentsRef.get()
            if (!commentSnap.exists) {
                throw IllegalStateException("Comment does not exist.")
            }

            // 2) 查目前這個 user 有沒有按過讚
            val likeSnap = likeRef.get()

            val currentCount = commentSnap.get<Int>("thumbUpCount")

            // 設計成「按讚 / 取消讚」的 toggle 行為：
            if (likeSnap.exists) {
                // 已經按過 → 取消讚：刪掉 like，thumbUpCount - 1
                val newCount = max(0, currentCount - 1)
                likeRef.delete()
                commentsRef.update(
                    mapOf(
                        "thumbUpCount" to newCount,
                        "updateAt" to Clock.System.now().toEpochMilliseconds()
                    )
                )
            } else {
                // 還沒按過 → 新增讚：新增 like，thumbUpCount + 1
                val like = CommentLike(
                    customerId = currentUserId,
                    commentId = commentId
                )

                val newCount = currentCount + 1
                likeRef.set(like)
                commentsRef.update(
                    mapOf(
                        "thumbUpCount" to newCount,
                        "updateAt" to Clock.System.now().toEpochMilliseconds()
                    )
                )
            }

            onSuccess()

        } catch (e: Exception) {
            onError("Unexpected error: ${e.message}")
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun readCommentsByProductIdFlow(productId: String): Flow<RequestState<List<CommentItem>>> =
        channelFlow {
            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    send(RequestState.Error("User is not available."))
                    return@channelFlow
                }

                val database = Firebase.firestore

                val customerDoc = database.collection("customers").document(userId).get()
                val customerEmail = customerDoc.get<String?>("email") ?: ""

                database.collection("comments")
                    .where { "productId" equalTo productId }
                    .orderBy("createdAt", Direction.DESCENDING)
                    .snapshots
                    .collectLatest { query ->

                        if (query.documents.isEmpty()) {
                            send(RequestState.Success(emptyList()))
                            return@collectLatest
                        }

                        val comments = query.documents.map { doc ->
                            CommentItem(
                                id = doc.id,
                                productId = productId,
                                customerAccount = customerEmail,
                                content = doc.get<String?>("description") ?: "",
                                rating = doc.get<Int>("rate"),
                                likes = doc.get<Int>("thumbUpCount"),
                                createdAt = doc.get<Long>("createdAt")
                            )
                        }

                        send(RequestState.Success(comments))
                    }

            } catch (e: Exception) {
                send(RequestState.Error("Error while reading comments: ${e.message}"))
            }
        }

    override fun readCommentsFlowByOrderId(orderId: String): Flow<RequestState<List<CommentUiModel>>> =
        channelFlow {

            try {
                val userId = getCurrentUserId()
                if (userId == null) {
                    send(RequestState.Error("User is not available."))
                    return@channelFlow
                }

                val database = Firebase.firestore
                val productCollection = database.collection("product")

                database.collection("comments")
                    .where { "orderId" equalTo orderId }
                    .snapshots
                    .collectLatest { query ->

                        if (query.documents.isEmpty()) {
                            send(RequestState.Success(emptyList()))
                            return@collectLatest
                        }

                        // 1) 取得所有 productId
                        val productIds = query.documents
                            .map { it.get<String>("productId") }
                            .distinct()

                        // 2) 一次查所有商品資料
                        val productSnap = productCollection
                            .where { "id" inArray productIds }
                            .get()

                        val productMap = productSnap.documents.associate { doc ->
                            val id = doc.get<String>("id")
                            id to ProductInfo(
                                id = id,
                                name = doc.get<String>("title"),
                                thumbnail = doc.get<String>("thumbnail")
                            )
                        }

                        // 3) 建立 UI Model
                        val comments = query.documents.map { doc ->
                            val pid = doc.get<String>("productId")

                            CommentUiModel(
                                id = doc.id,
                                customerId = doc.get(field = "customerId"),
                                productInfo = productMap[pid] ?: ProductInfo(
                                    id = pid,
                                    name = "Unknown",
                                    thumbnail = ""
                                ),
                                orderId = orderId,
                                rate = doc.get(field = "rate"),
                                thumbUpCount = doc.get(field = "thumbUpCount"),
                                description = doc.get(field = "description"),
                                createdAt = doc.get(field = "createdAt"),
                                updateAt = doc.get(field = "updateAt"),
                            )
                        }

                        send(RequestState.Success(comments))
                    }

            } catch (e: Exception) {
                send(RequestState.Error("Error while reading comments: ${e.message}"))
            }
        }
}