package dev.nutrisport.shared.domain

import kotlinx.serialization.Serializable

@Serializable
data class ConsigneeInfo(
    val name: String,
    val phone: PhoneNumber,
    val city: String,
    val postalCode: String,
    val address: String
)
