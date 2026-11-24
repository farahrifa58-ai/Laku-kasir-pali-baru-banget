package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.LakuKasirViewModel
import com.example.ui.components.SoftElevatedCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkDangerRed
import com.example.ui.theme.DarkEmeraldPrimary
import com.example.ui.theme.EmeraldGreenDark
import com.example.util.LicenseManager
import java.util.Locale

@Composable
fun ActivationScreen(
    viewModel: LakuKasirViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val accentGreen = MaterialTheme.colorScheme.primary
    val onAccentGreen = MaterialTheme.colorScheme.onPrimary
    val accentGreenText = if (isDark) DarkEmeraldPrimary else EmeraldGreenDark
    val accentRed = if (isDark) DarkDangerRed else DangerRed

    val isActivating by viewModel.isActivating.collectAsState()
    val activationError by viewModel.activationError.collectAsState()
    val isCheckingRestore by viewModel.isCheckingLicense.collectAsState()

    var businessName by remember { mutableStateOf("") }
    var activationCode by remember { mutableStateOf("") }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val deviceId = remember { LicenseManager.getDeviceId(context) }
    val displayedError = localValidationError ?: activationError

    val submitAction = {
        focusManager.clearFocus()
        val trimmedName = businessName.trim()
        val trimmedCode = activationCode.trim().uppercase(Locale.US)
        when {
            trimmedName.isEmpty() -> {
                localValidationError = "Nama Usaha / Toko wajib diisi."
            }
            trimmedCode.isEmpty() -> {
                localValidationError = "Kode Aktivasi Pembelian wajib diisi."
            }
            else -> {
                localValidationError = null
                viewModel.submitActivation(
                    businessName = trimmedName,
                    rawCode = trimmedCode
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Identity Header
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, accentGreen.copy(alpha = 0.25f)),
                shadowElevation = if (isDark) 0.dp else 8.dp,
                modifier = Modifier.size(84.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_laku_kasir),
                        contentDescription = "Logo Laku Kasir",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Aktivasi Laku Kasir",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Masukkan Nama Usaha dan Kode Aktivasi sekali pakai untuk membuka aplikasi secara permanen di perangkat ini (100% Offline setelah aktif).",
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Activation Form Card
            SoftElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Badge: Lisensi 1 Perangkat Tanpa Login
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentGreen.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = accentGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tanpa Login Akun • Data Tersimpan 100% Lokal di Perangkat",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentGreenText
                            )
                        }
                    }

                    // Input 1: Nama Usaha / Toko
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = {
                            businessName = it
                            localValidationError = null
                            viewModel.clearActivationError()
                        },
                        enabled = !isActivating && !isCheckingRestore,
                        label = { Text("Nama Usaha / Toko *") },
                        placeholder = { Text("Contoh: Warung Berkah Jaya") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = accentGreen
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentGreen,
                            focusedLabelColor = accentGreenText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_business_name_input")
                    )

                    // Input 2: Kode Aktivasi Pembelian (Auto Uppercase)
                    OutlinedTextField(
                        value = activationCode,
                        onValueChange = { raw ->
                            activationCode = raw.uppercase(Locale.US)
                            localValidationError = null
                            viewModel.clearActivationError()
                        },
                        enabled = !isActivating && !isCheckingRestore,
                        label = { Text("Kode Aktivasi Pembelian *") },
                        placeholder = { Text("Contoh: LAKU-2026-XXXX") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = accentGreen
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { submitAction() }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentGreen,
                            focusedLabelColor = accentGreenText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_code_input")
                    )

                    // Error Message Banner
                    if (!displayedError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, accentRed.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("activation_error_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = accentRed,
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = displayedError,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accentRed,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    // Primary CTA: Aktifkan Aplikasi
                    Button(
                        onClick = submitAction,
                        enabled = !isActivating && !isCheckingRestore,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentGreen,
                            contentColor = onAccentGreen
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("activation_submit_button")
                    ) {
                        if (isActivating) {
                            CircularProgressIndicator(
                                color = onAccentGreen,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Memverifikasi Lisensi...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = onAccentGreen
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = onAccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aktifkan Aplikasi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = onAccentGreen
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Secondary CTA: Beli Kode Aktivasi / Hubungi Admin
                    OutlinedButton(
                        onClick = { LicenseManager.contactSellerForLicense(context) },
                        enabled = !isActivating,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, accentGreen.copy(alpha = 0.55f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentGreenText),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_contact_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Beli Kode Aktivasi / Hubungi Admin",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Auto-Restore Retry for Re-install
                    TextButton(
                        onClick = {
                            localValidationError = null
                            viewModel.retryAutoRestore { restored ->
                                if (restored) {
                                    Toast.makeText(
                                        context,
                                        "Lisensi perangkat berhasil dipulihkan otomatis!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Belum ditemukan lisensi terdaftar untuk perangkat ini.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        enabled = !isActivating && !isCheckingRestore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activation_retry_restore_button")
                    ) {
                        if (isCheckingRestore) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = accentGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Memeriksa lisensi perangkat...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = accentGreenText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Install ulang? Cek Pemulihan Lisensi Otomatis",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentGreenText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Device ID Info Pill with Copy Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ID Perangkat Unik",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = deviceId,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
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
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .testTag("activation_copy_device_id_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin ID Perangkat",
                            tint = accentGreenText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
