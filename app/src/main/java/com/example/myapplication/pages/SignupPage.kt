package com.example.myapplication.pages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.AuthState
import com.example.myapplication.AuthViewModel
import com.example.myapplication.ImgBBUploader
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SignupPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var ime by remember { mutableStateOf("") }
    var prezime by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var uploading by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    val photoUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            selectedImageUri = photoUri.value
        }
    }
    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Authenticated -> navController.navigate("home")
            is AuthState.Message -> Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            is AuthState.Error -> Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF89D99C)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registracija", fontSize = MaterialTheme.typography.headlineMedium.fontSize)

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(value = ime, onValueChange = { ime = it }, label = { Text("Ime") })
        OutlinedTextField(value = prezime, onValueChange = { prezime = it }, label = { Text("Prezime") })
        OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Broj telefona") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Lozinka") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Sakrij lozinku" else "Prikaži lozinku")
                }
            }
        )


        Spacer(Modifier.height(8.dp))

        Row {
            Button(onClick = { galleryLauncher.launch("image/*") }) { Text("Galerija") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                val file = File(context.cacheDir, "temp.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                photoUri.value = uri
                cameraLauncher.launch(uri)
            }) {
                Text("Kamera")
            }
        }

        selectedImageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Odabrana slika",
                modifier = Modifier.size(100.dp).padding(8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        if (selectedImageUri != null && !uploading) {
            Button(onClick = {
                scope.launch {
                    uploading = true
                    imageUrl = withContext(Dispatchers.IO) {
                        ImgBBUploader.uploadImage(context, selectedImageUri!!)
                    }
                    uploading = false
                    if (imageUrl != null)
                        Toast.makeText(context, "Slika uspešno uploadovana", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(context, "Greška pri uploadu", Toast.LENGTH_SHORT).show()
                }
            }) { Text("Otpremi sliku") }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = { authViewModel.signup3(email, password, ime, prezime, phoneNumber, imageUrl) },
            enabled = !uploading) { Text("Napravi nalog") }

        TextButton(onClick = { navController.navigate("login") }) { Text("Već imam nalog — Prijava") }
    }
}

