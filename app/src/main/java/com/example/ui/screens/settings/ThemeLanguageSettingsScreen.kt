package com.example.ui.screens.settings

import android.widget.Toast
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageCatalog
import com.example.data.model.StoreProfile
import com.example.ui.LakuKasirViewModel
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.theme.PrimaryGreen
import com.example.util.LakuStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeLanguageSettingsScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lang = storeProfile.language
    val activeLangObj = remember(lang) { LanguageCatalog.getLanguage(lang) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSubHeader(
            title = LakuStrings.get("theme_language_title", lang),
            subtitle = LakuStrings.get("theme_language_desc", lang),
            lang = lang,
            onBack = onBack
        )

        // Theme Card
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
                    Icon(Icons.Default.Palette, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LakuStrings.get("theme", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                ThemeOptionRow(
                    title = LakuStrings.get("theme_light", lang),
                    subtitle = LakuStrings.get("theme_light_desc", lang),
                    icon = Icons.Default.LightMode,
                    selected = storeProfile.themeMode == "light",
                    onClick = { viewModel.setThemeMode("light") },
                    tag = "theme_option_light"
                )

                ThemeOptionRow(
                    title = LakuStrings.get("theme_dark", lang),
                    subtitle = LakuStrings.get("theme_dark_desc", lang),
                    icon = Icons.Default.DarkMode,
                    selected = storeProfile.themeMode == "dark",
                    onClick = { viewModel.setThemeMode("dark") },
                    tag = "theme_option_dark"
                )

                ThemeOptionRow(
                    title = LakuStrings.get("theme_system", lang),
                    subtitle = LakuStrings.get("theme_language_desc", lang),
                    icon = Icons.Default.SettingsBrightness,
                    selected = storeProfile.themeMode == "system",
                    onClick = { viewModel.setThemeMode("system") },
                    tag = "theme_option_system"
                )
            }
        }

        // Language Card
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LakuStrings.get("language", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                        .testTag("open_language_picker_card"),
                    shape = RoundedCornerShape(14.dp),
                    color = PrimaryGreen.copy(alpha = 0.08f),
                    border = BorderStroke(1.5.dp, PrimaryGreen.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(activeLangObj.flag, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = activeLangObj.nativeName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = activeLangObj.country,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        FilledTonalButton(
                            onClick = { showLanguageDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("choose_language_button")
                        ) {
                            Text(LakuStrings.get("choose_language", lang), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Text(
                    text = LakuStrings.get("quick_languages_label", lang),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val quickCodes = listOf("id", "en", "ms", "vi", "th", "fil", "ja", "ko", "zh", "ar", "de", "fr", "es")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickCodes.forEach { code ->
                        val item = LanguageCatalog.getLanguage(code)
                        val isSelected = lang.equals(code, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setLanguage(item.code)
                                Toast.makeText(
                                    context,
                                    "${LakuStrings.get("language_changed_toast", item.code)} ${item.nativeName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            label = {
                                Text(
                                    "${item.flag} ${item.nativeName}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("quick_lang_$code")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            activeLanguageCode = lang,
            uiLang = lang,
            onDismiss = { showLanguageDialog = false },
            onSelectLanguage = { selected ->
                viewModel.setLanguage(selected.code)
                showLanguageDialog = false
                Toast.makeText(
                    context,
                    "${LakuStrings.get("language_changed_toast", selected.code)} ${selected.nativeName}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (selected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
            )
        }
    }
}
