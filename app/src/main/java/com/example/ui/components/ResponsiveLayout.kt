package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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

enum class WindowWidthTier {
    COMPACT,   // Phone: < 600.dp
    MEDIUM,    // 7"-10" Tablet: 600.dp .. 1023.dp
    EXPANDED   // Large Tablet / Desktop Windows: >= 1024.dp
}

data class ResponsiveLayoutInfo(
    val screenWidth: Dp,
    val tier: WindowWidthTier,
    val useSidebarNavigation: Boolean, // >= 768.dp
    val useSplitPosView: Boolean       // >= 1024.dp
)

fun computeResponsiveLayoutInfo(maxWidth: Dp): ResponsiveLayoutInfo {
    val tier = when {
        maxWidth >= 1024.dp -> WindowWidthTier.EXPANDED
        maxWidth >= 600.dp -> WindowWidthTier.MEDIUM
        else -> WindowWidthTier.COMPACT
    }
    return ResponsiveLayoutInfo(
        screenWidth = maxWidth,
        tier = tier,
        useSidebarNavigation = maxWidth >= 768.dp,
        useSplitPosView = maxWidth >= 1024.dp
    )
}

private data class SidebarNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

/**
 * Adaptive navigation container for all top-level Laku Kasir tabs:
 * - Phone (< 768.dp): Standard TopBar + Bottom Navigation Bar (AppBottomBar)
 * - Tablet & Desktop (>= 768.dp): Sleek Left Vertical Sidebar with Store Logo/Name + Main Content Pane
 */
@Composable
fun AppNavigationShell(
    title: String,
    currentScreen: Screen,
    storeProfile: StoreProfile,
    cartItemCount: Int,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    floatingActionButton: @Composable () -> Unit = {},
    maxContentWidth: Dp = 1280.dp,
    content: @Composable (layoutInfo: ResponsiveLayoutInfo, contentPadding: PaddingValues) -> Unit
) {
    val lang = storeProfile.language
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val dividerColor = if (isDark) DarkBorder else SoftCardBorder
    val headerBg = if (isDark) DarkBackground else Color.White
    val titleColor = if (isDark) DarkTextPrimary else Color(0xFF0F172A)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val layoutInfo = remember(maxWidth) { computeResponsiveLayoutInfo(maxWidth) }

        if (!layoutInfo.useSidebarNavigation) {
            // Compact / Phone Layout (< 768.dp) — keeps the polished phone layout intact
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    AppTopBar(
                        title = title,
                        storeProfile = storeProfile,
                        onSettingsClick = onOpenSettings
                    )
                },
                bottomBar = {
                    AppBottomBar(
                        currentScreen = currentScreen,
                        cartItemCount = cartItemCount,
                        lang = lang,
                        onNavigate = onNavigate
                    )
                },
                floatingActionButton = floatingActionButton
            ) { innerPadding ->
                content(layoutInfo, innerPadding)
            }
        } else {
            // Tablet & Desktop Layout (>= 768.dp) — Sleek Left Vertical Sidebar + Wide Content Area
            val sidebarWidth = if (layoutInfo.tier == WindowWidthTier.EXPANDED) 224.dp else 204.dp

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AppSidebarRail(
                    currentScreen = currentScreen,
                    storeProfile = storeProfile,
                    cartItemCount = cartItemCount,
                    lang = lang,
                    onNavigate = onNavigate,
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.width(sidebarWidth)
                )

                VerticalDivider(thickness = 1.dp, color = dividerColor)

                Scaffold(
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0),
                    topBar = {
                        Surface(color = headerBg, shadowElevation = 0.dp) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(58.dp)
                                        .padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 19.sp
                                        ),
                                        color = titleColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = storeProfile.storeName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) DarkEmeraldPrimary else EmeraldGreenPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                                HorizontalDivider(thickness = 1.dp, color = dividerColor)
                            }
                        }
                    },
                    floatingActionButton = floatingActionButton
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = maxContentWidth)
                                .fillMaxSize()
                        ) {
                            content(layoutInfo, PaddingValues(0.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSidebarRail(
    currentScreen: Screen,
    storeProfile: StoreProfile,
    cartItemCount: Int,
    lang: String,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val railBg = if (isDark) DarkSurface else Color.White
    val dividerColor = if (isDark) DarkBorder else SoftCardBorder
    val activeColor = if (isDark) DarkEmeraldPrimary else EmeraldGreenPrimary
    val activeBg = if (isDark) DarkEmeraldPrimary.copy(alpha = 0.16f) else EmeraldGreenPrimary.copy(alpha = 0.12f)
    val inactiveColor = if (isDark) DarkTextSecondary else Color(0xFF475569)
    val titleColor = if (isDark) DarkTextPrimary else Color(0xFF0F172A)

    val navItems = listOf(
        SidebarNavItem(Screen.POS, LakuStrings.get("nav_pos", lang), Icons.Default.ShoppingCart, cartItemCount),
        SidebarNavItem(Screen.PRODUCTS, LakuStrings.get("nav_products", lang), Icons.Default.Inventory),
        SidebarNavItem(Screen.HISTORY, LakuStrings.get("nav_history", lang), Icons.AutoMirrored.Filled.ReceiptLong),
        SidebarNavItem(Screen.EXPENSES, LakuStrings.get("nav_expenses", lang), Icons.Default.Payments),
        SidebarNavItem(Screen.DASHBOARD, LakuStrings.get("nav_dashboard", lang), Icons.Default.BarChart)
    )

    Surface(
        color = railBg,
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            // Top Brand & Store Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                StoreAvatar(
                    storeProfile = storeProfile,
                    size = 42.dp,
                    shape = RoundedCornerShape(12.dp),
                    contentDescription = LakuStrings.get("store_profile", lang)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LakuStrings.get("app_title", lang),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = storeProfile.storeName,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = activeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 1.dp, color = dividerColor)
            Spacer(modifier = Modifier.height(12.dp))

            // Primary Navigation Tabs
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                navItems.forEach { item ->
                    val selected = currentScreen == item.screen
                    val interactionSource = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) activeBg else Color.Transparent)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = true),
                                onClick = { onNavigate(item.screen) }
                            )
                            .padding(horizontal = 12.dp)
                            .testTag("sidebar_nav_${item.screen.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) activeColor else inactiveColor,
                            modifier = Modifier.size(21.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.label,
                            fontSize = 13.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) activeColor else titleColor.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (item.badgeCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "${item.badgeCount}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(thickness = 1.dp, color = dividerColor)
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Settings Button
            val settingsInteraction = remember { MutableInteractionSource() }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = settingsInteraction,
                        indication = ripple(bounded = true),
                        onClick = onOpenSettings
                    )
                    .padding(horizontal = 12.dp)
                    .testTag("sidebar_settings_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = LakuStrings.get("settings", lang),
                    tint = inactiveColor,
                    modifier = Modifier.size(21.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = LakuStrings.get("settings", lang),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Centers sub-screens and dialogs neatly on Tablet & Desktop so they don't stretch awkwardly.
 */
@Composable
fun ResponsiveCenteredContainer(
    maxWidth: Dp = 640.dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth(),
            content = content
        )
    }
}
