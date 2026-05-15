package com.kokila.jalsanchay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun TipsScreen() {

    LazyColumn(
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

        item {

            Text(
                text = "💡 Harvesting Wisdom",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            TipCard(
                title = "Clean your gutters",
                description =
                    "Regularly remove leaves and debris from your gutters to ensure maximum water flow and quality."
            )

            TipCard(
                title = "Install First Flush Diverter",
                description =
                    "Prevents dirty first rainwater from entering the tank."
            )

            TipCard(
                title = "Mulch your garden",
                description =
                    "Retains soil moisture and reduces water usage."
            )

            TipCard(
                title = "Check for leaks",
                description =
                    "Inspect pipes and tank fittings regularly to avoid water loss."
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                        elevation =
                        CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                        )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "💧 Pro Tip: Roof Pitch",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text =
                            "Steeper roofs collect rainwater faster and improve drainage efficiency."
                    )
                }
            }
        }
    }
}

@Composable
fun TipCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color.White
                    )
                )
            )
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),

                elevation =
                CardDefaults.cardElevation(
                defaultElevation = 8.dp
                )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = description)
        }
    }
}