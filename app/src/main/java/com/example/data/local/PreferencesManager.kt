package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.StoreProfile
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("laku_kasir_prefs", Context.MODE_PRIVATE)

    private val _storeProfile = MutableStateFlow(loadStoreProfile())
    val storeProfile: StateFlow<StoreProfile> = _storeProfile.asStateFlow()

    private fun loadStoreProfile(): StoreProfile {
        val currency = prefs.getString("currency_code", "IDR") ?: "IDR"
        val language = prefs.getString("language", "id") ?: "id"
        FormatUtils.currentCurrencyCode = currency
        FormatUtils.currentLanguageCode = language
        return StoreProfile(
            storeName = prefs.getString("store_name", "Laku Kasir Store") ?: "Laku Kasir Store",
            ownerName = prefs.getString("owner_name", "Rafline") ?: "Rafline",
            address = prefs.getString("address", "Jl. Merdeka No. 45, Jakarta") ?: "Jl. Merdeka No. 45, Jakarta",
            phone = prefs.getString("phone", "081234567890") ?: "081234567890",
            operatingHours = prefs.getString("operating_hours", "08:00 - 21:00") ?: "08:00 - 21:00",
            receiptNotes = prefs.getString(
                "receipt_notes",
                "Terima kasih telah berbelanja di Laku Kasir!\nBarang yang sudah dibeli tidak dapat ditukar."
            ) ?: "Terima kasih telah berbelanja!",
            logoUri = prefs.getString("logo_uri", null),
            storeIconKey = prefs.getString("store_icon_key", "default_logo") ?: "default_logo",
            storeIconBgColor = prefs.getLong("store_icon_bg_color", 0xFF16A34A),
            showStoreNameOnReceipt = prefs.getBoolean("show_store_name_receipt", true),
            showStoreLogoOnReceipt = prefs.getBoolean("show_store_logo_receipt", true),
            showCashierNameOnReceipt = prefs.getBoolean("show_cashier_name_receipt", true),
            showStoreAddressOnReceipt = prefs.getBoolean("show_store_address_receipt", true),
            showStorePhoneOnReceipt = prefs.getBoolean("show_store_phone_receipt", true),
            showCustomerNameOnReceipt = prefs.getBoolean("show_customer_name_receipt", true),
            showOperatingHoursOnReceipt = prefs.getBoolean("show_operating_hours_receipt", true),
            receiptPrefix = prefs.getString("receipt_prefix", "LK") ?: "LK",
            ppnMode = prefs.getString("ppn_mode", "manual") ?: "manual",
            defaultPpnRate = prefs.getFloat("default_ppn_rate", 11.0f).toDouble(),
            discountMode = prefs.getString("discount_mode", "manual") ?: "manual",
            defaultDiscountValue = prefs.getFloat("default_discount_value", 0.0f).toDouble(),
            printerName = prefs.getString("printer_name", "") ?: "",
            printerAddress = prefs.getString("printer_address", "") ?: "",
            paperSize = prefs.getString("paper_size", "58mm") ?: "58mm",
            lastBackupTimestamp = prefs.getLong("last_backup_timestamp", 0L),
            themeMode = prefs.getString("theme_mode", "light") ?: "light",
            language = language,
            currencyCode = currency,
            appVersion = "1.2"
        )
    }

    fun saveStoreProfile(profile: StoreProfile) {
        FormatUtils.currentCurrencyCode = profile.currencyCode
        FormatUtils.currentLanguageCode = profile.language
        prefs.edit()
            .putString("store_name", profile.storeName)
            .putString("owner_name", profile.ownerName)
            .putString("address", profile.address)
            .putString("phone", profile.phone)
            .putString("operating_hours", profile.operatingHours)
            .putString("receipt_notes", profile.receiptNotes)
            .putString("logo_uri", profile.logoUri)
            .putString("store_icon_key", profile.storeIconKey)
            .putLong("store_icon_bg_color", profile.storeIconBgColor)
            .putBoolean("show_store_name_receipt", profile.showStoreNameOnReceipt)
            .putBoolean("show_store_logo_receipt", profile.showStoreLogoOnReceipt)
            .putBoolean("show_cashier_name_receipt", profile.showCashierNameOnReceipt)
            .putBoolean("show_store_address_receipt", profile.showStoreAddressOnReceipt)
            .putBoolean("show_store_phone_receipt", profile.showStorePhoneOnReceipt)
            .putBoolean("show_customer_name_receipt", profile.showCustomerNameOnReceipt)
            .putBoolean("show_operating_hours_receipt", profile.showOperatingHoursOnReceipt)
            .putString("receipt_prefix", profile.receiptPrefix)
            .putString("ppn_mode", profile.ppnMode)
            .putFloat("default_ppn_rate", profile.defaultPpnRate.toFloat())
            .putString("discount_mode", profile.discountMode)
            .putFloat("default_discount_value", profile.defaultDiscountValue.toFloat())
            .putString("printer_name", profile.printerName)
            .putString("printer_address", profile.printerAddress)
            .putString("paper_size", profile.paperSize)
            .putLong("last_backup_timestamp", profile.lastBackupTimestamp)
            .putString("theme_mode", profile.themeMode)
            .putString("language", profile.language)
            .putString("currency_code", profile.currencyCode)
            .apply()
        _storeProfile.value = profile
    }

    fun setThemeMode(mode: String) {
        saveStoreProfile(_storeProfile.value.copy(themeMode = mode))
    }

    fun setLanguage(lang: String) {
        saveStoreProfile(_storeProfile.value.copy(language = lang))
    }

    fun setCurrency(code: String) {
        saveStoreProfile(_storeProfile.value.copy(currencyCode = code))
    }

    fun updateLastBackupTimestamp(timestamp: Long) {
        saveStoreProfile(_storeProfile.value.copy(lastBackupTimestamp = timestamp))
    }

    fun hasSeededSampleProducts(): Boolean {
        return prefs.getBoolean("sample_products_seeded_v2", false)
    }

    fun setSampleProductsSeeded(seeded: Boolean) {
        prefs.edit().putBoolean("sample_products_seeded_v2", seeded).apply()
    }
}
