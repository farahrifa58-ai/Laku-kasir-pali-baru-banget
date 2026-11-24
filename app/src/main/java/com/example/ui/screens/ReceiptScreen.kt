package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreProfile
import com.example.data.model.Transaction
import com.example.ui.components.CompactScreenHeader
import com.example.ui.components.SoftElevatedCard
import com.example.ui.components.StoreAvatar
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkEmeraldPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.SoftCardBorder
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
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accentGreen = MaterialTheme.colorScheme.primary
    val onAccentGreen = MaterialTheme.colorScheme.onPrimary
    val accentGreenText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark
    val paidAccent = if (isDark) DarkEmeraldPrimary else StatusPaid
    val debtAccent = if (isDark) Color(0xFFFB923C) else StatusDebt

    val receiptText = remember(transaction, storeProfile, lang) {
        BluetoothPrinterHelper.buildEscPosReceiptText(transaction, storeProfile)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CompactScreenHeader(
                title = LakuStrings.get("receipt", lang),
                backContentDescription = LakuStrings.get("back", lang),
                onBack = onBack,
                backButtonTestTag = "receipt_back_button",
                actions = {
                    IconButton(
                        onClick = { showShareBottomSheet = true },
                        modifier = Modifier.testTag("receipt_share_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = LakuStrings.get("share", lang),
                            tint = if (isDark) DarkTextSecondary else Color(0xFF475569)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = if (isDark) DarkBorder else SoftCardBorder
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 520.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("receipt_print_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = LakuStrings.get("print_btn", lang),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LakuStrings.get("print_btn", lang),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = { showShareBottomSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("receipt_share_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = LakuStrings.get("share_btn", lang),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = LakuStrings.get("share_btn", lang),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Button(
                            onClick = onNewTransaction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentGreen,
                                contentColor = onAccentGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("receipt_new_transaction_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = onAccentGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LakuStrings.get("new_transaction", lang),
                                fontWeight = FontWeight.Bold,
                                color = onAccentGreen,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact Thermal Receipt Card
            SoftElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .testTag("receipt_card")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Compact Store Header
                    if (storeProfile.showStoreLogoOnReceipt) {
                        StoreAvatar(
                            storeProfile = storeProfile,
                            size = 48.dp,
                            shape = RoundedCornerShape(12.dp),
                            contentDescription = LakuStrings.get("store_profile", lang),
                            modifier = Modifier.border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(12.dp)
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (storeProfile.showStoreNameOnReceipt && storeProfile.storeName.isNotBlank()) {
                        Text(
                            text = storeProfile.storeName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = accentGreenText,
                            textAlign = TextAlign.Center
                        )
                    }

                    val subInfoParts = buildList {
                        if (storeProfile.showCashierNameOnReceipt && storeProfile.ownerName.isNotBlank()) {
                            add("${LakuStrings.get("cashier_label", lang)}: ${storeProfile.ownerName}")
                        }
                        if (storeProfile.showStoreAddressOnReceipt && storeProfile.address.isNotBlank()) {
                            add(storeProfile.address)
                        }
                    }
                    if (subInfoParts.isNotEmpty()) {
                        Text(
                            text = subInfoParts.joinToString(" • "),
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val contactParts = buildList {
                        if (storeProfile.showStorePhoneOnReceipt && storeProfile.phone.isNotBlank()) {
                            add("WA: ${storeProfile.phone}")
                        }
                        if (storeProfile.showOperatingHoursOnReceipt && storeProfile.operatingHours.isNotBlank()) {
                            add("${LakuStrings.get("hours_label", lang)}: ${storeProfile.operatingHours}")
                        }
                    }
                    if (contactParts.isNotEmpty()) {
                        Text(
                            text = contactParts.joinToString("  •  "),
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentGreen,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Transaction Metadata
                    ReceiptMetaRow(
                        label = LakuStrings.get("receipt_no", lang),
                        value = transaction.transactionCode,
                        valueBold = true
                    )
                    ReceiptMetaRow(
                        label = LakuStrings.get("receipt_date", lang),
                        value = FormatUtils.formatDateTime(transaction.timestamp, lang)
                    )
                    if (storeProfile.showCustomerNameOnReceipt && transaction.customerName.isNotBlank()) {
                        ReceiptMetaRow(
                            label = LakuStrings.get("receipt_customer", lang),
                            value = transaction.customerName,
                            valueBold = true
                        )
                    }
                    if (storeProfile.showCustomerNameOnReceipt && transaction.customerPhone.isNotBlank()) {
                        ReceiptMetaRow(
                            label = LakuStrings.get("receipt_phone", lang),
                            value = transaction.customerPhone
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Items Header
                    Text(
                        text = LakuStrings.get("receipt_items_header", lang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    transaction.items.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.productName,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(item.subtotal),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${FormatUtils.formatQty(item.quantity, item.unit)} x ${FormatUtils.formatRupiah(item.sellingPrice)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    ReceiptMetaRow(
                        label = LakuStrings.get("subtotal", lang),
                        value = FormatUtils.formatRupiah(transaction.subtotal)
                    )

                    if (transaction.discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(LakuStrings.get("discount", lang), fontSize = 11.5.sp, color = MaterialTheme.colorScheme.error)
                            Text("- ${FormatUtils.formatRupiah(transaction.discount)}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    if (transaction.ppnAmount > 0) {
                        val ppnLabel = if (transaction.ppnRate > 0) "${LakuStrings.get("ppn_label", lang)} (${transaction.ppnRate.toInt()}%)" else LakuStrings.get("ppn_label", lang)
                        ReceiptMetaRow(
                            label = ppnLabel,
                            value = "+ ${FormatUtils.formatRupiah(transaction.ppnAmount)}"
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(thickness = 1.5.dp, color = accentGreen)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = LakuStrings.get("receipt_total", lang),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentGreenText
                        )
                        Text(
                            text = FormatUtils.formatRupiah(transaction.total),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentGreenText
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    ReceiptMetaRow(
                        label = LakuStrings.get("receipt_method", lang),
                        value = LakuStrings.translatePaymentMethod(transaction.paymentMethod, lang),
                        valueBold = true
                    )

                    if (transaction.paymentMethod == "Tunai") {
                        ReceiptMetaRow(
                            label = LakuStrings.get("receipt_cash_received", lang),
                            value = FormatUtils.formatRupiah(transaction.cashReceived)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(LakuStrings.get("change", lang), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text(FormatUtils.formatRupiah(transaction.changeAmount), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = accentGreenText)
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (transaction.isPaid) paidAccent.copy(alpha = 0.14f) else debtAccent.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = if (transaction.isPaid) LakuStrings.get("paid_badge", lang) else LakuStrings.get("debt_badge_full", lang),
                            color = if (transaction.isPaid) paidAccent else debtAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }

                    if (storeProfile.receiptNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = storeProfile.receiptNotes,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "${LakuStrings.get("app_name_short", lang)} • ${LakuStrings.get("version", lang)}",
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showShareBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showShareBottomSheet = false },
            sheetState = sheetState,
            sheetMaxWidth = 560.dp,
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
private fun ReceiptMetaRow(
    label: String,
    value: String,
    valueBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
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
