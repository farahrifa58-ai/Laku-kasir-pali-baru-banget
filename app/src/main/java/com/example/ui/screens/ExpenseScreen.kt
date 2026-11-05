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
import androidx.compose.material3.FloatingActionButton
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

    Scaffold(
        topBar = {
            AppTopBar(
                title = LakuStrings.get("expenses_title", lang),
                storeProfile = storeProfile,
                onSettingsClick = onOpenSettings
            )
        },
        bottomBar = {
            AppBottomBar(
                currentScreen = Screen.EXPENSES,
                cartItemCount = cart.size,
                lang = lang,
                onNavigate = onNavigate
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    expenseToEdit = null
                    showExpenseDialog = true
                },
                containerColor = EmeraldGreenPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Pengeluaran")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period selector tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val periods = listOf(
                        "today" to (if (lang == "en") "Today" else "Hari Ini"),
                        "week" to (if (lang == "en") "7 Days" else "7 Hari"),
                        "month" to (if (lang == "en") "This Month" else "Bulan Ini"),
                        "all" to (if (lang == "en") "All" else "Semua")
                    )

                    periods.forEach { (key, label) ->
                        val isSelected = selectedPeriod == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPeriod = key },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)), // Soft rose red
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                                        .background(DangerRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoneyOff,
                                        contentDescription = null,
                                        tint = DangerRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "en") "Total Expenses" else "Total Pengeluaran",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = FormatUtils.formatRupiah(totalExpenseAmount),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DangerRed
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${filteredExpenses.size} catatan pengeluaran",
                                fontSize = 12.sp,
                                color = Color(0xFFB91C1C)
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
                                text = if (lang == "en") "Add" else "Catat",
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
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_search_input"),
                        placeholder = { Text("Cari catatan pengeluaran...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
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
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
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
                        text = if (lang == "en") "Expense Records" else "Daftar Pengeluaran",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${filteredExpenses.size} data",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty state
            if (filteredExpenses.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
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
                                text = if (lang == "en") "No expenses recorded yet" else "Belum ada catatan pengeluaran",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (lang == "en") "Tap the button below to record store expenses." else "Catat pengeluaran seperti listrik, sewa tempat, gaji, dan kebutuhan toko lainnya.",
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
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Catat Pengeluaran Pertama")
                            }
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
                    text = if (lang == "en") "Delete Expense?" else "Hapus Catatan Pengeluaran?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (lang == "en") {
                        "Are you sure you want to delete '${expenseToDelete?.title}' (${FormatUtils.formatRupiah(expenseToDelete?.amount ?: 0.0)})?"
                    } else {
                        "Apakah Anda yakin ingin menghapus catatan pengeluaran '${expenseToDelete?.title}' sebesar ${FormatUtils.formatRupiah(expenseToDelete?.amount ?: 0.0)}?"
                    }
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
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(expense.timestamp) { dateFormat.format(Date(expense.timestamp)) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DangerRed.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = expense.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = expense.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedDate,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Amount
                Text(
                    text = "- ${FormatUtils.formatRupiah(expense.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = DangerRed
                )
            }

            if (expense.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Catatan: ${expense.note}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Edit and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = LakuStrings.get("edit", lang),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = DangerRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
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
                    text = if (isEdit) {
                        if (lang == "en") "Edit Expense Record" else "Edit Catatan Pengeluaran"
                    } else {
                        if (lang == "en") "Add Store Expense" else "Catat Pengeluaran Baru"
                    },
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
                    label = { Text("Nama / Keterangan Pengeluaran *", fontSize = 12.sp) },
                    placeholder = { Text("cth: Listrik Toko, Beli Kresek", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Category selector dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori Pengeluaran", fontSize = 12.sp) },
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
                                text = { Text(cat, fontSize = 13.sp) },
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
                    label = { Text("Jumlah Biaya (${FormatUtils.currencySymbol}) *", fontSize = 12.sp) },
                    placeholder = { Text("cth: 50.000", fontSize = 12.sp) },
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
                    label = { Text("Catatan Tambahan (Opsional)", fontSize = 12.sp) },
                    placeholder = { Text("cth: Pembayaran via transfer BCA", fontSize = 12.sp) },
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
                        errorText = "Mohon isi nama / keterangan pengeluaran."
                    } else if (amount <= 0.0) {
                        errorText = "Jumlah biaya harus lebih besar dari 0."
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
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(LakuStrings.get("save", lang), color = Color.White, fontWeight = FontWeight.Bold)
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
