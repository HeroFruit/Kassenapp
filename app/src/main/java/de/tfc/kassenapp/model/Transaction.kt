package de.tfc.kassenapp.model

import java.util.UUID

data class TransactionItem(
    val name: String,
    val price: Double,
    val quantity: Int
)

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<TransactionItem>,
    val totalAmount: Double
)
