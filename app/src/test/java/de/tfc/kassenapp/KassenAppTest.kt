package de.tfc.kassenapp

import de.tfc.kassenapp.model.CartItem
import de.tfc.kassenapp.model.CatalogItem
import de.tfc.kassenapp.ui.formatPrice
import org.junit.Assert.assertEquals
import org.junit.Test

class KassenAppTest {

    @Test
    fun testCartItemTotalPrice() {
        val item = CatalogItem(name = "Kaffee", price = 2.50)
        val cartItem = CartItem(item = item, quantity = 3)

        assertEquals(7.50, cartItem.totalPrice, 0.001)
    }

    @Test
    fun testPriceFormatting() {
        val formatted = formatPrice(2.50)
        assertEquals("2,50 €", formatted)
    }

    @Test
    fun testCartTotalSum() {
        val item1 = CatalogItem(name = "Kaffee", price = 2.50)
        val item2 = CatalogItem(name = "Kuchen", price = 3.00)

        val cart = listOf(
            CartItem(item = item1, quantity = 2), // 5.00
            CartItem(item = item2, quantity = 1)  // 3.00
        )

        val total = cart.sumOf { it.totalPrice }
        assertEquals(8.00, total, 0.001)
    }
}
