package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.util.LakuStrings
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    lang: String = "id",
    onContinue: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2200)
        onContinue()
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8F5E9), // Mint green
            Color(0xFFC8E6C9), // Fresh light green
            Color(0xFFFFFFFF), // Crisp pure white
            Color(0xFFFFFFFF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Card(
                modifier = Modifier
                    .size(175.dp)
                    .shadow(16.dp, shape = RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_laku_kasir),
                        contentDescription = LakuStrings.get("app_title", lang),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = LakuStrings.get("app_name_short", lang),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldGreenDark,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = LakuStrings.get("by_rafline", lang),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF334155),
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = CircleShape,
                color = EmeraldGreenPrimary.copy(alpha = 0.12f),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = LakuStrings.get("version", lang),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreenDark,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("splash_continue_button")
            ) {
                Text(
                    text = LakuStrings.get("open_cashier", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = LakuStrings.get("open_cashier", lang),
                    tint = Color.White
                )
            }
        }
    }
}
