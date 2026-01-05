package dev.nutrisport.shared.domain

import dev.nutrisport.shared.util.toFormattedString
import dev.nutrisport.shared.util.toLocalDateTime
import kotlinx.datetime.number
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Serializable
data class Order (
    val id: String = generateOrderNumber(),
    val customerId: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val token: String? = null,
    val payMethod: String = PayMethod.CashOnDelivery.title,
    val consignee: String,
    val address: String = "",
    val phone: PhoneNumber,
    val shipStatus: String = ShipStatus.Pending.title,
    val createAt: Long =  Clock.System.now().toEpochMilliseconds(),
    val shipAt: Long? = null,
    val paidAt: Long? = null,
    val arriveAt: Long? = null,
    val completedAt: Long? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null
) {
    companion object {
        fun generateOrderNumber(
            nowMillis: Long = Clock.System.now().toEpochMilliseconds()
        ): String {
            val dateTimePart = nowMillis.toFormattedString("yyyyMMddHHmmss")

            // 取 UUID 前 4 位
            val randomPart = Uuid.random().toHexString().take(4).uppercase()
            println("$dateTimePart$randomPart")
            return "$dateTimePart$randomPart"
        }

    }
}

enum class PayMethod(
    val title: String
) {
    CashOnDelivery(
        title = "貨到付款"
    ),
    PAYPAL(
        title = "PayPal"
    ),
}

enum class ShipStatus(
    val title: String
) {
    Pending(
        title = "待出貨"
    ),
    Shipped(
        title = "已出貨"
    ),
    Shipping(
        title = "運送中"
    ),
    Delivered(
        title = "已送達"
    ),
    Completed(
        title = "已完成"
    )
}