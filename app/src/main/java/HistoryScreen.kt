package com.kokila.jalsanchay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedTextField

data class RainHistory(
    val id: String = "",
    val rainfall: String = "",
    val savedWater: String = "",
    val timestamp: Long = 0L
)

@Composable
fun HistoryScreen() {

    var historyList by remember {
        mutableStateOf(listOf<RainHistory>())
    }
    var searchText by remember {
        mutableStateOf("")
    }
    val filteredList =
        historyList.filter {

            it.rainfall.contains(searchText, true) ||

                    it.savedWater.contains(searchText, true) ||

                    SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.US
                    ).format(Date(it.timestamp))
                        .contains(searchText, true)
        }

    LaunchedEffect(Unit) {

        FirebaseFirestore.getInstance()
            .collection("rainfall_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { result, error ->

                if (result == null) return@addSnapshotListener

                val tempList = mutableListOf<RainHistory>()

                for (doc in result.documents) {

                    val rainfall =
                        doc.get("rainfall").toString()
                    val savedWater =
                        doc.get("savedWater").toString()
                    val timestamp =
                        doc.getLong("timestamp") ?: 0L

                    tempList.add(
                        RainHistory(
                            id = doc.id,
                            rainfall = rainfall,
                            savedWater = savedWater,
                            timestamp = timestamp
                        )
                    )
                }

                historyList = tempList
            }
    }

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
            .padding(16.dp)
    ) {

        Text(
            text = "📜 Rainfall History",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText,

            onValueChange = {
                searchText = it
            },

            label = {
                Text("Search History")
            },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (historyList.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "🌧",
                    fontSize = 70.sp
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "No Rainfall Records Yet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text =
                        "Add rainfall entries to view harvesting history.",
                    color = Color.Gray
                )
            }

        } else {
            LazyColumn {

                items(filteredList) { item ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),

                        elevation =
                            CardDefaults.cardElevation(
                                defaultElevation = 8.dp
                            )

                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Text(
                                text = "🌧 ${item.rainfall} mm Rainfall",
                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    "💧 Water Harvested: ${item.savedWater} L"
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            val formattedDate =
                                SimpleDateFormat(
                                    "dd MMM yyyy, hh:mm a",
                                    Locale.US
                                ).format(Date(item.timestamp))

                            Text(
                                text = "🕒 $formattedDate"
                            )
                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {

                                IconButton(
                                    onClick = {

                                        FirebaseFirestore.getInstance()
                                            .collection("rainfall_logs")
                                            .document(item.id)
                                            .delete()
                                    }
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}