package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreProfile
import com.example.ui.LakuKasirViewModel
import com.example.ui.theme.PrimaryGreen
import com.example.util.FormatUtils
import com.example.util.LakuStrings

@Composable
fun TaxDiscountSettingsScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lang = storeProfile.language

    var ppnMode by remember(storeProfile.ppnMode) { mutableStateOf(storeProfile.ppnMode) }
    var ppnRateText by remember(storeProfile.defaultPpnRate) {
        mutableStateOf(
            if (storeProfile.defaultPpnRate % 1.0 == 0.0) storeProfile.defaultPpnRate.toInt().toString()
            else storeProfile.defaultPpnRate.toString()
        )
    }
    var discountMode by remember(storeProfile.discountMode) { mutableStateOf(storeProfile.discountMode) }
    var defaultDiscountText by remember(storeProfile.defaultDiscountValue, storeProfile.discountMode) {
        mutableStateOf(
            if (storeProfile.defaultDiscountValue <= 0.0) ""
            else if (storeProfile.discountMode == "percent") {
                if (storeProfile.defaultDiscountValue % 1.0 == 0.0) storeProfile.defaultDiscountValue.toInt().toString()
                else storeProfile.defaultDiscountValue.toString()
            } else {
                FormatUtils.formatNumberWithDots(storeProfile.defaultDiscountValue)
            }
        )
    }
    var savedBanner by remember { mutableStateOf(false) }

    val parsedPpnRate = ppnRateText.replace(",", ".").toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 11.0
    val parsedDiscountValue = if (discountMode == "percent") {
        defaultDiscountText.replace(",", ".").toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0
    } else {
        FormatUtils.parseNumberFromDotted(defaultDiscountText)
    }

    // Live simulation calculation on 100,000 subtotal
    val sampleSubtotal = 100000.0
    val simDiscount = if (discountMode == "percent") {
        sampleSubtotal * (parsedDiscountValue / 100.0)
    } else {
        parsedDiscountValue.coerceAtMost(sampleSubtotal)
    }
    val afterDisc = (sampleSubtotal - simDiscount).coerceAtLeast(0.0)
    val simPpn = if (ppnMode == "auto") afterDisc * (parsedPpnRate / 100.0) else 0.0
    val simTotal = afterDisc + simPpn

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSubHeader(
            title = LakuStrings.get("ppn_discount_title", lang),
            subtitle = LakuStrings.get("ppn_discount_desc", lang),
            lang = lang,
            onBack = onBack
        )

        // PPN Settings Card
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
                    Icon(Icons.Default.Percent, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LakuStrings.get("ppn_section_title", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeChoiceCard(
                        title = LakuStrings.get("ppn_mode_auto", lang),
                        icon = Icons.Default.AutoAwesome,
                        selected = ppnMode == "auto",
                        onClick = { ppnMode = "auto"; savedBanner = false },
                        modifier = Modifier.weight(1f).testTag("ppn_mode_auto_card")
                    )
                    ModeChoiceCard(
                        title = LakuStrings.get("ppn_mode_manual", lang),
                        icon = Icons.Default.EditNote,
                        selected = ppnMode == "manual",
                        onClick = { ppnMode = "manual"; savedBanner = false },
                        modifier = Modifier.weight(1f).testTag("ppn_mode_manual_card")
                    )
                }

                Text(
                    text = if (ppnMode == "auto") LakuStrings.get("ppn_auto_desc", lang)
                    else LakuStrings.get("ppn_manual_desc", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (ppnMode == "auto") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("5", "10", "11", "12").forEach { rateOpt ->
                            FilterChip(
                                selected = ppnRateText == rateOpt,
                                onClick = { ppnRateText = rateOpt; savedBanner = false },
                                label = { Text("$rateOpt%", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = ppnRateText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }.take(5)
                            ppnRateText = filtered
                            savedBanner = false
                        },
                        label = { Text(LakuStrings.get("ppn_rate_label", lang)) },
                        suffix = { Text("%", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_ppn_rate")
                    )
                }
            }
        }

        // Discount Settings Card
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
                    Icon(Icons.Default.LocalOffer, contentDescription = null, tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = LakuStrings.get("discount_section_title", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeChoiceCard(
                        title = LakuStrings.get("discount_mode_manual", lang),
                        icon = Icons.Default.Payments,
                        selected = discountMode == "manual",
                        onClick = {
                            if (discountMode != "manual") {
                                discountMode = "manual"
                                defaultDiscountText = ""
                                savedBanner = false
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("discount_mode_manual_card")
                    )
                    ModeChoiceCard(
                        title = LakuStrings.get("discount_mode_percent", lang),
                        icon = Icons.Default.Percent,
                        selected = discountMode == "percent",
                        onClick = {
                            if (discountMode != "percent") {
                                discountMode = "percent"
                                defaultDiscountText = ""
                                savedBanner = false
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("discount_mode_percent_card")
                    )
                }

                Text(
                    text = if (discountMode == "percent") LakuStrings.get("discount_percent_desc", lang)
                    else LakuStrings.get("discount_manual_desc", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = defaultDiscountText,
                    onValueChange = { input ->
                        defaultDiscountText = if (discountMode == "percent") {
                            input.filter { it.isDigit() || it == '.' || it == ',' }.take(5)
                        } else {
                            FormatUtils.formatInputDotted(input)
                        }
                        savedBanner = false
                    },
                    label = { Text("${LakuStrings.get("default_discount_label", lang)} (${LakuStrings.get("optional", lang)})") },
                    prefix = if (discountMode == "manual") {
                        { Text("${FormatUtils.currencySymbol} ") }
                    } else null,
                    suffix = if (discountMode == "percent") {
                        { Text("%", fontWeight = FontWeight.Bold) }
                    } else null,
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (discountMode == "percent") KeyboardType.Decimal else KeyboardType.Number
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_default_discount")
                )
            }
        }

        // Simulation Preview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryGreen.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = LakuStrings.get("simulation_example", lang),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    fontSize = 13.sp
                )
                SimLineRow(LakuStrings.get("sample_subtotal", lang), FormatUtils.formatRupiah(sampleSubtotal))
                SimLineRow(
                    label = if (discountMode == "percent") "${LakuStrings.get("discount", lang)} (${parsedDiscountValue}%)"
                    else LakuStrings.get("discount", lang),
                    value = "-${FormatUtils.formatRupiah(simDiscount)}",
                    valueColor = MaterialTheme.colorScheme.error
                )
                SimLineRow(
                    label = if (ppnMode == "auto") "${LakuStrings.get("ppn_label", lang)} (${parsedPpnRate}%)"
                    else "${LakuStrings.get("ppn_label", lang)} (${LakuStrings.get("ppn_mode_manual", lang)})",
                    value = "+${FormatUtils.formatRupiah(simPpn)}"
                )
                HorizontalDivider(color = PrimaryGreen.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                SimLineRow(
                    label = LakuStrings.get("total_payment", lang),
                    value = FormatUtils.formatRupiah(simTotal),
                    isBold = true,
                    valueColor = PrimaryGreen
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
                        ppnMode = ppnMode,
                        defaultPpnRate = parsedPpnRate,
                        discountMode = discountMode,
                        defaultDiscountValue = parsedDiscountValue
                    )
                )
                savedBanner = true
                Toast.makeText(context, LakuStrings.get("settings_saved", lang), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_tax_discount_button"),
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
private fun ModeChoiceCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) PrimaryGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) PrimaryGreen else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) PrimaryGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SimLineRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 14.sp else 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = if (isBold) 14.sp else 12.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}
