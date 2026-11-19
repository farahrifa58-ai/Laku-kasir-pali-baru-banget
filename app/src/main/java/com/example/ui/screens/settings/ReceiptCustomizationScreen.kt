package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreProfile
import com.example.ui.LakuKasirViewModel
import com.example.ui.components.StoreAvatar
import com.example.ui.theme.PrimaryGreen
import com.example.util.FormatUtils
import com.example.util.LakuStrings

@Composable
fun ReceiptCustomizationScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lang = storeProfile.language

    var showStoreName by remember(storeProfile.showStoreNameOnReceipt) { mutableStateOf(storeProfile.showStoreNameOnReceipt) }
    var showStoreLogo by remember(storeProfile.showStoreLogoOnReceipt) { mutableStateOf(storeProfile.showStoreLogoOnReceipt) }
    var showCashierName by remember(storeProfile.showCashierNameOnReceipt) { mutableStateOf(storeProfile.showCashierNameOnReceipt) }
    var showStoreAddress by remember(storeProfile.showStoreAddressOnReceipt) { mutableStateOf(storeProfile.showStoreAddressOnReceipt) }
    var showStorePhone by remember(storeProfile.showStorePhoneOnReceipt) { mutableStateOf(storeProfile.showStorePhoneOnReceipt) }
    var showCustomerName by remember(storeProfile.showCustomerNameOnReceipt) { mutableStateOf(storeProfile.showCustomerNameOnReceipt) }
    var showOperatingHours by remember(storeProfile.showOperatingHoursOnReceipt) { mutableStateOf(storeProfile.showOperatingHoursOnReceipt) }
    var receiptPrefix by remember(storeProfile.receiptPrefix) { mutableStateOf(storeProfile.receiptPrefix) }
    var receiptNotes by remember(storeProfile.receiptNotes) { mutableStateOf(storeProfile.receiptNotes) }
    var savedBanner by remember { mutableStateOf(false) }

    val cleanPrefix = receiptPrefix.trim().ifBlank { "LK" }.uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSubHeader(
            title = LakuStrings.get("receipt_customization", lang),
            subtitle = LakuStrings.get("receipt_customization_desc", lang),
            lang = lang,
            onBack = onBack
        )

        // Live Thermal Receipt Simulation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = LakuStrings.get("receipt_preview_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = LakuStrings.get("receipt_preview_badge", lang),
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    Surface(
                        color = PrimaryGreen.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = storeProfile.paperSize,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated Paper Receipt
                Surface(
                    modifier = Modifier
                        .widthIn(max = if (storeProfile.paperSize == "80mm") 340.dp else 280.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showStoreLogo) {
                            StoreAvatar(
                                storeProfile = storeProfile,
                                size = 50.dp,
                                shape = RoundedCornerShape(10.dp),
                                contentDescription = LakuStrings.get("store_profile", lang)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (showStoreName) {
                            Text(
                                text = storeProfile.storeName.uppercase(),
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (showCashierName && storeProfile.ownerName.isNotBlank()) {
                            Text(
                                text = "${LakuStrings.get("cashier_label", lang)}: ${storeProfile.ownerName}",
                                color = Color(0xFF475569),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (showStoreAddress && storeProfile.address.isNotBlank()) {
                            Text(
                                text = storeProfile.address,
                                color = Color(0xFF475569),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (showStorePhone && storeProfile.phone.isNotBlank()) {
                            Text(
                                text = "WA: ${storeProfile.phone}",
                                color = Color(0xFF475569),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (showOperatingHours && storeProfile.operatingHours.isNotBlank()) {
                            Text(
                                text = "${LakuStrings.get("hours_label", lang)}: ${storeProfile.operatingHours}",
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        ReceiptSimDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        ReceiptSimRow(
                            left = LakuStrings.get("receipt_no", lang),
                            right = "$cleanPrefix-20261119-001"
                        )
                        ReceiptSimRow(
                            left = LakuStrings.get("receipt_date", lang),
                            right = FormatUtils.formatDateShort(System.currentTimeMillis(), lang)
                        )
                        if (showCustomerName) {
                            ReceiptSimRow(
                                left = LakuStrings.get("receipt_customer", lang),
                                right = "Budi Santoso"
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        ReceiptSimDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Kopi Susu Gula Aren",
                                color = Color(0xFF0F172A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            ReceiptSimRow(
                                left = "2 ${LakuStrings.translateUnit("pcs", lang)} x ${FormatUtils.formatRupiah(18000.0)}",
                                right = FormatUtils.formatRupiah(36000.0)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Roti Bakar Keju",
                                color = Color(0xFF0F172A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            ReceiptSimRow(
                                left = "1 ${LakuStrings.translateUnit("pcs", lang)} x ${FormatUtils.formatRupiah(14000.0)}",
                                right = FormatUtils.formatRupiah(14000.0)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        ReceiptSimDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        ReceiptSimRow(
                            left = LakuStrings.get("receipt_total", lang),
                            right = FormatUtils.formatRupiah(50000.0),
                            isBold = true
                        )
                        ReceiptSimRow(
                            left = LakuStrings.get("receipt_method", lang),
                            right = LakuStrings.translatePaymentMethod("Tunai", lang)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        ReceiptSimDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        if (receiptNotes.isNotBlank()) {
                            Text(
                                text = receiptNotes,
                                color = Color(0xFF64748B),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Toggle Switches Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = LakuStrings.get("receipt_elements_title", lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                ReceiptToggleRow(
                    label = LakuStrings.get("show_store_name", lang),
                    checked = showStoreName,
                    onCheckedChange = { showStoreName = it; savedBanner = false },
                    tag = "toggle_show_store_name"
                )
                ReceiptToggleRow(
                    label = LakuStrings.get("show_store_logo", lang),
                    checked = showStoreLogo,
                    onCheckedChange = { showStoreLogo = it; savedBanner = false },
                    tag = "toggle_show_store_logo"
                )
                ReceiptToggleRow(
                    label = LakuStrings.get("show_cashier_name", lang),
                    checked = showCashierName,
                    onCheckedChange = { showCashierName = it; savedBanner = false },
                    tag = "toggle_show_cashier_name"
                )
                ReceiptToggleRow(
                    label = LakuStrings.get("show_store_address", lang),
                    checked = showStoreAddress,
                    onCheckedChange = { showStoreAddress = it; savedBanner = false },
                    tag = "toggle_show_store_address"
                )
                ReceiptToggleRow(
                    label = LakuStrings.get("show_store_phone", lang),
                    checked = showStorePhone,
                    onCheckedChange = { showStorePhone = it; savedBanner = false },
                    tag = "toggle_show_store_phone"
                )
                ReceiptToggleRow(
                    label = LakuStrings.get("show_customer_name", lang),
                    checked = showCustomerName,
                    onCheckedChange = { showCustomerName = it; savedBanner = false },
                    tag = "toggle_show_customer_name"
                )
                ReceiptToggleRow(
                    label = LakuStrings.get("show_operating_hours", lang),
                    checked = showOperatingHours,
                    onCheckedChange = { showOperatingHours = it; savedBanner = false },
                    tag = "toggle_show_operating_hours"
                )
            }
        }

        // Prefix & Footer Notes Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = receiptPrefix,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isLetterOrDigit() }.take(8).uppercase()
                        receiptPrefix = filtered
                        savedBanner = false
                    },
                    label = { Text(LakuStrings.get("receipt_prefix_label", lang)) },
                    placeholder = { Text(LakuStrings.get("receipt_prefix_placeholder", lang)) },
                    supportingText = {
                        Text("${LakuStrings.get("receipt_prefix_helper", lang)} $cleanPrefix-20261119-001")
                    },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_receipt_prefix")
                )

                OutlinedTextField(
                    value = receiptNotes,
                    onValueChange = { receiptNotes = it; savedBanner = false },
                    label = { Text(LakuStrings.get("receipt_notes", lang)) },
                    placeholder = { Text(LakuStrings.get("receipt_notes_placeholder", lang)) },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_receipt_notes")
                )
            }
        }

        if (savedBanner) {
            Surface(
                color = PrimaryGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = LakuStrings.get("settings_saved", lang),
                    color = PrimaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp
                )
            }
        }

        Button(
            onClick = {
                viewModel.updateStoreProfile(
                    storeProfile.copy(
                        showStoreNameOnReceipt = showStoreName,
                        showStoreLogoOnReceipt = showStoreLogo,
                        showCashierNameOnReceipt = showCashierName,
                        showStoreAddressOnReceipt = showStoreAddress,
                        showStorePhoneOnReceipt = showStorePhone,
                        showCustomerNameOnReceipt = showCustomerName,
                        showOperatingHoursOnReceipt = showOperatingHours,
                        receiptPrefix = cleanPrefix,
                        receiptNotes = receiptNotes.trim()
                    )
                )
                savedBanner = true
                Toast.makeText(context, LakuStrings.get("settings_saved", lang), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_receipt_customization_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(LakuStrings.get("save_changes", lang), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ReceiptToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag),
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryGreen)
        )
    }
}

@Composable
private fun ReceiptSimDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFCBD5E1))
    )
}

@Composable
private fun ReceiptSimRow(
    left: String,
    right: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = left,
            color = if (isBold) Color(0xFF0F172A) else Color(0xFF475569),
            fontSize = if (isBold) 12.sp else 10.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = right,
            color = Color(0xFF0F172A),
            fontSize = if (isBold) 12.sp else 10.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
