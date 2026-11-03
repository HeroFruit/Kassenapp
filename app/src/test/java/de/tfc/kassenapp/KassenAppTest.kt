package de.tfc.kassenapp

import de.tfc.kassenapp.model.CartItem
import de.tfc.kassenapp.model.CatalogItem
import de.tfc.kassenapp.model.Transaction
import de.tfc.kassenapp.model.TransactionItem
import de.tfc.kassenapp.ui.formatPrice
import de.tfc.kassenapp.ui.formatTimestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

    @Test
    fun testTransactionCreation() {
        val items = listOf(
            TransactionItem(name = "Wasser", price = 1.50, quantity = 2)
        )
        val tx = Transaction(items = items, totalAmount = 3.00)

        assertEquals(1, tx.items.size)
        assertEquals(3.00, tx.totalAmount, 0.001)
        assertNotNull(formatTimestamp(tx.timestamp))
    }
}
