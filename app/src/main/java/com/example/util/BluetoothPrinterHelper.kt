package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

data class PairedPrinterDevice(
    val name: String,
    val address: String
)

object BluetoothPrinterHelper {
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun hasBluetoothPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        return manager?.adapter?.isEnabled == true
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(context: Context): List<PairedPrinterDevice> {
        if (!hasBluetoothPermission(context)) return emptyList()
        return try {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter: BluetoothAdapter = manager?.adapter ?: return emptyList()
            if (!adapter.isEnabled) return emptyList()
            adapter.bondedDevices
                ?.map { device: BluetoothDevice ->
                    PairedPrinterDevice(
                        name = device.name?.ifBlank { "Bluetooth Device" } ?: "Bluetooth Device",
                        address = device.address ?: ""
                    )
                }
                ?.filter { it.address.isNotBlank() }
                ?.sortedBy { it.name.lowercase() }
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun printRawText(
        context: Context,
        printerAddress: String,
        content: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (printerAddress.isBlank() || !hasBluetoothPermission(context)) return@withContext false
        try {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = manager?.adapter ?: return@withContext false
            if (!adapter.isEnabled) return@withContext false
            val device = adapter.getRemoteDevice(printerAddress)
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.use { s ->
                s.connect()
                val out: OutputStream = s.outputStream
                // ESC @ (Initialize printer)
                out.write(byteArrayOf(0x1B, 0x40))
                out.write(content.toByteArray(Charsets.UTF_8))
                // Feed 3 lines
                out.write(byteArrayOf(0x0A, 0x0A, 0x0A))
                out.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun buildEscPosReceiptText(
        transaction: Transaction,
        storeProfile: StoreProfile
    ): String {
        val lang = storeProfile.language
        val lineLen = if (storeProfile.paperSize == "80mm") 48 else 32
        val divider = "-".repeat(lineLen)
        val doubleDivider = "=".repeat(lineLen)

        fun center(text: String): String {
            val trimmed = text.trim()
            if (trimmed.length >= lineLen) return trimmed
            val pad = (lineLen - trimmed.length) / 2
            return " ".repeat(pad) + trimmed
        }

        fun pair(left: String, right: String): String {
            val space = (lineLen - left.length - right.length).coerceAtLeast(1)
            return left + " ".repeat(space) + right
        }

        return buildString {
            appendLine(doubleDivider)
            if (storeProfile.showStoreNameOnReceipt && storeProfile.storeName.isNotBlank()) {
                appendLine(center(storeProfile.storeName.uppercase()))
            }
            if (storeProfile.showCashierNameOnReceipt && storeProfile.ownerName.isNotBlank()) {
                appendLine(center("${LakuStrings.get("cashier_label", lang)}: ${storeProfile.ownerName}"))
            }
            if (storeProfile.showStoreAddressOnReceipt && storeProfile.address.isNotBlank()) {
                appendLine(center(storeProfile.address))
            }
            if (storeProfile.showStorePhoneOnReceipt && storeProfile.phone.isNotBlank()) {
                appendLine(center("WA: ${storeProfile.phone}"))
            }
            if (storeProfile.showOperatingHoursOnReceipt && storeProfile.operatingHours.isNotBlank()) {
                appendLine(center("${LakuStrings.get("hours_label", lang)}: ${storeProfile.operatingHours}"))
            }
            appendLine(doubleDivider)
            appendLine(pair(LakuStrings.get("receipt_no", lang), transaction.transactionCode))
            appendLine(pair(LakuStrings.get("receipt_date", lang), FormatUtils.formatDateTime(transaction.timestamp, lang)))
            if (storeProfile.showCustomerNameOnReceipt && transaction.customerName.isNotBlank()) {
                appendLine(pair(LakuStrings.get("receipt_customer", lang), transaction.customerName))
            }
            appendLine(divider)
            transaction.items.forEach { item ->
                appendLine(item.productName)
                val left = "${FormatUtils.formatQty(item.quantity, item.unit)} x ${FormatUtils.formatRupiah(item.sellingPrice)}"
                val right = FormatUtils.formatRupiah(item.subtotal)
                appendLine(pair(left, right))
            }
            appendLine(divider)
            appendLine(pair(LakuStrings.get("subtotal", lang), FormatUtils.formatRupiah(transaction.subtotal)))
            if (transaction.discount > 0) {
                appendLine(pair(LakuStrings.get("discount", lang), "-${FormatUtils.formatRupiah(transaction.discount)}"))
            }
            if (transaction.ppnAmount > 0) {
                val ppnLbl = if (transaction.ppnRate > 0) "${LakuStrings.get("ppn_label", lang)} (${transaction.ppnRate.toInt()}%)" else LakuStrings.get("ppn_label", lang)
                appendLine(pair(ppnLbl, "+${FormatUtils.formatRupiah(transaction.ppnAmount)}"))
            }
            appendLine(doubleDivider)
            appendLine(pair(LakuStrings.get("receipt_total", lang), FormatUtils.formatRupiah(transaction.total)))
            appendLine(pair(LakuStrings.get("receipt_method", lang), LakuStrings.translatePaymentMethod(transaction.paymentMethod, lang)))
            if (transaction.paymentMethod == "Tunai") {
                appendLine(pair(LakuStrings.get("receipt_cash_received", lang), FormatUtils.formatRupiah(transaction.cashReceived)))
                appendLine(pair(LakuStrings.get("change", lang), FormatUtils.formatRupiah(transaction.changeAmount)))
            }
            appendLine(
                pair(
                    LakuStrings.get("receipt_status", lang),
                    if (transaction.isPaid) LakuStrings.get("paid_badge", lang) else LakuStrings.get("debt_badge", lang)
                )
            )
            appendLine(divider)
            if (storeProfile.receiptNotes.isNotBlank()) {
                storeProfile.receiptNotes.split("\n").forEach { line ->
                    appendLine(center(line))
                }
            }
            appendLine(center("Laku Kasir Pro v1.2"))
            appendLine(doubleDivider)
        }
    }

    suspend fun printTestReceipt(
        context: Context,
        storeProfile: StoreProfile
    ): Boolean {
        val sampleTx = createSampleTransaction(storeProfile)
        val text = buildEscPosReceiptText(sampleTx, storeProfile)
        return printRawText(context, storeProfile.printerAddress, text)
    }

    fun createSampleTransaction(storeProfile: StoreProfile): Transaction {
        val prefix = storeProfile.receiptPrefix.trim().ifBlank { "LK" }.uppercase()
        val items = listOf(
            com.example.data.model.TransactionItem(
                productId = 1,
                productName = "Kopi Susu Gula Aren",
                category = "Makanan & Minuman",
                unit = "pcs",
                costPrice = 10000.0,
                sellingPrice = 18000.0,
                quantity = 2.0,
                subtotal = 36000.0
            ),
            com.example.data.model.TransactionItem(
                productId = 2,
                productName = "Roti Bakar Keju",
                category = "Makanan & Minuman",
                unit = "pcs",
                costPrice = 8000.0,
                sellingPrice = 14000.0,
                quantity = 1.0,
                subtotal = 14000.0
            )
        )
        return Transaction(
            id = 0,
            transactionCode = "$prefix-20261119-001",
            timestamp = System.currentTimeMillis(),
            subtotal = 50000.0,
            discount = 0.0,
            ppnRate = if (storeProfile.ppnMode == "auto") storeProfile.defaultPpnRate else 0.0,
            ppnAmount = if (storeProfile.ppnMode == "auto") 50000.0 * (storeProfile.defaultPpnRate / 100.0) else 0.0,
            total = if (storeProfile.ppnMode == "auto") 50000.0 + (50000.0 * (storeProfile.defaultPpnRate / 100.0)) else 50000.0,
            paymentMethod = "Tunai",
            cashReceived = 100000.0,
            changeAmount = if (storeProfile.ppnMode == "auto") 100000.0 - (50000.0 + 50000.0 * (storeProfile.defaultPpnRate / 100.0)) else 50000.0,
            customerName = "Budi Santoso",
            customerPhone = "081234567890",
            isPaid = true,
            itemsJson = com.example.data.model.TransactionItem.listToJsonString(items)
        )
    }
}
