package com.kokila.jalsanchay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

data class HarvestEntry(
    val water: Float,
    val date: String
)

@Composable
fun DashboardScreen(
    onLogout: () -> Unit
){

    var totalWater by remember {
        mutableIntStateOf(0)
    }
    var todayWater by remember {
        mutableIntStateOf(0)
    }

    var roofArea by remember {
        mutableIntStateOf(1000)
    }

    var tankCapacity by remember {
        mutableIntStateOf(5000)
    }

    var runoffCoefficient by remember {
        mutableFloatStateOf(0.8f)
    }

    var dailyUsage by remember {
        mutableIntStateOf(150)
    }

    var tankPercentage by remember {
        mutableFloatStateOf(0f)
    }

    var latestRainfall by remember {
        mutableIntStateOf(0)
    }

    var rainfallHistory by remember {
        mutableStateOf(listOf<HarvestEntry>())
    }

    var smartAlert by remember {
        mutableStateOf(
            "Checking weather conditions..."
        )
    }

    var rainfallInput by remember {
        mutableStateOf("")
    }

    var inputError by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current

    val infiniteTransition =
        rememberInfiniteTransition(
            label = ""
        )

    val waveShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    var showProfileMenu by remember {
        mutableStateOf(false)
    }
    var userName by remember { mutableStateOf("User") }
    var userEmail by remember { mutableStateOf("") }


    val statusMessage =
        when {

            tankPercentage < 0.3f ->
                "Low Water Storage"

            tankPercentage < 0.7f ->
                "Moderate Storage"

            tankPercentage < 0.9f ->
                "High Water Storage"

            else ->
                "Tank Full"
        }

    val tankCondition =
        when {

            tankPercentage > 0.9f ->
                "Excellent"

            tankPercentage > 0.7f ->
                "Good"

            tankPercentage >= 0.3f ->
                "Moderate"

            else ->
                "Low Water Level"
        }

    val overflowRisk =
        when {

            tankPercentage > 0.95f ->
                "High"

            tankPercentage > 0.8f ->
                "Moderate"

            else ->
                "Low"
        }
    val filterStatus =
        when {

            latestRainfall > 50 ->
                "Needs Cleaning"

            latestRainfall > 20 ->
                "Monitor Filter"

            else ->
                "Clean"
        }
    val avgDailyUsage =
        if (dailyUsage > 0)
            dailyUsage.toFloat()
        else
            0f

    val co2Reduction =
        (todayWater / 1000f) * 2f

    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection("app_configuration")
            .document("settings")
            .get()

            .addOnSuccessListener { document ->

                if (document.exists()) {

                    roofArea =
                        document.getLong("roofArea")
                            ?.toInt() ?: 1000

                    tankCapacity =
                        document.getLong("tankCapacity")
                            ?.toInt() ?: 5000

                    runoffCoefficient =
                        document.getDouble("runoffCoefficient")
                            ?.toFloat() ?: 0.8f

                    dailyUsage =
                        document.getLong("dailyUsage")
                            ?.toInt() ?: 150
                }
            }

        FirebaseFirestore
            .getInstance()
            .collection("rainfall_logs")
            .orderBy("timestamp")
            .addSnapshotListener { value, error ->

                if (value != null && !value.isEmpty) {

                    // TOTAL WATER
                    val totalSavedWater = value.documents.sumOf { doc ->
                        doc.getLong("savedWater")?.toInt() ?: 0
                    }

                    totalWater = totalSavedWater


                    // TODAY WATER
                    val calendar = java.util.Calendar.getInstance()

                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendar.set(java.util.Calendar.MINUTE, 0)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)

                    val startOfDay = calendar.timeInMillis

                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                    val endOfDay = calendar.timeInMillis

                    todayWater = value.documents
                        .filter { doc ->
                            val ts = doc.getLong("timestamp") ?:  return@filter false
                                    ts in startOfDay until endOfDay
                        }
                        .sumOf { doc ->
                            doc.getLong("savedWater")?.toInt() ?: 0
                        }

                    // TANK %
                    val latestWater = value.documents.lastOrNull()
                        ?.getLong("savedWater")
                        ?.toFloat() ?: 0f

                    tankPercentage =
                        (latestWater / tankCapacity.toFloat())
                            .coerceIn(0f, 1f)


                    smartAlert =
                        when {

                            latestRainfall >= 50 -> {

                                NotificationHelper.showNotification(
                                    context,
                                    "Heavy Rain Alert",
                                    "Prepare overflow storage tanks."
                                )

                                "⚠ Heavy rainfall detected. Prepare overflow storage tanks."
                            }

                            latestRainfall >= 20 -> {

                                NotificationHelper.showNotification(
                                    context,
                                    "Rainfall Alert",
                                    "Good time to harvest rainwater."
                                )

                                "☔ Moderate rainfall expected. Good time to harvest water."
                            }

                            tankPercentage > 0.9f -> {

                                NotificationHelper.showNotification(
                                    context,
                                    "Tank Full Alert",
                                    "Tank almost full."
                                )

                                "🚨 Tank almost full. Use water efficiently."
                            }

                            tankPercentage < 0.2f -> {

                                NotificationHelper.showNotification(
                                    context,
                                    "Low Water Alert",
                                    "Water level low."
                                )

                                "⚠ Water level low. Conserve water carefully."
                            }

                            else -> {

                                "✅ Weather conditions normal for harvesting."
                            }
                        }

                    val historyList =
                        mutableListOf<HarvestEntry>()

                    for (doc in value.documents) {

                        val saved =
                            doc.getLong("savedWater")
                                ?.toFloat() ?: 0f

                        val timestamp =
                            doc.getLong("timestamp") ?: 0L

                        val date =
                            SimpleDateFormat(
                                "dd MMM",
                                Locale.getDefault()
                            ).format(Date(timestamp))

                        historyList.add(
                            HarvestEntry(
                                water = saved,
                                date = date
                            )
                        )
                    }

                    rainfallHistory = historyList
                }
                val currentUser = FirebaseAuth.getInstance().currentUser

                userName =
                    currentUser?.displayName
                        ?: currentUser?.email?.substringBefore("@")
                                ?: "User"

                userEmail = currentUser?.email ?: "No Email"
            }



    }
    val currentUser = FirebaseAuth.getInstance().currentUser

    userName =
        currentUser?.displayName
            ?: currentUser?.email?.substringBefore("@")
                    ?: "User"

    userEmail = currentUser?.email ?: "No Email"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color.White
                    )
                )
            )
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp)
    ) {
        Column {

            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 14.dp
                    ),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                // LOGO + TITLE
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.WaterDrop,

                        contentDescription = null,

                        tint = Color(0xFF2563EB)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        text = "Jal-Sanchay",

                        fontWeight = FontWeight.Bold,

                        fontSize = 22.sp,

                        color = Color(0xFF1E3A8A)
                    )
                }

                // PROFILE BUTTON
                Box {

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Color(0xFF6D28D9)
                            )
                            .clickable {

                                showProfileMenu = true
                            },

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                userName.first().uppercase(),

                            color = Color.White,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }

                    DropdownMenu(
                        expanded = showProfileMenu,

                        onDismissRequest = {

                            showProfileMenu = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = {

                                Column {

                                    Text(
                                        text = userName,
                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Text(
                                        text = userEmail,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            },

                            onClick = { }
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {

                                Text(
                                    text = "Logout",
                                    color = Color.Red
                                )
                            },

                            onClick = {

                                showProfileMenu = false

                                FirebaseAuth
                                    .getInstance()
                                    .signOut()

                                onLogout()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DASHBOARD TITLE
            Column(
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {

                Text(
                    text = "Water Monitoring Dashboard",

                    style =
                        MaterialTheme.typography.headlineMedium,

                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                Text(
                    text =
                        "Track and optimize your rainwater harvesting",

                    color = Color.Gray
                )
            }
        }



        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text("🏠 Estimated Usage Days")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text =
                            if (avgDailyUsage > 0)
                                "${(totalWater / avgDailyUsage).toInt()} Days"
                            else
                                "0 Days",

                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Blue
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text("💧 Total Water")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$totalWater L",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Blue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "➕ New Rainfall Entry",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = rainfallInput,

                    onValueChange = {

                        rainfallInput = it
                        inputError = ""
                    },

                    label = {
                        Text("Rainfall Amount (mm)")
                    },

                    modifier = Modifier.fillMaxWidth(),

                    isError = inputError.isNotEmpty()
                )

                if (inputError.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = inputError,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {

                        val rainfall =
                            rainfallInput.toIntOrNull()

                        when {

                            rainfallInput.isBlank() -> {

                                inputError =
                                    "Please enter rainfall amount"

                                return@Button
                            }

                            rainfall == null -> {

                                inputError =
                                    "Only numeric values allowed"

                                return@Button
                            }

                            rainfall <= 0 -> {

                                inputError =
                                    "Rainfall must be greater than 0"

                                return@Button
                            }
                        }

                        val savedWater =
                            (
                                    roofArea *
                                            rainfall *
                                            0.0929 *
                                            runoffCoefficient
                                    ).toInt()
                                .coerceAtMost(tankCapacity)

                        val data = hashMapOf(

                            "rainfall" to rainfall,

                            "savedWater" to savedWater,

                            "timestamp" to System.currentTimeMillis()
                        )

                        FirebaseFirestore
                            .getInstance()
                            .collection("rainfall_logs")
                            .add(data)

                        rainfallInput = ""
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Record Rainfall")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


        val animatedTank by animateFloatAsState(
            targetValue = tankPercentage,
            label = "tankAnimation"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(220.dp)
                        .background(
                            Color.LightGray,
                            RoundedCornerShape(20.dp)
                        ),

                    contentAlignment =
                        Alignment.BottomCenter
                ) {

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .width(140.dp)
                            .height(220.dp)
                    ) {
                        val fillHeight = size.height * (1f - animatedTank)
                        if (animatedTank > 0f) {

                            val waveHeight = 20f

                            val path = Path()

                            path.moveTo(0f, fillHeight)

                            for (x in 0..size.width.toInt()) {

                                val y =
                                    waveHeight *
                                            sin(
                                                ((x + waveShift) * 0.02)
                                            ).toFloat()

                                path.lineTo(x.toFloat(), y + fillHeight)
                            }

                            path.lineTo(
                                size.width,
                                size.height
                            )

                            path.lineTo(
                                0f,
                                size.height
                            )

                            path.close()

                            drawPath(
                                path = path,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }

                    Text(
                        text =
                            "${(animatedTank * 100).toInt()}%",

                        color = Color.White,

                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text =
                        when {
                            animatedTank <= 0f ->
                                "Tank Empty"

                            animatedTank >= 1f ->
                                "Tank Full"

                            else ->
                                "Tank: ${(animatedTank * 100).toInt()}% Filled"
                        },

                    style =
                        MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = { animatedTank },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = statusMessage,
                    color = Color.Blue
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // LITERS SAVED TODAY CARD
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "💧 Saved Today",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Today Water: $todayWater L",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF2563EB)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Today Water Saved",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // TOTAL SAVINGS CARD
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    val costPer1000L = 50f

                    val moneySaved =
                        (totalWater / 1000f) * costPer1000L

                    Text(
                        text = "💰 Total Savings",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "₹${moneySaved.toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF16A34A)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Estimated Savings",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                FirebaseFirestore.getInstance()
                    .collection("rainfall_logs")
                    .get()
                    .addOnSuccessListener { result ->

                        var monthlyWater = 0
                        var totalEntries = 0
                        var totalRainfall = 0

                        for (doc in result.documents) {

                            val rainfall =
                                doc.getLong("rainfall")
                                    ?.toInt() ?: 0

                            val savedWater =
                                doc.getLong("savedWater")
                                    ?.toInt() ?: 0

                            totalRainfall += rainfall
                            monthlyWater += savedWater
                            totalEntries++
                        }

                        PdfGenerator.createMonthlyPdf(
                            context = context,
                            totalWater = monthlyWater,
                            totalRainfall = totalRainfall,
                            totalEntries = totalEntries
                        )
                    }
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("📄 Generate Monthly Report")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "📈 Harvesting History",
                    style =
                        MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (rainfallHistory.isEmpty()) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "🌧",
                            fontSize = 50.sp
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(
                            text =
                                "No rainfall records yet",

                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text =
                                "Start tracking rainwater harvesting to view analytics and reports.",

                            color = Color.Gray
                        )
                    }

                } else {

                    AnalyticsChart(
                        data = rainfallHistory
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "💡 Smart Recommendation",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = smartAlert,
                    color = Color.Blue
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "🌱 Environmental Impact",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text =
                        "Water Conserved: $totalWater L"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text =
                        "Estimated CO₂ Reduction: $co2Reduction kg"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "🛠 Tank Health Status",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text =
                        "Tank Condition: $tankCondition"
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text =
                        "Overflow Risk: $overflowRisk"
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text =
                        "Filter Status: $filterStatus"
                )

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = { animatedTank },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}