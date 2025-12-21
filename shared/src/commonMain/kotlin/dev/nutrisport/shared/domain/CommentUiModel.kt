package dev.nutrisport.shared.domain

data class CommentUiModel(
    val id: String,
    val customerId: String,
    val productInfo: ProductInfo,
    val orderId: String,
    val rate: Int = 0,
    val thumbUpCount: Int = 0,
    val description: String? = null,
    val createdAt: Long = 0L,
    val updateAt: Long = 0L,
)

data class ProductInfo(
    val id: String,
    val name: String,
    val thumbnail: String
)
