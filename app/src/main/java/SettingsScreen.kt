package com.kokila.jalsanchay

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun SettingsScreen() {

    val context = LocalContext.current
    var roofArea by remember {
        mutableStateOf("1000")
    }

    var tankCapacity by remember {
        mutableStateOf("5000")
    }

    var runoffCoefficient by remember {
        mutableStateOf("0.8")
    }

    var dailyUsage by remember {
        mutableStateOf("150")
    }

    val sharedPreferences =
        context.getSharedPreferences(
            "app_settings",
            Context.MODE_PRIVATE
        )

    var darkMode by remember {

        mutableStateOf(
            sharedPreferences.getBoolean(
                "dark_mode",
                false
            )
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color.White
                    )
                )
            )
            .padding(16.dp)
    ) {

        Text(
            text = "⚙ Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text("System Configuration")

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = roofArea,
                    onValueChange = {
                        roofArea = it
                    },
                    label = {
                        Text("Roof Area")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tankCapacity,
                    onValueChange = {
                        tankCapacity = it
                    },
                    label = {
                        Text("Tank Capacity")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = runoffCoefficient,
                    onValueChange = {
                        runoffCoefficient = it
                    },
                    label = {
                        Text("Runoff Coefficient")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dailyUsage,
                    onValueChange = {
                        dailyUsage = it
                    },
                    label = {
                        Text("Daily Usage")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {

                        if (
                            roofArea.isBlank() ||
                            tankCapacity.isBlank() ||
                            runoffCoefficient.isBlank() ||
                            dailyUsage.isBlank()
                        ) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        val data = hashMapOf(
                            "roofArea" to (roofArea.toIntOrNull() ?: 0),
                            "tankCapacity" to (tankCapacity.toIntOrNull() ?: 0),
                            "runoffCoefficient" to (runoffCoefficient.toFloatOrNull() ?: 0f),
                            "dailyUsage" to (dailyUsage.toIntOrNull() ?: 0)
                        )

                        val db = FirebaseFirestore.getInstance()

                        db.collection("app_configuration")
                            .document("settings")
                            .set(data)
                            .addOnSuccessListener {

                                Toast.makeText(context, "Saved Successfully", Toast.LENGTH_SHORT).show()

                                sharedPreferences.edit().apply {
                                    putString("roofArea", roofArea)
                                    putString("tankCapacity", tankCapacity)
                                    putString("runoffCoefficient", runoffCoefficient)
                                    putString("dailyUsage", dailyUsage)
                                    apply()
                                }
                            }
                            .addOnFailureListener { e ->

                                Toast.makeText(
                                    context,
                                    "Error: ${e.message ?: "Unknown error"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Save Configuration")
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {

                        roofArea = ""
                        tankCapacity = ""
                        runoffCoefficient = ""
                        dailyUsage = ""

                        Toast.makeText(
                            context,
                            "Fields Reset",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Reset Configuration")
                }
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),

                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text("Dark Mode")

                        Switch(
                            checked = darkMode,

                            onCheckedChange = {

                                darkMode = it

                                ThemeState.isDarkMode.value = it

                                sharedPreferences.edit()
                                    .putBoolean(
                                        "dark_mode",
                                        it
                                    )
                                    .apply()
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text("📊 Current Configuration")

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Roof Area: $roofArea sq.ft")

                    Text("Tank Capacity: $tankCapacity L")

                    Text("Runoff Coefficient: $runoffCoefficient")

                    Text("Daily Usage: $dailyUsage L/day")
                }
            }
        }


    }
}