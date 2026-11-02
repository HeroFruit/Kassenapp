package de.tfc.kassenapp.ui

import android.content.res.Configuration
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.tfc.kassenapp.model.CartItem
import de.tfc.kassenapp.model.CatalogItem
import de.tfc.kassenapp.viewmodel.KassenViewModel
import java.util.Locale

fun formatPrice(price: Double): String {
    return String.format(Locale.GERMAN, "%.2f €", price)
}

@Composable
fun KassenAppScreen(
    viewModel: KassenViewModel,
    modifier: Modifier = Modifier
) {
    var showAddItemDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<CatalogItem?>(null) }
    var itemForContextMenu by remember { mutableStateOf<CatalogItem?>(null) }
    var itemToDeleteConfirm by remember { mutableStateOf<CatalogItem?>(null) }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        if (isPortrait) {
            // Vertikales Layout für Smartphone im Hochformat (Portrait)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Oben: Grid mit Artikel-Quadraten
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    CatalogGridSection(
                        items = viewModel.catalogItems,
                        onItemClick = { item -> viewModel.addToCart(item) },
                        onItemLongClick = { item -> itemForContextMenu = item },
                        onAddNewItemClick = { showAddItemDialog = true }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Unten: Warenkorb & Gesamtpreis
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    CartSection(
                        cartItems = viewModel.cartItems,
                        totalPrice = viewModel.totalPrice,
                        onAddOne = { cartItem -> viewModel.addToCart(cartItem.item) },
                        onRemoveOne = { cartItem -> viewModel.removeFromCart(cartItem) },
                        onRemoveEntireLine = { cartItem -> viewModel.removeEntireLineFromCart(cartItem) },
                        onClearCart = { viewModel.clearCart() }
                    )
                }
            }
        } else {
            // Horizontales Layout für Querformat (Landscape) / Tablets
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Linkes 2/3 des Bildschirms: Grid mit Artikel-Quadraten
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .padding(8.dp)
                ) {
                    CatalogGridSection(
                        items = viewModel.catalogItems,
                        onItemClick = { item -> viewModel.addToCart(item) },
                        onItemLongClick = { item -> itemForContextMenu = item },
                        onAddNewItemClick = { showAddItemDialog = true }
                    )
                }

                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Rechtes 1/3 des Bildschirms: Warenkorb & Gesamtpreis
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    CartSection(
                        cartItems = viewModel.cartItems,
                        totalPrice = viewModel.totalPrice,
                        onAddOne = { cartItem -> viewModel.addToCart(cartItem.item) },
                        onRemoveOne = { cartItem -> viewModel.removeFromCart(cartItem) },
                        onRemoveEntireLine = { cartItem -> viewModel.removeEntireLineFromCart(cartItem) },
                        onClearCart = { viewModel.clearCart() }
                    )
                }
            }
        }
    }

    // Dialog: Neuen Artikel hinzufügen (Name und Preis Feld)
    if (showAddItemDialog) {
        AddEditItemDialog(
            title = "Neuen Artikel hinzufügen",
            initialName = "",
            initialPrice = "",
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, price ->
                viewModel.addCatalogItem(name, price)
                showAddItemDialog = false
            }
        )
    }

    // Dialog: Artikel bearbeiten (Name und Preis Feld)
    itemToEdit?.let { targetItem ->
        AddEditItemDialog(
            title = "Artikel bearbeiten",
            initialName = targetItem.name,
            initialPrice = String.format(Locale.GERMAN, "%.2f", targetItem.price),
            onDismiss = { itemToEdit = null },
            onConfirm = { name, price ->
                viewModel.updateCatalogItem(targetItem.id, name, price)
                itemToEdit = null
            }
        )
    }

    // Menü aufrufen bei langem Drücken auf ein Artikelquadrat (Bearbeiten oder Löschen)
    itemForContextMenu?.let { targetItem ->
        AlertDialog(
            onDismissRequest = { itemForContextMenu = null },
            title = { Text(text = targetItem.name, fontWeight = FontWeight.Bold) },
            text = { Text("Preis: ${formatPrice(targetItem.price)}\nWas möchten Sie mit diesem Artikel tun?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToEdit = targetItem
                        itemForContextMenu = null
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bearbeiten")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        itemToDeleteConfirm = targetItem
                        itemForContextMenu = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Löschen")
                }
            }
        )
    }

    // Bestätigung zum Löschen
    itemToDeleteConfirm?.let { targetItem ->
        AlertDialog(
            onDismissRequest = { itemToDeleteConfirm = null },
            title = { Text("Artikel löschen") },
            text = { Text("Möchten Sie \"${targetItem.name}\" wirklich aus der Preisliste löschen?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCatalogItem(targetItem)
                        itemToDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDeleteConfirm = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun CatalogGridSection(
    items: List<CatalogItem>,
    onItemClick: (CatalogItem) -> Unit,
    onItemLongClick: (CatalogItem) -> Unit,
    onAddNewItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Artikel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gedrückt halten: Bearbeiten / Löschen",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { it.id }) { item ->
                ItemSquareCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onLongClick = { onItemLongClick(item) }
                )
            }
            item(key = "add_new_item_square") {
                AddItemSquareCard(
                    onClick = onAddNewItemClick
                )
            }
        }
    }
}

@Composable
fun ItemSquareCard(
    item: CatalogItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatPrice(item.price),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AddItemSquareCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Artikel hinzufügen",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "+ Artikel",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CartSection(
    cartItems: List<CartItem>,
    totalPrice: Double,
    onAddOne: (CartItem) -> Unit,
    onRemoveOne: (CartItem) -> Unit,
    onRemoveEntireLine: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Warenkorb",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onClearCart,
                enabled = cartItems.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Preisliste leeren",
                    tint = if (cartItems.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Warenkorb ist leer\nKlicke oben/links auf einen Artikel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(cartItems, key = { it.item.id }) { cartItem ->
                    CartItemRow(
                        cartItem = cartItem,
                        onAddOne = { onAddOne(cartItem) },
                        onRemoveOne = { onRemoveOne(cartItem) },
                        onRemoveEntireLine = { onRemoveEntireLine(cartItem) }
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gesamtpreis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = formatPrice(totalPrice),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    cartItem: CartItem,
    onAddOne: () -> Unit,
    onRemoveOne: () -> Unit,
    onRemoveEntireLine: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Text(
                    text = "${cartItem.quantity}x ${cartItem.item.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatPrice(cartItem.item.price)} / Stk.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatPrice(cartItem.totalPrice),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onRemoveOne,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Ein Stück entfernen",
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onAddOne,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ein Stück hinzufügen",
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onRemoveEntireLine,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Zeile entfernen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditItemDialog(
    title: String,
    initialName: String,
    initialPrice: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double) -> Unit
) {
    var nameText by remember { mutableStateOf(initialName) }
    var priceText by remember { mutableStateOf(initialPrice) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = {
                        nameText = it
                        errorMessage = null
                    },
                    label = { Text("Artikelname") },
                    placeholder = { Text("z.B. Cola") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        errorMessage = null
                    },
                    label = { Text("Preis in €") },
                    placeholder = { Text("z.B. 2.50") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = nameText.trim()
                    if (trimmedName.isEmpty()) {
                        errorMessage = "Bitte geben Sie einen Namen ein."
                        return@Button
                    }
                    val normalizedPrice = priceText.replace(',', '.').trim()
                    val parsedPrice = normalizedPrice.toDoubleOrNull()
                    if (parsedPrice == null || parsedPrice < 0) {
                        errorMessage = "Bitte geben Sie einen gültigen Preis ein (z.B. 2.50)."
                        return@Button
                    }
                    onConfirm(trimmedName, parsedPrice)
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
