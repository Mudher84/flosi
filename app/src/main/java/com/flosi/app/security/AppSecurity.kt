package com.flosi.app.security

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Base64
import android.view.WindowManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object AppSecurity {
    private const val PREFS = "flosi_app_security_v2"
    private const val K_PIN_SALT = "pin_salt"
    private const val K_PIN_HASH = "pin_hash"
    private const val K_PIN_FAILURES = "pin_failures"
    private const val K_PIN_LOCKOUT_UNTIL = "pin_lockout_until_wall"
    private const val K_BIOMETRIC = "biometric"
    private const val K_AUTO_LOCK_SECONDS = "auto_lock_seconds"
    private const val K_SCREEN_SECURE = "screen_secure"
    private const val K_LAST_BACKGROUND = "last_background_elapsed"
    private const val PBKDF2_ITERATIONS = 120_000
    private const val HASH_BITS = 256

    @Volatile private var sessionUnlocked = false

    fun hasPin(context: Context): Boolean = prefs(context).contains(K_PIN_HASH)
    fun biometricEnabled(context: Context): Boolean = prefs(context).getBoolean(K_BIOMETRIC, false)
    fun screenSecureEnabled(context: Context): Boolean = prefs(context).getBoolean(K_SCREEN_SECURE, true)
    fun autoLockSeconds(context: Context): Int = prefs(context).getInt(K_AUTO_LOCK_SECONDS, 0).coerceAtLeast(0)

    /**
     * A configured protection method must also have a usable unlock path.
     * Old installs could enable biometrics without a PIN. If the biometric sensor later
     * becomes unavailable, treating that stale flag as a lock would permanently lock the
     * user out. New biometric enrollment requires a PIN fallback; legacy biometric-only
     * installs are considered protected only while biometrics are actually available.
     */
    fun protectionConfigured(context: Context): Boolean =
        hasPin(context) || (biometricEnabled(context) && biometricAvailable(context))

    fun biometricStatus(context: Context): Int = BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    fun biometricAvailable(context: Context): Boolean = biometricStatus(context) == BiometricManager.BIOMETRIC_SUCCESS

    fun setPin(context: Context, pin: String) {
        require(pin.matches(Regex("\\d{6}"))) { "رمز PIN يجب أن يكون 6 أرقام" }
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val hash = derive(pin, salt)
        try {
            prefs(context).edit()
                .putString(K_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(K_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
                .remove(K_PIN_FAILURES).remove(K_PIN_LOCKOUT_UNTIL)
                .apply()
        } finally {
            salt.fill(0);hash.fill(0)
        }
        sessionUnlocked = true
    }

    fun clearPin(context: Context) {
        // A biometric-only configuration can become impossible to unlock if the sensor is
        // removed/disabled. Clearing PIN therefore also disables biometric app-lock.
        prefs(context).edit()
            .remove(K_PIN_SALT).remove(K_PIN_HASH)
            .remove(K_PIN_FAILURES).remove(K_PIN_LOCKOUT_UNTIL)
            .putBoolean(K_BIOMETRIC, false)
            .apply()
        sessionUnlocked = true
    }

    fun pinRetryAfterSeconds(context:Context):Long {
        val until=prefs(context).getLong(K_PIN_LOCKOUT_UNTIL,0L)
        val remaining=until-System.currentTimeMillis()
        if(remaining<=0L){
            if(until!=0L)prefs(context).edit().remove(K_PIN_LOCKOUT_UNTIL).apply()
            return 0L
        }
        return (remaining+999L)/1000L
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        if(pinRetryAfterSeconds(context)>0L)return false
        if (!pin.matches(Regex("\\d{6}"))) return false
        val p = prefs(context)
        val saltRaw = p.getString(K_PIN_SALT, null) ?: return false
        val hashRaw = p.getString(K_PIN_HASH, null) ?: return false
        val correct=runCatching {
            val salt = Base64.decode(saltRaw, Base64.NO_WRAP)
            val expected = Base64.decode(hashRaw, Base64.NO_WRAP)
            val actual = derive(pin, salt)
            try { MessageDigest.isEqual(expected, actual) } finally {salt.fill(0);expected.fill(0);actual.fill(0)}
        }.getOrDefault(false)
        if(correct){
            p.edit().remove(K_PIN_FAILURES).remove(K_PIN_LOCKOUT_UNTIL).apply()
            return true
        }
        val failures=(p.getInt(K_PIN_FAILURES,0)+1).coerceAtMost(1000)
        val lockSeconds=when{
            failures>=15->300L
            failures>=10->120L
            failures>=5->30L
            else->0L
        }
        p.edit().putInt(K_PIN_FAILURES,failures).apply()
        if(lockSeconds>0L)p.edit().putLong(K_PIN_LOCKOUT_UNTIL,System.currentTimeMillis()+lockSeconds*1000L).apply()
        return false
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        if (enabled) {
            require(hasPin(context)) { "فعّل PIN أولاً ليكون وسيلة احتياطية إذا تعطلت البصمة أو الوجه" }
            require(biometricAvailable(context)) { "القياسات الحيوية غير متاحة على هذا الجهاز" }
        }
        prefs(context).edit().putBoolean(K_BIOMETRIC, enabled).apply()
        if (!enabled && !hasPin(context)) sessionUnlocked = true
    }

    fun setAutoLockSeconds(context: Context, seconds: Int) {
        require(seconds in setOf(0, 30, 60, 300))
        prefs(context).edit().putInt(K_AUTO_LOCK_SECONDS, seconds).apply()
    }

    fun setScreenSecureEnabled(context: Context, enabled: Boolean) {prefs(context).edit().putBoolean(K_SCREEN_SECURE, enabled).apply()}

    fun markUnlocked(context: Context) {
        sessionUnlocked = true
        prefs(context).edit().remove(K_LAST_BACKGROUND).remove(K_PIN_FAILURES).remove(K_PIN_LOCKOUT_UNTIL).apply()
    }

    fun lockNow() {sessionUnlocked = false}

    fun onBackground(context: Context) {
        if (!protectionConfigured(context)) return
        prefs(context).edit().putLong(K_LAST_BACKGROUND, SystemClock.elapsedRealtime()).apply()
        if (autoLockSeconds(context) == 0) sessionUnlocked = false
    }

    fun shouldLock(context: Context): Boolean {
        if (!protectionConfigured(context)) return false
        if (!sessionUnlocked) return true
        val last = prefs(context).getLong(K_LAST_BACKGROUND, -1L)
        if (last < 0L) return false
        val timeoutMs = autoLockSeconds(context) * 1000L
        val elapsed=(SystemClock.elapsedRealtime()-last).coerceAtLeast(0L)
        val expired = timeoutMs == 0L || elapsed >= timeoutMs
        if (expired) sessionUnlocked = false
        return expired
    }

    fun applySecureFlag(activity: Activity, enabled: Boolean = screenSecureEnabled(activity)) {
        if (enabled) activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun authenticateBiometric(activity: FragmentActivity,onSuccess: () -> Unit,onUnavailable: (String) -> Unit,onUsePin: () -> Unit = {}) {
        if (!biometricEnabled(activity)) {onUnavailable("البصمة أو الوجه غير مفعّلين داخل Flosi");return}
        val hasPin = hasPin(activity)
        if (!biometricAvailable(activity)) {onUnavailable(if (hasPin) "تعذر استخدام القياسات الحيوية. استخدم PIN." else "تعذر استخدام القياسات الحيوية على هذا الجهاز.");return}
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {super.onAuthenticationSucceeded(result);markUnlocked(activity);onSuccess()}
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {super.onAuthenticationError(errorCode, errString);if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON && hasPin) onUsePin() else onUnavailable(errString.toString())}
            override fun onAuthenticationFailed() {super.onAuthenticationFailed();onUnavailable(if (hasPin) "لم يتم التحقق. حاول مرة ثانية أو استخدم PIN." else "لم يتم التحقق. حاول مرة ثانية.")}
        })
        val info = BiometricPrompt.PromptInfo.Builder().setTitle("فتح Flosi").setSubtitle("تحقق بالبصمة أو الوجه").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG).setNegativeButtonText(if (hasPin) "استخدام PIN" else "إلغاء").build()
        prompt.authenticate(info)
    }

    private fun derive(pin: String, salt: ByteArray): ByteArray {
        val chars = pin.toCharArray()
        return try {SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(PBEKeySpec(chars, salt, PBKDF2_ITERATIONS, HASH_BITS)).encoded} finally {chars.fill('\u0000')}
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
