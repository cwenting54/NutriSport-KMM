package dev.nutrisport.shared.domain

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class CartItem (
    val id: String = Uuid.random().toHexString(),
    val productId: String,
    val price: Double = 0.0,
    val weight: Int? = null,
    val flavor: String? = null,
    val quantity: Int,
)

data class CartItemUiModel (
    val id: String,
    val productId: String,
    val productTitle: String,
    val thumbnail: String,
    val price: Double = 0.0,
    val weight: Int? = null,
    val flavor: String? = null,
    val quantity: Int,
)


