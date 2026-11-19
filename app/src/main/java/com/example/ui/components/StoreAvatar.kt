package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.StoreProfile

data class StoreIconOption(
    val key: String,
    val icon: ImageVector?,
    val labelKey: String
)

object StoreIconCatalog {
    val ICONS: List<StoreIconOption> = listOf(
        StoreIconOption("default_logo", null, "icon_default"),
        StoreIconOption("storefront", Icons.Default.Storefront, "icon_store"),
        StoreIconOption("coffee", Icons.Default.Coffee, "icon_cafe"),
        StoreIconOption("restaurant", Icons.Default.Restaurant, "icon_food"),
        StoreIconOption("grocery", Icons.Default.LocalGroceryStore, "icon_grocery"),
        StoreIconOption("shopping_bag", Icons.Default.ShoppingBag, "icon_retail"),
        StoreIconOption("bakery", Icons.Default.BakeryDining, "icon_bakery"),
        StoreIconOption("fashion", Icons.Default.Checkroom, "icon_fashion"),
        StoreIconOption("electronics", Icons.Default.Devices, "icon_electronics"),
        StoreIconOption("pharmacy", Icons.Default.LocalPharmacy, "icon_pharmacy"),
        StoreIconOption("salon", Icons.Default.Spa, "icon_salon"),
        StoreIconOption("hardware", Icons.Default.Build, "icon_service"),
        StoreIconOption("pets", Icons.Default.Pets, "icon_pets")
    )

    val BG_COLORS: List<Long> = listOf(
        0xFF16A34A, // Emerald Green
        0xFF0284C7, // Ocean Blue
        0xFF4F46E5, // Royal Indigo
        0xFF7C3AED, // Violet
        0xFFD97706, // Warm Amber
        0xFFDC2626, // Crimson Red
        0xFFDB2777, // Berry Pink
        0xFF0D9488, // Deep Teal
        0xFF1E293B, // Slate Dark
        0xFF65A30D  // Fresh Lime
    )

    fun getIcon(key: String): ImageVector? {
        return ICONS.find { it.key == key }?.icon
    }
}

@Composable
fun StoreAvatar(
    logoUri: String?,
    iconKey: String,
    bgColorLong: Long,
    size: Dp = 64.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    contentDescription: String = "Store Logo",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bgColor = Color(bgColorLong)
    val selectedIcon = StoreIconCatalog.getIcon(iconKey)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                if (logoUri != null) Color(0xFFF8FAFC)
                else if (iconKey == "default_logo") bgColor.copy(alpha = 0.14f)
                else bgColor
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            logoUri != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(logoUri)
                        .error(R.drawable.logo_laku_kasir)
                        .fallback(R.drawable.logo_laku_kasir)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            selectedIcon != null -> {
                Icon(
                    imageVector = selectedIcon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.54f)
                )
            }
            else -> {
                Image(
                    painter = painterResource(id = R.drawable.logo_laku_kasir),
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(size * 0.14f)
                )
            }
        }
    }
}

@Composable
fun StoreAvatar(
    storeProfile: StoreProfile,
    size: Dp = 64.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    contentDescription: String = "Store Logo",
    modifier: Modifier = Modifier
) {
    StoreAvatar(
        logoUri = storeProfile.logoUri,
        iconKey = storeProfile.storeIconKey,
        bgColorLong = storeProfile.storeIconBgColor,
        size = size,
        shape = shape,
        contentDescription = contentDescription,
        modifier = modifier
    )
}
