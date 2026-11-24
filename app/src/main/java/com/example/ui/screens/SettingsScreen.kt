package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageCatalog
import com.example.ui.LakuKasirViewModel
import com.example.ui.components.SoftElevatedCard
import com.example.ui.components.StoreAvatar
import com.example.ui.screens.settings.*
import com.example.ui.theme.PrimaryGreen
import com.example.util.LakuStrings
import com.example.util.LicenseManager

private enum class SettingsSection {
    HUB,
    STORE_PROFILE,
    LICENSE_STATUS,
    RECEIPT_CUSTOMIZATION,
    TAX_DISCOUNT,
    PRINTER,
    THEME_LANGUAGE,
    TUTORIAL,
    BACKUP_RESTORE,
    ABOUT
}

@Composable
fun SettingsScreen(
    viewModel: LakuKasirViewModel,
    onBack: () -> Unit = {}
) {
    val storeProfile by viewModel.storeProfile.collectAsStateWithLifecycle()
    val licenseInfo by viewModel.licenseInfo.collectAsStateWithLifecycle()
    val lang = storeProfile.language
    var currentSection by remember { mutableStateOf(SettingsSection.HUB) }

    BackHandler(enabled = currentSection != SettingsSection.HUB) {
        currentSection = SettingsSection.HUB
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
        ) {
        when (currentSection) {
            SettingsSection.HUB -> {
                val activeLangObj = remember(lang) { LanguageCatalog.getLanguage(lang) }
                val themeLabel = when (storeProfile.themeMode) {
                    "dark" -> LakuStrings.get("theme_dark", lang)
                    "system" -> LakuStrings.get("theme_system", lang)
                    else -> LakuStrings.get("theme_light", lang)
                }
                val ppnSummary = if (storeProfile.ppnMode == "auto") {
                    val r = if (storeProfile.defaultPpnRate % 1.0 == 0.0) storeProfile.defaultPpnRate.toInt().toString() else storeProfile.defaultPpnRate.toString()
                    "${LakuStrings.get("ppn_label", lang)}: $r%"
                } else {
                    "${LakuStrings.get("ppn_label", lang)}: ${LakuStrings.get("ppn_mode_manual", lang)}"
                }
                val discountSummary = if (storeProfile.discountMode == "percent") {
                    "${LakuStrings.get("discount", lang)}: %"
                } else {
                    "${LakuStrings.get("discount", lang)}: ${LakuStrings.get("discount_mode_manual", lang)}"
                }
                val printerSummary = if (storeProfile.printerName.isNotBlank()) {
                    "${storeProfile.printerName} • ${storeProfile.paperSize}"
                } else {
                    "${storeProfile.paperSize} • ${LakuStrings.get("paper_size_title", lang)}"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with Back button
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("settings_hub_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = LakuStrings.get("back", lang)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LakuStrings.get("settings", lang),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = LakuStrings.get("settings_subtitle", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                // 1. Store Profile Card (Featured Hero Card)
                val themePrimary = MaterialTheme.colorScheme.primary
                SoftElevatedCard(
                    onClick = { currentSection = SettingsSection.STORE_PROFILE },
                    shape = RoundedCornerShape(16.dp),
                    borderColor = themePrimary.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_card_store_profile")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StoreAvatar(
                            storeProfile = storeProfile,
                            size = 58.dp,
                            shape = RoundedCornerShape(14.dp),
                            contentDescription = LakuStrings.get("store_profile", lang)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LakuStrings.get("store_profile", lang),
                                style = MaterialTheme.typography.labelMedium,
                                color = themePrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = storeProfile.storeName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (storeProfile.operatingHours.isNotBlank()) {
                                    "${LakuStrings.get("hours_label", lang)}: ${storeProfile.operatingHours}"
                                } else {
                                    LakuStrings.get("store_profile_card_desc", lang)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = themePrimary
                        )
                    }
                }

                // 1b. License Status Menu Card (opens dedicated detail screen)
                SettingsMenuCard(
                    title = "Status Lisensi Perangkat",
                    subtitle = "Aktif Permanen • 100% Offline Lokal",
                    badgeText = "AKTIF",
                    icon = Icons.Default.VerifiedUser,
                    accentColor = themePrimary,
                    onClick = { currentSection = SettingsSection.LICENSE_STATUS },
                    tag = "settings_card_license_status"
                )

                // 2. Receipt Customization Card
                SettingsMenuCard(
                    title = LakuStrings.get("receipt_customization", lang),
                    subtitle = LakuStrings.get("receipt_customization_desc", lang),
                    badgeText = "${storeProfile.receiptPrefix.ifBlank { "LK" }}-•••",
                    icon = Icons.Default.ReceiptLong,
                    accentColor = Color(0xFF0284C7),
                    onClick = { currentSection = SettingsSection.RECEIPT_CUSTOMIZATION },
                    tag = "settings_card_receipt"
                )

                // 3. PPN & Discount Card
                SettingsMenuCard(
                    title = LakuStrings.get("ppn_discount_title", lang),
                    subtitle = "$ppnSummary • $discountSummary",
                    icon = Icons.Default.Percent,
                    accentColor = Color(0xFFD97706),
                    onClick = { currentSection = SettingsSection.TAX_DISCOUNT },
                    tag = "settings_card_tax_discount"
                )

                // 4. Printer & Paper Size Card
                SettingsMenuCard(
                    title = LakuStrings.get("printer_settings", lang),
                    subtitle = printerSummary,
                    badgeText = storeProfile.paperSize,
                    icon = Icons.Default.Print,
                    accentColor = Color(0xFF0D9488),
                    onClick = { currentSection = SettingsSection.PRINTER },
                    tag = "settings_card_printer"
                )

                // 5. Theme & Language Card
                SettingsMenuCard(
                    title = LakuStrings.get("theme_language_title", lang),
                    subtitle = "$themeLabel • ${activeLangObj.flag} ${activeLangObj.nativeName}",
                    icon = Icons.Default.Translate,
                    accentColor = Color(0xFF7C3AED),
                    onClick = { currentSection = SettingsSection.THEME_LANGUAGE },
                    tag = "settings_card_theme_language"
                )

                // 6. Tutorial Card
                SettingsMenuCard(
                    title = LakuStrings.get("tutorial_title", lang),
                    subtitle = LakuStrings.get("tutorial_desc", lang),
                    icon = Icons.Default.MenuBook,
                    accentColor = Color(0xFFDB2777),
                    onClick = { currentSection = SettingsSection.TUTORIAL },
                    tag = "settings_card_tutorial"
                )

                // 7. Backup & Restore Card
                SettingsMenuCard(
                    title = LakuStrings.get("backup_restore", lang),
                    subtitle = LakuStrings.get("backup_restore_card_desc", lang),
                    icon = Icons.Default.CloudSync,
                    accentColor = Color(0xFF4F46E5),
                    onClick = { currentSection = SettingsSection.BACKUP_RESTORE },
                    tag = "settings_card_backup"
                )

                // 8. About App Card
                SettingsMenuCard(
                    title = LakuStrings.get("about_app", lang),
                    subtitle = LakuStrings.get("about_app_subtitle", lang),
                    badgeText = "v1.2",
                    icon = Icons.Default.Info,
                    accentColor = themePrimary,
                    onClick = { currentSection = SettingsSection.ABOUT },
                    tag = "settings_card_about"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        SettingsSection.STORE_PROFILE -> StoreProfileSettingsScreen(
            viewModel = viewModel,
            storeProfile = storeProfile,
            onBack = { currentSection = SettingsSection.HUB }
        )

        SettingsSection.LICENSE_STATUS -> LicenseStatusSettingsScreen(
            licenseInfo = licenseInfo,
            storeName = storeProfile.storeName,
            lang = lang,
            onBack = { currentSection = SettingsSection.HUB }
        )

        SettingsSection.RECEIPT_CUSTOMIZATION -> ReceiptCustomizationScreen(
            viewModel = viewModel,
            storeProfile = storeProfile,
            onBack = { currentSection = SettingsSection.HUB }
        )

        SettingsSection.TAX_DISCOUNT -> TaxDiscountSettingsScreen(
            viewModel = viewModel,
            storeProfile = storeProfile,
            onBack = { currentSection = SettingsSection.HUB }
        )

        SettingsSection.PRINTER -> PrinterSettingsScreen(
            viewModel = viewModel,
            storeProfile = storeProfile,
            onBack = { currentSection = SettingsSection.HUB }
        )

        SettingsSection.THEME_LANGUAGE -> ThemeLanguageSettingsScreen(
            viewModel = viewModel,
            storeProfile = storeProfile,
            onBack = { currentSection = SettingsSection.HUB }
        )

        SettingsSection.TUTORIAL -> TutorialSettingsScreen(
            storeProfile = storeProfile,
            onBack = { currentSection = SettingsSection.HUB }
        )

        SettingsSection.BACKUP_RESTORE -> BackupRestoreSettingsScreen(
            viewModel = viewModel,
            storeProfile = storeProfile,
            onBack = { currentSection = SettingsSection.HUB }
        )

        SettingsSection.ABOUT -> AboutAppSettingsScreen(
            storeProfile = storeProfile,
            onBack = { currentSection = SettingsSection.HUB }
        )
        }
        }
    }
}

@Composable
private fun SettingsMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    tag: String,
    badgeText: String? = null
) {
    SoftElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!badgeText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = accentColor.copy(alpha = 0.13f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LicenseStatusSettingsScreen(
    licenseInfo: com.example.util.LicenseInfo,
    storeName: String,
    lang: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val themePrimary = MaterialTheme.colorScheme.primary
    var showFullLicenseCode by remember { mutableStateOf(false) }

    val deviceId = remember(licenseInfo.registeredDeviceId) {
        licenseInfo.registeredDeviceId.ifBlank { LicenseManager.getDeviceId(context) }
    }
    val displayedCode = if (licenseInfo.licenseCode.isBlank()) {
        "-"
    } else if (showFullLicenseCode) {
        licenseInfo.licenseCode
    } else {
        LicenseManager.maskLicenseCode(licenseInfo.licenseCode)
    }
    val registeredBusiness = licenseInfo.businessName.ifBlank { storeName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsSubHeader(
            title = "Status Lisensi Perangkat",
            subtitle = "Lisensi 1 Perangkat • 100% Offline Lokal",
            lang = lang,
            onBack = onBack
        )

        // Hero Status Card with guaranteed single-line AKTIF pill
        SoftElevatedCard(
            shape = RoundedCornerShape(16.dp),
            borderColor = themePrimary.copy(alpha = 0.35f),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("license_detail_status_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(themePrimary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = themePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lisensi Aktif Permanen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Terikat pada perangkat ini • 100% Offline Lokal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = themePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "AKTIF",
                        color = themePrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // License Details Card
        SoftElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("license_detail_info_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Registered Business Name
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Nama Usaha Terdaftar",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = registeredBusiness,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // License Code Row with Show/Hide Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kode Lisensi",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayedCode,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (licenseInfo.licenseCode.isNotBlank()) {
                        IconButton(
                            onClick = { showFullLicenseCode = !showFullLicenseCode },
                            modifier = Modifier.testTag("settings_toggle_license_visibility")
                        ) {
                            Icon(
                                imageVector = if (showFullLicenseCode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showFullLicenseCode) "Sembunyikan kode" else "Tampilkan kode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Device ID Row with Copy Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ID Perangkat Terdaftar (Auto-Restore)",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = deviceId,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Device ID", deviceId))
                            Toast.makeText(context, "ID Perangkat disalin", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("settings_copy_device_id")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin ID Perangkat",
                            tint = themePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Info Note Card (No logout button — 1-device offline permanence)
        SoftElevatedCard(
            shape = RoundedCornerShape(16.dp),
            borderColor = themePrimary.copy(alpha = 0.22f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = themePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Lisensi aktif permanen untuk perangkat ini. Semua data produk, transaksi, dan pengeluaran tersimpan 100% lokal di memori HP Anda (Offline-First). Jika aplikasi dipasang ulang di perangkat yang sama, lisensi akan dipulihkan otomatis menggunakan ID Perangkat di atas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
