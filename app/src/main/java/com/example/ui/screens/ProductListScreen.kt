package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import coil.request.ImageRequest
import com.example.data.model.Categories
import com.example.data.model.CurrencyCatalog
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.ui.LakuKasirViewModel
import com.example.ui.Screen
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CompactSearchBar
import com.example.ui.components.CurrencySelectionDialog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.util.FormatUtils
import com.example.util.LakuStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val lang = storeProfile.language
    val products by viewModel.allProducts.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val isProLicense by viewModel.isProLicense.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showOnlyLowStock by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val categoriesList = listOf("Semua") + Categories.ALL_CATEGORIES

    val filtered = products.filter { p ->
        val matchesQuery = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true) ||
                p.supplierName.contains(searchQuery, ignoreCase = true)
        val matchesCat = selectedCategory == "Semua" || p.category.equals(selectedCategory, ignoreCase = true)
        val matchesLowStock = !showOnlyLowStock || p.isLowStock
        matchesQuery && matchesCat && matchesLowStock
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = LakuStrings.get("nav_products", lang),
                storeProfile = storeProfile,
                onSettingsClick = onOpenSettings,
                onLicenseBadgeClick = { viewModel.openActivationDialog() }
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = Screen.PRODUCTS,
                cartItemCount = cart.size,
                lang = lang,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!isProLicense && products.size >= com.example.util.LicenseManager.MAX_TRIAL_PRODUCTS) {
                        viewModel.trialLimitDialogMessage.value = "Batas Produk Uji Coba Tercapai!\nVersi Trial dibatasi maksimal ${com.example.util.LicenseManager.MAX_TRIAL_PRODUCTS} produk. Silakan aktivasi lisensi PRO untuk menambahkan produk tanpa batas."
                        viewModel.openActivationDialog()
                    } else {
                        viewModel.productToEdit.value = null
                        onNavigate(Screen.ADD_EDIT_PRODUCT)
                    }
                },
                containerColor = EmeraldGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = LakuStrings.get("add_product", lang))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Currency & Total Products Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val curr = CurrencyCatalog.getCurrency(FormatUtils.currentCurrencyCode)
                Surface(
                    onClick = { showCurrencyDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGreenPrimary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, EmeraldGreenPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(curr.flag, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${curr.name} (${curr.symbol})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(LakuStrings.get("change_currency", lang), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Text(
                    text = "${filtered.size} ${LakuStrings.get("product_count_suffix", lang)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Search Bar with Barcode Scanner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = LakuStrings.get("search_product_placeholder", lang),
                    searchContentDescription = LakuStrings.get("search", lang),
                    clearContentDescription = LakuStrings.get("clear", lang),
                    modifier = Modifier.weight(1f)
                )

                // Button Scan Barcode Kamera
                Surface(
                    onClick = { showBarcodeScanner = true },
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldGreenPrimary,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = LakuStrings.get("scan_barcode_title", lang),
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    FilterChip(
                        selected = showOnlyLowStock,
                        onClick = { showOnlyLowStock = !showOnlyLowStock },
                        label = { Text("⚠️ ${LakuStrings.get("low_stock_chip", lang)}", fontSize = 12.sp, fontWeight = if (showOnlyLowStock) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DangerRed,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                items(categoriesList) { cat ->
                    val isSelected = !showOnlyLowStock && cat.equals(selectedCategory, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            showOnlyLowStock = false
                            selectedCategory = cat
                        },
                        label = { Text(LakuStrings.translateCategory(cat, lang), fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = EmeraldGreenPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = LakuStrings.get("empty_catalog_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = LakuStrings.get("empty_catalog_desc", lang),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.productToEdit.value = null
                                    onNavigate(Screen.ADD_EDIT_PRODUCT)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LakuStrings.get("add_new_product_btn", lang), fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(LakuStrings.get("no_filtered_products", lang), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filtered, key = { it.id }) { product ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(EmeraldGreenPrimary.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (product.imageUri != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(product.imageUri)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = product.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Inventory,
                                                contentDescription = null,
                                                tint = EmeraldGreenPrimary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${LakuStrings.translateCategory(product.category, lang)} • ${LakuStrings.get("unit_prefix", lang)} ${LakuStrings.translateUnit(product.unit, lang)}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (product.barcode.isNotBlank()) {
                                            Text(
                                                text = "Barcode: ${product.barcode}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    // Edit & Delete
                                    IconButton(onClick = {
                                        viewModel.productToEdit.value = product
                                        onNavigate(Screen.ADD_EDIT_PRODUCT)
                                    }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = LakuStrings.get("edit", lang), tint = EmeraldGreenPrimary)
                                    }

                                    IconButton(onClick = {
                                        productToDelete = product
                                    }) {
                                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = LakuStrings.get("delete", lang), tint = DangerRed)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(8.dp))

                                // Pricing and Stock Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${LakuStrings.get("cost_short", lang)} ${FormatUtils.formatRupiah(product.costPrice)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${LakuStrings.get("sell_short", lang)} ${FormatUtils.formatRupiah(product.sellingPrice)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = EmeraldGreenDark
                                        )
                                    }

                                    // Stock Controls
                                    if (product.isUnlimitedStock) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = EmeraldGreenPrimary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = LakuStrings.get("stock_unlimited_short", lang),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = EmeraldGreenDark,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { viewModel.quickUpdateStock(product, -1.0) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Remove, contentDescription = "-1", tint = DangerRed)
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (product.isLowStock) DangerRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = FormatUtils.formatQty(product.stock, product.unit),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (product.isLowStock) DangerRed else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.quickUpdateStock(product, 1.0) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = "+1", tint = EmeraldGreenPrimary)
                                            }
                                        }
                                    }
                                }

                                // Supplier direct WhatsApp contact button
                                if (product.supplierPhone.isNotBlank() || product.supplierName.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Supplier: ${product.supplierName.ifBlank { "Supplier" }}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (product.supplierPhone.isNotBlank()) {
                                                Text(
                                                    text = product.supplierPhone,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (product.supplierPhone.isNotBlank()) {
                                            Button(
                                                onClick = {
                                                    FormatUtils.openWhatsApp(
                                                        context,
                                                        product.supplierPhone,
                                                        "Halo ${product.supplierName.ifBlank { "Supplier" }}, kami dari toko ingin memesan restock untuk produk: ${product.name}."
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        val prod = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(LakuStrings.get("delete_product_title", lang), fontWeight = FontWeight.Bold) },
            text = { Text("${LakuStrings.get("delete_product_confirm", lang)} (${prod.name})") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(prod)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(LakuStrings.get("delete", lang), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text(LakuStrings.get("cancel", lang))
                }
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            activeCode = FormatUtils.currentCurrencyCode,
            lang = lang,
            onSelect = { curr ->
                FormatUtils.currentCurrencyCode = curr.code
                viewModel.setCurrency(curr.code)
                showCurrencyDialog = false
                android.widget.Toast.makeText(context, "${LakuStrings.get("currency_changed_toast", lang)} ${curr.name} (${curr.symbol})", android.widget.Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            lang = lang,
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                showBarcodeScanner = false
                searchQuery = scannedCode.trim()
                android.widget.Toast.makeText(context, "${LakuStrings.get("searching_barcode_toast", lang)} $scannedCode", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}
