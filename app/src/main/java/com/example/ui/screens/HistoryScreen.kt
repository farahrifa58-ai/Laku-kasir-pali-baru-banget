package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import com.example.ui.LakuKasirViewModel
import com.example.ui.Screen
import com.example.ui.components.AppNavigationShell
import com.example.ui.components.CompactSearchBar
import com.example.ui.components.SoftElevatedCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkDangerRed
import com.example.ui.theme.DarkEmeraldPrimary
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.StatusDebt
import com.example.ui.theme.StatusPaid
import com.example.util.FormatUtils
import com.example.util.LakuStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    onSelectTransaction: (Transaction) -> Unit
) {
    val context = LocalContext.current
    val lang = storeProfile.language
    val transactions by viewModel.allTransactions.collectAsState()
    val cart by viewModel.cart.collectAsState()

    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accentGreen = MaterialTheme.colorScheme.primary
    val onAccentGreen = MaterialTheme.colorScheme.onPrimary
    val accentGreenText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark
    val accentRed = if (isDark) DarkDangerRed else DangerRed
    val paidAccent = if (isDark) DarkEmeraldPrimary else StatusPaid
    val debtAccent = if (isDark) Color(0xFFFB923C) else StatusDebt

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // all, paid, debt
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    val filtered = transactions.filter { tx ->
        val matchesQuery = searchQuery.isBlank() ||
                tx.transactionCode.contains(searchQuery, ignoreCase = true) ||
                tx.customerName.contains(searchQuery, ignoreCase = true) ||
                tx.customerPhone.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "paid" -> tx.isPaid
            "debt" -> !tx.isPaid
            else -> true
        }
        matchesQuery && matchesFilter
    }

    AppNavigationShell(
        title = LakuStrings.get("nav_history", lang),
        currentScreen = Screen.HISTORY,
        storeProfile = storeProfile,
        cartItemCount = cart.size,
        onNavigate = onNavigate,
        onOpenSettings = onOpenSettings
    ) { layoutInfo, innerPadding ->
        val listColumns = if (layoutInfo.screenWidth >= 960.dp) 2 else 1
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = if (layoutInfo.useSidebarNavigation) 20.dp else 14.dp, vertical = 10.dp)
        ) {
            // Search Bar
            CompactSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = LakuStrings.get("search_history", lang),
                searchContentDescription = LakuStrings.get("search", lang),
                clearContentDescription = LakuStrings.get("clear", lang),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (Semua, Lunas, Kasbon)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" },
                    label = { Text("${LakuStrings.get("filter_all", lang)} (${transactions.size})", fontSize = 12.sp, fontWeight = if (selectedFilter == "all") FontWeight.Bold else FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = accentGreen,
                        selectedLabelColor = onAccentGreen
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == "all",
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                FilterChip(
                    selected = selectedFilter == "paid",
                    onClick = { selectedFilter = "paid" },
                    label = { Text("${LakuStrings.get("filter_paid", lang)} (${transactions.count { it.isPaid }})", fontSize = 12.sp, fontWeight = if (selectedFilter == "paid") FontWeight.Bold else FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = accentGreen,
                        selectedLabelColor = onAccentGreen
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == "paid",
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                FilterChip(
                    selected = selectedFilter == "debt",
                    onClick = { selectedFilter = "debt" },
                    label = { Text("${LakuStrings.get("filter_debt", lang)} (${transactions.count { !it.isPaid }})", fontSize = 12.sp, fontWeight = if (selectedFilter == "debt") FontWeight.Bold else FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = StatusDebt,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == "debt",
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List of Transactions
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SoftElevatedCard(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = accentGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = LakuStrings.get("no_transactions_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = LakuStrings.get("no_transactions_desc", lang),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onNavigate(Screen.POS) },
                                colors = ButtonDefaults.buttonColors(containerColor = accentGreen, contentColor = onAccentGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(LakuStrings.get("open_cashier_menu", lang), fontWeight = FontWeight.Bold, color = onAccentGreen)
                            }
                        }
                    }
                }
            } else if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(LakuStrings.get("no_matching_transactions", lang), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(listColumns),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
                ) {
                    gridItems(filtered, key = { it.id }) { tx ->
                        SoftElevatedCard(
                            shape = RoundedCornerShape(16.dp),
                            onClick = { onSelectTransaction(tx) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = tx.transactionCode,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = FormatUtils.formatDateTime(tx.timestamp, lang),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Status Badge (Lunas / Kasbon)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (tx.isPaid) paidAccent.copy(alpha = 0.14f) else debtAccent.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = if (tx.isPaid) LakuStrings.get("paid_badge", lang) else LakuStrings.get("debt_badge", lang),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (tx.isPaid) paidAccent else debtAccent,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Items summary
                                Text(
                                    text = "${tx.items.size} item: " + tx.items.take(3).joinToString(", ") { "${it.productName} (${it.quantity.toInt()}x)" } +
                                            if (tx.items.size > 3) "..." else "",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )

                                if (tx.customerName.isNotBlank() || tx.customerPhone.isNotBlank()) {
                                    Text(
                                        text = "${LakuStrings.get("customer_prefix", lang)} ${tx.customerName.ifBlank { "-" }}${if (tx.customerPhone.isNotBlank()) " • ${tx.customerPhone}" else ""}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${LakuStrings.get("method_prefix", lang)} ${LakuStrings.translatePaymentMethod(tx.paymentMethod, lang)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = FormatUtils.formatRupiah(tx.total),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = accentGreenText
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // If Kasbon, provide direct "Tandai Lunas" button
                                        if (!tx.isPaid) {
                                            Button(
                                                onClick = { viewModel.markTransactionAsPaid(tx) },
                                                colors = ButtonDefaults.buttonColors(containerColor = accentGreen, contentColor = onAccentGreen),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = onAccentGreen, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(LakuStrings.get("mark_paid_btn", lang), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onAccentGreen)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }

                                        // WhatsApp shortcut
                                        if (tx.customerPhone.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    FormatUtils.openWhatsApp(
                                                        context,
                                                        tx.customerPhone,
                                                        "Halo ${tx.customerName.ifBlank { "Pelanggan" }}, kami dari ${storeProfile.storeName} terkait transaksi ${tx.transactionCode} sejumlah ${FormatUtils.formatRupiah(tx.total)}."
                                                    )
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Call, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                                            }
                                        }

                                        // View Receipt Icon
                                        IconButton(
                                            onClick = { onSelectTransaction(tx) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = LakuStrings.get("view_receipt", lang), tint = accentGreen)
                                        }

                                        // Delete transaction
                                        IconButton(
                                            onClick = { transactionToDelete = tx },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = LakuStrings.get("delete", lang), tint = accentRed)
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

    // Delete Dialog
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text(LakuStrings.get("delete_tx_title", lang), fontWeight = FontWeight.Bold) },
            text = { Text("${LakuStrings.get("delete_tx_confirm", lang)} ${tx.transactionCode}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(LakuStrings.get("delete", lang), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(LakuStrings.get("cancel", lang))
                }
            }
        )
    }
}
