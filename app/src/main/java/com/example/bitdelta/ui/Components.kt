package com.example.bitdelta.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitdelta.ui.theme.BitDeltaTheme

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(32.dp))
            .background(BitDeltaTheme.glassColor)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
    ) {
        content()
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = BitDeltaTheme.textColor)
        Text(label.uppercase(), fontSize = 10.sp, color = BitDeltaTheme.accentColor.copy(alpha = 0.7f))
    }
}

@Composable
fun LiquidBackground() {
    val infinite = rememberInfiniteTransition(label = "liquid")
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing)
        ),
        label = "angle"
    )
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            Modifier
                .size(450.dp)
                .offset((-150).dp, 100.dp)
                .graphicsLayer(rotationZ = angle)
                .background(Brush.radialGradient(listOf(BitDeltaTheme.accentColor.copy(alpha = 0.12f), Color.Transparent)), CircleShape)
                .blur(90.dp)
        )
        Box(
            Modifier
                .size(350.dp)
                .align(Alignment.BottomEnd)
                .offset(100.dp, 50.dp)
                .graphicsLayer(rotationZ = -angle)
                .background(Brush.radialGradient(listOf(BitDeltaTheme.accentColor.copy(alpha = 0.08f), Color.Transparent)), CircleShape)
                .blur(70.dp)
        )
    }
}

@Composable
fun SensorWarning() {
    Box(
        Modifier
            .size(260.dp)
            .clip(CircleShape)
            .background(BitDeltaTheme.glassColor)
            .border(2.dp, Color.Red.copy(alpha = 0.3f), CircleShape)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text("Датчик не найден", color = BitDeltaTheme.textColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
