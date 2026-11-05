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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import com.example.ui.LakuKasirViewModel
import com.example.ui.Screen
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.util.FormatUtils
import com.example.util.LakuStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit
) {
    val lang = storeProfile.language
    val products by viewModel.allProducts.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val selectedPeriod by viewModel.dashboardPeriod.collectAsState()

    // Filter transactions and expenses based on selected period
    val now = Calendar.getInstance()
    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val startOfWeek = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }.timeInMillis

    val startOfMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val filteredTransactions = transactions.filter {
        when (selectedPeriod) {
            "today" -> it.timestamp >= startOfToday
            "week" -> it.timestamp >= startOfWeek
            "month" -> it.timestamp >= startOfMonth
            else -> true
        }
    }

    val filteredExpenses = expenses.filter {
        when (selectedPeriod) {
            "today" -> it.timestamp >= startOfToday
            "week" -> it.timestamp >= startOfWeek
            "month" -> it.timestamp >= startOfMonth
            else -> true
        }
    }

    // 1. Omzet Penjualan
    val omzetPenjualan = filteredTransactions.sumOf { it.total }

    // 2. Modal Terjual (HPP)
    val modalTerjual = filteredTransactions.sumOf { tx ->
        tx.items.sumOf { item -> item.costPrice * item.quantity }
    }

    // 3. Laba Kotor = Omzet - Modal Terjual
    val labaKotor = maxOf(0.0, omzetPenjualan - modalTerjual)

    // 4. Biaya Pengeluaran Toko
    val biayaPengeluaran = filteredExpenses.sumOf { it.amount }

    // 5. Estimasi Laba Bersih = Laba Kotor - Pengeluaran Toko
    val estimasiLabaBersih = labaKotor - biayaPengeluaran

    // 6. Nilai Aset Stok = Total Modal * Stok yang masih ada di gudang/toko
    val nilaiAsetStok = products.filter { !it.isUnlimitedStock }.sumOf { it.costPrice * it.stock }

    Scaffold(
        topBar = {
            AppTopBar(
                title = LakuStrings.get("nav_dashboard", lang),
                storeProfile = storeProfile,
                onSettingsClick = onOpenSettings
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = Screen.DASHBOARD,
                cartItemCount = cart.size,
                lang = lang,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
        ) {
            // Filter Periode Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val periods = listOf(
                        "today" to LakuStrings.get("period_today", lang),
                        "week" to LakuStrings.get("period_week", lang),
                        "month" to LakuStrings.get("period_month", lang),
                        "all" to LakuStrings.get("period_all", lang)
                    )
                    periods.forEach { (key, label) ->
                        val isSelected = selectedPeriod == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.dashboardPeriod.value = key },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // 6 Metric Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: Omzet & Laba Bersih
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = LakuStrings.get("revenue", lang),
                            value = FormatUtils.formatRupiah(omzetPenjualan),
                            subtitle = "${filteredTransactions.size} ${LakuStrings.get("transactions_suffix", lang)}",
                            icon = Icons.Default.Payments,
                            cardColor = EmeraldGreenPrimary,
                            textColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = LakuStrings.get("net_profit", lang),
                            value = FormatUtils.formatRupiah(estimasiLabaBersih),
                            subtitle = if (estimasiLabaBersih >= 0) LakuStrings.get("surplus_clean", lang) else LakuStrings.get("deficit", lang),
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            cardColor = if (estimasiLabaBersih >= 0) EmeraldGreenDark else DangerRed,
                            textColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Modal Terjual & Laba Kotor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = LakuStrings.get("cost_sold", lang),
                            value = FormatUtils.formatRupiah(modalTerjual),
                            subtitle = LakuStrings.get("cost_sold_subtitle", lang),
                            icon = Icons.Default.ArrowDownward,
                            cardColor = MaterialTheme.colorScheme.surface,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = LakuStrings.get("gross_profit", lang),
                            value = FormatUtils.formatRupiah(labaKotor),
                            subtitle = LakuStrings.get("gross_profit_subtitle", lang),
                            icon = Icons.Default.ArrowUpward,
                            cardColor = MaterialTheme.colorScheme.surface,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: Biaya Pengeluaran Toko & Nilai Aset Stok
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = LakuStrings.get("store_expense", lang),
                            value = FormatUtils.formatRupiah(biayaPengeluaran),
                            subtitle = "${filteredExpenses.size} catatan • Ketuk untuk kelola",
                            icon = Icons.Default.MoneyOff,
                            cardColor = MaterialTheme.colorScheme.surface,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.EXPENSES) }
                        )

                        MetricCard(
                            title = LakuStrings.get("stock_asset", lang),
                            value = FormatUtils.formatRupiah(nilaiAsetStok),
                            subtitle = "Total nilai gudang",
                            icon = Icons.Default.Inventory2,
                            cardColor = MaterialTheme.colorScheme.surface,
                            textColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Sales Trend Chart
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = EmeraldGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Grafik Tren Penjualan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "Ringkasan pemasukan harian toko Anda",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Render Canvas Bar Chart for last 7 days
                        SalesBarChart(transactions = transactions)
                    }
                }
            }

            // Top Selling Category breakdown
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Penjualan Berdasarkan Kategori",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val allSoldItems = transactions.flatMap { it.items }
                        val categoryTotals = allSoldItems.groupBy { it.category }
                            .mapValues { entry -> entry.value.sumOf { it.subtotal } }
                            .toList()
                            .sortedByDescending { it.second }

                        if (categoryTotals.isEmpty()) {
                            Text(
                                text = "Belum ada data transaksi untuk kategori.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val maxCat = categoryTotals.firstOrNull()?.second ?: 1.0
                            categoryTotals.take(5).forEach { (cat, total) ->
                                val progress = (total / maxCat).toFloat().coerceIn(0.05f, 1f)
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = cat, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(text = FormatUtils.formatRupiah(total), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreenDark)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EmeraldGreenPrimary.copy(alpha = 0.15f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progress)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EmeraldGreenPrimary)
                                        )
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

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.9f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
fun SalesBarChart(transactions: List<Transaction>) {
    // Group sales by the last 7 days
    val sdfDay = SimpleDateFormat("dd/MM", Locale.getDefault())
    val sdfKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    val daysList = (6 downTo 0).map { dayOffset ->
        val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -dayOffset) }
        val key = sdfKey.format(c.time)
        val label = sdfDay.format(c.time)
        key to label
    }

    val dailyTotals = transactions.groupBy {
        sdfKey.format(Date(it.timestamp))
    }.mapValues { entry ->
        entry.value.sumOf { it.total }
    }

    val maxVal = maxOf(1.0, daysList.maxOfOrNull { dailyTotals[it.first] ?: 0.0 } ?: 1.0)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            daysList.forEach { (key, label) ->
                val total = dailyTotals[key] ?: 0.0
                val ratio = (total / maxVal).toFloat().coerceIn(0.04f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .height((100 * ratio).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (total > 0) EmeraldGreenPrimary else EmeraldGreenPrimary.copy(alpha = 0.15f))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
