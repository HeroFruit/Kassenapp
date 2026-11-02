package de.tfc.kassenapp.model

import java.util.UUID

data class CatalogItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double
)
