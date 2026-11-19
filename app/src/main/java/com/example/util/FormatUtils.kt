package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.model.CurrencyCatalog
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    var currentCurrencyCode: String by mutableStateOf("IDR")
    var currentLanguageCode: String by mutableStateOf("id")

    val currencySymbol: String
        get() = CurrencyCatalog.getCurrency(currentCurrencyCode).symbol

    fun formatCurrency(amount: Double, code: String = currentCurrencyCode): String {
        val curr = CurrencyCatalog.getCurrency(code)
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }

        val pattern = if (curr.decimalDigits > 0) {
            val decimals = "0".repeat(curr.decimalDigits)
            "#,##0.$decimals"
        } else {
            "#,##0"
        }

        val formatter = DecimalFormat(pattern, symbols)
        val formattedNumber = formatter.format(amount)

        return if (curr.isSymbolPrefix) {
            "${curr.symbol} $formattedNumber"
        } else {
            "$formattedNumber ${curr.symbol}"
        }
    }

    fun formatInputDotted(rawInput: String): String {
        val cleanDigits = rawInput.filter { it.isDigit() }
        if (cleanDigits.isEmpty()) return ""
        val trimmed = cleanDigits.trimStart('0').ifEmpty { "0" }
        return try {
            val number = trimmed.toLong()
            val symbols = DecimalFormatSymbols(Locale.US).apply {
                groupingSeparator = '.'
            }
            val formatter = DecimalFormat("#,##0", symbols)
            formatter.format(number)
        } catch (e: Exception) {
            trimmed
        }
    }

    fun parseNumberFromDotted(dottedStr: String): Double {
        if (dottedStr.isBlank()) return 0.0
        val clean = dottedStr.replace(".", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    fun formatNumberWithDots(number: Double): String {
        if (number == 0.0) return ""
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '.'
        }
        val formatter = DecimalFormat("#,##0", symbols)
        return formatter.format(number.toLong())
    }

    fun formatRupiah(amount: Double): String {
        return formatCurrency(amount, currentCurrencyCode)
    }

    fun formatQty(qty: Double, unit: String): String {
        val localizedUnit = LakuStrings.translateUnit(unit, currentLanguageCode)
        return if (qty % 1.0 == 0.0) {
            "${qty.toInt()} $localizedUnit"
        } else {
            String.format(Locale.US, "%.2f %s", qty, localizedUnit)
        }
    }

    fun formatQuantity(qty: Double): String {
        return if (qty % 1.0 == 0.0) {
            qty.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", qty)
        }
    }

    fun formatDateTime(timestamp: Long, lang: String = currentLanguageCode): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", LakuStrings.localeFor(lang))
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long, lang: String = currentLanguageCode): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", LakuStrings.localeFor(lang))
        return sdf.format(Date(timestamp))
    }

    fun openWhatsApp(context: Context, phone: String, message: String = "") {
        if (phone.isBlank()) {
            Toast.makeText(context, LakuStrings.get("whatsapp_no", currentLanguageCode), Toast.LENGTH_SHORT).show()
            return
        }
        var cleanPhone = phone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "62" + cleanPhone.substring(1)
        }
        try {
            val encodedMsg = Uri.encode(message)
            val uri = Uri.parse("https://wa.me/$cleanPhone?text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openDialer(context: Context, phone: String) {
        if (phone.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Dialer error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, title: String, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
