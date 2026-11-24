package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import com.example.ui.LakuKasirViewModel
import com.example.ui.Screen
import com.example.ui.components.AppNavigationShell
import com.example.ui.components.SoftElevatedCard
import com.example.ui.components.WindowWidthTier
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkDangerRed
import com.example.ui.theme.DarkEmeraldPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
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

    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accentGreen = MaterialTheme.colorScheme.primary
    val accentGreenText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark
    val accentRed = if (isDark) DarkDangerRed else DangerRed

    // Filter transactions and expenses based on selected period
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

    AppNavigationShell(
        title = LakuStrings.get("nav_dashboard", lang),
        currentScreen = Screen.DASHBOARD,
        storeProfile = storeProfile,
        cartItemCount = cart.size,
        onNavigate = onNavigate,
        onOpenSettings = onOpenSettings
    ) { layoutInfo, innerPadding ->
        val isWideCharts = layoutInfo.screenWidth >= 860.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = if (layoutInfo.useSidebarNavigation) 20.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
        ) {
            // Filter Periode Tabs
            item {
                val periods = listOf(
                    "today" to LakuStrings.get("period_today", lang),
                    "week" to LakuStrings.get("period_week", lang),
                    "month" to LakuStrings.get("period_month", lang),
                    "all" to LakuStrings.get("period_all", lang)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(periods) { (key, label) ->
                        val isSelected = selectedPeriod == key
                        Surface(
                            onClick = { viewModel.dashboardPeriod.value = key },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) accentGreen else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // 6 Metric Cards — Adaptive 2 cols (Phone), 3 cols (Tablet), 4+2 cols (Desktop)
            item {
                val revenueCard: @Composable (Modifier) -> Unit = { mod ->
                    if (isDark) {
                        MetricCard(
                            title = LakuStrings.get("revenue", lang),
                            value = FormatUtils.formatRupiah(omzetPenjualan),
                            subtitle = "${filteredTransactions.size} ${LakuStrings.get("transactions_suffix", lang)}",
                            icon = Icons.Default.Payments,
                            cardColor = DarkSurface,
                            textColor = DarkTextPrimary,
                            titleColor = DarkTextSecondary,
                            valueColor = DarkEmeraldPrimary,
                            subtitleBadgeColor = DarkEmeraldPrimary.copy(alpha = 0.14f),
                            subtitleTextColor = DarkEmeraldPrimary,
                            borderColor = DarkEmeraldPrimary.copy(alpha = 0.45f),
                            iconTint = DarkEmeraldPrimary,
                            iconBadgeColor = DarkEmeraldPrimary.copy(alpha = 0.16f),
                            modifier = mod
                        )
                    } else {
                        MetricCard(
                            title = LakuStrings.get("revenue", lang),
                            value = FormatUtils.formatRupiah(omzetPenjualan),
                            subtitle = "${filteredTransactions.size} ${LakuStrings.get("transactions_suffix", lang)}",
                            icon = Icons.Default.Payments,
                            cardColor = EmeraldGreenPrimary,
                            textColor = Color.White,
                            borderColor = EmeraldGreenPrimary,
                            modifier = mod
                        )
                    }
                }

                val netProfitCard: @Composable (Modifier) -> Unit = { mod ->
                    val isSurplus = estimasiLabaBersih >= 0
                    if (isDark) {
                        val profitAccent = if (isSurplus) DarkEmeraldPrimary else DarkDangerRed
                        MetricCard(
                            title = LakuStrings.get("net_profit", lang),
                            value = FormatUtils.formatRupiah(estimasiLabaBersih),
                            subtitle = if (isSurplus) LakuStrings.get("surplus_clean", lang) else LakuStrings.get("deficit", lang),
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            cardColor = DarkSurface,
                            textColor = DarkTextPrimary,
                            titleColor = DarkTextSecondary,
                            valueColor = profitAccent,
                            subtitleBadgeColor = profitAccent.copy(alpha = 0.14f),
                            subtitleTextColor = profitAccent,
                            borderColor = profitAccent.copy(alpha = 0.45f),
                            iconTint = profitAccent,
                            iconBadgeColor = profitAccent.copy(alpha = 0.16f),
                            modifier = mod
                        )
                    } else {
                        val profitBg = if (isSurplus) EmeraldGreenDark else DangerRed
                        MetricCard(
                            title = LakuStrings.get("net_profit", lang),
                            value = FormatUtils.formatRupiah(estimasiLabaBersih),
                            subtitle = if (isSurplus) LakuStrings.get("surplus_clean", lang) else LakuStrings.get("deficit", lang),
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            cardColor = profitBg,
                            textColor = Color.White,
                            borderColor = profitBg,
                            modifier = mod
                        )
                    }
                }

                val costCard: @Composable (Modifier) -> Unit = { mod ->
                    MetricCard(
                        title = LakuStrings.get("cost_sold", lang),
                        value = FormatUtils.formatRupiah(modalTerjual),
                        subtitle = LakuStrings.get("cost_sold_subtitle", lang),
                        icon = Icons.Default.ArrowDownward,
                        cardColor = MaterialTheme.colorScheme.surface,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        titleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = mod
                    )
                }

                val grossProfitCard: @Composable (Modifier) -> Unit = { mod ->
                    MetricCard(
                        title = LakuStrings.get("gross_profit", lang),
                        value = FormatUtils.formatRupiah(labaKotor),
                        subtitle = LakuStrings.get("gross_profit_subtitle", lang),
                        icon = Icons.Default.ArrowUpward,
                        cardColor = MaterialTheme.colorScheme.surface,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        titleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        iconTint = accentGreen,
                        modifier = mod
                    )
                }

                val expenseCard: @Composable (Modifier) -> Unit = { mod ->
                    MetricCard(
                        title = LakuStrings.get("store_expense", lang),
                        value = FormatUtils.formatRupiah(biayaPengeluaran),
                        subtitle = "${filteredExpenses.size} ${LakuStrings.get("records_tap_manage", lang)}",
                        icon = Icons.Default.MoneyOff,
                        cardColor = MaterialTheme.colorScheme.surface,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        titleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        iconTint = accentRed,
                        onClick = { onNavigate(Screen.EXPENSES) },
                        modifier = mod
                    )
                }

                val stockAssetCard: @Composable (Modifier) -> Unit = { mod ->
                    MetricCard(
                        title = LakuStrings.get("stock_asset", lang),
                        value = FormatUtils.formatRupiah(nilaiAsetStok),
                        subtitle = LakuStrings.get("total_warehouse_value", lang),
                        icon = Icons.Default.Inventory2,
                        cardColor = MaterialTheme.colorScheme.surface,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        titleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        iconTint = accentGreenText,
                        modifier = mod
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (layoutInfo.tier) {
                        WindowWidthTier.EXPANDED -> {
                            // Desktop: 4 KPI cards on row 1 + 2 balanced cards on row 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                revenueCard(Modifier.weight(1f))
                                netProfitCard(Modifier.weight(1f))
                                costCard(Modifier.weight(1f))
                                grossProfitCard(Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                expenseCard(Modifier.weight(1f))
                                stockAssetCard(Modifier.weight(1f))
                            }
                        }
                        WindowWidthTier.MEDIUM -> {
                            // Tablet: 3 columns x 2 balanced rows
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                revenueCard(Modifier.weight(1f))
                                netProfitCard(Modifier.weight(1f))
                                costCard(Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                grossProfitCard(Modifier.weight(1f))
                                expenseCard(Modifier.weight(1f))
                                stockAssetCard(Modifier.weight(1f))
                            }
                        }
                        WindowWidthTier.COMPACT -> {
                            // Phone: 2 columns x 3 rows (original polished mobile layout)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                revenueCard(Modifier.weight(1f))
                                netProfitCard(Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                costCard(Modifier.weight(1f))
                                grossProfitCard(Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                expenseCard(Modifier.weight(1f))
                                stockAssetCard(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Charts Section — Side-by-side on Wide Screens, Stacked on Phone
            item {
                val trendChartCard: @Composable (Modifier) -> Unit = { mod ->
                    SoftElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        modifier = mod
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(accentGreen.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                            contentDescription = null,
                                            tint = accentGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = LakuStrings.get("sales_trend_chart", lang),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = LakuStrings.get("daily_income_summary", lang),
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accentGreen.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = LakuStrings.get("period_week", lang),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentGreenText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            SalesBarChart(transactions = transactions, lang = lang)
                        }
                    }
                }

                val categoryBreakdownCard: @Composable (Modifier) -> Unit = { mod ->
                    SoftElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        modifier = mod
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = LakuStrings.get("sales_by_category", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val allSoldItems = transactions.flatMap { it.items }
                            val categoryTotals = allSoldItems.groupBy { it.category }
                                .mapValues { entry -> entry.value.sumOf { it.subtotal } }
                                .toList()
                                .sortedByDescending { it.second }

                            if (categoryTotals.isEmpty()) {
                                Text(
                                    text = LakuStrings.get("no_category_data", lang),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                val maxCat = categoryTotals.firstOrNull()?.second ?: 1.0
                                categoryTotals.take(5).forEach { (cat, total) ->
                                    val progress = (total / maxCat).toFloat().coerceIn(0.05f, 1f)
                                    Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = LakuStrings.translateCategory(cat, lang),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = FormatUtils.formatRupiah(total),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = accentGreenText
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(accentGreen.copy(alpha = 0.16f))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(progress)
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(accentGreenText, accentGreen)
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isWideCharts) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        trendChartCard(Modifier.weight(1.35f))
                        categoryBreakdownCard(Modifier.weight(1f))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        trendChartCard(Modifier.fillMaxWidth())
                        categoryBreakdownCard(Modifier.fillMaxWidth())
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
    modifier: Modifier = Modifier,
    titleColor: Color = textColor.copy(alpha = 0.85f),
    valueColor: Color = textColor,
    subtitleBadgeColor: Color? = null,
    subtitleTextColor: Color = textColor.copy(alpha = 0.72f),
    borderColor: Color? = null,
    iconTint: Color = textColor.copy(alpha = 0.9f),
    iconBadgeColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    SoftElevatedCard(
        shape = RoundedCornerShape(16.dp),
        containerColor = cardColor,
        borderColor = borderColor,
        onClick = onClick,
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
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                )
                if (iconBadgeColor != null) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconBadgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (subtitleBadgeColor != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = subtitleBadgeColor
                ) {
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = subtitleTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = subtitleTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatCompactNominal(amount: Double, lang: String): String {
    if (amount <= 0.0) return ""
    val isId = lang.equals("id", ignoreCase = true)
    return when {
        amount >= 1_000_000_000.0 -> {
            val v = String.format(Locale.US, "%.1f", amount / 1_000_000_000.0).removeSuffix(".0")
            if (isId) "${v.replace('.', ',')}M" else "${v}B"
        }
        amount >= 1_000_000.0 -> {
            val v = String.format(Locale.US, "%.1f", amount / 1_000_000.0).removeSuffix(".0")
            if (isId) "${v.replace('.', ',')}jt" else "${v}M"
        }
        amount >= 1_000.0 -> {
            val v = String.format(Locale.US, "%.1f", amount / 1_000.0).removeSuffix(".0")
            if (isId) "${v.replace('.', ',')}rb" else "${v}k"
        }
        else -> "${amount.toInt()}"
    }
}

@Composable
fun SalesBarChart(transactions: List<Transaction>, lang: String = "id") {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val chartAccent = MaterialTheme.colorScheme.primary
    val chartAccentText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark

    val locale = LakuStrings.localeFor(lang)
    val sdfDay = remember(locale) { SimpleDateFormat("dd/MM", locale) }
    val sdfKey = remember { SimpleDateFormat("yyyyMMdd", Locale.US) }

    val daysList = remember(locale) {
        (6 downTo 0).map { dayOffset ->
            val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -dayOffset) }
            val key = sdfKey.format(c.time)
            val label = sdfDay.format(c.time)
            key to label
        }
    }

    val dailyTotals = remember(transactions) {
        transactions.groupBy {
            sdfKey.format(Date(it.timestamp))
        }.mapValues { entry ->
            entry.value.sumOf { it.total }
        }
    }

    val values = daysList.map { dailyTotals[it.first] ?: 0.0 }
    val maxVal = maxOf(1.0, values.maxOrNull() ?: 1.0)
    val hasAnySales = values.any { it > 0.0 }

    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val topPadding = 26.dp.toPx()
            val bottomPadding = 10.dp.toPx()
            val chartHeight = (size.height - topPadding - bottomPadding).coerceAtLeast(1f)
            val baselineY = topPadding + chartHeight

            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            for (i in 0..2) {
                val y = topPadding + (chartHeight * i / 2f)
                drawLine(
                    color = gridColor.copy(alpha = if (i == 2) 0.9f else 0.7f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (i == 2) 1.2.dp.toPx() else 1.dp.toPx(),
                    pathEffect = if (i == 2) null else dashEffect
                )
            }

            val colWidth = size.width / values.size
            val points = values.mapIndexed { idx, total ->
                val x = colWidth * idx + colWidth / 2f
                val ratio = if (hasAnySales) (total / maxVal).toFloat().coerceIn(0f, 1f) else 0f
                val y = baselineY - (chartHeight * ratio)
                Offset(x, y)
            }

            if (points.size >= 2) {
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.lastIndex) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val midX = (p0.x + p1.x) / 2f
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }

                val areaPath = Path().apply {
                    addPath(linePath)
                    lineTo(points.last().x, baselineY)
                    lineTo(points.first().x, baselineY)
                    close()
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            chartAccent.copy(alpha = 0.28f),
                            chartAccent.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        startY = topPadding,
                        endY = baselineY
                    )
                )

                drawPath(
                    path = linePath,
                    color = chartAccent,
                    style = Stroke(
                        width = 2.6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            points.forEachIndexed { idx, pt ->
                val total = values[idx]
                val isToday = idx == points.lastIndex

                if (total > 0.0) {
                    drawCircle(
                        color = chartAccent.copy(alpha = 0.2f),
                        radius = if (isToday) 9.dp.toPx() else 7.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = chartAccentText,
                        radius = if (isToday) 5.dp.toPx() else 4.2.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = cardBg,
                        radius = if (isToday) 2.2.dp.toPx() else 1.8.dp.toPx(),
                        center = pt
                    )

                    val compactLabel = formatCompactNominal(total, lang)
                    if (compactLabel.isNotEmpty()) {
                        val measured = textMeasurer.measure(
                            text = compactLabel,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = chartAccentText
                            )
                        )
                        val labelX = (pt.x - measured.size.width / 2f)
                            .coerceIn(2f, size.width - measured.size.width - 2f)
                        val labelY = (pt.y - measured.size.height - 8.dp.toPx())
                            .coerceAtLeast(0f)
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(labelX, labelY)
                        )
                    }
                } else {
                    drawCircle(
                        color = gridColor,
                        radius = 2.6.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            daysList.forEachIndexed { idx, (_, label) ->
                val isToday = idx == daysList.lastIndex
                val hasValue = values[idx] > 0.0
                Text(
                    text = label,
                    fontSize = 10.5.sp,
                    fontWeight = if (isToday || hasValue) FontWeight.Bold else FontWeight.Medium,
                    color = if (isToday) chartAccentText else labelColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
