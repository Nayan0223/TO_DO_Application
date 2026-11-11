package com.example.myapplication1

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AuthSession(
    val token: String,
    val email: String
)

object AuthClient {
    // Set this to the deployed HTTPS URL of the Spring Boot API.
    private const val BASE_URL = "https://your-api-host.example"

    suspend fun register(email: String, password: String): AuthSession =
        authenticate("/api/auth/register", email, password)

    suspend fun login(email: String, password: String): AuthSession =
        authenticate("/api/auth/login", email, password)

    suspend fun logout(token: String) {
        withContext(Dispatchers.IO) {
            request("/api/auth/logout", token = token)
        }
    }

    private suspend fun authenticate(path: String, email: String, password: String): AuthSession =
        withContext(Dispatchers.IO) {
            val response = request(
                path = path,
                body = JSONObject()
                    .put("email", email.trim())
                    .put("password", password)
            )
            AuthSession(
                token = response.optString("token"),
                email = response.optString("email")
            )
        }

    private fun request(
        path: String,
        body: JSONObject? = null,
        token: String? = null
    ): JSONObject {
        val connection = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
            requestMethod = if (path.endsWith("/logout")) "POST" else "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            if (token != null) {
                setRequestProperty("Authorization", "Bearer " + token)
            }
        }

        try {
            if (body != null) {
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write(body.toString())
                }
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val errorJson = runCatching { JSONObject(responseText) }.getOrNull()
                val message = errorJson?.optString("detail")
                    ?.takeIf { it.isNotBlank() }
                    ?: errorJson?.optString("message")?.takeIf { it.isNotBlank() }
                    ?: errorJson?.optString("error")?.takeIf { it.isNotBlank() }
                    ?: "Request failed. Check your connection and try again."
                throw IllegalStateException(message)
            }
            return if (responseText.isBlank()) JSONObject() else JSONObject(responseText)
        } finally {
            connection.disconnect()
        }
    }
}
