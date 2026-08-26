package com.flosi.app.subscription

import android.content.Context
import android.provider.Settings
import com.flosi.app.BuildConfig
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

internal data class ServerEntitlement(
    val active: Boolean,
    val trialEndsAt: Long?,
    val serverNow: Long
)

internal object SubscriptionEntitlementApi {
    fun configured(): Boolean = BuildConfig.FLOSI_BACKEND_BASE_URL.isNotBlank()

    suspend fun check(context: Context, user: FirebaseUser, purchaseToken: String? = null, productId: String? = null): ServerEntitlement? {
        val base = BuildConfig.FLOSI_BACKEND_BASE_URL.trimEnd('/')
        if (base.isBlank()) return null
        val idToken = user.getIdToken(true).await().token ?: return null
        val body = JSONObject()
            .put("deviceId", deviceHash(context))
            .put("purchaseToken", purchaseToken ?: JSONObject.NULL)
            .put("productId", productId ?: JSONObject.NULL)
        return withContext(Dispatchers.IO) {
            val conn = (URL("$base/v1/entitlement").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 10_000
                doOutput = true
                setRequestProperty("Authorization", "Bearer $idToken")
                setRequestProperty("Content-Type", "application/json")
            }
            runCatching {
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                if (conn.responseCode !in 200..299) return@runCatching null
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                ServerEntitlement(
                    active = json.optBoolean("active", false),
                    trialEndsAt = if (json.has("trialEndsAt") && !json.isNull("trialEndsAt")) json.getLong("trialEndsAt") else null,
                    serverNow = json.optLong("serverNow", System.currentTimeMillis())
                )
            }.getOrNull().also { conn.disconnect() }
        }
    }

    private fun deviceHash(context: Context): String {
        val raw = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
        val material = "flosi-device-v1|${context.packageName}|$raw"
        return MessageDigest.getInstance("SHA-256").digest(material.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
