package com.example.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreProfile
import com.example.ui.LakuKasirViewModel
import com.example.ui.theme.PrimaryGreen
import com.example.util.BluetoothPrinterHelper
import com.example.util.LakuStrings
import com.example.util.PairedPrinterDevice
import com.example.util.ReceiptExportHelper
import kotlinx.coroutines.launch

@Composable
fun PrinterSettingsScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lang = storeProfile.language

    var hasPermission by remember { mutableStateOf(BluetoothPrinterHelper.hasBluetoothPermission(context)) }
    var isBtEnabled by remember { mutableStateOf(BluetoothPrinterHelper.isBluetoothEnabled(context)) }
    var pairedDevices by remember { mutableStateOf<List<PairedPrinterDevice>>(emptyList()) }
    var isPrintingTest by remember { mutableStateOf(false) }

    fun reloadPrinters() {
        hasPermission = BluetoothPrinterHelper.hasBluetoothPermission(context)
        isBtEnabled = BluetoothPrinterHelper.isBluetoothEnabled(context)
        pairedDevices = if (hasPermission && isBtEnabled) {
            BluetoothPrinterHelper.getPairedDevices(context)
        } else {
            emptyList()
        }
    }

    LaunchedEffect(Unit) {
        reloadPrinters()
    }

    val btPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        reloadPrinters()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSubHeader(
            title = LakuStrings.get("printer_settings", lang),
            subtitle = LakuStrings.get("printer_settings_desc", lang),
            lang = lang,
            onBack = onBack
        )

        // Selected Printer Status Card
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            color = if (storeProfile.printerAddress.isNotBlank()) PrimaryGreen.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = null,
                                    tint = if (storeProfile.printerAddress.isNotBlank()) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = LakuStrings.get("selected_printer", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (storeProfile.printerName.isNotBlank()) storeProfile.printerName
                                else LakuStrings.get("no_printer_selected", lang),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (storeProfile.printerAddress.isNotBlank()) {
                                Text(
                                    text = storeProfile.printerAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryGreen
                                )
                            }
                        }
                    }

                    if (storeProfile.printerAddress.isNotBlank()) {
                        TextButton(
                            onClick = {
                                viewModel.updateStoreProfile(
                                    storeProfile.copy(printerName = "", printerAddress = "")
                                )
                            },
                            modifier = Modifier.testTag("disconnect_printer_button")
                        ) {
                            Text(
                                LakuStrings.get("disconnect_printer", lang),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        isPrintingTest = true
                        scope.launch {
                            val sampleTx = BluetoothPrinterHelper.createSampleTransaction(storeProfile)
                            if (storeProfile.printerAddress.isNotBlank() && hasPermission) {
                                val ok = BluetoothPrinterHelper.printTestReceipt(context, storeProfile)
                                isPrintingTest = false
                                if (ok) {
                                    Toast.makeText(context, LakuStrings.get("test_print_success", lang), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, LakuStrings.get("test_print_failed", lang), Toast.LENGTH_SHORT).show()
                                    ReceiptExportHelper.printReceipt(context, sampleTx, storeProfile)
                                }
                            } else {
                                isPrintingTest = false
                                ReceiptExportHelper.printReceipt(context, sampleTx, storeProfile)
                            }
                        }
                    },
                    enabled = !isPrintingTest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("test_print_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    if (isPrintingTest) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LakuStrings.get("test_print_btn", lang), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Paper Size Selection Card
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LakuStrings.get("paper_size_title", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                PaperSizeOptionCard(
                    title = LakuStrings.get("paper_58mm_title", lang),
                    description = LakuStrings.get("paper_58mm_desc", lang),
                    selected = storeProfile.paperSize == "58mm",
                    onClick = {
                        viewModel.updateStoreProfile(storeProfile.copy(paperSize = "58mm"))
                        Toast.makeText(context, LakuStrings.get("settings_saved", lang), Toast.LENGTH_SHORT).show()
                    },
                    tag = "paper_size_58mm"
                )

                PaperSizeOptionCard(
                    title = LakuStrings.get("paper_80mm_title", lang),
                    description = LakuStrings.get("paper_80mm_desc", lang),
                    selected = storeProfile.paperSize == "80mm",
                    onClick = {
                        viewModel.updateStoreProfile(storeProfile.copy(paperSize = "80mm"))
                        Toast.makeText(context, LakuStrings.get("settings_saved", lang), Toast.LENGTH_SHORT).show()
                    },
                    tag = "paper_size_80mm"
                )
            }
        }

        // Connected / Paired Bluetooth Printers Card
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFF0284C7))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LakuStrings.get("connected_printers", lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = { reloadPrinters() },
                        modifier = Modifier.testTag("refresh_printers_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LakuStrings.get("refresh_printers", lang), fontSize = 12.sp)
                    }
                }

                when {
                    !hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        Text(
                            text = LakuStrings.get("bluetooth_permission_needed", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                btPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("grant_bt_permission_button")
                        ) {
                            Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(LakuStrings.get("grant_bluetooth_permission", lang))
                        }
                    }
                    !isBtEnabled -> {
                        Text(
                            text = LakuStrings.get("bluetooth_off_notice", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    pairedDevices.isEmpty() -> {
                        Text(
                            text = LakuStrings.get("no_paired_printers", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        pairedDevices.forEach { device ->
                            val isSelected = storeProfile.printerAddress.equals(device.address, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateStoreProfile(
                                            storeProfile.copy(
                                                printerName = device.name,
                                                printerAddress = device.address
                                            )
                                        )
                                        Toast.makeText(
                                            context,
                                            "${LakuStrings.get("printer_connected_toast", lang)} ${device.name}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .testTag("paired_printer_${device.address}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryGreen.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            Icons.Default.Print,
                                            contentDescription = null,
                                            tint = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = device.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = device.address,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen)
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                        } catch (_: Exception) { }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_bt_settings_button")
                ) {
                    Icon(Icons.Default.SettingsBluetooth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LakuStrings.get("open_bluetooth_settings", lang))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PaperSizeOptionCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) PrimaryGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) PrimaryGreen else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (selected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
            )
        }
    }
}
