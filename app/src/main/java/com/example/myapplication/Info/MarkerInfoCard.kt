package com.example.myapplication.Info

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MarkerInfoCard(
    markerData: Map<String, Any>,
    currentUserId: String?,
    onDelete: () -> Unit
) {
    val type = markerData["type"] as? String
    val createdBy = markerData["createdBy"] as? String
    val createdAt = markerData["createdAt"]?.toString()?.take(19) ?: "Nepoznat datum"

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (type) {
                "master" -> {
                    Text(text = "Majstor: ${markerData["name"]}", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Profesija: ${markerData["profession"]}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Telefon: ${markerData["phone"]}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Ocena: ⭐${markerData["rating"]}", style = MaterialTheme.typography.bodyMedium)
                }
                "job" -> {
                    Text(text = "Posao: ${markerData["profession"]}", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Opis: ${markerData["description"]}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Kontakt: ${markerData["phone"]}", style = MaterialTheme.typography.bodyMedium)
                }
                else -> {
                    Text(text = "Nepoznato", style = MaterialTheme.typography.titleLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Kreirano: $createdAt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (createdBy == currentUserId) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Obriši")
                    }
                }
            }
        }
    }
}
