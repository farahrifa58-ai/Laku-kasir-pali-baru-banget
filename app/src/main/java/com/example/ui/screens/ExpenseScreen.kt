package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.Expense
import com.example.data.model.StoreProfile
import com.example.ui.LakuKasirViewModel
import com.example.ui.Screen
import com.example.ui.components.AppNavigationShell
import com.example.ui.components.CompactSearchBar
import com.example.ui.components.SoftElevatedCard
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
fun ExpenseScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit
) {
    val lang = storeProfile.language
    val expenses by viewModel.allExpenses.collectAsState()
    val cart by viewModel.cart.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("all") } // "today", "week", "month", "all"
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    // Dialog state
    var showExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    // Filter calculations
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

    val filteredExpenses = expenses.filter { expense ->
        val matchesPeriod = when (selectedPeriod) {
            "today" -> expense.timestamp >= startOfToday
            "week" -> expense.timestamp >= startOfWeek
            "month" -> expense.timestamp >= startOfMonth
            else -> true
        }
        val matchesCategory = selectedCategoryFilter == "Semua" || expense.category == selectedCategoryFilter
        val matchesSearch = searchQuery.isBlank() ||
                expense.title.contains(searchQuery, ignoreCase = true) ||
                expense.note.contains(searchQuery, ignoreCase = true) ||
                expense.category.contains(searchQuery, ignoreCase = true)

        matchesPeriod && matchesCategory && matchesSearch
    }

    val totalExpenseAmount = filteredExpenses.sumOf { it.amount }

    AppNavigationShell(
        title = LakuStrings.get("expenses_title", lang),
        currentScreen = Screen.EXPENSES,
        storeProfile = storeProfile,
        cartItemCount = cart.size,
        onNavigate = onNavigate,
        onOpenSettings = onOpenSettings
    ) { layoutInfo, innerPadding ->
        val isTwoCol = layoutInfo.screenWidth >= 960.dp
        val hPad = if (layoutInfo.useSidebarNavigation) 20.dp else 14.dp
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = hPad, end = hPad, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period selector tabs
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
                            onClick = { selectedPeriod = key },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
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

            // Summary Card
            item {
                val isDark = MaterialTheme.colorScheme.background == DarkBackground
                val accentRed = if (isDark) DarkDangerRed else DangerRed
                SoftElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    containerColor = if (isDark) DarkSurface else Color(0xFFFFF5F5),
                    borderColor = if (isDark) DarkDangerRed.copy(alpha = 0.42f) else Color(0xFFFECACA),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(accentRed.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoneyOff,
                                        contentDescription = null,
                                        tint = accentRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = LakuStrings.get("total_expenses", lang),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) DarkTextPrimary else Color(0xFF991B1B)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = FormatUtils.formatRupiah(totalExpenseAmount),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${filteredExpenses.size} ${LakuStrings.get("expense_records_count", lang)}",
                                fontSize = 12.sp,
                                color = if (isDark) DarkTextSecondary else Color(0xFFB91C1C)
                            )
                        }

                        Button(
                            onClick = {
                                expenseToEdit = null
                                showExpenseDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("record_expense_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LakuStrings.get("add_short", lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Search and Category Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactSearchBar(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = LakuStrings.get("search_expense_placeholder", lang),
                        searchContentDescription = LakuStrings.get("search", lang),
                        clearContentDescription = LakuStrings.get("clear", lang),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_search_input")
                    )

                    // Category horizontal list
                    val allCats = listOf("Semua") + Categories.EXPENSE_CATEGORIES
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(allCats) { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = cat },
                                label = {
                                    Text(
                                        text = LakuStrings.translateExpenseCategory(cat, lang),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                    selectedBorderColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LakuStrings.get("expense_records_title", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${filteredExpenses.size} ${LakuStrings.get("data_suffix", lang)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty state
            if (filteredExpenses.isEmpty()) {
                item {
                    SoftElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = LakuStrings.get("no_expenses_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = LakuStrings.get("no_expenses_desc", lang),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    expenseToEdit = null
                                    showExpenseDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LakuStrings.get("record_first_expense", lang), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (isTwoCol) {
                val rows = filteredExpenses.chunked(2)
                items(rows, key = { row -> row.joinToString("_") { it.id.toString() } }) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { expense ->
                            Box(modifier = Modifier.weight(1f)) {
                                ExpenseItemCard(
                                    expense = expense,
                                    lang = lang,
                                    onEdit = {
                                        expenseToEdit = expense
                                        showExpenseDialog = true
                                    },
                                    onDelete = {
                                        expenseToDelete = expense
                                    }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        lang = lang,
                        onEdit = {
                            expenseToEdit = expense
                            showExpenseDialog = true
                        },
                        onDelete = {
                            expenseToDelete = expense
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Expense Dialog
    if (showExpenseDialog) {
        AddEditExpenseDialog(
            initialExpense = expenseToEdit,
            lang = lang,
            onDismiss = {
                showExpenseDialog = false
                expenseToEdit = null
            },
            onSave = { id, title, category, amount, note, timestamp ->
                if (id == 0L) {
                    viewModel.addExpense(title, category, amount, note)
                } else {
                    viewModel.updateExpense(id, title, category, amount, note, timestamp)
                }
                showExpenseDialog = false
                expenseToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = {
                Text(
                    text = LakuStrings.get("delete_expense_title", lang),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${LakuStrings.get("delete_expense_confirm", lang)} '${expenseToDelete?.title}' (${FormatUtils.formatRupiah(expenseToDelete?.amount ?: 0.0)})"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        expenseToDelete?.let { viewModel.deleteExpense(it) }
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(LakuStrings.get("delete", lang), color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { expenseToDelete = null }) {
                    Text(LakuStrings.get("cancel", lang))
                }
            }
        )
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    lang: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accentRed = if (isDark) DarkDangerRed else DangerRed
    val accentGreen = MaterialTheme.colorScheme.primary
    val accentGreenText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark

    val formattedDate = remember(expense.timestamp, lang) {
        val locale = LakuStrings.localeFor(lang)
        SimpleDateFormat("dd MMM yyyy • HH:mm", locale).format(Date(expense.timestamp))
    }

    SoftElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Icon (left) + Title & Category Badge (center-left) + Red Amount & Single-line Date (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentRed.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = accentRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = LakuStrings.translateExpenseCategory(expense.category, lang),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "- ${FormatUtils.formatRupiah(expense.amount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.5.sp,
                        color = accentRed,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.8.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Note (left) + Edit & Delete buttons (right) aligned cleanly
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    if (expense.note.isNotBlank()) {
                        Text(
                            text = "${LakuStrings.get("note_prefix", lang)} ${expense.note}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = LakuStrings.get("edit", lang),
                                tint = accentGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LakuStrings.get("edit", lang),
                                fontSize = 11.5.sp,
                                color = accentGreenText,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Surface(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        color = accentRed.copy(alpha = 0.11f),
                        border = BorderStroke(1.dp, accentRed.copy(alpha = 0.3f)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = LakuStrings.get("delete", lang),
                                tint = accentRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = LakuStrings.get("delete", lang),
                                fontSize = 11.5.sp,
                                color = accentRed,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseDialog(
    initialExpense: Expense?,
    lang: String,
    onDismiss: () -> Unit,
    onSave: (id: Long, title: String, category: String, amount: Double, note: String, timestamp: Long) -> Unit
) {
    val isEdit = initialExpense != null
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var category by remember { mutableStateOf(initialExpense?.category ?: Categories.EXPENSE_CATEGORIES.first()) }
    var amountText by remember { mutableStateOf(initialExpense?.let { FormatUtils.formatNumberWithDots(it.amount) } ?: "") }
    var note by remember { mutableStateOf(initialExpense?.note ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isEdit) Color(0xFFE0F2FE) else Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEdit) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isEdit) Color(0xFF0284C7) else DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isEdit) LakuStrings.get("edit_expense_title", lang) else LakuStrings.get("add_expense_title", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorText.isNotBlank()) {
                    Text(
                        text = errorText,
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Nama / Keterangan Pengeluaran
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorText = ""
                    },
                    label = { Text(LakuStrings.get("expense_name_label", lang), fontSize = 12.sp) },
                    placeholder = { Text(LakuStrings.get("expense_name_placeholder", lang), fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Category selector dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = LakuStrings.translateExpenseCategory(category, lang),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(LakuStrings.get("expense_category_label", lang), fontSize = 12.sp) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { categoryExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true },
                        shape = RoundedCornerShape(10.dp)
                    )

                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        Categories.EXPENSE_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(LakuStrings.translateExpenseCategory(cat, lang), fontSize = 13.sp) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Jumlah Pengeluaran
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = FormatUtils.formatInputDotted(it)
                        errorText = ""
                    },
                    label = { Text("${LakuStrings.get("expense_amount_label", lang)} (${FormatUtils.currencySymbol}) *", fontSize = 12.sp) },
                    placeholder = { Text("${LakuStrings.get("example_prefix", lang)} 50.000", fontSize = 12.sp) },
                    prefix = { Text("${FormatUtils.currencySymbol} ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Catatan tambahan (opsional)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(LakuStrings.get("expense_note_label", lang), fontSize = 12.sp) },
                    placeholder = { Text(LakuStrings.get("expense_note_placeholder", lang), fontSize = 12.sp) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = FormatUtils.parseNumberFromDotted(amountText)
                    if (title.isBlank()) {
                        errorText = LakuStrings.get("expense_name_error", lang)
                    } else if (amount <= 0.0) {
                        errorText = LakuStrings.get("expense_amount_error", lang)
                    } else {
                        onSave(
                            initialExpense?.id ?: 0L,
                            title.trim(),
                            category.trim(),
                            amount,
                            note.trim(),
                            initialExpense?.timestamp ?: System.currentTimeMillis()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(LakuStrings.get("save", lang), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(LakuStrings.get("cancel", lang))
            }
        }
    )
}
