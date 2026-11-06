package de.tfc.kassenapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import de.tfc.kassenapp.model.CartItem
import de.tfc.kassenapp.model.CatalogItem
import de.tfc.kassenapp.model.ThemeMode
import de.tfc.kassenapp.model.Transaction
import de.tfc.kassenapp.model.TransactionItem
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class KassenViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kassenapp_prefs", Context.MODE_PRIVATE)

    val catalogItems = mutableStateListOf<CatalogItem>()
    val cartItems = mutableStateListOf<CartItem>()
    val transactions = mutableStateListOf<Transaction>()

    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    var uiScale by mutableFloatStateOf(1.0f)
        private set

    var articleSquareScale by mutableFloatStateOf(1.0f)
        private set

    val totalPrice: Double
        get() = cartItems.sumOf { it.totalPrice }

    val totalRevenue: Double
        get() = transactions.sumOf { it.totalAmount }

    init {
        loadSettings()
        loadCatalog()
        loadTransactions()
    }

    private fun loadSettings() {
        val themeStr = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
        themeMode = try {
            ThemeMode.valueOf(themeStr ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
        uiScale = prefs.getFloat("ui_scale", 1.0f)
        articleSquareScale = prefs.getFloat("article_square_scale", 1.0f)
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        prefs.edit { putString("theme_mode", mode.name) }
    }

    fun updateUiScale(scale: Float) {
        val rounded = ((scale * 10f).roundToInt() / 10f).coerceIn(1.0f, 2.0f)
        uiScale = rounded
        prefs.edit { putFloat("ui_scale", rounded) }
    }

    fun updateArticleSquareScale(scale: Float) {
        val rounded = ((scale * 10f).roundToInt() / 10f).coerceIn(1.0f, 2.0f)
        articleSquareScale = rounded
        prefs.edit { putFloat("article_square_scale", rounded) }
    }

    private fun loadCatalog() {
        val savedJson = prefs.getString("catalog_items", null)
        if (!savedJson.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(savedJson)
                val loaded = mutableListOf<CatalogItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    loaded.add(
                        CatalogItem(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            price = obj.getDouble("price")
                        )
                    )
                }
                catalogItems.clear()
                catalogItems.addAll(loaded)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        catalogItems.clear()
    }

    private fun saveCatalog() {
        try {
            val jsonArray = JSONArray()
            for (item in catalogItems) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("price", item.price)
                jsonArray.put(obj)
            }
            prefs.edit { putString("catalog_items", jsonArray.toString()) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadTransactions() {
        val savedJson = prefs.getString("transactions_db", null)
        if (!savedJson.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(savedJson)
                val loaded = mutableListOf<Transaction>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.getString("id")
                    val timestamp = obj.getLong("timestamp")
                    val totalAmount = obj.getDouble("totalAmount")

                    val itemsArr = obj.getJSONArray("items")
                    val txItems = mutableListOf<TransactionItem>()
                    for (j in 0 until itemsArr.length()) {
                        val itemObj = itemsArr.getJSONObject(j)
                        txItems.add(
                            TransactionItem(
                                name = itemObj.getString("name"),
                                price = itemObj.getDouble("price"),
                                quantity = itemObj.getInt("quantity")
                            )
                        )
                    }

                    loaded.add(
                        Transaction(
                            id = id,
                            timestamp = timestamp,
                            items = txItems,
                            totalAmount = totalAmount
                        )
                    )
                }
                transactions.clear()
                transactions.addAll(loaded.sortedByDescending { it.timestamp })
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        transactions.clear()
    }

    private fun saveTransactions() {
        try {
            val jsonArray = JSONArray()
            for (tx in transactions) {
                val obj = JSONObject()
                obj.put("id", tx.id)
                obj.put("timestamp", tx.timestamp)
                obj.put("totalAmount", tx.totalAmount)

                val itemsArr = JSONArray()
                for (txItem in tx.items) {
                    val itemObj = JSONObject()
                    itemObj.put("name", txItem.name)
                    itemObj.put("price", txItem.price)
                    itemObj.put("quantity", txItem.quantity)
                    itemsArr.put(itemObj)
                }
                obj.put("items", itemsArr)

                jsonArray.put(obj)
            }
            prefs.edit { putString("transactions_db", jsonArray.toString()) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkoutNextCustomer(): Boolean {
        if (cartItems.isEmpty()) return false

        val txItems = cartItems.map {
            TransactionItem(
                name = it.item.name,
                price = it.item.price,
                quantity = it.quantity
            )
        }

        val newTransaction = Transaction(
            timestamp = System.currentTimeMillis(),
            items = txItems,
            totalAmount = totalPrice
        )

        transactions.add(0, newTransaction)
        saveTransactions()
        cartItems.clear()
        return true
    }

    fun clearArticleDatabase() {
        catalogItems.clear()
        cartItems.clear()
        saveCatalog()
    }

    fun clearStatisticsDatabase() {
        transactions.clear()
        saveTransactions()
    }

    fun addToCart(item: CatalogItem) {
        val index = cartItems.indexOfFirst { it.item.id == item.id }
        if (index != -1) {
            val existing = cartItems[index]
            cartItems[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            cartItems.add(CartItem(item = item, quantity = 1))
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        val index = cartItems.indexOfFirst { it.item.id == cartItem.item.id }
        if (index != -1) {
            val existing = cartItems[index]
            if (existing.quantity > 1) {
                cartItems[index] = existing.copy(quantity = existing.quantity - 1)
            } else {
                cartItems.removeAt(index)
            }
        }
    }

    fun removeEntireLineFromCart(cartItem: CartItem) {
        cartItems.removeAll { it.item.id == cartItem.item.id }
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun addCatalogItem(name: String, price: Double) {
        catalogItems.add(CatalogItem(name = name, price = price))
        saveCatalog()
    }

    fun updateCatalogItem(id: String, newName: String, newPrice: Double) {
        val index = catalogItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val updated = catalogItems[index].copy(name = newName, price = newPrice)
            catalogItems[index] = updated
            saveCatalog()

            val cartIndex = cartItems.indexOfFirst { it.item.id == id }
            if (cartIndex != -1) {
                cartItems[cartIndex] = cartItems[cartIndex].copy(item = updated)
            }
        }
    }

    fun deleteCatalogItem(item: CatalogItem) {
        catalogItems.removeAll { it.id == item.id }
        cartItems.removeAll { it.item.id == item.id }
        saveCatalog()
    }
}
