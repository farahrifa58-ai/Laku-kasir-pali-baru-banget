package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import com.example.ui.components.BarcodeScannerDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Categories
import com.example.data.model.CurrencyCatalog
import com.example.data.model.Product
import com.example.ui.components.CurrencySelectionDialog
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkDangerRed
import com.example.ui.theme.DarkEmeraldPrimary
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.SoftCardBorder
import com.example.util.FormatUtils
import com.example.util.ImageUtils
import com.example.util.LakuStrings
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditProductScreen(
    productToEdit: Product?,
    lang: String,
    onBack: () -> Unit,
    onCurrencyChanged: ((String) -> Unit)? = null,
    onSave: (
        id: Long,
        name: String,
        category: String,
        unit: String,
        barcode: String,
        costPrice: Double,
        sellingPrice: Double,
        stock: Double,
        isUnlimitedStock: Boolean,
        minStockAlert: Double,
        supplierName: String,
        supplierPhone: String,
        imageUri: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val isEdit = productToEdit != null
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accentGreen = MaterialTheme.colorScheme.primary
    val onAccentGreen = MaterialTheme.colorScheme.onPrimary
    val accentGreenText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark
    val accentRed = if (isDark) DarkDangerRed else DangerRed

    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: Categories.ALL_CATEGORIES.first()) }
    var unit by remember { mutableStateOf(productToEdit?.unit ?: "pcs") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var costPriceText by remember { mutableStateOf(if (productToEdit != null && productToEdit.costPrice > 0) FormatUtils.formatNumberWithDots(productToEdit.costPrice) else "") }
    var sellingPriceText by remember { mutableStateOf(if (productToEdit != null && productToEdit.sellingPrice > 0) FormatUtils.formatNumberWithDots(productToEdit.sellingPrice) else "") }
    var stockText by remember { mutableStateOf(if (productToEdit != null) (if (productToEdit.stock % 1.0 == 0.0) productToEdit.stock.toInt().toString() else productToEdit.stock.toString()) else "10") }
    var isUnlimitedStock by remember { mutableStateOf(productToEdit?.isUnlimitedStock ?: false) }
    var minStockAlertText by remember { mutableStateOf(if (productToEdit != null) productToEdit.minStockAlert.toInt().toString() else "5") }
    var supplierName by remember { mutableStateOf(productToEdit?.supplierName ?: "") }
    var supplierPhone by remember { mutableStateOf(productToEdit?.supplierPhone ?: "") }
    var imageUri by remember { mutableStateOf(productToEdit?.imageUri) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val permanentPath = com.example.util.ImageUtils.saveImageToInternalStorage(context, uri, "prod")
            if (permanentPath != null) {
                imageUri = permanentPath
            }
        }
    }

    val onPerformSave = {
        if (name.isBlank()) {
            errorMessage = LakuStrings.get("product_name_required", lang)
        } else {
            val sellingPrice = FormatUtils.parseNumberFromDotted(sellingPriceText)
            val costPrice = FormatUtils.parseNumberFromDotted(costPriceText)
            val stock = stockText.toDoubleOrNull() ?: 0.0
            val minAlert = minStockAlertText.toDoubleOrNull() ?: 5.0

            onSave(
                productToEdit?.id ?: 0L,
                name,
                category,
                unit,
                barcode,
                costPrice,
                sellingPrice,
                stock,
                isUnlimitedStock,
                minAlert,
                supplierName,
                supplierPhone,
                imageUri
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEdit) LakuStrings.get("edit_product", lang) else LakuStrings.get("add_product", lang),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("add_product_back_button")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LakuStrings.get("back", lang))
                        }
                    },
                    actions = {
                        Button(
                            onClick = onPerformSave,
                            colors = ButtonDefaults.buttonColors(containerColor = accentGreen, contentColor = onAccentGreen),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("save_product_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = onAccentGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LakuStrings.get("save", lang),
                                fontWeight = FontWeight.Bold,
                                color = onAccentGreen,
                                fontSize = 14.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDark) DarkBackground else Color.White,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                androidx.compose.material3.HorizontalDivider(
                    thickness = 1.dp,
                    color = if (isDark) DarkBorder else SoftCardBorder
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upload Foto Produk Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val resolvedImageModel = remember(imageUri) {
                        ImageUtils.resolveProductImageModel(imageUri)
                    }
                    if (resolvedImageModel != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(resolvedImageModel)
                                .crossfade(true)
                                .build(),
                            contentDescription = LakuStrings.get("upload_product_photo", lang),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp),
                            shape = CircleShape,
                            color = accentGreen
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = LakuStrings.get("upload_product_photo", lang),
                                tint = onAccentGreen,
                                modifier = Modifier.padding(8.dp).size(20.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = LakuStrings.get("upload_product_photo", lang),
                                tint = accentGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = LakuStrings.get("upload_product_photo", lang),
                                fontWeight = FontWeight.SemiBold,
                                color = accentGreenText,
                                fontSize = 14.sp
                            )
                            Text(
                                text = LakuStrings.get("tap_choose_gallery", lang),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Nama Produk
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = { Text(LakuStrings.get("product_name", lang)) },
                placeholder = { Text(LakuStrings.get("product_name_placeholder", lang)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_name_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Kategori dan Satuan (Berdampingan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Kategori Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.weight(1.3f)
                ) {
                    OutlinedTextField(
                        value = LakuStrings.translateCategory(category, lang),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(LakuStrings.get("category", lang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("category_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        Categories.ALL_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(LakuStrings.translateCategory(cat, lang), fontWeight = if (cat == category) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    category = cat
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Satuan Dropdown (Di samping Kategori)
                ExposedDropdownMenuBox(
                    expanded = unitDropdownExpanded,
                    onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                    modifier = Modifier.weight(0.9f)
                ) {
                    OutlinedTextField(
                        value = LakuStrings.translateUnit(unit, lang),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(LakuStrings.get("unit", lang)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("unit_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = unitDropdownExpanded,
                        onDismissRequest = { unitDropdownExpanded = false }
                    ) {
                        Categories.ALL_UNITS.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(LakuStrings.translateUnit(u, lang), fontWeight = if (u == unit) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    unit = u
                                    unitDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Kode / Barcode Produk dengan Tombol Scan Kamera & Buat Otomatis
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text(LakuStrings.get("barcode", lang)) },
                    placeholder = { Text("899123456789 / KODE-01") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("barcode_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Tombol Scan Barcode Pakai Kamera
                Surface(
                    onClick = { showBarcodeScanner = true },
                    shape = RoundedCornerShape(12.dp),
                    color = accentGreen,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("add_product_scan_barcode_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = LakuStrings.get("scan_barcode_title", lang),
                            tint = onAccentGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Tombol Buat Barcode Otomatis
                IconButton(
                    onClick = {
                        val randomCode = "BRC-" + (100000..999999).random().toString()
                        barcode = randomCode
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = LakuStrings.get("generate_barcode", lang),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Pilihan Mata Uang untuk Harga Modal / HPP & Harga Jual
            val activeCurrency = remember(FormatUtils.currentCurrencyCode) {
                CurrencyCatalog.getCurrency(FormatUtils.currentCurrencyCode)
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, accentGreen.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = activeCurrency.flag,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${activeCurrency.name} (${activeCurrency.symbol})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentGreenText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { showCurrencyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = accentGreen, contentColor = onAccentGreen),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = onAccentGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LakuStrings.get("change_country", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = onAccentGreen,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Quick Currency Selection Chips (Asia Tenggara, Asia, Dolar, Eropa, Rusia, Afrika)
                    val quickCurrencies = listOf(
                        "IDR" to "🇮🇩 IDR (Rp)",
                        "MYR" to "🇲🇾 MYR (RM)",
                        "SGD" to "🇸🇬 SGD (S$)",
                        "THB" to "🇹🇭 THB (฿)",
                        "PHP" to "🇵🇭 PHP (₱)",
                        "VND" to "🇻🇳 VND (₫)",
                        "USD" to "🇺🇸 USD ($)",
                        "JPY" to "🇯🇵 JPY (¥)",
                        "KRW" to "🇰🇷 KRW (₩)",
                        "CNY" to "🇨🇳 CNY (¥)",
                        "SAR" to "🇸🇦 SAR",
                        "AED" to "🇦🇪 AED",
                        "EUR" to "🇪🇺 EUR (€)",
                        "GBP" to "🇬🇧 GBP (£)",
                        "RUB" to "🇷🇺 RUB (₽)",
                        "ZAR" to "🇿🇦 ZAR (R)",
                        "EGP" to "🇪🇬 EGP (E£)",
                        "NGN" to "🇳🇬 NGN (₦)"
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(quickCurrencies) { (code, label) ->
                            val isSelected = FormatUtils.currentCurrencyCode.equals(code, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    FormatUtils.currentCurrencyCode = code
                                    onCurrencyChanged?.invoke(code)
                                    val curr = CurrencyCatalog.getCurrency(code)
                                    Toast.makeText(context, "${LakuStrings.get("currency_changed_toast", lang)} ${curr.name} (${curr.symbol})", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = accentGreen,
                                    selectedLabelColor = onAccentGreen
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                    selectedBorderColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // Harga Modal / HPP & Harga Jual (Berdampingan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = { costPriceText = FormatUtils.formatInputDotted(it) },
                    label = { Text(LakuStrings.get("cost_price", lang)) },
                    prefix = { Text("${FormatUtils.currencySymbol} ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cost_price_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = sellingPriceText,
                    onValueChange = { sellingPriceText = FormatUtils.formatInputDotted(it) },
                    label = { Text(LakuStrings.get("selling_price", lang)) },
                    prefix = { Text("${FormatUtils.currencySymbol} ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("selling_price_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Indikator Keuntungan / Margin Laba Otomatis dengan Mata Uang Terpilih
            val costVal = FormatUtils.parseNumberFromDotted(costPriceText)
            val sellVal = FormatUtils.parseNumberFromDotted(sellingPriceText)
            val profitVal = sellVal - costVal
            val profitPercent = if (costVal > 0.0) ((profitVal / costVal) * 100.0) else 0.0

            if (sellVal > 0.0 || costVal > 0.0) {
                val isProfit = profitVal >= 0
                val accentColor = if (isProfit) accentGreenText else accentRed
                val badgeBg = if (isProfit) accentGreen.copy(alpha = 0.16f) else accentRed.copy(alpha = 0.16f)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isProfit) accentGreen.copy(alpha = 0.1f) else accentRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, (if (isProfit) accentGreen else accentRed).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (if (isProfit) LakuStrings.get("est_profit_margin", lang) else LakuStrings.get("loss_warning", lang)).trimEnd(':'),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = FormatUtils.formatCurrency(profitVal),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = "${if (isProfit) "+" else ""}${String.format(java.util.Locale.US, "%.1f", profitPercent)}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Stok Barang (Pilihan Ketik & Unlimited) & Batas Peringatan Stok Menipis
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = LakuStrings.get("stock_unlimited", lang),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = LakuStrings.get("stock_unlimited_desc", lang),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isUnlimitedStock,
                            onCheckedChange = { isUnlimitedStock = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = onAccentGreen,
                                checkedTrackColor = accentGreen
                            ),
                            modifier = Modifier.testTag("unlimited_stock_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = if (isUnlimitedStock) "∞" else stockText,
                            onValueChange = { stockText = it },
                            enabled = !isUnlimitedStock,
                            label = { Text(LakuStrings.get("stock", lang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stock_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = minStockAlertText,
                            onValueChange = { minStockAlertText = it.filter { ch -> ch.isDigit() } },
                            enabled = !isUnlimitedStock,
                            label = { Text(LakuStrings.get("min_stock_alert", lang)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("min_stock_alert_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Info Supplier (Nama Supplier & Nomor WhatsApp yang bisa langsung dihubungi)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = LakuStrings.get("supplier_info_title", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text(LakuStrings.get("supplier_name", lang)) },
                        placeholder = { Text(LakuStrings.get("supplier_name_placeholder", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supplier_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = supplierPhone,
                        onValueChange = { supplierPhone = it },
                        label = { Text(LakuStrings.get("supplier_phone", lang)) },
                        placeholder = { Text(LakuStrings.get("whatsapp_no_placeholder", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        trailingIcon = {
                            if (supplierPhone.isNotBlank()) {
                                IconButton(onClick = {
                                    FormatUtils.openWhatsApp(
                                        context,
                                        supplierPhone,
                                        "Halo ${supplierName.ifBlank { "Supplier" }}, kami dari toko ingin menanyakan pesanan stok barang."
                                    )
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "WhatsApp",
                                        tint = EmeraldGreenPrimary
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supplier_phone_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Simpan Button
            Button(
                onClick = onPerformSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_product_button"),
                colors = ButtonDefaults.buttonColors(containerColor = accentGreen, contentColor = onAccentGreen),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = onAccentGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEdit) LakuStrings.get("save_changes", lang) else LakuStrings.get("add_to_catalog", lang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = onAccentGreen
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        }
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            activeCode = FormatUtils.currentCurrencyCode,
            lang = lang,
            onSelect = { curr ->
                FormatUtils.currentCurrencyCode = curr.code
                onCurrencyChanged?.invoke(curr.code)
                showCurrencyDialog = false
                Toast.makeText(context, "${LakuStrings.get("currency_changed_toast", lang)} ${curr.name} (${curr.symbol})", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            lang = lang,
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                barcode = scannedCode.trim()
                showBarcodeScanner = false
                Toast.makeText(context, "${LakuStrings.get("barcode_scanned_toast", lang)} $scannedCode", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
