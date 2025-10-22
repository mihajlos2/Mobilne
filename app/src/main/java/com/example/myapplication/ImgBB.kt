package com.example.myapplication

import android.content.Context
import android.net.Uri
import android.util.Base64
import okhttp3.*
import org.json.JSONObject
import java.io.InputStream

object ImgBBUploader {
    private const val API_KEY = "283fb6422044116eea1f5a5ea7d1cb68"
    private val client = OkHttpClient()

    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)

            val formBody = FormBody.Builder()
                .add("key", API_KEY)
                .add("image", base64)
                .build()

            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload")
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            val json = JSONObject(body)
            json.getJSONObject("data").getString("url")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
