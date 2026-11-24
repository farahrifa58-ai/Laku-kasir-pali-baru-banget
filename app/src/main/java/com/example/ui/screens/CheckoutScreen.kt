package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Categories
import com.example.data.model.Transaction
import com.example.ui.LakuKasirViewModel
import com.example.ui.components.CompactScreenHeader
import com.example.ui.components.SoftElevatedCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkEmeraldPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.SoftCardBorder
import com.example.ui.theme.SoftShadowColor
import com.example.ui.theme.StatusDebt
import com.example.util.FormatUtils
import com.example.util.LakuStrings

@Composable
fun CheckoutScreen(
    viewModel: LakuKasirViewModel,
    lang: String,
    onBack: () -> Unit,
    onSuccess: (Transaction) -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val discountMode by viewModel.discountMode.collectAsState()
    val manualDiscount by viewModel.manualDiscount.collectAsState()
    val isPpnAuto by viewModel.isPpnAuto.collectAsState()
    val ppnRate by viewModel.ppnRate.collectAsState()
    val manualPpnAmountText by viewModel.manualPpnAmount.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val cashReceivedText by viewModel.cashReceivedInput.collectAsState()

    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accentGreen = MaterialTheme.colorScheme.primary
    val onAccentGreen = MaterialTheme.colorScheme.onPrimary
    val accentGreenText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark

    val cartSubtotal = remember(cart) { cart.sumOf { it.subtotal } }
    val discountVal = remember(cartSubtotal, discountMode, manualDiscount) {
        viewModel.calculateDiscountAmount(cartSubtotal, discountMode, manualDiscount)
    }
    val manualPpnVal = remember(manualPpnAmountText) { FormatUtils.parseNumberFromDotted(manualPpnAmountText) }
    val ppnVal = remember(isPpnAuto, ppnRate, manualPpnVal, cartSubtotal, discountVal) {
        when {
            isPpnAuto -> {
                val afterDiscount = maxOf(0.0, cartSubtotal - discountVal)
                afterDiscount * (ppnRate / 100.0)
            }
            manualPpnAmountText.isNotBlank() -> manualPpnVal
            else -> 0.0
        }
    }
    val totalPayable = remember(cartSubtotal, discountVal, ppnVal) {
        maxOf(0.0, (cartSubtotal - discountVal) + ppnVal)
    }

    val cashReceived = remember(cashReceivedText) {
        FormatUtils.parseNumberFromDotted(cashReceivedText)
    }

    val changeAmount = remember(selectedPaymentMethod, cashReceived, totalPayable) {
        if (selectedPaymentMethod == "Tunai" && cashReceived > totalPayable) {
            cashReceived - totalPayable
        } else {
            0.0
        }
    }

    val quickAddOptions = remember(FormatUtils.currentCurrencyCode) {
        if (FormatUtils.currentCurrencyCode == "IDR" || FormatUtils.currentCurrencyCode == "VND" || FormatUtils.currentCurrencyCode == "KRW") {
            listOf(10000.0, 20000.0, 50000.0, 100000.0)
        } else if (FormatUtils.currentCurrencyCode == "JPY") {
            listOf(500.0, 1000.0, 5000.0, 10000.0)
        } else {
            listOf(5.0, 10.0, 20.0, 50.0)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CompactScreenHeader(
                title = LakuStrings.get("payment", lang),
                backContentDescription = LakuStrings.get("back", lang),
                onBack = onBack,
                backButtonTestTag = "checkout_back_button"
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                viewModel.processCheckout { savedTx ->
                                    onSuccess(savedTx)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentGreen,
                                contentColor = onAccentGreen
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .widthIn(max = 580.dp)
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_checkout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = onAccentGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LakuStrings.get("confirm_view_receipt", lang),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = onAccentGreen
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Compact Total Bill Highlight Banner
            if (isDark) {
                SoftElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    containerColor = DarkSurface,
                    borderColor = DarkEmeraldPrimary.copy(alpha = 0.48f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LakuStrings.get("total_bill", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            if (discountVal > 0) {
                                Text(
                                    text = "${LakuStrings.get("saved_discount", lang)} ${FormatUtils.formatRupiah(discountVal)}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            text = FormatUtils.formatRupiah(totalPayable),
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkEmeraldPrimary,
                            maxLines = 1
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = EmeraldGreenDark.copy(alpha = 0.2f),
                            spotColor = EmeraldGreenDark.copy(alpha = 0.25f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(EmeraldGreenDark, EmeraldGreenPrimary)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LakuStrings.get("total_bill", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f),
                                letterSpacing = 0.8.sp
                            )
                            if (discountVal > 0) {
                                Text(
                                    text = "${LakuStrings.get("saved_discount", lang)} ${FormatUtils.formatRupiah(discountVal)}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            text = FormatUtils.formatRupiah(totalPayable),
                            fontSize = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }

            // Payment Methods (Compact 2-row layout: 3 + 2)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = LakuStrings.get("payment_method", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val firstRowMethods = Categories.PAYMENT_METHODS.take(3)
                val secondRowMethods = Categories.PAYMENT_METHODS.drop(3)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    firstRowMethods.forEach { method ->
                        PaymentMethodChip(
                            method = method,
                            isSelected = selectedPaymentMethod == method,
                            lang = lang,
                            onClick = {
                                viewModel.selectedPaymentMethod.value = method
                                if (method == "Tunai" && cashReceivedText.isBlank()) {
                                    viewModel.setExactCash()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (secondRowMethods.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        secondRowMethods.forEach { method ->
                            PaymentMethodChip(
                                method = method,
                                isSelected = selectedPaymentMethod == method,
                                lang = lang,
                                onClick = {
                                    viewModel.selectedPaymentMethod.value = method
                                    if (method == "Tunai" && cashReceivedText.isBlank()) {
                                        viewModel.setExactCash()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Compact Cash Payment Section
            if (selectedPaymentMethod == "Tunai") {
                SoftElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LakuStrings.get("cash_payment_title", lang),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = accentGreenText
                            )
                            Surface(
                                onClick = { viewModel.setExactCash() },
                                shape = RoundedCornerShape(8.dp),
                                color = accentGreen,
                                modifier = Modifier.testTag("exact_cash_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = onAccentGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = LakuStrings.get("exact_cash", lang),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onAccentGreen
                                    )
                                }
                            }
                        }

                        // Compact Cash Input Field
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = LakuStrings.get("cash_received", lang),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.65f),
                                border = BorderStroke(1.dp, accentGreen.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${FormatUtils.currencySymbol} ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = accentGreenText
                                    )
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (cashReceivedText.isEmpty()) {
                                            Text(
                                                text = FormatUtils.formatNumberWithDots(totalPayable),
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        }
                                        BasicTextField(
                                            value = cashReceivedText,
                                            onValueChange = { viewModel.cashReceivedInput.value = FormatUtils.formatInputDotted(it) },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            cursorBrush = SolidColor(accentGreen),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("cash_received_input")
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Add Nominal Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickAddOptions.forEach { amount ->
                                val btnText = if (amount >= 1000) "+${(amount / 1000).toInt()}k" else "+${amount.toInt()}"
                                OutlinedButton(
                                    onClick = { viewModel.addQuickCash(amount) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(9.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = btnText,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentGreenText,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Compact Change Row
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (changeAmount > 0.0) {
                                accentGreen.copy(alpha = 0.14f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("change_info_row")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = LakuStrings.get("change", lang),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(changeAmount),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = if (changeAmount > 0.0) 16.sp else 14.sp,
                                    color = if (changeAmount > 0.0) accentGreenText else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else if (selectedPaymentMethod == "Kasbon") {
                SoftElevatedCard(
                    shape = RoundedCornerShape(14.dp),
                    containerColor = StatusDebt.copy(alpha = 0.09f),
                    borderColor = StatusDebt.copy(alpha = 0.3f),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = LakuStrings.get("debt_note_title", lang),
                            fontWeight = FontWeight.Bold,
                            color = StatusDebt,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = LakuStrings.get("debt_note_desc", lang),
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Compact Single-Row PPN (Tax) Card
            SoftElevatedCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LakuStrings.get("ppn_full_title", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val rateLabel = if (ppnRate % 1.0 == 0.0) "${ppnRate.toInt()}%" else "$ppnRate%"
                        Text(
                            text = if (isPpnAuto) {
                                "${LakuStrings.get("ppn_rate_prefix", lang)} $rateLabel  (+${FormatUtils.formatRupiah(ppnVal)})"
                            } else {
                                LakuStrings.get("ppn_mode_manual", lang)
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isPpnAuto) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isPpnAuto) accentGreenText else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!isPpnAuto) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .width(102.dp)
                                .height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = FormatUtils.currencySymbol,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (manualPpnAmountText.isEmpty()) {
                                        Text(
                                            text = "0",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    BasicTextField(
                                        value = manualPpnAmountText,
                                        onValueChange = { viewModel.manualPpnAmount.value = FormatUtils.formatInputDotted(it) },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End
                                        ),
                                        cursorBrush = SolidColor(accentGreen),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    Switch(
                        checked = isPpnAuto,
                        onCheckedChange = { viewModel.isPpnAuto.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = onAccentGreen,
                            checkedTrackColor = accentGreen
                        ),
                        modifier = Modifier
                            .scale(0.86f)
                            .testTag("ppn_auto_switch")
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun PaymentMethodChip(
    method: String,
    isSelected: Boolean,
    lang: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (method) {
        "Tunai" -> Icons.Default.Money
        "Transfer" -> Icons.Default.AccountBalance
        "QRIS" -> Icons.Default.QrCode
        "Debit/Kredit" -> Icons.Default.CreditCard
        "Kasbon" -> Icons.Default.PendingActions
        else -> Icons.Default.Money
    }

    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accentGreen = MaterialTheme.colorScheme.primary
    val onAccentGreen = MaterialTheme.colorScheme.onPrimary
    val accentGreenText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark
    val selectedColor = if (method == "Kasbon") StatusDebt else accentGreen
    val selectedContentColor = if (method == "Kasbon") Color.White else onAccentGreen

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) selectedColor else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .height(40.dp)
            .shadow(
                elevation = if (isSelected) 4.dp else 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = SoftShadowColor.copy(alpha = 0.06f),
                spotColor = SoftShadowColor.copy(alpha = 0.09f)
            )
            .testTag("payment_method_$method")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) selectedContentColor else accentGreenText,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = LakuStrings.translatePaymentMethod(method, lang),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 11.5.sp,
                color = if (isSelected) selectedContentColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
