package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.Expense
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import com.example.data.model.TransactionItem
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CartItem(
    val product: Product,
    val quantity: Double
) {
    val subtotal: Double
        get() = product.sellingPrice * quantity

    val costSubtotal: Double
        get() = product.costPrice * quantity
}

enum class Screen {
    SPLASH,
    POS,
    CHECKOUT,
    PRODUCTS,
    HISTORY,
    DASHBOARD,
    SETTINGS,
    ADD_EDIT_PRODUCT,
    RECEIPT_VIEW,
    EXPENSES
}

class LakuKasirViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val productDao = db.productDao()
    private val transactionDao = db.transactionDao()
    private val expenseDao = db.expenseDao()
    private val prefsManager = PreferencesManager(application)

    val storeProfile: StateFlow<StoreProfile> = prefsManager.storeProfile

    val isProLicense: StateFlow<Boolean> = com.example.util.LicenseManager.isProFlow

    // Trial Limit & Activation Dialog States
    val trialLimitDialogMessage = MutableStateFlow<String?>(null)
    val showActivationDialog = MutableStateFlow(false)

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Splash display helper
    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished.asStateFlow()

    // Products Flow
    val allProducts: StateFlow<List<Product>> = productDao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transactions Flow
    val allTransactions: StateFlow<List<Transaction>> = transactionDao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expenses Flow
    val allExpenses: StateFlow<List<Expense>> = expenseDao.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product search and filter in POS
    val posSearchQuery = MutableStateFlow("")
    val posSelectedCategory = MutableStateFlow("Semua")

    // Filtered Products for POS
    val filteredPosProducts = combine(
        allProducts,
        posSearchQuery,
        posSelectedCategory
    ) { products, query, cat ->
        products.filter { p ->
            val matchQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true)
            val matchCategory = cat == "Semua" || p.category.equals(cat, ignoreCase = true)
            matchQuery && matchCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    // Checkout Details
    val customerName = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val discountMode = MutableStateFlow(storeProfile.value.discountMode) // "manual" or "percent"
    val manualDiscount = MutableStateFlow(formatDefaultDiscount(storeProfile.value))
    val isPpnAuto = MutableStateFlow(storeProfile.value.ppnMode == "auto")
    val ppnRate = MutableStateFlow(storeProfile.value.defaultPpnRate)
    val manualPpnAmount = MutableStateFlow("")
    val selectedPaymentMethod = MutableStateFlow("Tunai")
    val cashReceivedInput = MutableStateFlow("")

    // Active product for edit
    val productToEdit = MutableStateFlow<Product?>(null)

    // Last completed transaction for Receipt view
    val activeReceiptTransaction = MutableStateFlow<Transaction?>(null)

    // Period filter for Dashboard: "today", "week", "month", "all"
    val dashboardPeriod = MutableStateFlow("today")

    init {
        com.example.util.LicenseManager.init(application)
    }

    private fun formatDefaultDiscount(profile: StoreProfile): String {
        if (profile.defaultDiscountValue <= 0.0) return ""
        return if (profile.discountMode == "percent") {
            if (profile.defaultDiscountValue % 1.0 == 0.0) {
                profile.defaultDiscountValue.toInt().toString()
            } else {
                profile.defaultDiscountValue.toString()
            }
        } else {
            FormatUtils.formatNumberWithDots(profile.defaultDiscountValue)
        }
    }

    fun openActivationDialog() {
        showActivationDialog.value = true
    }

    fun closeActivationDialog() {
        showActivationDialog.value = false
    }

    fun clearTrialLimitDialog() {
        trialLimitDialogMessage.value = null
    }

    fun activateLicense(key: String): Boolean {
        return com.example.util.LicenseManager.activateWithKey(key)
    }

    fun resetToTrial() {
        com.example.util.LicenseManager.resetToTrial()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun finishSplash() {
        _splashFinished.value = true
        _currentScreen.value = Screen.POS
    }

    // Cart operations
    fun addToCart(product: Product, quantity: Double = 1.0) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            val newQty = existing.quantity + quantity
            currentList[index] = existing.copy(quantity = newQty)
        } else {
            currentList.add(CartItem(product, quantity))
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(productId: Long, newQuantity: Double) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            if (newQuantity <= 0.0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = currentList[index].copy(quantity = newQuantity)
            }
            _cart.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        val profile = storeProfile.value
        _cart.value = emptyList()
        customerName.value = ""
        customerPhone.value = ""
        discountMode.value = profile.discountMode
        manualDiscount.value = formatDefaultDiscount(profile)
        manualPpnAmount.value = ""
        isPpnAuto.value = profile.ppnMode == "auto"
        ppnRate.value = profile.defaultPpnRate
        cashReceivedInput.value = ""
        selectedPaymentMethod.value = "Tunai"
    }

    // Calculations for Cart & Checkout
    val cartSubtotal: Double
        get() = _cart.value.sumOf { it.subtotal }

    fun calculateDiscountAmount(subtotal: Double, mode: String, input: String): Double {
        if (input.isBlank()) return 0.0
        return if (mode == "percent") {
            val pct = input.replace(",", ".").toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
            subtotal * (pct / 100.0)
        } else {
            FormatUtils.parseNumberFromDotted(input).coerceAtMost(subtotal)
        }
    }

    val discountAmount: Double
        get() = calculateDiscountAmount(cartSubtotal, discountMode.value, manualDiscount.value)

    val calculatedPpnAmount: Double
        get() = when {
            isPpnAuto.value -> {
                val afterDiscount = maxOf(0.0, cartSubtotal - discountAmount)
                (afterDiscount * (ppnRate.value / 100.0))
            }
            manualPpnAmount.value.isNotBlank() -> FormatUtils.parseNumberFromDotted(manualPpnAmount.value)
            else -> 0.0
        }

    val totalPayable: Double
        get() = maxOf(0.0, (cartSubtotal - discountAmount) + calculatedPpnAmount)

    val cashReceivedAmount: Double
        get() = FormatUtils.parseNumberFromDotted(cashReceivedInput.value)

    val changeAmount: Double
        get() = if (selectedPaymentMethod.value == "Tunai") {
            maxOf(0.0, cashReceivedAmount - totalPayable)
        } else {
            0.0
        }

    fun setExactCash() {
        cashReceivedInput.value = FormatUtils.formatInputDotted(totalPayable.toInt().toString())
    }

    fun addQuickCash(amount: Double) {
        val current = FormatUtils.parseNumberFromDotted(cashReceivedInput.value)
        cashReceivedInput.value = FormatUtils.formatInputDotted((current + amount).toInt().toString())
    }

    // Checkout process
    fun processCheckout(onSuccess: (Transaction) -> Unit) {
        val items = _cart.value
        if (items.isEmpty()) return
        if (!com.example.util.LicenseManager.isProActivated()) {
            val currentTxCount = allTransactions.value.size
            if (currentTxCount >= com.example.util.LicenseManager.MAX_TRIAL_TRANSACTIONS) {
                trialLimitDialogMessage.value = "Batas Uji Coba Tercapai!\nVersi Trial dibatasi maksimal ${com.example.util.LicenseManager.MAX_TRIAL_TRANSACTIONS} kali transaksi. Silakan aktivasi lisensi PRO untuk melanjutkan transaksi kasir tanpa batas."
                showActivationDialog.value = true
                return
            }
        }

        val now = System.currentTimeMillis()
        val prefix = storeProfile.value.receiptPrefix.trim().ifBlank { "LK" }.uppercase()
        val codePrefix = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(now))
        val randomSuffix = (100..999).random()
        val code = "$prefix-$codePrefix-$randomSuffix"

        val transactionItems = items.map {
            TransactionItem(
                productId = it.product.id,
                productName = it.product.name,
                category = it.product.category,
                unit = it.product.unit,
                costPrice = it.product.costPrice,
                sellingPrice = it.product.sellingPrice,
                quantity = it.quantity,
                subtotal = it.subtotal
            )
        }

        val effectiveCashReceived = if (selectedPaymentMethod.value == "Tunai") {
            if (cashReceivedAmount > 0) cashReceivedAmount else totalPayable
        } else {
            totalPayable
        }

        val effectiveChange = if (selectedPaymentMethod.value == "Tunai" && effectiveCashReceived > totalPayable) {
            effectiveCashReceived - totalPayable
        } else {
            0.0
        }

        val isPaid = selectedPaymentMethod.value != "Kasbon"

        val discountNoteText = when {
            discountAmount <= 0.0 -> ""
            discountMode.value == "percent" -> "Diskon ${manualDiscount.value}%"
            else -> "Diskon Langsung"
        }

        val transaction = Transaction(
            transactionCode = code,
            timestamp = now,
            subtotal = cartSubtotal,
            discount = discountAmount,
            discountNote = discountNoteText,
            ppnRate = if (isPpnAuto.value) ppnRate.value else 0.0,
            ppnAmount = calculatedPpnAmount,
            total = totalPayable,
            paymentMethod = selectedPaymentMethod.value,
            cashReceived = effectiveCashReceived,
            changeAmount = effectiveChange,
            customerName = customerName.value.trim(),
            customerPhone = customerPhone.value.trim(),
            isPaid = isPaid,
            itemsJson = TransactionItem.listToJsonString(transactionItems)
        )

        viewModelScope.launch {
            val id = transactionDao.insert(transaction)
            val savedTx = transaction.copy(id = id)

            items.forEach { item ->
                if (!item.product.isUnlimitedStock) {
                    val updatedStock = maxOf(0.0, item.product.stock - item.quantity)
                    productDao.updateStock(item.product.id, updatedStock)
                }
            }

            clearCart()

            activeReceiptTransaction.value = savedTx
            _currentScreen.value = Screen.RECEIPT_VIEW
            onSuccess(savedTx)
        }
    }

    // Product CRUD
    fun saveProduct(
        id: Long = 0,
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
    ) {
        if (id == 0L && !com.example.util.LicenseManager.isProActivated()) {
            val currentProductCount = allProducts.value.size
            if (currentProductCount >= com.example.util.LicenseManager.MAX_TRIAL_PRODUCTS) {
                trialLimitDialogMessage.value = "Batas Produk Uji Coba Tercapai!\nVersi Trial dibatasi maksimal ${com.example.util.LicenseManager.MAX_TRIAL_PRODUCTS} produk. Silakan aktivasi lisensi PRO untuk menambahkan produk tanpa batas."
                showActivationDialog.value = true
                return
            }
        }

        viewModelScope.launch {
            val product = Product(
                id = id,
                name = name.trim(),
                category = category.trim(),
                unit = unit.trim(),
                barcode = barcode.trim(),
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                stock = if (isUnlimitedStock) 0.0 else stock,
                isUnlimitedStock = isUnlimitedStock,
                minStockAlert = minStockAlert,
                supplierName = supplierName.trim(),
                supplierPhone = supplierPhone.trim(),
                imageUri = imageUri
            )
            if (id == 0L) {
                productDao.insert(product)
            } else {
                productDao.update(product)
            }
            productToEdit.value = null
            _currentScreen.value = Screen.PRODUCTS
        }
    }

    fun loadDemoSampleProducts() {
        viewModelScope.launch {
            val sampleItems = listOf(
                Product(name = "Kopi Susu Gula Aren", category = "Makanan & Minuman", unit = "pcs", barcode = "DEMO-01", costPrice = 8000.0, sellingPrice = 18000.0, stock = 50.0, isUnlimitedStock = false, minStockAlert = 5.0, supplierName = "Kopi Nusantara", supplierPhone = "081234567890"),
                Product(name = "Roti Bakar Cokelat Keju", category = "Makanan & Minuman", unit = "pcs", barcode = "DEMO-02", costPrice = 10000.0, sellingPrice = 22000.0, stock = 30.0, isUnlimitedStock = false, minStockAlert = 5.0, supplierName = "Bakery Indo", supplierPhone = "081234567890")
            )
            sampleItems.forEach { productDao.insert(it) }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productDao.delete(product)
        }
    }

    fun quickUpdateStock(product: Product, delta: Double) {
        if (product.isUnlimitedStock) return
        val newStock = maxOf(0.0, product.stock + delta)
        viewModelScope.launch {
            productDao.updateStock(product.id, newStock)
        }
    }

    // Expense
    fun addExpense(title: String, category: String, amount: Double, note: String) {
        viewModelScope.launch {
            val expense = Expense(
                title = title.trim(),
                category = category.trim(),
                amount = amount,
                note = note.trim()
            )
            expenseDao.insert(expense)
        }
    }

    fun updateExpense(id: Long, title: String, category: String, amount: Double, note: String, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val expense = Expense(
                id = id,
                title = title.trim(),
                category = category.trim(),
                amount = amount,
                timestamp = timestamp,
                note = note.trim()
            )
            expenseDao.update(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseDao.delete(expense)
        }
    }

    // Mark Kasbon as Paid
    fun markTransactionAsPaid(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.updatePaymentStatus(transaction.id, true)
            if (activeReceiptTransaction.value?.id == transaction.id) {
                activeReceiptTransaction.value = transaction.copy(isPaid = true)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
            if (activeReceiptTransaction.value?.id == transaction.id) {
                activeReceiptTransaction.value = null
                _currentScreen.value = Screen.HISTORY
            }
        }
    }

    // Settings
    fun updateStoreProfile(profile: StoreProfile) {
        val old = storeProfile.value
        prefsManager.saveStoreProfile(profile)
        // Sync active checkout defaults when tax/discount settings change
        if (old.ppnMode != profile.ppnMode || old.defaultPpnRate != profile.defaultPpnRate) {
            isPpnAuto.value = profile.ppnMode == "auto"
            ppnRate.value = profile.defaultPpnRate
        }
        if (old.discountMode != profile.discountMode || old.defaultDiscountValue != profile.defaultDiscountValue) {
            discountMode.value = profile.discountMode
            manualDiscount.value = formatDefaultDiscount(profile)
        }
    }

    fun setThemeMode(mode: String) {
        prefsManager.setThemeMode(mode)
    }

    fun setLanguage(lang: String) {
        prefsManager.setLanguage(lang)
    }

    fun setCurrency(code: String) {
        prefsManager.setCurrency(code)
    }

    fun updateLastBackupTimestamp(timestamp: Long = System.currentTimeMillis()) {
        prefsManager.updateLastBackupTimestamp(timestamp)
    }
}
