package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.LanguageCatalog
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary

@Composable
fun LanguageSelectionDialog(
    activeCode: String,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("Semua") }

    val filteredLanguages = remember(searchQuery, selectedRegion) {
        LanguageCatalog.ALL_LANGUAGES.filter { lang ->
            val matchesSearch = searchQuery.isBlank() ||
                    lang.name.contains(searchQuery, ignoreCase = true) ||
                    lang.nativeName.contains(searchQuery, ignoreCase = true) ||
                    lang.country.contains(searchQuery, ignoreCase = true) ||
                    lang.code.contains(searchQuery, ignoreCase = true)
            val matchesRegion = selectedRegion == "Semua" || lang.region == selectedRegion
            matchesSearch && matchesRegion
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = EmeraldGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pilih Bahasa Aplikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari bahasa atau negara...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_language_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Region filter chips (Asia Tenggara, Asia, Eropa, Rusia, Amerika, Afrika)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(LanguageCatalog.REGIONS) { region ->
                        val isSelected = selectedRegion == region
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRegion = region },
                            label = { Text(region, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                // Languages List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredLanguages) { item ->
                        val isCurrent = item.code.equals(activeCode, ignoreCase = true)
                        Surface(
                            onClick = {
                                onSelect(item)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) EmeraldGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                1.dp,
                                if (isCurrent) EmeraldGreenPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("language_item_${item.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.flag,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.nativeName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isCurrent) EmeraldGreenDark else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = EmeraldGreenPrimary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = item.code.uppercase(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldGreenDark,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${item.name} • ${item.country}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Terpilih",
                                        tint = EmeraldGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = EmeraldGreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
