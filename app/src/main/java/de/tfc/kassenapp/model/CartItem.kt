package de.tfc.kassenapp.model

data class CartItem(
    val item: CatalogItem,
    val quantity: Int = 1
) {
    val totalPrice: Double
        get() = item.price * quantity
}
