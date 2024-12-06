package com.example.notification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.notification.ui.theme.NotificationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            println("Notification permission granted")
        } else {
            println("Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    println("Notification permission already granted")
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        setContent {
            NotificationTheme {
                ReminderScreen(
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ReminderScreen(
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    var reminderText by remember { mutableStateOf("") }
    var reminderTime by remember { mutableFloatStateOf(1f) }
    var isReminderSet by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = reminderText,
                onValueChange = { reminderText = it },
                label = { Text("Введите текст напоминания") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Время напоминания: ${reminderTime.toInt()} минут")

            Slider(
                value = reminderTime,
                onValueChange = { reminderTime = it },
                valueRange = 1f..60f,
                steps = 59
            )

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        when (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        )) {
                            PackageManager.PERMISSION_GRANTED -> {
                                isReminderSet = true
                            }
                            else -> {
                                showPermissionDialog = true
                            }
                        }
                    } else {
                        isReminderSet = true
                    }
                },
                enabled = reminderText.isNotBlank()
            ) {
                Text("Установить напоминание")
            }

            if (showPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDialog = false },
                    title = { Text("Требуется разрешение") },
                    text = { Text("Для отправки уведомлений необходимо предоставить разрешение") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onRequestPermission()
                                showPermissionDialog = false
                            }
                        ) {
                            Text("Предоставить")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showPermissionDialog = false }
                        ) {
                            Text("Отмена")
                        }
                    }
                )
            }

            LaunchedEffect(isReminderSet) {
                if (isReminderSet) {
                    delay(reminderTime.toLong() * 60 * 1000)
                    notificationHelper.showNotification(reminderText)
                    isReminderSet = false
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotificationTheme {
        Greeting("Android")
    }
}