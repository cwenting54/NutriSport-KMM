package dev.nutrisport.shared.domain

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
@Serializable
data class Comments (
    val id: String = Uuid.random().toHexString(),
    val customerId: String,
    val productId: String,
    val orderId: String,
    val rate: Int = 0,
    val thumbUpCount: Int = 0,
    val description: String? = null,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updateAt: Long = Clock.System.now().toEpochMilliseconds(),
)

@OptIn(ExperimentalTime::class)
@Serializable
data class CommentLike(
    val customerId: String = "",
    val commentId: String = "",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
)

