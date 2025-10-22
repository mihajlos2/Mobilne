package com.example.myapplication.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Job
import com.example.myapplication.models.Master

@Composable
fun MarkerTableDialog(
    masters: List<Master>,
    jobs: List<Job>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📍 Lista svih markera")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Zatvori")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Majstori:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (masters.isEmpty()) {
                    Text("Nema majstora.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(masters) { master ->
                            MarkerRow(
                                title = master.name ?: "Nepoznato ime",
                                subtitle = "Prof: ${master.profession ?: "Nepoznato"}",
                                coords = "(${master.location.latitude}, ${master.location.longitude})"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()

                Text("Poslovi:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (jobs.isEmpty()) {
                    Text("Nema aktivnih poslova.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(jobs) { job ->
                            MarkerRow(
                                title = job.title ?: "Bez naslova",
                                subtitle = "Profesija: ${job.profession ?: "Nepoznato"}",
                                coords = "(${job.location.latitude}, ${job.location.longitude})"
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun MarkerRow(title: String, subtitle: String, coords: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, style = MaterialTheme.typography.bodySmall)
        Text(coords, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Divider(modifier = Modifier.padding(top = 4.dp))
    }
}
