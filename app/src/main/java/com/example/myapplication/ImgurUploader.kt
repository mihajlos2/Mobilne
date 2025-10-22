package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

object ImgurUploader {
    private const val CLIENT_ID = "TVOJ_IMGUR_CLIENT_ID"

    suspend fun uploadImage(context: Context, imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val imageBytes = inputStream?.readBytes()
            inputStream?.close()

            val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", base64Image)
                .build()

            val request = Request.Builder()
                .url("https://api.imgur.com/3/image")
                .addHeader("Authorization", "Client-ID $CLIENT_ID")
                .post(requestBody)
                .build()

            val response = OkHttpClient().newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null

            if (response.isSuccessful) {
                val json = JSONObject(body)
                json.getJSONObject("data").getString("link")
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
