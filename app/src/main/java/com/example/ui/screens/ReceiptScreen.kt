package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import com.example.ui.Screen
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.StatusDebt
import com.example.ui.theme.StatusPaid
import com.example.util.FormatUtils
import com.example.util.LakuStrings
import com.example.util.ReceiptExportHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    transaction: Transaction,
    storeProfile: StoreProfile,
    lang: String,
    onBack: () -> Unit,
    onNewTransaction: () -> Unit
) {
    val context = LocalContext.current
    var showShareBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Build receipt share text
    val receiptText = remember(transaction, storeProfile) {
        buildString {
            appendLine("===============================")
            appendLine("       ${storeProfile.storeName.uppercase()}")
            appendLine("   Owner: ${storeProfile.ownerName}")
            appendLine("   ${storeProfile.address}")
            appendLine("   WA: ${storeProfile.phone}")
            appendLine("===============================")
            appendLine("No Transaksi: ${transaction.transactionCode}")
            appendLine("Tanggal     : ${FormatUtils.formatDateTime(transaction.timestamp)}")
            if (transaction.customerName.isNotBlank()) {
                appendLine("Pelanggan   : ${transaction.customerName}")
            }
            if (transaction.customerPhone.isNotBlank()) {
                appendLine("No HP       : ${transaction.customerPhone}")
            }
            appendLine("-------------------------------")
            transaction.items.forEach { item ->
                appendLine("${item.productName}")
                appendLine("  ${FormatUtils.formatQty(item.quantity, item.unit)} x ${FormatUtils.formatRupiah(item.sellingPrice)} = ${FormatUtils.formatRupiah(item.subtotal)}")
            }
            appendLine("-------------------------------")
            appendLine("Subtotal    : ${FormatUtils.formatRupiah(transaction.subtotal)}")
            if (transaction.discount > 0) {
                appendLine("Diskon      : -${FormatUtils.formatRupiah(transaction.discount)}")
            }
            if (transaction.ppnAmount > 0) {
                appendLine("PPN         : +${FormatUtils.formatRupiah(transaction.ppnAmount)}")
            }
            appendLine("TOTAL       : ${FormatUtils.formatRupiah(transaction.total)}")
            appendLine("Metode      : ${transaction.paymentMethod}")
            if (transaction.paymentMethod == "Tunai") {
                appendLine("Diterima    : ${FormatUtils.formatRupiah(transaction.cashReceived)}")
                appendLine("Kembalian   : ${FormatUtils.formatRupiah(transaction.changeAmount)}")
            }
            appendLine("Status      : ${if (transaction.isPaid) "LUNAS" else "KASBON / BELUM LUNAS"}")
            appendLine("===============================")
            appendLine("${storeProfile.receiptNotes}")
            appendLine("===============================")
            appendLine("Dibuat dengan Laku Kasir Pro")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LakuStrings.get("receipt", lang), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("receipt_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showShareBottomSheet = true
                    }, modifier = Modifier.testTag("receipt_share_button")) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Bagikan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE0F2FE), // Soft light blue header
                    titleContentColor = Color(0xFF0C4A6E),
                    navigationIconContentColor = Color(0xFF0369A1),
                    actionIconContentColor = Color(0xFF0369A1)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Receipt Card Thermal Paper Style
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receipt_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo Toko yang Rapi: Rounded rectangle dengan border halus dan proporsional
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFC)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (storeProfile.logoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(storeProfile.logoUri)
                                    .error(R.drawable.logo_laku_kasir)
                                    .fallback(R.drawable.logo_laku_kasir)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Logo Toko Struk",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.logo_laku_kasir),
                                contentDescription = "Logo Toko",
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nama Toko, Pemilik, Alamat, Nomor WhatsApp dengan jarak yang lapang
                    Text(
                        text = storeProfile.storeName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = EmeraldGreenDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pemilik: ${storeProfile.ownerName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = storeProfile.address,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "WA: ${storeProfile.phone}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Detail Transaksi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("No. Struk", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(transaction.transactionCode, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tanggal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatDateTime(transaction.timestamp), fontSize = 12.sp)
                    }

                    if (transaction.customerName.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pelanggan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(transaction.customerName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (transaction.customerPhone.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("No. WhatsApp", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(transaction.customerPhone, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Rincian Barang
                    Text(
                        text = "RINCIAN BARANG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    transaction.items.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = item.productName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${FormatUtils.formatQty(item.quantity, item.unit)} x ${FormatUtils.formatRupiah(item.sellingPrice)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(item.subtotal),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Rincian Subtotal, Diskon, PPN, dan Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatRupiah(transaction.subtotal), fontSize = 12.sp)
                    }

                    if (transaction.discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Diskon", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            Text("- ${FormatUtils.formatRupiah(transaction.discount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    if (transaction.ppnAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (transaction.ppnRate > 0) "PPN (${transaction.ppnRate.toInt()}%)" else "PPN", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+ ${FormatUtils.formatRupiah(transaction.ppnAmount)}", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 2.dp, color = EmeraldGreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    // TOTAL BESAR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreenDark
                        )
                        Text(
                            text = FormatUtils.formatRupiah(transaction.total),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Metode Bayar & Kembalian
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Metode Bayar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(transaction.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (transaction.paymentMethod == "Tunai") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tunai Diterima", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FormatUtils.formatRupiah(transaction.cashReceived), fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kembalian", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(FormatUtils.formatRupiah(transaction.changeAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreenDark)
                        }
                    }

                    // Status Badge
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (transaction.isPaid) StatusPaid.copy(alpha = 0.12f) else StatusDebt.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (transaction.isPaid) "LUNAS" else "KASBON (BELUM LUNAS)",
                            color = if (transaction.isPaid) StatusPaid else StatusDebt,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Pesan Terima Kasih
                    Text(
                        text = storeProfile.receiptNotes,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Laku Kasir Pro v1.1",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )

                    if (!com.example.util.LicenseManager.isProActivated()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFF3E0),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D))
                        ) {
                            Text(
                                text = "MODE UJI COBA (TRIAL VERSION)",
                                color = Color(0xFFE65100),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // AKSI CETAK & BAGIKAN STRUK MINIMALIS: CUMA 2 BUTTON
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (lang == "en") "Receipt Actions" else "Aksi Cetak & Bagikan Struk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EmeraldGreenDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Button 1: Cetak Struk
                        Button(
                            onClick = {
                                ReceiptExportHelper.printReceipt(context, transaction, storeProfile)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("receipt_print_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Cetak",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "en") "Print" else "Cetak Struk",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        // Button 2: Bagikan Struk (Share)
                        Button(
                            onClick = { showShareBottomSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)), // Ocean Sky Blue
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("receipt_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Bagikan",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "en") "Share" else "Bagikan Struk",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Tombol Transaksi Baru
            Button(
                onClick = onNewTransaction,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("receipt_new_transaction_button")
            ) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LakuStrings.get("new_transaction", lang),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheet untuk Pilihan Bagikan & Unduh Struk
    if (showShareBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Sheet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (lang == "en") "Share & Download Receipt" else "Bagikan & Unduh Struk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (lang == "en") "Select format to send or save" else "Pilih format untuk dikirim atau disimpan",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // 1. Kirim Struk ke WhatsApp (Gambar)
                ShareOptionItem(
                    title = if (transaction.customerPhone.isNotBlank()) "Kirim Struk ke WhatsApp (${transaction.customerPhone})" else "Kirim Struk ke WhatsApp",
                    subtitle = "Kirim gambar struk lengkap langsung ke chat WhatsApp",
                    icon = Icons.Default.Send,
                    iconTint = Color.White,
                    iconBackground = Color(0xFF25D366),
                    onClick = {
                        val caption = "Struk pembelian ${storeProfile.storeName} (${transaction.transactionCode}) - Total: ${FormatUtils.formatRupiah(transaction.total)}"
                        ReceiptExportHelper.shareReceiptImageToWhatsApp(context, transaction, storeProfile, caption)
                        showShareBottomSheet = false
                    }
                )

                // 2. Kirim Dokumen PDF ke WhatsApp
                ShareOptionItem(
                    title = "Kirim Dokumen PDF ke WhatsApp",
                    subtitle = "Kirim file dokumen PDF resmi via WhatsApp",
                    icon = Icons.Default.PictureAsPdf,
                    iconTint = Color.White,
                    iconBackground = Color(0xFF1E7E34),
                    onClick = {
                        val caption = "Struk PDF ${storeProfile.storeName} (${transaction.transactionCode}) - Total: ${FormatUtils.formatRupiah(transaction.total)}"
                        ReceiptExportHelper.shareReceiptPdfToWhatsApp(context, transaction, storeProfile, caption)
                        showShareBottomSheet = false
                    }
                )

                // 3. Unduh Foto / Gambar Struk
                ShareOptionItem(
                    title = "Unduh Foto / Gambar Struk",
                    subtitle = "Simpan gambar struk ke Galeri / folder Pictures",
                    icon = Icons.Default.Image,
                    iconTint = Color.White,
                    iconBackground = EmeraldGreenDark,
                    onClick = {
                        ReceiptExportHelper.downloadReceiptImage(context, transaction, storeProfile)
                        showShareBottomSheet = false
                    }
                )

                // 4. Unduh Dokumen PDF
                ShareOptionItem(
                    title = "Unduh Dokumen PDF",
                    subtitle = "Simpan file PDF struk ke folder Download perangkat",
                    icon = Icons.Default.Download,
                    iconTint = Color.White,
                    iconBackground = Color(0xFF0284C7),
                    onClick = {
                        ReceiptExportHelper.downloadReceiptPdf(context, transaction, storeProfile)
                        showShareBottomSheet = false
                    }
                )

                // 5. Bagikan Format Teks Struk
                ShareOptionItem(
                    title = "Bagikan Format Teks Struk",
                    subtitle = "Kirim atau salin teks struk ke aplikasi lain",
                    icon = Icons.Default.Share,
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = {
                        FormatUtils.shareText(context, "Struk ${transaction.transactionCode}", receiptText)
                        showShareBottomSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun ShareOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
