package com.example.myapplication.Dialog

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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MasterRankingDialog(
    masterJobRepository: MasterJobRepository,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var masters by remember { mutableStateOf<List<Master>>(emptyList()) }

    // Listener za automatsko osvežavanje kad Firestore promeni podatke
    LaunchedEffect(Unit) {
        masterJobRepository.listenToMasters { updatedMasters ->
            masters = updatedMasters.sortedByDescending { it.rating } // sortirano po ratingu
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lista majstora i ocene") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(masters) { master ->
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
                                                val userId = masterJobRepository.getCurrentUserId()
                                                val alreadyRated = masterJobRepository.hasUserRatedMaster(master.id, userId)

                                                if (alreadyRated) {
                                                    return@launch
                                                }

                                                val newReviewCount = master.reviewCount + 1
                                                val newRating = ((master.rating * master.reviewCount) + star) / newReviewCount

                                                masterJobRepository.updateMasterRating(
                                                    masterId = master.id,
                                                    newRating = newRating,
                                                    newReviewCount = newReviewCount
                                                )

                                                masterJobRepository.saveUserRating(master.id, userId, star)
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
