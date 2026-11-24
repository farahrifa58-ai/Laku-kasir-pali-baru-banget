package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreProfile
import com.example.ui.Screen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkEmeraldPrimary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.SoftCardBorder
import com.example.util.LakuStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    storeProfile: StoreProfile,
    onSettingsClick: () -> Unit,
    navigationIcon: (@Composable () -> Unit)? = null,
    onLicenseBadgeClick: (() -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val topBarBg = if (isDark) DarkBackground else Color.White
    val topBarTitleColor = if (isDark) DarkTextPrimary else Color(0xFF0F172A)
    val topBarStoreColor = if (isDark) DarkEmeraldPrimary else EmeraldGreenPrimary
    val topBarActionColor = if (isDark) DarkTextSecondary else Color(0xFF475569)
    val dividerColor = if (isDark) DarkBorder else SoftCardBorder

    Surface(
        shadowElevation = 0.dp,
        color = topBarBg
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        StoreAvatar(
                            storeProfile = storeProfile,
                            size = 38.dp,
                            shape = RoundedCornerShape(10.dp),
                            contentDescription = LakuStrings.get("store_profile", storeProfile.language)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = topBarTitleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = storeProfile.storeName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = topBarStoreColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    navigationIcon?.invoke()
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("top_bar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = LakuStrings.get("settings", storeProfile.language),
                            tint = topBarActionColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarBg,
                    titleContentColor = topBarTitleColor,
                    actionIconContentColor = topBarActionColor,
                    navigationIconContentColor = topBarActionColor
                )
            )
            HorizontalDivider(thickness = 1.dp, color = dividerColor)
        }
    }
}

private data class BottomNavTab(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun AppBottomBar(
    currentScreen: Screen,
    cartItemCount: Int,
    lang: String,
    onNavigate: (Screen) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val barBg = if (isDark) DarkSurface else Color.White
    val borderCol = if (isDark) DarkBorder else SoftCardBorder
    val activeColor = if (isDark) DarkEmeraldPrimary else EmeraldGreenPrimary
    val activePillColor = if (isDark) DarkEmeraldPrimary.copy(alpha = 0.16f) else EmeraldGreenPrimary.copy(alpha = 0.12f)
    val inactiveColor = if (isDark) DarkTextSecondary else Color(0xFF64748B)

    val tabs = listOf(
        BottomNavTab(Screen.POS, LakuStrings.get("nav_pos", lang), Icons.Default.ShoppingCart, cartItemCount),
        BottomNavTab(Screen.PRODUCTS, LakuStrings.get("nav_products", lang), Icons.Default.Inventory),
        BottomNavTab(Screen.HISTORY, LakuStrings.get("nav_history", lang), Icons.AutoMirrored.Filled.ReceiptLong),
        BottomNavTab(Screen.EXPENSES, LakuStrings.get("nav_expenses", lang), Icons.Default.Payments),
        BottomNavTab(Screen.DASHBOARD, LakuStrings.get("nav_dashboard", lang), Icons.Default.BarChart)
    )

    Surface(
        color = barBg,
        shadowElevation = if (isDark) 0.dp else 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(thickness = 1.dp, color = borderCol)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val selected = currentScreen == tab.screen
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = true),
                                onClick = { onNavigate(tab.screen) }
                            )
                            .padding(vertical = 4.dp)
                            .testTag("bottom_nav_${tab.screen.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) activePillColor else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (tab.badgeCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = "${tab.badgeCount}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (selected) activeColor else inactiveColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (selected) activeColor else inactiveColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            letterSpacing = (-0.2).sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) activeColor else inactiveColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
