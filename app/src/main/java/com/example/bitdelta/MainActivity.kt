package com.example.bitdelta

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import com.example.bitdelta.ui.*
import com.example.bitdelta.ui.theme.*
import kotlin.math.sqrt

val Context.dataStore by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    
    private var _stepsCount by mutableFloatStateOf(0f)
    private var initialSteps = -1f
    private var isSensorAvailable by mutableStateOf(true)

    // Параметры для распознавания типа активности
    private var lastMagnitude = 0f
    private var _currentActivity by mutableStateOf("Покой")
    private var _workoutSteps by mutableIntStateOf(0)
    private var isWorkoutActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        if (stepCounterSensor == null) {
            isSensorAvailable = false
        }

        setContent {
            BitDeltaTheme {
                MainNavigation(
                    steps = _stepsCount.toInt(),
                    isSensorAvailable = isSensorAvailable,
                    currentActivity = _currentActivity,
                    workoutSteps = _workoutSteps
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stepCounterSensor?.let { 
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) 
        }
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (initialSteps == -1f) initialSteps = event.values[0]
            val totalSteps = event.values[0] - initialSteps
            
            if (isWorkoutActive) {
                _workoutSteps += (totalSteps - _stepsCount).toInt()
            }
            _stepsCount = totalSteps
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = sqrt(x * x + y * y + z * z)
            
            // Простейшее распознавание: покой, ходьба, бег
            val diff = Math.abs(magnitude - lastMagnitude)
            _currentActivity = when {
                diff < 0.5f -> "Покой"
                diff < 5.0f -> {
                    isWorkoutActive = true
                    "Ходьба"
                }
                else -> {
                    isWorkoutActive = true
                    "Бег"
                }
            }
            lastMagnitude = magnitude
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    data object Home : Screen("home", Icons.Default.Home, "Главная")
    data object Training : Screen("training", Icons.Default.FitnessCenter, "Тренинг")
    data object Achievements : Screen("achievements", Icons.Default.EmojiEvents, "Награды")
    data object Settings : Screen("settings", Icons.Default.Settings, "Опции")
}

@Composable
fun MainNavigation(steps: Int, isSensorAvailable: Boolean, currentActivity: String, workoutSteps: Int) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }
    
    // Система уровней: каждые 5000 шагов - новый уровень
    val xpPerLevel = 5000
    val userLevel = (steps / xpPerLevel) + 1
    val currentXPProgress = (steps % xpPerLevel).toFloat() / xpPerLevel

    Row(modifier = Modifier.fillMaxSize().background(DeepBlue)) {
        if (isTablet) {
            NavigationSideBar(navController)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { if (!isTablet) BottomNavigationBar(navController) },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                LiquidBackground()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(Screen.Home.route) { 
                        HomeScreen(steps, userLevel, currentXPProgress, isSensorAvailable, currentActivity) 
                    }
                    composable(Screen.Training.route) { TrainingScreen(currentActivity, workoutSteps) }
                    composable(Screen.Achievements.route) { AchievementsScreen(steps) }
                    composable(Screen.Settings.route) { SettingsScreen() }
                }
            }
        }
    }
}
