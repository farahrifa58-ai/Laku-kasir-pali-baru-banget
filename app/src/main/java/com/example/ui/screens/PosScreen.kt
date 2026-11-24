package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.Categories
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.ui.CartItem
import com.example.ui.LakuKasirViewModel
import com.example.ui.Screen
import com.example.ui.components.AppNavigationShell
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CompactSearchBar
import com.example.ui.components.SoftElevatedCard
import com.example.ui.components.WindowWidthTier
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkDangerRed
import com.example.ui.theme.DarkEmeraldPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.SoftCardBorder
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.WarningYellow
import com.example.util.FormatUtils
import com.example.util.ImageUtils
import com.example.util.LakuStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCheckout: () -> Unit
) {
    val context = LocalContext.current
    val lang = storeProfile.language
    val products by viewModel.filteredPosProducts.collectAsState()
    val allProductsCount by viewModel.allProducts.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val searchQuery by viewModel.posSearchQuery.collectAsState()
    val selectedCategory by viewModel.posSelectedCategory.collectAsState()

    var showCartSheet by remember { mutableStateOf(false) }
    var productForKgDialog by remember { mutableStateOf<Product?>(null) }
    var showBarcodeScanner by remember { mutableStateOf(false) }

    val categoriesList = listOf("Semua") + Categories.ALL_CATEGORIES

    AppNavigationShell(
        title = LakuStrings.get("app_title", lang),
        currentScreen = Screen.POS,
        storeProfile = storeProfile,
        cartItemCount = cart.size,
        onNavigate = onNavigate,
        onOpenSettings = onOpenSettings,
        maxContentWidth = 1440.dp
    ) { layoutInfo, innerPadding ->
        val isDark = MaterialTheme.colorScheme.background == DarkBackground
        val gridColumns = when (layoutInfo.tier) {
            WindowWidthTier.EXPANDED -> 4
            WindowWidthTier.MEDIUM -> 3
            WindowWidthTier.COMPACT -> 2
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Left / Main POS Catalog Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (layoutInfo.useSidebarNavigation) 18.dp else 14.dp, vertical = 10.dp)
                ) {

                // Search Bar with Barcode Scanner Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactSearchBar(
                        value = searchQuery,
                        onValueChange = { viewModel.posSearchQuery.value = it },
                        placeholder = LakuStrings.get("search_product_barcode", lang),
                        searchContentDescription = LakuStrings.get("search", lang),
                        clearContentDescription = LakuStrings.get("clear", lang),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pos_search_input")
                    )

                    Surface(
                        onClick = { showBarcodeScanner = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("pos_scan_barcode_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = LakuStrings.get("scan_barcode_title", lang),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(categoriesList) { cat ->
                        val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                        val displayCat = LakuStrings.translateCategory(cat, lang)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.posSelectedCategory.value = cat },
                            label = { Text(displayCat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product Catalog List or Empty State
                if (allProductsCount.isEmpty()) {
                    // Empty Catalog State: User requested new users add products manually
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SoftElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val isDark = MaterialTheme.colorScheme.background == DarkBackground
                                val accentGreen = MaterialTheme.colorScheme.primary
                                val onAccentGreen = MaterialTheme.colorScheme.onPrimary
                                val accentGreenText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(accentGreen.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = accentGreen,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = LakuStrings.get("no_products", lang),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = accentGreenText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = LakuStrings.get("no_products_desc", lang),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        viewModel.productToEdit.value = null
                                        onNavigate(Screen.ADD_EDIT_PRODUCT)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentGreen, contentColor = onAccentGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pos_empty_add_product_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = onAccentGreen)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(LakuStrings.get("add_product_btn", lang), fontWeight = FontWeight.Bold, color = onAccentGreen)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.loadDemoSampleProducts() },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, accentGreen.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pos_load_sample_products_button")
                                ) {
                                    Text(
                                        text = "Muat 4 Contoh Produk",
                                        fontWeight = FontWeight.SemiBold,
                                        color = accentGreenText
                                    )
                                }
                            }
                        }
                    }
                } else if (products.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LakuStrings.get("no_matching_products", lang),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            top = 2.dp,
                            bottom = if (!layoutInfo.useSplitPosView && cart.isNotEmpty()) 88.dp else 14.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        gridItems(products, key = { it.id }) { product ->
                            PosProductCard(
                                product = product,
                                cartQuantity = cart.firstOrNull { it.product.id == product.id }?.quantity ?: 0.0,
                                lang = lang,
                                onAddClick = {
                                    if (product.unit.equals("kg", ignoreCase = true)) {
                                        productForKgDialog = product
                                    } else {
                                        viewModel.addToCart(product, 1.0)
                                    }
                                },
                                onIncrement = {
                                    viewModel.addToCart(product, 1.0)
                                },
                                onDecrement = {
                                    val currentQty = cart.firstOrNull { it.product.id == product.id }?.quantity ?: 0.0
                                    viewModel.updateCartQuantity(product.id, currentQty - 1.0)
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Floating Cart Bar (Phone & Tablet < 1024.dp only)
            if (!layoutInfo.useSplitPosView && cart.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .widthIn(max = 560.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 8.dp,
                    color = if (isDark) DarkSurface else EmeraldGreenDark,
                    border = if (isDark) BorderStroke(1.dp, DarkEmeraldPrimary.copy(alpha = 0.55f)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCartSheet = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("floating_cart_bar"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(
                                containerColor = if (isDark) DarkEmeraldPrimary else Color.White,
                                contentColor = if (isDark) MaterialTheme.colorScheme.onPrimary else EmeraldGreenDark
                            ) {
                                Text(
                                    text = "${cart.size}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${cart.size} ${LakuStrings.get("items_in_cart", lang)}",
                                    fontSize = 12.sp,
                                    color = if (isDark) DarkTextSecondary else Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(viewModel.cartSubtotal),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = if (isDark) DarkTextPrimary else Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { showCartSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) DarkEmeraldPrimary else Color.White,
                                contentColor = if (isDark) MaterialTheme.colorScheme.onPrimary else EmeraldGreenDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = if (isDark) MaterialTheme.colorScheme.onPrimary else EmeraldGreenDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LakuStrings.get("cart_btn", lang),
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) MaterialTheme.colorScheme.onPrimary else EmeraldGreenDark,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            }

            // Permanent Split-View POS Cart Panel on Desktop (>= 1024.dp)
            if (layoutInfo.useSplitPosView) {
                VerticalDivider(
                    thickness = 1.dp,
                    color = if (isDark) DarkBorder else SoftCardBorder
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight()
                        .testTag("desktop_split_cart_panel")
                ) {
                    if (cart.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = LakuStrings.get("cart", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pilih produk di sebelah kiri untuk mulai mencatat pesanan kasir.",
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        CartSheetContent(
                            viewModel = viewModel,
                            lang = lang,
                            onClose = {},
                            onCheckout = onOpenCheckout
                        )
                    }
                }
            }
        }
    }

    // Kg / Fractional Weight Selector Dialog
    if (productForKgDialog != null) {
        val kgProduct = productForKgDialog!!
        KgWeightDialog(
            product = kgProduct,
            lang = lang,
            onDismiss = { productForKgDialog = null },
            onConfirm = { selectedWeight ->
                viewModel.addToCart(kgProduct, selectedWeight)
                productForKgDialog = null
            }
        )
    }

    // Cart Bottom Sheet (Phone / Tablet < 1024.dp)
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            sheetMaxWidth = 560.dp
        ) {
            CartSheetContent(
                viewModel = viewModel,
                lang = lang,
                onClose = { showCartSheet = false },
                onCheckout = {
                    showCartSheet = false
                    onOpenCheckout()
                }
            )
        }
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            lang = lang,
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { scannedCode ->
                showBarcodeScanner = false
                val allProductsList = allProductsCount
                val matched = allProductsList.find { it.barcode.equals(scannedCode.trim(), ignoreCase = true) }
                if (matched != null) {
                    viewModel.addToCart(matched, 1.0)
                    Toast.makeText(context, "${LakuStrings.get("added_to_cart_toast", lang)} ${matched.name}", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.posSearchQuery.value = scannedCode.trim()
                    Toast.makeText(context, "$scannedCode (${LakuStrings.get("barcode_not_found_toast", lang)})", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

@Composable
fun PosProductCard(
    product: Product,
    cartQuantity: Double,
    lang: String,
    onAddClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accentGreen = MaterialTheme.colorScheme.primary
    val onAccentGreen = MaterialTheme.colorScheme.onPrimary
    val priceGreen = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark
    val accentRed = if (isDark) DarkDangerRed else DangerRed

    SoftElevatedCard(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(7.dp)
        ) {
            // Top Product Photo / Icon Container (compact height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accentGreen.copy(alpha = if (isDark) 0.12f else 0.09f)),
                contentAlignment = Alignment.Center
            ) {
                val resolvedImageModel = remember(product.imageUri) {
                    ImageUtils.resolveProductImageModel(product.imageUri)
                }
                if (resolvedImageModel != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(resolvedImageModel)
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
                        tint = accentGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Product Name (max 2 lines, consistent height across grid rows)
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 14.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Clean Category Label (no dangling bullet)
            Text(
                text = LakuStrings.translateCategory(product.category, lang),
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Compact Stock Indicator
            if (product.isUnlimitedStock) {
                Text(
                    text = LakuStrings.get("stock_unlimited_short", lang),
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (product.isLowStock) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = accentRed,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Text(
                        text = if (product.isLowStock) {
                            "${LakuStrings.get("low_stock_prefix", lang)} ${FormatUtils.formatQty(product.stock, product.unit)}"
                        } else {
                            "${LakuStrings.get("stock_prefix", lang)} ${FormatUtils.formatQty(product.stock, product.unit)}"
                        },
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp,
                        fontWeight = if (product.isLowStock) FontWeight.Bold else FontWeight.Normal,
                        color = if (product.isLowStock) accentRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bold Emerald Price (bright #22C55E in Dark Mode, crisp #15803D in Light Mode)
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = FormatUtils.formatRupiah(product.sellingPrice),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.5.sp,
                    color = priceGreen,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "/${LakuStrings.translateUnit(product.unit, lang)}",
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Full-width Compact Action Button or Stepper Control
            if (cartQuantity > 0.0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentGreen.copy(alpha = 0.14f))
                        .border(1.dp, accentGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = LakuStrings.get("delete", lang),
                            tint = priceGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = if (cartQuantity % 1.0 == 0.0) "${cartQuantity.toInt()}" else String.format(java.util.Locale.US, "%.2f", cartQuantity),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = priceGreen
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = LakuStrings.get("add_btn", lang),
                            tint = priceGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentGreen,
                        contentColor = onAccentGreen
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .testTag("add_to_cart_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = onAccentGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (product.unit.equals("kg", ignoreCase = true)) LakuStrings.get("choose_kg_btn", lang) else LakuStrings.get("add_btn", lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = onAccentGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun KgWeightDialog(
    product: Product,
    lang: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val quickOptions = listOf(0.25, 0.50, 1.0, 2.0, 3.0, 5.0)
    var customWeightText by remember { mutableStateOf("1.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = LakuStrings.get("kg_dialog_title", lang), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = LakuStrings.get("kg_quick_options", lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickOptions.take(4).forEach { weight ->
                        OutlinedButton(
                            onClick = { customWeightText = weight.toString() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (customWeightText == weight.toString()) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = EmeraldGreenPrimary,
                                    contentColor = Color.White
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text(
                                text = "$weight kg",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickOptions.drop(4).forEach { weight ->
                        OutlinedButton(
                            onClick = { customWeightText = weight.toString() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (customWeightText == weight.toString()) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = EmeraldGreenPrimary,
                                    contentColor = Color.White
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text(
                                text = "$weight kg",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = customWeightText,
                    onValueChange = { customWeightText = it },
                    label = { Text(LakuStrings.get("kg_custom_label", lang)) },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                val weightVal = customWeightText.toDoubleOrNull() ?: 0.0
                val totalEst = weightVal * product.sellingPrice
                val isDarkDialog = MaterialTheme.colorScheme.background == DarkBackground
                Text(
                    text = "${LakuStrings.get("estimated_price", lang)} ${FormatUtils.formatRupiah(totalEst)}",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkDialog) DarkEmeraldPrimary else EmeraldGreenDark,
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weight = customWeightText.toDoubleOrNull() ?: 1.0
                    if (weight > 0.0) {
                        onConfirm(weight)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(LakuStrings.get("add_confirm", lang), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(LakuStrings.get("cancel", lang))
            }
        }
    )
}

@Composable
fun CartSheetContent(
    viewModel: LakuKasirViewModel,
    lang: String,
    onClose: () -> Unit,
    onCheckout: () -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val customerPhone by viewModel.customerPhone.collectAsState()
    val discountMode by viewModel.discountMode.collectAsState()
    val discountText by viewModel.manualDiscount.collectAsState()
    val cartSubtotal = remember(cart) { cart.sumOf { it.subtotal } }
    val computedDiscount = remember(cartSubtotal, discountMode, discountText) {
        viewModel.calculateDiscountAmount(cartSubtotal, discountMode, discountText)
    }
    val cartTotalAfterDiscount = remember(cartSubtotal, computedDiscount) {
        maxOf(0.0, cartSubtotal - computedDiscount)
    }

    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accentGreen = MaterialTheme.colorScheme.primary
    val onAccentGreen = MaterialTheme.colorScheme.onPrimary
    val accentGreenText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark
    val accentRed = if (isDark) DarkDangerRed else DangerRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = accentGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LakuStrings.get("cart", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            TextButton(onClick = { viewModel.clearCart() }) {
                Text(LakuStrings.get("clear_cart", lang), color = accentRed, fontSize = 13.sp)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // Items List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cart) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${FormatUtils.formatRupiah(item.product.sellingPrice)} x ${FormatUtils.formatQty(item.quantity, item.product.unit)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatRupiah(item.subtotal),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = accentGreenText
                        )
                    }

                    // Quantity controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val delta = if (item.product.unit.equals("kg", ignoreCase = true)) 0.25 else 1.0
                                viewModel.updateCartQuantity(item.product.id, item.quantity - delta)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = LakuStrings.get("delete", lang), tint = accentGreenText)
                        }

                        Text(
                            text = if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()}" else String.format(java.util.Locale.US, "%.2f", item.quantity),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = {
                                val delta = if (item.product.unit.equals("kg", ignoreCase = true)) 0.25 else 1.0
                                viewModel.updateCartQuantity(item.product.id, item.quantity + delta)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = LakuStrings.get("add_btn", lang), tint = accentGreenText)
                        }

                        IconButton(
                            onClick = { viewModel.removeFromCart(item.product.id) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = LakuStrings.get("delete", lang), tint = accentRed)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Discount (Manual / Percent), Customer Name, and WhatsApp
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = discountMode == "manual",
                        onClick = {
                            if (discountMode != "manual") {
                                viewModel.discountMode.value = "manual"
                                viewModel.manualDiscount.value = ""
                            }
                        },
                        label = { Text(LakuStrings.get("discount_mode_manual", lang), fontSize = 11.sp, fontWeight = if (discountMode == "manual") FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = accentGreen,
                            selectedLabelColor = onAccentGreen
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = discountMode == "manual",
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = discountMode == "percent",
                        onClick = {
                            if (discountMode != "percent") {
                                viewModel.discountMode.value = "percent"
                                viewModel.manualDiscount.value = ""
                            }
                        },
                        label = { Text(LakuStrings.get("discount_mode_percent", lang), fontSize = 11.sp, fontWeight = if (discountMode == "percent") FontWeight.Bold else FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = accentGreen,
                            selectedLabelColor = onAccentGreen
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = discountMode == "percent",
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = discountText,
                    onValueChange = { input ->
                        viewModel.manualDiscount.value = if (discountMode == "percent") {
                            input.filter { it.isDigit() || it == '.' || it == ',' }.take(5)
                        } else {
                            FormatUtils.formatInputDotted(input)
                        }
                    },
                    label = {
                        Text(
                            if (discountMode == "percent") LakuStrings.get("discount_percent_label", lang)
                            else LakuStrings.get("discount_amount_label", lang)
                        )
                    },
                    leadingIcon = { Icon(imageVector = Icons.Default.Percent, contentDescription = null, tint = accentGreen) },
                    prefix = if (discountMode == "manual") {
                        { Text("${FormatUtils.currencySymbol} ") }
                    } else null,
                    suffix = if (discountMode == "percent") {
                        { Text("%", fontWeight = FontWeight.Bold) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (discountMode == "percent") KeyboardType.Decimal else KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { viewModel.customerName.value = it },
                    label = { Text(LakuStrings.get("customer_name_optional", lang)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = accentGreen) },
                    placeholder = { Text(LakuStrings.get("customer_name_placeholder", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { viewModel.customerPhone.value = it },
                    label = { Text(LakuStrings.get("customer_phone_optional", lang)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp), tint = accentGreen) },
                    placeholder = { Text(LakuStrings.get("whatsapp_no_placeholder", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(LakuStrings.get("subtotal", lang), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(FormatUtils.formatRupiah(cartSubtotal), fontWeight = FontWeight.Bold)
        }

        if (computedDiscount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (discountMode == "percent" && discountText.isNotBlank()) "${LakuStrings.get("discount", lang)} ($discountText%)"
                    else LakuStrings.get("discount", lang),
                    color = accentRed
                )
                Text("- ${FormatUtils.formatRupiah(computedDiscount)}", color = accentRed, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(LakuStrings.get("total_payment", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                FormatUtils.formatRupiah(cartTotalAfterDiscount),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = accentGreenText
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tombol Lanjut ke Pembayaran
        Button(
            onClick = onCheckout,
            colors = ButtonDefaults.buttonColors(containerColor = accentGreen, contentColor = onAccentGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("proceed_to_checkout_button")
        ) {
            Text(
                text = LakuStrings.get("checkout", lang),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = onAccentGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
