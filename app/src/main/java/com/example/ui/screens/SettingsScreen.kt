package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LanguageCatalog
import com.example.ui.LakuKasirViewModel
import com.example.ui.components.StoreAvatar
import com.example.ui.screens.settings.*
import com.example.ui.theme.PrimaryGreen
import com.example.util.LakuStrings

private enum class SettingsSection {
    HUB,
    STORE_PROFILE,
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
    val lang = storeProfile.language
    var currentSection by remember { mutableStateOf(SettingsSection.HUB) }

    BackHandler(enabled = currentSection != SettingsSection.HUB) {
        currentSection = SettingsSection.HUB
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { currentSection = SettingsSection.STORE_PROFILE }
                        .testTag("settings_card_store_profile"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
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
                                color = PrimaryGreen,
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
                            tint = PrimaryGreen
                        )
                    }
                }

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
                    badgeText = "v1.1",
                    icon = Icons.Default.Info,
                    accentColor = PrimaryGreen,
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    color = accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
