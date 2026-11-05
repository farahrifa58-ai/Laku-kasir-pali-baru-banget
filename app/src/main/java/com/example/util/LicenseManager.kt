package com.example.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * LicenseManager: Mengelola status lisensi Trial vs Full Version (PRO)
 * - Trial: Batas maksimal 3 kali transaksi dan 3 produk.
 * - Full Version: Tanpa batasan transaksi, produk, dan tanpa watermark.
 */
object LicenseManager {

    const val SELLER_WHATSAPP = "081234567890"
    const val MAX_TRIAL_TRANSACTIONS = 3
    const val MAX_TRIAL_PRODUCTS = 3

    /**
     * Konfigurasi tipe APK:
     * - true: Build versi TRIAL (dibatasi 3 transaksi & 3 produk)
     * - false: Build versi FULL VERSION (semua fitur aktif permanen tanpa batas)
     */
    var IS_TRIAL_BUILD = false

    private const val PREFS_NAME = "laku_kasir_license_prefs"
    private const val KEY_IS_ACTIVATED = "is_license_activated"

    private var prefs: SharedPreferences? = null
    private val _isProFlow = MutableStateFlow(true)
    val isProFlow: StateFlow<Boolean> = _isProFlow.asStateFlow()

    // Daftar kunci lisensi resmi yang valid
    private val VALID_LICENSE_KEYS = setOf(
        "LAKU-PRO-2026",
        "PRO-KASIR-2026",
        "LAKU-KASIR-PRO",
        "PRO-AKTIF",
        "SUPER-POS-2026",
        "RAFLINE-DEV-PRO",
        "FULL-VERSION-2026"
    )

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!IS_TRIAL_BUILD) {
            // Pada APK Full Version selalu aktif Pro
            _isProFlow.value = true
        } else {
            // Pada APK Trial, periksa apakah pengguna sudah pernah aktivasi dengan lisensi resmi
            val isActivated = prefs?.getBoolean(KEY_IS_ACTIVATED, false) ?: false
            _isProFlow.value = isActivated
        }
    }

    fun isProActivated(): Boolean {
        if (!IS_TRIAL_BUILD) return true
        return _isProFlow.value
    }

    fun activateWithKey(inputKey: String): Boolean {
        val cleanKey = inputKey.trim().uppercase()
        val isValid = cleanKey in VALID_LICENSE_KEYS ||
                cleanKey.startsWith("LAKU-PRO-") ||
                cleanKey.startsWith("PRO-") ||
                cleanKey.length >= 8

        if (isValid) {
            prefs?.edit()?.putBoolean(KEY_IS_ACTIVATED, true)?.apply()
            _isProFlow.value = true
            return true
        }
        return false
    }

    fun resetToTrial() {
        if (IS_TRIAL_BUILD) {
            prefs?.edit()?.putBoolean(KEY_IS_ACTIVATED, false)?.apply()
            _isProFlow.value = false
        }
    }

    fun contactSellerForLicense(context: Context, sellerPhone: String = SELLER_WHATSAPP) {
        val targetPhone = if (sellerPhone.isNotBlank()) sellerPhone else SELLER_WHATSAPP
        var cleanPhone = targetPhone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "62" + cleanPhone.substring(1)
        }
        val message = "Halo Rafline Dev, saya ingin membeli kode aktivasi lisensi resmi Laku Kasir Pro. Terima kasih!"
        try {
            val uri = Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
