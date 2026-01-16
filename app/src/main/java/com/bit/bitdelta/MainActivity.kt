package com.bit.bitdelta

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HomeMax
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bit.bitdelta.ui.*
import com.bit.bitdelta.ui.theme.*

val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    
    private var _stepsCount by mutableFloatStateOf(0f)
    private var initialSteps = -1f
    private var isSensorAvailable by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        
        if (stepCounterSensor == null && stepDetectorSensor == null) {
            isSensorAvailable = false
        }

        setContent {
            BitDeltaTheme {
                MainNavigation(
                    steps = _stepsCount.toInt(),
                    isSensorAvailable = isSensorAvailable
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Используем FASTEST для живого отклика и NORMAL для стабильности в фоне
        stepCounterSensor?.let { 
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) 
        }
        stepDetectorSensor?.let { 
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) 
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val sensorValue = event.values[0]
                if (initialSteps == -1f) {
                    // Ключевое исправление: калибруем базу с учетом уже сделанных шагов через детектор
                    initialSteps = sensorValue - _stepsCount
                }
                
                val calculatedSteps = sensorValue - initialSteps
                if (calculatedSteps > _stepsCount) {
                    _stepsCount = calculatedSteps
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                // Детектор срабатывает мгновенно. 
                // Он помогает не ждать "пачки" от основного счетчика.
                _stepsCount += 1f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    data object Home : Screen("home", Icons.Default.HomeMax, "Главная")
    data object Achievements : Screen("achievements", Icons.Default.WorkspacePremium, "Награды")
    data object Settings : Screen("settings", Icons.Default.Settings, "Опции")
}

@Composable
fun MainNavigation(steps: Int, isSensorAvailable: Boolean) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600

    var hasPermission by remember { 
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasPermission) {
            launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }
    
    val xpPerLevel = 3000
    val userLevel = (steps / xpPerLevel) + 1
    val xpProgress = (steps % xpPerLevel).toFloat() / xpPerLevel

    Row(modifier = Modifier.fillMaxSize().background(DeepBlue)) {
        if (isTablet) NavigationSideBar(navController)
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { if (!isTablet) BottomNavigationBar(navController) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                LiquidBackground()
                NavHost(navController, Screen.Home.route) {
                    composable(Screen.Home.route) { 
                        HomeScreen(
                            steps = steps, 
                            level = userLevel, 
                            xpProgress = xpProgress, 
                            isAvailable = isSensorAvailable,
                            hasPermission = hasPermission,
                            onGrantPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                                }
                            }
                        ) 
                    }
                    composable(Screen.Achievements.route) { AchievementsScreen(steps) }
                    composable(Screen.Settings.route) { SettingsScreen() }
                }
            }
        }
    }
}
