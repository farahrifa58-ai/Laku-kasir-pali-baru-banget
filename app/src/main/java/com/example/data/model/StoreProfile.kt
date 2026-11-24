package com.example.data.model

data class StoreProfile(
    val storeName: String = "Laku Kasir Store",
    val ownerName: String = "Rafline",
    val address: String = "Jl. Sudirman No. 12, Jakarta",
    val phone: String = "081234567890",
    val operatingHours: String = "08:00 - 21:00",
    val receiptNotes: String = "Terima kasih telah berbelanja!\nBarang yang sudah dibeli tidak dapat ditukar atau dikembalikan.",
    val logoUri: String? = null,
    val storeIconKey: String = "default_logo", // "default_logo" or icon key from StoreIconCatalog
    val storeIconBgColor: Long = 0xFF16A34A,   // ARGB Long
    // Receipt customization toggles
    val showStoreNameOnReceipt: Boolean = true,
    val showStoreLogoOnReceipt: Boolean = true,
    val showCashierNameOnReceipt: Boolean = true,
    val showStoreAddressOnReceipt: Boolean = true,
    val showStorePhoneOnReceipt: Boolean = true,
    val showCustomerNameOnReceipt: Boolean = true,
    val showOperatingHoursOnReceipt: Boolean = true,
    val receiptPrefix: String = "LK",
    // Tax (PPN) & Discount settings
    val ppnMode: String = "manual",       // "auto" or "manual"
    val defaultPpnRate: Double = 11.0,
    val discountMode: String = "manual",  // "manual" or "percent"
    val defaultDiscountValue: Double = 0.0,
    // Printer settings
    val printerName: String = "",
    val printerAddress: String = "",
    val paperSize: String = "58mm",       // "58mm" or "80mm"
    // Backup metadata
    val lastBackupTimestamp: Long = 0L,
    // Theme & Language
    val themeMode: String = "light",      // "light", "dark", "system"
    val language: String = "id",
    val currencyCode: String = "IDR",
    val appVersion: String = "1.2"
)
