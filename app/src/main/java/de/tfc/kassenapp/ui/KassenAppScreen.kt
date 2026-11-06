package de.tfc.kassenapp.ui

import android.content.ContentValues
import android.content.Context
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.tfc.kassenapp.model.CartItem
import de.tfc.kassenapp.model.CatalogItem
import de.tfc.kassenapp.model.ThemeMode
import de.tfc.kassenapp.model.Transaction
import de.tfc.kassenapp.viewmodel.KassenViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val RedButtonColor = Color(0xFFD32F2F)
private val RedButtonTextColor = Color.White

enum class ExportFormat {
    PDF, EXCEL_CSV, NATIVE_JSON
}

data class DailyArticleSummary(
    val articleName: String,
    val totalQuantity: Int,
    val totalRevenue: Double
)

data class DailySalesSummary(
    val dateString: String,
    val dayTimestamp: Long,
    val articles: List<DailyArticleSummary>,
    val dayTotalRevenue: Double,
    val dayTotalQuantity: Int
)

fun formatPrice(price: Double): String {
    return String.format(Locale.GERMAN, "%.2f €", price)
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN)
    return sdf.format(Date(timestamp))
}

fun getDailySalesSummaries(transactions: List<Transaction>): List<DailySalesSummary> {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)

    val groupedByDay = transactions.groupBy { tx ->
        dateFormat.format(Date(tx.timestamp))
    }

    return groupedByDay.map { (dateStr, dayTxList) ->
        val articleMap = mutableMapOf<String, Pair<Int, Double>>()

        for (tx in dayTxList) {
            for (item in tx.items) {
                val current = articleMap.getOrDefault(item.name, Pair(0, 0.0))
                val newQty = current.first + item.quantity
                val newRev = current.second + (item.price * item.quantity)
                articleMap[item.name] = Pair(newQty, newRev)
            }
        }

        val articleSummaries = articleMap.map { (name, pair) ->
            DailyArticleSummary(
                articleName = name,
                totalQuantity = pair.first,
                totalRevenue = pair.second
            )
        }.sortedByDescending { it.totalQuantity }

        val dayTotalRev = dayTxList.sumOf { it.totalAmount }
        val dayTotalQty = articleSummaries.sumOf { it.totalQuantity }
        val maxTimestamp = dayTxList.maxOfOrNull { it.timestamp } ?: 0L

        DailySalesSummary(
            dateString = dateStr,
            dayTimestamp = maxTimestamp,
            articles = articleSummaries,
            dayTotalRevenue = dayTotalRev,
            dayTotalQuantity = dayTotalQty
        )
    }.sortedByDescending { it.dayTimestamp }
}

fun exportStatisticsToDownloads(
    context: Context,
    format: ExportFormat,
    dailySummaries: List<DailySalesSummary>,
    totalRevenue: Double
): String? {
    val timeStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMAN).format(Date())

    return when (format) {
        ExportFormat.PDF -> {
            val fileName = "kassenapp_statistik_$timeStr.pdf"
            val pdfBytes = generatePdfData(dailySummaries, totalRevenue)
            saveToDownloads(context, fileName, "application/pdf", pdfBytes)
        }
        ExportFormat.EXCEL_CSV -> {
            val fileName = "kassenapp_statistik_$timeStr.csv"
            val csvBytes = generateCsvData(dailySummaries, totalRevenue)
            saveToDownloads(context, fileName, "text/csv", csvBytes)
        }
        ExportFormat.NATIVE_JSON -> {
            val fileName = "kassenapp_statistik_$timeStr.json"
            val jsonBytes = generateJsonData(dailySummaries, totalRevenue)
            saveToDownloads(context, fileName, "application/json", jsonBytes)
        }
    }
}

fun generatePdfData(dailySummaries: List<DailySalesSummary>, totalRevenue: Double): ByteArray {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = pdfDocument.startPage(pageInfo)
    var canvas = page.canvas
    val paint = Paint()

    paint.textSize = 20f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.BLACK
    canvas.drawText("Kassenapp - Verkaufsstatistik", 40f, 50f, paint)

    paint.textSize = 11f
    paint.isFakeBoldText = false
    val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN).format(Date())
    canvas.drawText("Erstellt am: $dateStr", 40f, 70f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("Gesamteinnahmen: ${formatPrice(totalRevenue)}", 40f, 95f, paint)

    paint.textSize = 11f
    var y = 130f

    for (day in dailySummaries) {
        if (y > 780f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = 50f
        }

        paint.isFakeBoldText = true
        paint.textSize = 13f
        canvas.drawText("Datum: ${day.dateString}  (Tagesumsatz: ${formatPrice(day.dayTotalRevenue)})", 40f, y, paint)
        y += 20f

        paint.isFakeBoldText = false
        paint.textSize = 11f
        for (art in day.articles) {
            if (y > 800f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
            canvas.drawText("   • ${art.totalQuantity}x ${art.articleName} — ${formatPrice(art.totalRevenue)}", 50f, y, paint)
            y += 16f
        }
        y += 12f
    }

    pdfDocument.finishPage(page)
    val outputStream = ByteArrayOutputStream()
    pdfDocument.writeTo(outputStream)
    pdfDocument.close()
    return outputStream.toByteArray()
}

fun generateCsvData(dailySummaries: List<DailySalesSummary>, totalRevenue: Double): ByteArray {
    val sb = StringBuilder()
    sb.append("\uFEFF")
    sb.append("Datum;Artikel;Verkaufte Menge;Artikel-Umsatz;Tages-Gesamtumsaetze\n")

    for (day in dailySummaries) {
        for (art in day.articles) {
            val dateStr = day.dateString
            val artName = art.articleName.replace(";", ",")
            val qty = art.totalQuantity
            val artRev = formatPrice(art.totalRevenue)
            val dayRev = formatPrice(day.dayTotalRevenue)
            sb.append("$dateStr;$artName;$qty;$artRev;$dayRev\n")
        }
    }

    sb.append("\nGesamteinnahmen: ${formatPrice(totalRevenue)}\n")
    return sb.toString().toByteArray(Charsets.UTF_8)
}

fun generateJsonData(dailySummaries: List<DailySalesSummary>, totalRevenue: Double): ByteArray {
    val root = JSONObject()
    root.put("appName", "Kassenapp")
    root.put("exportTimestamp", System.currentTimeMillis())
    root.put("totalRevenue", totalRevenue)

    val daysArr = JSONArray()
    for (day in dailySummaries) {
        val dayObj = JSONObject()
        dayObj.put("date", day.dateString)
        dayObj.put("dayTotalRevenue", day.dayTotalRevenue)

        val articlesArr = JSONArray()
        for (art in day.articles) {
            val artObj = JSONObject()
            artObj.put("articleName", art.articleName)
            artObj.put("totalQuantity", art.totalQuantity)
            artObj.put("totalRevenue", art.totalRevenue)
            articlesArr.put(artObj)
        }
        dayObj.put("articles", articlesArr)
        daysArr.put(dayObj)
    }
    root.put("dailySummaries", daysArr)

    return root.toString(2).toByteArray(Charsets.UTF_8)
}

fun saveToDownloads(context: Context, fileName: String, mimeType: String, bytes: ByteArray): String? {
    val resolver = context.contentResolver
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(bytes)
            }
            return "Downloads/$fileName"
        }
    }

    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloadsDir.exists()) downloadsDir.mkdirs()
    val file = File(downloadsDir, fileName)
    file.writeBytes(bytes)
    return file.absolutePath
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

    var showSideMenu by remember { mutableStateOf(false) }
    var showStatisticsDialog by remember { mutableStateOf(false) }
    var showClearArticlesConfirm by remember { mutableStateOf(false) }
    var showClearStatisticsConfirm by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    CatalogGridSection(
                        items = viewModel.catalogItems,
                        articleSquareScale = viewModel.articleSquareScale,
                        onItemClick = { item -> viewModel.addToCart(item) },
                        onItemLongClick = { item -> itemForContextMenu = item },
                        onAddNewItemClick = { showAddItemDialog = true },
                        onOpenSideMenu = { showSideMenu = true }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

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
                        onCheckoutNextCustomer = { viewModel.checkoutNextCustomer() }
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .padding(8.dp)
                ) {
                    CatalogGridSection(
                        items = viewModel.catalogItems,
                        articleSquareScale = viewModel.articleSquareScale,
                        onItemClick = { item -> viewModel.addToCart(item) },
                        onItemLongClick = { item -> itemForContextMenu = item },
                        onAddNewItemClick = { showAddItemDialog = true },
                        onOpenSideMenu = { showSideMenu = true }
                    )
                }

                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

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
                        onCheckoutNextCustomer = { viewModel.checkoutNextCustomer() }
                    )
                }
            }
        }
    }

    // Seitenmenü Sheet
    if (showSideMenu) {
        SideMenuSheet(
            themeMode = viewModel.themeMode,
            uiScale = viewModel.uiScale,
            articleSquareScale = viewModel.articleSquareScale,
            onThemeModeChange = { viewModel.updateThemeMode(it) },
            onUiScaleChange = { viewModel.updateUiScale(it) },
            onArticleSquareScaleChange = { viewModel.updateArticleSquareScale(it) },
            onOpenStatistics = { showStatisticsDialog = true },
            onClearArticlesClick = { showClearArticlesConfirm = true },
            onClearStatisticsClick = { showClearStatisticsConfirm = true },
            onDismiss = { showSideMenu = false }
        )
    }

    // Vollbild Statistik Fenster
    if (showStatisticsDialog) {
        StatisticsDialog(
            transactions = viewModel.transactions,
            totalRevenue = viewModel.totalRevenue,
            onDismiss = { showStatisticsDialog = false }
        )
    }

    // Bestätigung: Artikel-Datenbank leeren
    if (showClearArticlesConfirm) {
        AlertDialog(
            onDismissRequest = { showClearArticlesConfirm = false },
            title = { Text("Artikel-Datenbank leeren") },
            text = { Text("Möchten Sie wirklich alle Artikel aus der Datenbank löschen? Dies kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearArticleDatabase()
                        showClearArticlesConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedButtonColor,
                        contentColor = RedButtonTextColor
                    )
                ) {
                    Text("Artikel leeren", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearArticlesConfirm = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Bestätigung: Statistik-Datenbank leeren
    if (showClearStatisticsConfirm) {
        AlertDialog(
            onDismissRequest = { showClearStatisticsConfirm = false },
            title = { Text("Statistik-Datenbank leeren") },
            text = { Text("Möchten Sie wirklich die gesamte Statistik und alle Verkaufsdaten löschen? Dies kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearStatisticsDatabase()
                        showClearStatisticsConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedButtonColor,
                        contentColor = RedButtonTextColor
                    )
                ) {
                    Text("Statistik leeren", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearStatisticsConfirm = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Dialog: Neuen Artikel hinzufügen
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

    // Dialog: Artikel bearbeiten
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

    // Menü aufrufen bei langem Drücken auf ein Artikelquadrat
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
                    colors = ButtonDefaults.textButtonColors(contentColor = RedButtonColor)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Löschen")
                }
            }
        )
    }

    // Bestätigung zum Einzel-Löschen
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedButtonColor,
                        contentColor = RedButtonTextColor
                    )
                ) {
                    Text("Löschen", fontWeight = FontWeight.Bold)
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
    articleSquareScale: Float,
    onItemClick: (CatalogItem) -> Unit,
    onItemLongClick: (CatalogItem) -> Unit,
    onAddNewItemClick: () -> Unit,
    onOpenSideMenu: () -> Unit,
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

            IconButton(onClick = onOpenSideMenu) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menü öffnen",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        val minCardSize = 100.dp * articleSquareScale

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCardSize),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { it.id }) { item ->
                ItemSquareCard(
                    item = item,
                    scale = articleSquareScale,
                    onClick = { onItemClick(item) },
                    onLongClick = { onItemLongClick(item) }
                )
            }
            item(key = "add_new_item_square") {
                AddItemSquareCard(
                    scale = articleSquareScale,
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
    modifier: Modifier = Modifier,
    scale: Float = 1.0f
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape((12 * scale).dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding((8 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = (16 * scale).sp
                ),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = (4 * scale).dp)
            )

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = RoundedCornerShape((8 * scale).dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatPrice(item.price),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (14 * scale).sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(
                        vertical = (4 * scale).dp,
                        horizontal = (2 * scale).dp
                    )
                )
            }
        }
    }
}

@Composable
fun AddItemSquareCard(
    scale: Float = 1.0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape((12 * scale).dp),
        modifier = modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding((8 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Artikel hinzufügen",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size((36 * scale).dp)
            )
            Spacer(modifier = Modifier.height((4 * scale).dp))
            Text(
                text = "+ Artikel",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = (14 * scale).sp
                ),
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
    onCheckoutNextCustomer: () -> Unit,
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

            Button(
                onClick = onCheckoutNextCustomer,
                enabled = cartItems.isNotEmpty(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nächster Kunde", fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideMenuSheet(
    themeMode: ThemeMode,
    uiScale: Float,
    articleSquareScale: Float,
    onThemeModeChange: (ThemeMode) -> Unit,
    onUiScaleChange: (Float) -> Unit,
    onArticleSquareScaleChange: (Float) -> Unit,
    onOpenStatistics: () -> Unit,
    onClearArticlesClick: () -> Unit,
    onClearStatisticsClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Menü",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            // Erscheinungsbild
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Erscheinungsbild",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                        label = { Text("System") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                        label = { Text("Hell") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { onThemeModeChange(ThemeMode.DARK) },
                        label = { Text("Dunkel") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider()

            // UI Skalierung Slider (1x - 2x in 0,1 Schritten)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UI-Skalierung",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format(Locale.GERMAN, "%.1fx", uiScale),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = uiScale,
                    onValueChange = onUiScaleChange,
                    valueRange = 1.0f..2.0f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            // Artikelquadrat-Skalierung Slider (1x - 2x in 0,1 Schritten)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Artikelquadrat-Skalierung",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format(Locale.GERMAN, "%.1fx", articleSquareScale),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = articleSquareScale,
                    onValueChange = onArticleSquareScaleChange,
                    valueRange = 1.0f..2.0f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            // Statistik Knopf
            Button(
                onClick = {
                    onDismiss()
                    onOpenStatistics()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Statistik anzeigen")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rote Knöpfe ganz unten
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onDismiss()
                        onClearArticlesClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedButtonColor,
                        contentColor = RedButtonTextColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Artikel leeren", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onDismiss()
                        onClearStatisticsClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedButtonColor,
                        contentColor = RedButtonTextColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Statistik leeren", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsDialog(
    transactions: List<Transaction>,
    totalRevenue: Double,
    onDismiss: () -> Unit
) {
    val dailySummaries = remember(transactions) {
        getDailySalesSummaries(transactions)
    }

    var showExportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Statistik", fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Zurück"
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = { showExportDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export", fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Gesamteinnahmen",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = formatPrice(totalRevenue),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${dailySummaries.size} Tage aufgezeichnet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                HorizontalDivider()

                if (dailySummaries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Noch keine Verkäufe in der Datenbank vorhanden",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(dailySummaries, key = { it.dateString }) { dailySales ->
                            DailySalesCard(dailySales = dailySales)
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExportSelected = { format ->
                showExportDialog = false
                val savedPath = exportStatisticsToDownloads(context, format, dailySummaries, totalRevenue)
                if (savedPath != null) {
                    Toast.makeText(
                        context,
                        "Export erfolgreich im Download-Ordner gespeichert:\n$savedPath",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Fehler beim Exportieren der Datei.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExportSelected: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Statistik exportieren", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Wählen Sie das Dateiformat für den Export in den Download-Ordner:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                OutlinedCard(
                    onClick = { onExportSelected(ExportFormat.PDF) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📄 PDF-Dokument (.pdf)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedCard(
                    onClick = { onExportSelected(ExportFormat.EXCEL_CSV) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Excel-Tabelle (.csv)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedCard(
                    onClick = { onExportSelected(ExportFormat.NATIVE_JSON) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💾 Native Datenbank (.json)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun DailySalesCard(dailySales: DailySalesSummary) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dailySales.dateString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tagesumsatz: ${formatPrice(dailySales.dayTotalRevenue)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (article in dailySales.articles) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${article.totalQuantity}x ${article.articleName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatPrice(article.totalRevenue),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
