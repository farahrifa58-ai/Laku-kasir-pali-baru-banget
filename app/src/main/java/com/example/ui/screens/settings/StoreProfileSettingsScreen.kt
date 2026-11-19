package com.example.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreProfile
import com.example.ui.LakuKasirViewModel
import com.example.ui.components.StoreAvatar
import com.example.ui.components.StoreIconCatalog
import com.example.ui.theme.PrimaryGreen
import com.example.util.ImageUtils
import com.example.util.LakuStrings

@Composable
fun StoreProfileSettingsScreen(
    viewModel: LakuKasirViewModel,
    storeProfile: StoreProfile,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lang = storeProfile.language

    var storeName by remember(storeProfile.storeName) { mutableStateOf(storeProfile.storeName) }
    var ownerName by remember(storeProfile.ownerName) { mutableStateOf(storeProfile.ownerName) }
    var address by remember(storeProfile.address) { mutableStateOf(storeProfile.address) }
    var phone by remember(storeProfile.phone) { mutableStateOf(storeProfile.phone) }
    var operatingHours by remember(storeProfile.operatingHours) { mutableStateOf(storeProfile.operatingHours) }
    var logoUri by remember(storeProfile.logoUri) { mutableStateOf(storeProfile.logoUri) }
    var storeIconKey by remember(storeProfile.storeIconKey) { mutableStateOf(storeProfile.storeIconKey) }
    var storeIconBgColor by remember(storeProfile.storeIconBgColor) { mutableStateOf(storeProfile.storeIconBgColor) }
    var savedBanner by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = ImageUtils.saveImageToInternalStorage(context, uri, "store_logo")
            if (savedPath != null) {
                logoUri = savedPath
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
            title = LakuStrings.get("store_profile", lang),
            subtitle = LakuStrings.get("store_profile_card_desc", lang),
            lang = lang,
            onBack = onBack
        )

        // Photo / Icon & Background Color Card
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StoreAvatar(
                        logoUri = logoUri,
                        iconKey = storeIconKey,
                        bgColorLong = storeIconBgColor,
                        size = 80.dp,
                        shape = RoundedCornerShape(20.dp),
                        contentDescription = LakuStrings.get("store_profile", lang),
                        modifier = Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(20.dp)
                        )
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                logoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("upload_store_photo_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (logoUri == null) LakuStrings.get("upload_logo", lang) else LakuStrings.get("change_logo", lang),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (logoUri != null) {
                            OutlinedButton(
                                onClick = { logoUri = null },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("remove_store_photo_button")
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LakuStrings.get("delete_logo", lang), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Text(
                    text = LakuStrings.get("store_photo_hint", lang),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Store Icon Picker
                Text(
                    text = LakuStrings.get("choose_store_icon", lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(StoreIconCatalog.ICONS) { option ->
                        val isSelected = logoUri == null && storeIconKey == option.key
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(68.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) PrimaryGreen.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    logoUri = null
                                    storeIconKey = option.key
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .testTag("store_icon_${option.key}")
                        ) {
                            StoreAvatar(
                                logoUri = null,
                                iconKey = option.key,
                                bgColorLong = storeIconBgColor,
                                size = 38.dp,
                                shape = RoundedCornerShape(10.dp),
                                contentDescription = LakuStrings.get(option.labelKey, lang)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = LakuStrings.get(option.labelKey, lang),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Background Color Picker
                Text(
                    text = LakuStrings.get("choose_bg_color", lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(StoreIconCatalog.BG_COLORS) { colorLong ->
                        val isSelected = storeIconBgColor == colorLong
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .border(
                                    width = if (isSelected) 3.dp else 1.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.White.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    storeIconBgColor = colorLong
                                    if (logoUri != null) {
                                        logoUri = null
                                        if (storeIconKey == "default_logo") {
                                            storeIconKey = "storefront"
                                        }
                                    }
                                }
                                .testTag("store_bg_color_$colorLong"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = LakuStrings.get("selected", lang),
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Store Information Form Card
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
                    value = storeName,
                    onValueChange = { storeName = it; savedBanner = false },
                    label = { Text(LakuStrings.get("store_name", lang)) },
                    placeholder = { Text(LakuStrings.get("store_name_placeholder", lang)) },
                    leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_store_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it; savedBanner = false },
                    label = { Text(LakuStrings.get("owner_name", lang)) },
                    placeholder = { Text(LakuStrings.get("owner_name_placeholder", lang)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_owner_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; savedBanner = false },
                    label = { Text(LakuStrings.get("store_address", lang)) },
                    placeholder = { Text(LakuStrings.get("store_address_placeholder", lang)) },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_store_address"),
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; savedBanner = false },
                    label = { Text(LakuStrings.get("whatsapp_no", lang)) },
                    placeholder = { Text(LakuStrings.get("whatsapp_no_placeholder", lang)) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_store_phone"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = operatingHours,
                    onValueChange = { operatingHours = it; savedBanner = false },
                    label = { Text(LakuStrings.get("operating_hours", lang)) },
                    placeholder = { Text(LakuStrings.get("operating_hours_placeholder", lang)) },
                    supportingText = { Text(LakuStrings.get("operating_hours_helper", lang)) },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_operating_hours"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
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
                        storeName = storeName.trim().ifBlank { "Laku Kasir Store" },
                        ownerName = ownerName.trim(),
                        address = address.trim(),
                        phone = phone.trim(),
                        operatingHours = operatingHours.trim(),
                        logoUri = logoUri,
                        storeIconKey = storeIconKey,
                        storeIconBgColor = storeIconBgColor
                    )
                )
                savedBanner = true
                Toast.makeText(context, LakuStrings.get("settings_saved", lang), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_store_profile_button"),
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
