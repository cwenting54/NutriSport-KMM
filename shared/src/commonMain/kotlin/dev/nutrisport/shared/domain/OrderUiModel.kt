package dev.nutrisport.shared.domain

import dev.nutrisport.shared.domain.Order.Companion.generateOrderNumber
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class OrderUiModel (
    val id: String = generateOrderNumber(),
    val customerId: String,
    val items: List<CartItemUiModel>,
    val totalAmount: Double,
    val token: String? = null,
    val payMethod: PayMethod = PayMethod.CashOnDelivery,
    val consignee: String,
    val address: String = "",
    val phone: PhoneNumber,
    val shipStatus: ShipStatus = ShipStatus.Pending,
    val createAt: Long =  Clock.System.now().toEpochMilliseconds(),
    val shipAt: Long? = null,
    val paidAt: Long? = null,
    val arriveAt: Long? = null,
    val completedAt: Long? = null,
)

fun Order.toOrderUiModel(products: List<CartItemUiModel>): OrderUiModel {
    return OrderUiModel(
        id = this.id,
        customerId = this.customerId,
        items = products,
        totalAmount = this.totalAmount,
        token = this.token,
        payMethod = PayMethod.entries.first { it.title == this.payMethod },
        consignee = this.consignee,
        address = this.address,
        phone = this.phone,
        shipStatus = ShipStatus.entries.first { it.title == this.shipStatus },
        createAt = this.createAt,
        shipAt = this.shipAt,
        paidAt = this.paidAt,
        arriveAt = this.arriveAt,
        completedAt = this.completedAt,
    )
}