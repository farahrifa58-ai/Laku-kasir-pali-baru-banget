package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import com.example.ui.components.StoreAvatar
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.StatusDebt
import com.example.ui.theme.StatusPaid
import com.example.util.BluetoothPrinterHelper
import com.example.util.FormatUtils
import com.example.util.LakuStrings
import com.example.util.ReceiptExportHelper
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    var showShareBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val receiptText = remember(transaction, storeProfile, lang) {
        BluetoothPrinterHelper.buildEscPosReceiptText(transaction, storeProfile)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(LakuStrings.get("receipt", lang), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("receipt_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LakuStrings.get("back", lang))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showShareBottomSheet = true
                    }, modifier = Modifier.testTag("receipt_share_top_button")) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = LakuStrings.get("share", lang))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (MaterialTheme.colorScheme.background == com.example.ui.theme.DarkBackground) Color(0xFF0D2818) else Color(0xFFE8F8EE),
                    titleContentColor = if (MaterialTheme.colorScheme.background == com.example.ui.theme.DarkBackground) Color(0xFFDCFCE7) else Color(0xFF14532D),
                    navigationIconContentColor = if (MaterialTheme.colorScheme.background == com.example.ui.theme.DarkBackground) Color(0xFF86EFAC) else Color(0xFF15803D),
                    actionIconContentColor = if (MaterialTheme.colorScheme.background == com.example.ui.theme.DarkBackground) Color(0xFF86EFAC) else Color(0xFF15803D)
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
                    if (storeProfile.showStoreLogoOnReceipt) {
                        StoreAvatar(
                            storeProfile = storeProfile,
                            size = 76.dp,
                            shape = RoundedCornerShape(14.dp),
                            contentDescription = LakuStrings.get("store_profile", lang),
                            modifier = Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                                RoundedCornerShape(14.dp)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (storeProfile.showStoreNameOnReceipt && storeProfile.storeName.isNotBlank()) {
                        Text(
                            text = storeProfile.storeName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = EmeraldGreenDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    if (storeProfile.showCashierNameOnReceipt && storeProfile.ownerName.isNotBlank()) {
                        Text(
                            text = "${LakuStrings.get("cashier_label", lang)}: ${storeProfile.ownerName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (storeProfile.showStoreAddressOnReceipt && storeProfile.address.isNotBlank()) {
                        Text(
                            text = storeProfile.address,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (storeProfile.showStorePhoneOnReceipt && storeProfile.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "WA: ${storeProfile.phone}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (storeProfile.showOperatingHoursOnReceipt && storeProfile.operatingHours.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${LakuStrings.get("hours_label", lang)}: ${storeProfile.operatingHours}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Transaction Metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(LakuStrings.get("receipt_no", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(transaction.transactionCode, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(LakuStrings.get("receipt_date", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatDateTime(transaction.timestamp, lang), fontSize = 12.sp)
                    }

                    if (storeProfile.showCustomerNameOnReceipt && transaction.customerName.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(LakuStrings.get("receipt_customer", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(transaction.customerName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (storeProfile.showCustomerNameOnReceipt && transaction.customerPhone.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(LakuStrings.get("receipt_phone", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(transaction.customerPhone, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Items Header
                    Text(
                        text = LakuStrings.get("receipt_items_header", lang),
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(LakuStrings.get("subtotal", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatRupiah(transaction.subtotal), fontSize = 12.sp)
                    }

                    if (transaction.discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(LakuStrings.get("discount", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            Text("- ${FormatUtils.formatRupiah(transaction.discount)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    if (transaction.ppnAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val ppnLabel = if (transaction.ppnRate > 0) "${LakuStrings.get("ppn_label", lang)} (${transaction.ppnRate.toInt()}%)" else LakuStrings.get("ppn_label", lang)
                            Text(ppnLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+ ${FormatUtils.formatRupiah(transaction.ppnAmount)}", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 2.dp, color = EmeraldGreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LakuStrings.get("receipt_total", lang),
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(LakuStrings.get("receipt_method", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(LakuStrings.translatePaymentMethod(transaction.paymentMethod, lang), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (transaction.paymentMethod == "Tunai") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(LakuStrings.get("receipt_cash_received", lang), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(FormatUtils.formatRupiah(transaction.cashReceived), fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(LakuStrings.get("change", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(FormatUtils.formatRupiah(transaction.changeAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreenDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (transaction.isPaid) StatusPaid.copy(alpha = 0.12f) else StatusDebt.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (transaction.isPaid) LakuStrings.get("paid_badge", lang) else LakuStrings.get("debt_badge_full", lang),
                            color = if (transaction.isPaid) StatusPaid else StatusDebt,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (storeProfile.receiptNotes.isNotBlank()) {
                        Text(
                            text = storeProfile.receiptNotes,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${LakuStrings.get("app_title", lang)} • ${LakuStrings.get("version", lang)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Receipt Actions Card
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
                        text = LakuStrings.get("receipt_actions", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EmeraldGreenDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (storeProfile.printerAddress.isNotBlank() && BluetoothPrinterHelper.hasBluetoothPermission(context)) {
                                    scope.launch {
                                        val printed = BluetoothPrinterHelper.printRawText(
                                            context = context,
                                            printerAddress = storeProfile.printerAddress,
                                            content = receiptText
                                        )
                                        if (printed) {
                                            Toast.makeText(context, LakuStrings.get("test_print_success", lang), Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, LakuStrings.get("test_print_failed", lang), Toast.LENGTH_SHORT).show()
                                            ReceiptExportHelper.printReceipt(context, transaction, storeProfile)
                                        }
                                    }
                                } else {
                                    ReceiptExportHelper.printReceipt(context, transaction, storeProfile)
                                }
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
                                contentDescription = LakuStrings.get("print_btn", lang),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LakuStrings.get("print_btn", lang),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = { showShareBottomSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("receipt_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = LakuStrings.get("share_btn", lang),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LakuStrings.get("share_btn", lang),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

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
                            text = LakuStrings.get("share_download_title", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = LakuStrings.get("share_download_subtitle", lang),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                ShareOptionItem(
                    title = if (transaction.customerPhone.isNotBlank()) {
                        "${LakuStrings.get("share_wa_image_title", lang)} (${transaction.customerPhone})"
                    } else {
                        LakuStrings.get("share_wa_image_title", lang)
                    },
                    subtitle = LakuStrings.get("share_wa_image_desc", lang),
                    icon = Icons.Default.Send,
                    iconTint = Color.White,
                    iconBackground = Color(0xFF25D366),
                    onClick = {
                        val caption = "${LakuStrings.get("receipt", lang)} ${storeProfile.storeName} (${transaction.transactionCode}) - ${LakuStrings.get("receipt_total", lang)}: ${FormatUtils.formatRupiah(transaction.total)}"
                        ReceiptExportHelper.shareReceiptImageToWhatsApp(context, transaction, storeProfile, caption)
                        showShareBottomSheet = false
                    }
                )

                ShareOptionItem(
                    title = LakuStrings.get("share_wa_pdf_title", lang),
                    subtitle = LakuStrings.get("share_wa_pdf_desc", lang),
                    icon = Icons.Default.PictureAsPdf,
                    iconTint = Color.White,
                    iconBackground = Color(0xFF1E7E34),
                    onClick = {
                        val caption = "${LakuStrings.get("receipt", lang)} PDF ${storeProfile.storeName} (${transaction.transactionCode}) - ${LakuStrings.get("receipt_total", lang)}: ${FormatUtils.formatRupiah(transaction.total)}"
                        ReceiptExportHelper.shareReceiptPdfToWhatsApp(context, transaction, storeProfile, caption)
                        showShareBottomSheet = false
                    }
                )

                ShareOptionItem(
                    title = LakuStrings.get("download_image_title", lang),
                    subtitle = LakuStrings.get("download_image_desc", lang),
                    icon = Icons.Default.Image,
                    iconTint = Color.White,
                    iconBackground = EmeraldGreenDark,
                    onClick = {
                        ReceiptExportHelper.downloadReceiptImage(context, transaction, storeProfile)
                        showShareBottomSheet = false
                    }
                )

                ShareOptionItem(
                    title = LakuStrings.get("download_pdf_title", lang),
                    subtitle = LakuStrings.get("download_pdf_desc", lang),
                    icon = Icons.Default.Download,
                    iconTint = Color.White,
                    iconBackground = Color(0xFF0284C7),
                    onClick = {
                        ReceiptExportHelper.downloadReceiptPdf(context, transaction, storeProfile)
                        showShareBottomSheet = false
                    }
                )

                ShareOptionItem(
                    title = LakuStrings.get("share_text_title", lang),
                    subtitle = LakuStrings.get("share_text_desc", lang),
                    icon = Icons.Default.Share,
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    iconBackground = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = {
                        FormatUtils.shareText(context, "${LakuStrings.get("receipt", lang)} ${transaction.transactionCode}", receiptText)
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
