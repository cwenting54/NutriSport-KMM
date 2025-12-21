package dev.nutrisport.shared.domain

import dev.nutrisport.shared.util.toLocalDateString
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Serializable
data class CommentItem (
    val id: String = Uuid.random().toHexString(),
    val productId: String,
    val customerAccount: String,
    val content: String,
    val rating: Int,
    val likes: Int,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
) {
    val formatedCreatedAt: String
        get() = createdAt.toLocalDateString()
}


