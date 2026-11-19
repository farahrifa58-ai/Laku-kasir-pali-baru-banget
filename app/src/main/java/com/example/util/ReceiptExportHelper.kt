package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ReceiptExportHelper {

    private fun getRoundedCornerBitmap(bitmap: Bitmap, cornerRadius: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = android.graphics.RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return output
    }

    /**
     * Renders a realistic receipt image into a Bitmap respecting StoreProfile receipt customization and paper size.
     */
    fun generateReceiptBitmap(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ): Bitmap {
        val lang = storeProfile.language
        val is58mm = storeProfile.paperSize == "58mm"
        val width = if (is58mm) 440 else 576
        val margin = if (is58mm) 24f else 32f
        val baseHeight = 900
        val itemsHeight = transaction.items.size * 66
        val notesLines = storeProfile.receiptNotes.split("\n").size * 32
        val trialExtraHeight = if (!LicenseManager.isProActivated()) 90 else 0
        val height = baseHeight + itemsHeight + notesLines + trialExtraHeight

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val textSizeMain = if (is58mm) 20f else 24f
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = textSizeMain
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = textSizeMain + 1f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#15803D")
            textSize = if (is58mm) 28f else 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = if (is58mm) 19f else 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            strokeWidth = 2f
        }

        var y = 40f

        // Store Logo (if enabled)
        if (storeProfile.showStoreLogoOnReceipt) {
            val logoSize = if (is58mm) 86 else 104
            val logoX = (width - logoSize) / 2f
            val customBitmap = ImageUtils.loadBitmap(context, storeProfile.logoUri)

            if (customBitmap != null) {
                val scaledLogo = Bitmap.createScaledBitmap(customBitmap, logoSize, logoSize, true)
                val roundedLogo = getRoundedCornerBitmap(scaledLogo, 16f)
                canvas.drawBitmap(roundedLogo, logoX, y, null)
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = Color.parseColor("#E2E8F0")
                }
                canvas.drawRoundRect(
                    android.graphics.RectF(logoX, y, logoX + logoSize, y + logoSize),
                    16f, 16f, borderPaint
                )
                y += logoSize + 42f
            } else if (storeProfile.storeIconKey != "default_logo") {
                val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = storeProfile.storeIconBgColor.toInt()
                }
                canvas.drawRoundRect(
                    android.graphics.RectF(logoX, y, logoX + logoSize, y + logoSize),
                    20f, 20f, badgePaint
                )
                val initial = storeProfile.storeName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "S"
                val initialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = logoSize * 0.52f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                val textY = y + (logoSize / 2f) - ((initialPaint.descent() + initialPaint.ascent()) / 2f)
                canvas.drawText(initial, width / 2f, textY, initialPaint)
                y += logoSize + 42f
            } else {
                val defaultLogo = android.graphics.BitmapFactory.decodeResource(
                    context.resources,
                    com.example.R.drawable.logo_laku_kasir
                )
                if (defaultLogo != null) {
                    val scaledLogo = Bitmap.createScaledBitmap(defaultLogo, logoSize, logoSize, true)
                    val roundedLogo = getRoundedCornerBitmap(scaledLogo, 16f)
                    canvas.drawBitmap(roundedLogo, logoX, y, null)
                    y += logoSize + 42f
                }
            }
        } else {
            y += 16f
        }

        // Store Header fields according to customization toggles
        if (storeProfile.showStoreNameOnReceipt && storeProfile.storeName.isNotBlank()) {
            canvas.drawText(storeProfile.storeName.uppercase(), width / 2f, y, headerPaint)
            y += 34f
        }
        if (storeProfile.showCashierNameOnReceipt && storeProfile.ownerName.isNotBlank()) {
            canvas.drawText("${LakuStrings.get("cashier_label", lang)}: ${storeProfile.ownerName}", width / 2f, y, subHeaderPaint)
            y += 28f
        }
        if (storeProfile.showStoreAddressOnReceipt && storeProfile.address.isNotBlank()) {
            canvas.drawText(storeProfile.address, width / 2f, y, subHeaderPaint)
            y += 28f
        }
        if (storeProfile.showStorePhoneOnReceipt && storeProfile.phone.isNotBlank()) {
            canvas.drawText("WA: ${storeProfile.phone}", width / 2f, y, subHeaderPaint)
            y += 28f
        }
        if (storeProfile.showOperatingHoursOnReceipt && storeProfile.operatingHours.isNotBlank()) {
            canvas.drawText("${LakuStrings.get("hours_label", lang)}: ${storeProfile.operatingHours}", width / 2f, y, subHeaderPaint)
            y += 28f
        }
        y += 10f

        canvas.drawLine(margin, y, width - margin, y, linePaint)
        y += 32f

        fun drawRow(label: String, value: String, isBold: Boolean = false) {
            val p = if (isBold) boldPaint else textPaint
            canvas.drawText(label, margin, y, textPaint)
            val valWidth = p.measureText(value)
            canvas.drawText(value, width - margin - valWidth, y, p)
            y += 34f
        }

        drawRow(LakuStrings.get("receipt_no", lang), transaction.transactionCode, true)
        drawRow(LakuStrings.get("receipt_date", lang), FormatUtils.formatDateTime(transaction.timestamp, lang))
        if (storeProfile.showCustomerNameOnReceipt && transaction.customerName.isNotBlank()) {
            drawRow(LakuStrings.get("receipt_customer", lang), transaction.customerName)
        }
        if (storeProfile.showCustomerNameOnReceipt && transaction.customerPhone.isNotBlank()) {
            drawRow(LakuStrings.get("receipt_phone", lang), transaction.customerPhone)
        }

        y += 8f
        canvas.drawLine(margin, y, width - margin, y, linePaint)
        y += 32f

        canvas.drawText(LakuStrings.get("receipt_items_header", lang), margin, y, boldPaint)
        y += 32f

        transaction.items.forEach { item ->
            canvas.drawText(item.productName, margin, y, boldPaint)
            y += 26f
            val qtyPrice = "${FormatUtils.formatQty(item.quantity, item.unit)} x ${FormatUtils.formatRupiah(item.sellingPrice)}"
            val subtotal = FormatUtils.formatRupiah(item.subtotal)
            canvas.drawText(qtyPrice, margin, y, textPaint)
            val subWidth = textPaint.measureText(subtotal)
            canvas.drawText(subtotal, width - margin - subWidth, y, textPaint)
            y += 32f
        }

        y += 8f
        canvas.drawLine(margin, y, width - margin, y, linePaint)
        y += 32f

        drawRow(LakuStrings.get("subtotal", lang), FormatUtils.formatRupiah(transaction.subtotal))
        if (transaction.discount > 0) {
            drawRow(LakuStrings.get("discount", lang), "- ${FormatUtils.formatRupiah(transaction.discount)}")
        }
        if (transaction.ppnAmount > 0) {
            val ppnLabel = if (transaction.ppnRate > 0) {
                "${LakuStrings.get("ppn_label", lang)} (${transaction.ppnRate.toInt()}%)"
            } else {
                LakuStrings.get("ppn_label", lang)
            }
            drawRow(ppnLabel, "+ ${FormatUtils.formatRupiah(transaction.ppnAmount)}")
        }

        y += 8f
        linePaint.strokeWidth = 3f
        linePaint.color = Color.parseColor("#16A34A")
        canvas.drawLine(margin, y, width - margin, y, linePaint)
        linePaint.color = Color.LTGRAY
        linePaint.strokeWidth = 2f
        y += 36f

        val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#15803D")
            textSize = if (is58mm) 26f else 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(LakuStrings.get("receipt_total", lang), margin, y, totalPaint)
        val totVal = FormatUtils.formatRupiah(transaction.total)
        val totWidth = totalPaint.measureText(totVal)
        canvas.drawText(totVal, width - margin - totWidth, y, totalPaint)
        y += 40f

        drawRow(LakuStrings.get("receipt_method", lang), LakuStrings.translatePaymentMethod(transaction.paymentMethod, lang))
        if (transaction.paymentMethod == "Tunai") {
            drawRow(LakuStrings.get("receipt_cash_received", lang), FormatUtils.formatRupiah(transaction.cashReceived))
            drawRow(LakuStrings.get("change", lang), FormatUtils.formatRupiah(transaction.changeAmount), true)
        }
        drawRow(
            LakuStrings.get("receipt_status", lang),
            if (transaction.isPaid) LakuStrings.get("paid_badge", lang) else LakuStrings.get("debt_badge", lang),
            true
        )

        y += 10f
        canvas.drawLine(margin, y, width - margin, y, linePaint)
        y += 34f

        val centerNotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = if (is58mm) 18f else 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        storeProfile.receiptNotes.split("\n").forEach { line ->
            canvas.drawText(line, width / 2f, y, centerNotePaint)
            y += 28f
        }

        y += 16f
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            textSize = if (is58mm) 16f else 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Laku Kasir Pro v1.1 - by Rafline Dev", width / 2f, y, footerPaint)

        // Trim unused white space at bottom nicely
        val finalHeight = (y + 44f).toInt().coerceIn(300, height)
        return Bitmap.createBitmap(bitmap, 0, 0, width, finalHeight)
    }

    fun saveReceiptImageToCache(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ): Uri? {
        return try {
            val bitmap = generateReceiptBitmap(context, transaction, storeProfile)
            val cacheDir = File(context.cacheDir, "receipts").apply { mkdirs() }
            val file = File(cacheDir, "Struk_${transaction.transactionCode}.png")
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun downloadReceiptImage(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ): Boolean {
        return try {
            val bitmap = generateReceiptBitmap(context, transaction, storeProfile)
            val filename = "Struk_${transaction.transactionCode}_${System.currentTimeMillis()}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LakuKasir")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    val out: OutputStream? = resolver.openOutputStream(imageUri)
                    if (out != null) {
                        out.use {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                            it.flush()
                        }
                        Toast.makeText(context, "Saved to Pictures/LakuKasir", Toast.LENGTH_LONG).show()
                        return true
                    }
                }
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val folder = File(picturesDir, "LakuKasir").apply { mkdirs() }
                val file = File(folder, filename)
                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                }
                Toast.makeText(context, "Saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                return true
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun saveReceiptPdfToCache(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ): Uri? {
        return try {
            val bitmap = generateReceiptBitmap(context, transaction, storeProfile)
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            val cacheDir = File(context.cacheDir, "receipts").apply { mkdirs() }
            val file = File(cacheDir, "Struk_${transaction.transactionCode}.pdf")
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
                pdfDocument.close()
                fos.flush()
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun downloadReceiptPdf(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ): Boolean {
        return try {
            val bitmap = generateReceiptBitmap(context, transaction, storeProfile)
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            val filename = "Struk_${transaction.transactionCode}_${System.currentTimeMillis()}.pdf"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/LakuKasir")
                }
                val pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (pdfUri != null) {
                    val out: OutputStream? = resolver.openOutputStream(pdfUri)
                    if (out != null) {
                        out.use {
                            pdfDocument.writeTo(it)
                            pdfDocument.close()
                            it.flush()
                        }
                        Toast.makeText(context, "PDF saved to Downloads/LakuKasir", Toast.LENGTH_LONG).show()
                        return true
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val folder = File(downloadsDir, "LakuKasir").apply { mkdirs() }
                val file = File(folder, filename)
                FileOutputStream(file).use { fos ->
                    pdfDocument.writeTo(fos)
                    pdfDocument.close()
                    fos.flush()
                }
                Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                return true
            }
            false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun shareReceiptImageToWhatsApp(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile,
        caption: String = ""
    ) {
        val uri = saveReceiptImageToCache(context, transaction, storeProfile) ?: return
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage("com.whatsapp.w4b")
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    LakuStrings.get("share_btn", storeProfile.language)
                )
                context.startActivity(chooser)
            }
        }
    }

    fun shareReceiptPdfToWhatsApp(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile,
        caption: String = ""
    ) {
        val uri = saveReceiptPdfToCache(context, transaction, storeProfile) ?: return
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage("com.whatsapp.w4b")
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    LakuStrings.get("share_btn", storeProfile.language)
                )
                context.startActivity(chooser)
            }
        }
    }

    fun printReceipt(
        context: Context,
        transaction: Transaction,
        storeProfile: StoreProfile
    ) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
            if (printManager != null) {
                val printAdapter = object : android.print.PrintDocumentAdapter() {
                    private var currentAttributes: android.print.PrintAttributes? = null

                    override fun onLayout(
                        oldAttributes: android.print.PrintAttributes?,
                        newAttributes: android.print.PrintAttributes?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: android.os.Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        currentAttributes = newAttributes
                        val info = android.print.PrintDocumentInfo.Builder("Struk_${transaction.transactionCode}.pdf")
                            .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1)
                            .build()
                        callback?.onLayoutFinished(info, newAttributes != oldAttributes)
                    }

                    override fun onWrite(
                        pages: Array<out android.print.PageRange>?,
                        destination: android.os.ParcelFileDescriptor?,
                        cancellationSignal: android.os.CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onWriteCancelled()
                            return
                        }

                        val bitmap = generateReceiptBitmap(context, transaction, storeProfile)
                        val attrs = currentAttributes ?: android.print.PrintAttributes.Builder().build()
                        val printedDoc = android.print.pdf.PrintedPdfDocument(context, attrs)

                        try {
                            val page = printedDoc.startPage(0)
                            val canvas = page.canvas
                            val pageWidth = printedDoc.pageWidth.toFloat()

                            val targetWidth = when {
                                pageWidth <= 320f -> pageWidth * 0.90f
                                pageWidth <= 450f -> pageWidth * 0.82f
                                storeProfile.paperSize == "58mm" -> pageWidth * 0.65f
                                else -> pageWidth * 0.80f
                            }
                            val scale = targetWidth / bitmap.width.toFloat()
                            val scaledWidth = bitmap.width * scale
                            val offsetX = (pageWidth - scaledWidth) / 2f
                            val offsetY = 20f

                            canvas.save()
                            canvas.translate(offsetX, offsetY)
                            canvas.scale(scale, scale)
                            canvas.drawBitmap(bitmap, 0f, 0f, null)
                            canvas.restore()

                            printedDoc.finishPage(page)

                            FileOutputStream(destination?.fileDescriptor).use { output ->
                                printedDoc.writeTo(output)
                                output.flush()
                            }
                            printedDoc.close()
                            callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            printedDoc.close()
                            callback?.onWriteFailed(e.message)
                        }
                    }
                }
                printManager.print(
                    "Struk ${transaction.transactionCode}",
                    printAdapter,
                    android.print.PrintAttributes.Builder().build()
                )
            } else {
                Toast.makeText(context, "Print service unavailable", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Print error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
