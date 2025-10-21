package com.example.myapplication.filter

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilters: (FilterData) -> Unit,
    onResetFilters: () -> Unit,
    userLocation: LatLng? = null
) {
    var selectedEntity by remember { mutableStateOf("masters") } // "masters" ili "jobs"
    var selectedProfession by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf(2000) }
    var searchType by remember { mutableStateOf("profession") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filteri i pretraga") },
        text = {
            Column {

                // IZBOR ENTITETA
                Text("Filtriraj:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedEntity == "masters",
                        onClick = { selectedEntity = "masters" }
                    )
                    Text("Majstore", modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedEntity == "jobs",
                        onClick = { selectedEntity = "jobs" }
                    )
                    Text("Poslove", modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TIP PRETRAGE
                Text("Vrsta pretrage:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = searchType == "profession",
                        onClick = { searchType = "profession" }
                    )
                    Text("Po profesiji", modifier = Modifier.padding(start = 8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = searchType == "radius",
                        onClick = { searchType = "radius" }
                    )
                    Text("Po udaljenosti", modifier = Modifier.padding(start = 8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = searchType == "both",
                        onClick = { searchType = "both" }
                    )
                    Text("Profesija + udaljenost", modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // PROFESIJA FILTER
                if (searchType == "profession" || searchType == "both") {
                    OutlinedTextField(
                        value = selectedProfession,
                        onValueChange = { selectedProfession = it },
                        label = { Text("Profesija") },
                        placeholder = { Text("npr. Električar, Moler...") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(Icons.Default.Search, "Pretraži profesiju")
                        },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // RADIUS FILTER
                if (searchType == "radius" || searchType == "both") {
                    Text("Radius pretrage: ${radius / 1000} km")
                    Slider(
                        value = radius.toFloat(),
                        onValueChange = { radius = it.toInt() },
                        valueRange = 500f..10000f,
                        steps = 19
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("0.5 km", style = MaterialTheme.typography.labelSmall)
                        Text("10 km", style = MaterialTheme.typography.labelSmall)
                    }

                    if (userLocation == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ Nema dostupne lokacije za radius pretragu",
                            color = Color.Red,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val filterData = FilterData(
                        targetType = selectedEntity,
                        profession = selectedProfession.ifEmpty { null },
                        radius = if (searchType == "radius" || searchType == "both") radius else null,
                        searchType = searchType,
                        userLocation = userLocation
                    )
                    onApplyFilters(filterData)
                },
                enabled = !(searchType == "radius" && userLocation == null) &&
                        !(searchType == "both" && selectedProfession.isEmpty() && userLocation == null)
            ) {
                Text("Primeni filtere")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Otkaži") }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onResetFilters) { // crveni X
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Obriši filtere",
                        tint = Color.Red
                    )
                }
            }
        }
    )
}

data class FilterData(
    val targetType: String = "masters", // "masters" ili "jobs"
    val profession: String? = null,
    val radius: Int? = null,
    val searchType: String = "profession",
    val userLocation: LatLng? = null
)
