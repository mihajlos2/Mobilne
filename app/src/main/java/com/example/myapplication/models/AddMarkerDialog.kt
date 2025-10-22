package com.example.myapplication.models

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddMarkerDialog(
    onDismiss: () -> Unit,
    onAddMaster: (Map<String, String>) -> Unit,
    onAddJob: (Map<String, String>) -> Unit
) {
    var selectedType by remember { mutableStateOf("master") }
    var profession by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj novi marker") },
        text = {
            Column {
                // TIP MARKERA
                Text("Izaberi tip:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // MAJSTOR
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(4.dp)
                ) {
                    RadioButton(
                        selected = selectedType == "master",
                        onClick = { selectedType = "master" }
                    )
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Majstor")
                }

                // POSAO
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(4.dp)
                ) {
                    RadioButton(
                        selected = selectedType == "job",
                        onClick = { selectedType = "job" }
                    )
                    Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Posao")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FORMUAR ZA MAJSTORA
                if (selectedType == "master") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = profession,
                        onValueChange = { profession = it },
                        label = { Text("Profesija") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Opis usluga") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // FORMUAR ZA POSAO
                else {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = profession,
                        onValueChange = { profession = it },
                        label = { Text("Potreban majstor") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Opis posla") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedType == "master") {
                        onAddMaster(mapOf(
                            "profession" to profession,
                            "description" to description
                        ))
                    } else {
                        onAddJob(mapOf(
                            "profession" to profession,
                            "description" to description
                        ))
                    }
                },
                enabled = description.isNotBlank() && profession.isNotBlank()
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Otkaži")
            }
        }
    )
}