package com.example.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.StoreProfile
import com.example.ui.LakuKasirViewModel
import com.example.ui.theme.PrimaryGreen
import com.example.util.BackupRestoreHelper
import com.example.util.FormatUtils
import com.example.util.LakuStrings
import kotlinx.coroutines.launch

@Composable
fun SettingsSubHeader(
    title: String,
    subtitle: String,
    lang: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("settings_sub_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = LakuStrings.get("back", lang)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class TutorialStepItem(
    val number: Int,
    val titleKey: String,
    val bodyKey: String,
    val icon: ImageVector,
    val accent: Color
)

@Composable
fun TutorialSettingsScreen(
    storeProfile: StoreProfile,
    onBack: () -> Unit
) {
    val lang = storeProfile.language
    val steps = remember {
        listOf(
            TutorialStepItem(1, "tut_step_1_title", "tut_step_1_body", Icons.Default.Storefront, Color(0xFF16A34A)),
            TutorialStepItem(2, "tut_step_2_title", "tut_step_2_body", Icons.Default.Inventory2, Color(0xFF0284C7)),
            TutorialStepItem(3, "tut_step_3_title", "tut_step_3_body", Icons.Default.PointOfSale, Color(0xFF7C3AED)),
            TutorialStepItem(4, "tut_step_4_title", "tut_step_4_body", Icons.Default.Payments, Color(0xFFD97706)),
            TutorialStepItem(5, "tut_step_5_title", "tut_step_5_body", Icons.Default.Print, Color(0xFF0D9488)),
            TutorialStepItem(6, "tut_step_6_title", "tut_step_6_body", Icons.Default.BarChart, Color(0xFFDB2777)),
            TutorialStepItem(7, "tut_step_7_title", "tut_step_7_body", Icons.Default.CloudDone, Color(0xFF4F46E5))
        )
    }

    var expandedStep by remember { mutableStateOf<Int?>(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsSubHeader(
            title = LakuStrings.get("tutorial_title", lang),
            subtitle = LakuStrings.get("tutorial_desc", lang),
            lang = lang,
            onBack = onBack
        )

        Surface(
            color = PrimaryGreen.copy(alpha = 0.08f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = LakuStrings.get("tutorial_header_desc", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        steps.forEach { step ->
            val isExpanded = expandedStep == step.number
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expandedStep = if (isExpanded) null else step.number
                    }
                    .testTag("tutorial_step_${step.number}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp),
                border = if (isExpanded) BorderStroke(1.5.dp, step.accent.copy(alpha = 0.5f)) else null
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(step.accent.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(step.icon, contentDescription = null, tint = step.accent, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = LakuStrings.get(step.titleKey, lang),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = LakuStrings.get(step.bodyKey, lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BackupRestoreSettingsScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lang = storeProfile.language

    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    var isBusy by remember { mutableStateOf(false) }

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isBusy = true
            scope.launch {
                BackupRestoreHelper.restoreBackupFromJsonUri(
                    context = context,
                    uri = uri,
                    currentProfile = storeProfile,
                    onProfileRestored = { restored ->
                        viewModel.updateStoreProfile(restored)
                    },
                    onSuccess = { prodCount, txCount ->
                        isBusy = false
                        Toast.makeText(
                            context,
                            "${LakuStrings.get("restore_success_prefix", lang)} $prodCount ${LakuStrings.get("nav_products", lang)} & $txCount ${LakuStrings.get("transactions_suffix", lang)}!",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onError = { err ->
                        isBusy = false
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSubHeader(
            title = LakuStrings.get("backup_restore", lang),
            subtitle = LakuStrings.get("backup_restore_card_desc", lang),
            lang = lang,
            onBack = onBack
        )

        // Data Summary Card
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
                Text(
                    text = LakuStrings.get("backup_data_summary", lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCounterChip(
                        label = LakuStrings.get("nav_products", lang),
                        count = allProducts.size,
                        color = PrimaryGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCounterChip(
                        label = LakuStrings.get("transactions_suffix", lang),
                        count = allTransactions.size,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCounterChip(
                        label = LakuStrings.get("nav_expenses", lang),
                        count = allExpenses.size,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LakuStrings.get("last_backup_label", lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (storeProfile.lastBackupTimestamp > 0L) {
                            FormatUtils.formatDateTime(storeProfile.lastBackupTimestamp, lang)
                        } else {
                            LakuStrings.get("never_backed_up", lang)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (storeProfile.lastBackupTimestamp > 0L) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Actions Card
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = LakuStrings.get("backup_restore_desc", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Manual Local Backup to Device Storage
                Button(
                    onClick = {
                        isBusy = true
                        scope.launch {
                            val file = BackupRestoreHelper.createBackupJson(
                                context, storeProfile, allProducts, allTransactions, allExpenses
                            )
                            val saved = if (file != null) {
                                BackupRestoreHelper.saveBackupToDevice(context, file)
                            } else false
                            isBusy = false
                            if (saved) {
                                viewModel.updateLastBackupTimestamp()
                                Toast.makeText(context, LakuStrings.get("backup_saved_local_toast", lang), Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, LakuStrings.get("backup_failed_toast", lang), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("backup_manual_local_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LakuStrings.get("backup_manual_local", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // 2. Google Drive Backup
                Button(
                    onClick = {
                        scope.launch {
                            val file = BackupRestoreHelper.createBackupJson(
                                context, storeProfile, allProducts, allTransactions, allExpenses
                            )
                            if (file != null) {
                                viewModel.updateLastBackupTimestamp()
                                BackupRestoreHelper.backupToGoogleDrive(context, file)
                            } else {
                                Toast.makeText(context, LakuStrings.get("backup_failed_toast", lang), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("backup_google_drive_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LakuStrings.get("backup_google_drive", lang), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                // 3. WhatsApp Backup
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val file = BackupRestoreHelper.createBackupJson(
                                context, storeProfile, allProducts, allTransactions, allExpenses
                            )
                            if (file != null) {
                                viewModel.updateLastBackupTimestamp()
                                BackupRestoreHelper.shareBackupToWhatsApp(context, file)
                            } else {
                                Toast.makeText(context, LakuStrings.get("backup_failed_toast", lang), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("backup_whatsapp_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
                    border = BorderStroke(1.dp, PrimaryGreen)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LakuStrings.get("backup_whatsapp", lang), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // 4. Restore from JSON
                FilledTonalButton(
                    onClick = {
                        restoreFileLauncher.launch("application/json")
                    },
                    enabled = !isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("restore_json_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(LakuStrings.get("restore_json", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryCounterChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AboutAppSettingsScreen(
    storeProfile: StoreProfile,
    onBack: () -> Unit
) {
    val lang = storeProfile.language

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSubHeader(
            title = LakuStrings.get("about_app", lang),
            subtitle = LakuStrings.get("about_app_subtitle", lang),
            lang = lang,
            onBack = onBack
        )

        // Hero App Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(PrimaryGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_laku_kasir),
                        contentDescription = LakuStrings.get("app_title", lang),
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = LakuStrings.get("app_title", lang),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryGreen
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = PrimaryGreen.copy(alpha = 0.14f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "${LakuStrings.get("developer_name", lang)} • ${LakuStrings.get("version", lang)}",
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = LakuStrings.get("about_description", lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        // Developer & Version Metadata Card
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
                AboutInfoRow(
                    icon = Icons.Default.Code,
                    label = LakuStrings.get("developer_label", lang),
                    value = LakuStrings.get("developer_name", lang)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AboutInfoRow(
                    icon = Icons.Default.Verified,
                    label = LakuStrings.get("app_version_label", lang),
                    value = LakuStrings.get("app_version_value", lang)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AboutInfoRow(
                    icon = Icons.Default.WorkspacePremium,
                    label = LakuStrings.get("full_version_title", lang),
                    value = LakuStrings.get("full_version_desc", lang)
                )
            }
        }

        // Key Features Card
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LakuStrings.get("features_label", lang),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = LakuStrings.get("features_list", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AboutInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
