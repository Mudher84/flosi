package com.flosi.app.ui.screens.settings

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.flosi.app.security.AppSecurity
import com.flosi.app.ui.components.*

@Composable
fun SecurityCenterScreen(
    onBack: () -> Unit,
    onBackups: () -> Unit,
    onSecurityChanged: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var biometric by remember { mutableStateOf(AppSecurity.biometricEnabled(context)) }
    var hasPin by remember { mutableStateOf(AppSecurity.hasPin(context)) }
    var secureScreen by remember { mutableStateOf(AppSecurity.screenSecureEnabled(context)) }
    var autoLock by remember { mutableIntStateOf(AppSecurity.autoLockSeconds(context)) }
    var showPinDialog by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val biometricStatus = AppSecurity.biometricStatus(context)
    val biometricAvailable = biometricStatus == BiometricManager.BIOMETRIC_SUCCESS
    val activeLayers = listOf(hasPin, biometric, secureScreen).count { it }
    val protectionLabel = when {
        activeLayers >= 3 -> "حماية قوية"
        activeLayers == 2 -> "حماية جيدة"
        activeLayers == 1 -> "حماية أساسية"
        else -> "بدون قفل دخول"
    }

    FlosiPage("أمان Flosi", "خيارات واضحة لحماية بياناتك المالية", onBack) {
        CardBox {
            Metric("مستوى الحماية", protectionLabel, if (activeLayers >= 2) FlosiGreen else FlosiOrange)
            Text("فعّل فقط ما يناسبك. PIN والبصمة/الوجه خيارات مستقلة وليست إلزامية.", color = FlosiMuted)
        }

        SectionTitle("فتح التطبيق")
        CardBox {
            SecuritySwitchRow(
                title = "PIN من 6 أرقام",
                subtitle = if (hasPin) "مفعّل ويمكن تغييره متى تريد" else "اختياري — فعّله إذا تريد قفل رقمي سريع",
                checked = hasPin,
                onCheckedChange = { enabled ->
                    if (enabled) showPinDialog = true
                    else {
                        AppSecurity.clearPin(context)
                        hasPin = false
                        message = "تم إيقاف PIN"
                        onSecurityChanged()
                    }
                }
            )
            if (hasPin) {
                HorizontalDivider(color = FlosiLine)
                TextButton(onClick = { showPinDialog = true }) { Text("تغيير PIN") }
            }
            HorizontalDivider(color = FlosiLine)
            SecuritySwitchRow(
                title = "البصمة أو الوجه",
                subtitle = if (biometricAvailable) "اختياري — يستخدم القياسات الحيوية المسجلة في الجهاز" else "غير متاحة حالياً على هذا الجهاز",
                checked = biometric,
                enabled = biometricAvailable || biometric,
                onCheckedChange = { enabled ->
                    val error = runCatching { AppSecurity.setBiometricEnabled(context, enabled) }.exceptionOrNull()?.message
                    if (error != null) message = error
                    else {
                        biometric = enabled
                        message = if (enabled) "تم تفعيل البصمة أو الوجه" else "تم إيقاف البصمة أو الوجه"
                        onSecurityChanged()
                    }
                }
            )
        }

        SectionTitle("القفل والخصوصية")
        CardBox {
            Text("القفل التلقائي", color = FlosiText)
            Text("يعمل فقط عند تفعيل PIN أو البصمة/الوجه.", color = FlosiMuted)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(0 to "فوراً", 30 to "30ث", 60 to "1د", 300 to "5د").forEach { (seconds, label) ->
                    FilterChip(
                        selected = autoLock == seconds,
                        onClick = {
                            AppSecurity.setAutoLockSeconds(context, seconds)
                            autoLock = seconds
                            message = "تم حفظ مدة القفل التلقائي"
                            onSecurityChanged()
                        },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            HorizontalDivider(color = FlosiLine, modifier = Modifier.padding(vertical = 8.dp))
            SecuritySwitchRow(
                title = "حماية لقطة الشاشة",
                subtitle = "يمنع تصوير الشاشة ويخفي المحتوى من التطبيقات الأخيرة",
                checked = secureScreen,
                onCheckedChange = {
                    AppSecurity.setScreenSecureEnabled(context, it)
                    secureScreen = it
                    message = if (it) "تم تفعيل حماية الشاشة" else "تم إيقاف حماية الشاشة"
                    onSecurityChanged()
                }
            )
        }

        SectionTitle("البيانات")
        CardBox {
            ActionRow(
                title = "النسخ الاحتياطية المشفرة",
                subtitle = "إدارة النسخ والاستعادة بأمان",
                value = "إدارة",
                accent = FlosiPurple,
                onClick = onBackups
            )
        }

        if (message != null) {
            Surface(color = FlosiPurpleSoft, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Text(message!!, modifier = Modifier.padding(12.dp), color = FlosiPurple)
            }
        }

        if (hasPin || biometric) {
            Button(
                onClick = {
                    AppSecurity.lockNow()
                    message = "سيظهر القفل عند الرجوع أو إعادة فتح التطبيق."
                    onSecurityChanged()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("قفل Flosi الآن") }
        }
    }

    if (showPinDialog) {
        PinSetupDialog(
            onDismiss = { showPinDialog = false },
            onSave = { pin ->
                val error = runCatching { AppSecurity.setPin(context, pin) }.exceptionOrNull()?.message
                if (error == null) {
                    hasPin = true
                    showPinDialog = false
                    message = "تم حفظ PIN المكوّن من 6 أرقام"
                    onSecurityChanged()
                } else message = error
            }
        )
    }
}

@Composable
fun FlosiLockScreen(onUnlocked: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val biometricEnabled = AppSecurity.biometricEnabled(context)
    val biometricAvailable = AppSecurity.biometricAvailable(context)
    val hasPin = AppSecurity.hasPin(context)
    var prompted by remember { mutableStateOf(false) }

    fun unlockWithBiometric() {
        val host = activity
        if (host == null) {
            error = if (hasPin) "تعذر تشغيل البصمة. استخدم PIN." else "تعذر تشغيل البصمة أو الوجه."
            return
        }
        AppSecurity.authenticateBiometric(
            activity = host,
            onSuccess = { error = null; onUnlocked() },
            onUnavailable = { error = it },
            onUsePin = { error = null }
        )
    }

    LaunchedEffect(biometricEnabled, biometricAvailable) {
        if (biometricEnabled && biometricAvailable && !prompted) {
            prompted = true
            unlockWithBiometric()
        }
    }

    Surface(color = FlosiBg, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(color = FlosiPurpleSoft, shape = MaterialTheme.shapes.extraLarge) {
                Text("◆", modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp), color = FlosiPurple)
            }
            Spacer(Modifier.height(18.dp))
            Text("Flosi مقفول", style = MaterialTheme.typography.headlineSmall, color = FlosiText)
            Spacer(Modifier.height(6.dp))
            Text("تحقق قبل عرض بياناتك المالية", color = FlosiMuted)
            Spacer(Modifier.height(22.dp))

            if (hasPin) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(6); error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("PIN من 6 أرقام") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (AppSecurity.verifyPin(context, pin)) {
                            AppSecurity.markUnlocked(context)
                            pin = ""
                            error = null
                            onUnlocked()
                        } else error = "PIN غير صحيح"
                    },
                    enabled = pin.length == 6,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("فتح") }
            }

            if (biometricEnabled) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = ::unlockWithBiometric,
                    enabled = biometricAvailable,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (biometricAvailable) "استخدام البصمة أو الوجه" else "القياسات الحيوية غير متاحة")
                }
            }

            if (!hasPin && biometricEnabled && !biometricAvailable) {
                Spacer(Modifier.height(12.dp))
                Text("القياسات الحيوية المفعّلة غير متاحة حالياً على الجهاز.", color = FlosiRed)
            }
            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = FlosiRed)
            }
        }
    }
}

@Composable
private fun SecuritySwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(title, color = FlosiText)
            Text(subtitle, color = FlosiMuted)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun PinSetupDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val valid = pin.matches(Regex("\\d{6}")) && pin == confirm

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعيين PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("أدخل 6 أرقام فقط. PIN اختياري ويمكن إيقافه لاحقاً من صفحة الأمان.", color = FlosiMuted)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(6) },
                    label = { Text("PIN الجديد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it.filter(Char::isDigit).take(6) },
                    label = { Text("تأكيد PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                if (confirm.isNotEmpty() && pin != confirm) Text("الرمزان غير متطابقين", color = FlosiRed)
            }
        },
        confirmButton = { Button(onClick = { onSave(pin) }, enabled = valid) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
