package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.SoftCardBorder
import com.example.ui.theme.SoftShadowColor

/**
 * Clean Soft UI elevated card:
 * - Light Mode: crisp #FFFFFF surface, #E2E8F0 1dp border, soft diffused shadow
 * - Dark Mode: #1E293B surface, #334155 1dp border, subtle dark depth
 */
@Composable
fun SoftElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color? = null,
    shadowElevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val resolvedBorder = borderColor ?: if (isDark) DarkBorder else SoftCardBorder
    val effectiveElevation = if (isDark) 4.dp else shadowElevation
    val ambientShadow = if (isDark) Color.Black.copy(alpha = 0.25f) else SoftShadowColor.copy(alpha = 0.08f)
    val spotShadow = if (isDark) Color.Black.copy(alpha = 0.35f) else SoftShadowColor.copy(alpha = 0.11f)

    val cardModifier = modifier.shadow(
        elevation = effectiveElevation,
        shape = shape,
        clip = false,
        ambientColor = ambientShadow,
        spotColor = spotShadow
    )

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            color = containerColor,
            border = BorderStroke(1.dp, resolvedBorder)
        ) {
            Column(content = content)
        }
    } else {
        Surface(
            modifier = cardModifier,
            shape = shape,
            color = containerColor,
            border = BorderStroke(1.dp, resolvedBorder)
        ) {
            Column(content = content)
        }
    }
}

/**
 * Clean, neutral edge-to-edge header bar for sub-screens (Checkout, Receipt):
 * - Light Mode: crisp #FFFFFF with #E2E8F0 hairline bottom divider
 * - Dark Mode: seamless #0F172A with #334155 hairline bottom divider
 */
@Composable
fun CompactScreenHeader(
    title: String,
    backContentDescription: String,
    onBack: () -> Unit,
    backButtonTestTag: String = "compact_header_back",
    containerColor: Color? = null,
    titleColor: Color? = null,
    iconTint: Color? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val bg = containerColor ?: if (isDark) DarkBackground else Color.White
    val fgTitle = titleColor ?: if (isDark) DarkTextPrimary else Color(0xFF0F172A)
    val fgIcon = iconTint ?: if (isDark) DarkTextPrimary else Color(0xFF0F172A)
    val dividerColor = if (isDark) DarkBorder else SoftCardBorder

    Surface(
        color = bg,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(50.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag(backButtonTestTag)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = backContentDescription,
                        tint = fgIcon
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = fgTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                actions()
            }
            HorizontalDivider(thickness = 1.dp, color = dividerColor)
        }
    }
}
