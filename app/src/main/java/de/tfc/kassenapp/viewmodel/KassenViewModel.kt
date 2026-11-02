package de.tfc.kassenapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import de.tfc.kassenapp.model.CartItem
import de.tfc.kassenapp.model.CatalogItem
import org.json.JSONArray
import org.json.JSONObject

class KassenViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kassenapp_prefs", Context.MODE_PRIVATE)

    val catalogItems = mutableStateListOf<CatalogItem>()
    val cartItems = mutableStateListOf<CartItem>()

    val totalPrice: Double
        get() = cartItems.sumOf { it.totalPrice }

    init {
        loadCatalog()
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
            prefs.edit().putString("catalog_items", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
