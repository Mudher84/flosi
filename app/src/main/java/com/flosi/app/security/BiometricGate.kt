package com.flosi.app.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat

object BiometricGate {
    fun available(activity: FragmentActivity): Boolean {
        val result = BiometricManager.from(activity).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess:()->Unit,
        onError:(String)->Unit
    ) {
        val executor=ContextCompat.getMainExecutor(activity)
        val prompt=BiometricPrompt(activity,executor,object:BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
            override fun onAuthenticationError(errorCode:Int,errString:CharSequence)=onError(errString.toString())
        })
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("فتح فلوسي")
                .setSubtitle("استخدم البصمة أو قفل الجهاز")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()
        )
    }
}
