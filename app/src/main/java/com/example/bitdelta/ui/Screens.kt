package com.example.bitdelta.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitdelta.ui.theme.*
import com.example.bitdelta.dataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HomeScreen(steps: Int, level: Int, xpProgress: Float, isAvailable: Boolean, currentActivity: String, sessionSteps: Int) {
    val context = LocalContext.current
    val goal by context.dataStore.data.map { it[intPreferencesKey("step_goal")] ?: 10000 }.collectAsState(10000)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        LevelHeader(level, xpProgress)
        Spacer(Modifier.height(16.dp))
        
        // Статус активности
        GlassCard {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(8.dp).background(if(currentActivity != "Покой") Color.Green else Color.Gray, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(currentActivity.uppercase(), color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(Modifier.weight(0.5f))
        if (isAvailable) {
            KineticCore(steps)
        } else {
            SensorWarning()
        }
        
        if (currentActivity != "Покой") {
            Spacer(Modifier.height(16.dp))
            Text("СЕССИЯ: $sessionSteps", color = SkyBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(Modifier.weight(1f))
        GlassCard(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem("Дистанция", String.format(Locale.getDefault(), "%.2f км", steps * 0.0007))
                StatItem("Цель", "${goal / 1000}к")
                StatItem("Ранг", getRank(level))
            }
        }
    }
}

fun getRank(level: Int): String {
    return when {
        level < 5 -> "Новичок"
        level < 10 -> "Атлет"
        level < 25 -> "Мастер"
        else -> "Элита"
    }
}

@Composable
fun AchievementsScreen(totalSteps: Int) {
    val awards = remember(totalSteps) {
        listOf(
            Award("Контакт", "Система активна", totalSteps > 0),
            Award("Разминка", "5,000 шагов", totalSteps >= 5000),
            Award("Атлет", "Уровень 10 достигнут", totalSteps >= 20000),
            Award("Легенда", "Пройдено 100,000 шагов", totalSteps >= 100000)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("ДОСТИЖЕНИЯ", color = SkyBlue, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(awards) { award ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (award.isUnlocked) Icons.Rounded.WorkspacePremium else Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = if (award.isUnlocked) Color.Yellow else Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(award.title, color = TextWhite, fontWeight = FontWeight.Bold)
                            Text(award.desc, color = TextWhite.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

data class Award(val title: String, val desc: String, val isUnlocked: Boolean)

@Composable
fun LevelHeader(level: Int, progress: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("УРОВЕНЬ $level", color = SkyBlue, fontWeight = FontWeight.Bold)
            Text("КИБЕР-АТЛЕТ", color = TextWhite.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = SkyBlue,
            trackColor = NavyBlue,
        )
    }
}

@Composable
fun KineticCore(steps: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "core")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, 
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing), 
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(contentAlignment = Alignment.Center, modifier = Modifier.scale(pulseScale)) {
        Box(modifier = Modifier.size(280.dp).background(Brush.radialGradient(listOf(SkyBlue.copy(alpha = 0.15f), Color.Transparent)), CircleShape))
        Box(
            modifier = Modifier.size(240.dp).clip(CircleShape)
                .background(GlassWhite)
                .border(2.dp, Brush.linearGradient(listOf(SkyBlue, Color.Transparent)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { (steps % 10000) / 10000f },
                modifier = Modifier.size(210.dp),
                color = SkyBlue,
                strokeWidth = 10.dp,
                trackColor = Color.White.copy(alpha = 0.05f),
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(steps.toString(), fontSize = 56.sp, fontWeight = FontWeight.Black, color = TextWhite)
                Text("BIT DELTA", fontSize = 10.sp, color = SkyBlue, letterSpacing = 3.sp)
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val goalKey = intPreferencesKey("step_goal")
    val goal by context.dataStore.data.map { it[goalKey] ?: 10000 }.collectAsState(10000)

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("НАСТРОЙКИ", color = SkyBlue, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(40.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Дневная цель: $goal", color = TextWhite)
                Slider(
                    value = goal.toFloat(),
                    onValueChange = { newValue ->
                        scope.launch { context.dataStore.edit { it[goalKey] = newValue.toInt() } }
                    },
                    valueRange = 1000f..30000f,
                    colors = SliderDefaults.colors(thumbColor = SkyBlue, activeTrackColor = SkyBlue)
                )
            }
        }
    }
}
