package com.example.myapplication.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Master
import com.example.myapplication.models.MasterJobRepository
import kotlinx.coroutines.launch

@Composable
fun MasterRankingDialog(
    masters: List<Master>,
    masterJobRepository: MasterJobRepository,
    onDismiss: () -> Unit,
    onUpdateMasters: (List<Master>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var localMasters by remember { mutableStateOf(masters) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lista majstora i ocene") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(localMasters) { master ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = "${master.name} - ${master.profession}")
                        Text(
                            text = "Ocena: ${"%.1f".format(master.rating)} (${master.reviewCount} recenzija)",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row {
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = if (star <= master.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Oceni majstora",
                                    tint = Color.Yellow,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable {
                                            coroutineScope.launch {
                                                // Update rating
                                                masterJobRepository.updateMasterRating(
                                                    masterId = master.id,
                                                    newRating = star.toDouble(),
                                                    newReviewCount = master.reviewCount + 1
                                                )
                                                // Update lokalni master
                                                val updatedMaster = master.copy(
                                                    rating = star.toDouble(),
                                                    reviewCount = master.reviewCount + 1
                                                )
                                                localMasters = localMasters.map {
                                                    if (it.id == updatedMaster.id) updatedMaster else it
                                                }
                                                // Update spoljnu listu
                                                onUpdateMasters(localMasters)
                                            }
                                        }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Zatvori")
            }
        }
    )
}
