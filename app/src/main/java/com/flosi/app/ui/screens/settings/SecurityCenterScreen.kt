package com.flosi.app.ui.screens.settings

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
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
    val activeLayers = listOf(hasPin, biometric, secureScreen, autoLock >= 0).count { it }

    FlosiPage("الأمان", "حماية حقيقية وفشل مغلق", onBack) {
        CardBox {
            Metric("مستوى الحماية", if (hasPin) "محمي" else "يحتاج PIN", if (hasPin) FlosiGreen else FlosiOrange)
            Text(
                if (hasPin) "PIN احتياطي فعّال • $activeLayers طبقات مهيأة" else "عيّن PIN أولاً حتى لا ينفتح التطبيق عند تعذر البصمة.",
                color = FlosiMuted
            )
        }

        CardBox {
            SecuritySwitchRow(
                title = "البصمة والوجه",
                subtitle = if (biometricAvailable) "فتح سريع مع PIN احتياطي إلزامي" else "غير متاحة حالياً على الجهاز",
                checked = biometric,
                enabled = biometricAvailable || biometric,
                onCheckedChange = { enabled ->
                    val error = runCatching { AppSecurity.setBiometricEnabled(context, enabled) }.exceptionOrNull()?.message
                    if (error != null) message = error
                    else {
                        biometric = enabled
                        message = if (enabled) "تم تفعيل القياسات الحيوية" else "تم إيقاف القياسات الحيوية"
                        onSecurityChanged()
                    }
                }
            )
            HorizontalDivider(color = FlosiLine)
            ActionRow(
                title = if (hasPin) "تغيير PIN" else "تعيين PIN",
                subtitle = if (hasPin) "رمز احتياطي من 4 إلى 8 أرقام" else "مطلوب كمسار احتياطي آمن",
                value = if (hasPin) "مفعّل" else "غير مفعّل",
                accent = if (hasPin) FlosiGreen else FlosiOrange,
                onClick = { showPinDialog = true }
            )
            if (hasPin && !biometric) {
                TextButton(onClick = {
                    AppSecurity.clearPin(context)
                    hasPin = false
                    message = "تم حذف PIN. لا توجد طبقة قفل دخول مفعّلة."
                    onSecurityChanged()
                }) { Text("إزالة PIN") }
            }
        }

        CardBox {
            SecuritySwitchRow(
                title = "حماية الشاشة والـ Recent Apps",
                subtitle = "يمنع لقطات الشاشة وتسجيل المحتوى الحساس",
                checked = secureScreen,
                onCheckedChange = {
                    AppSecurity.setScreenSecureEnabled(context, it)
                    secureScreen = it
                    onSecurityChanged()
                }
            )
        }

        CardBox {
            Text("القفل التلقائي", color = FlosiText)
            Text("يبدأ العد عند مغادرة Flosi. إذا انتهت المدة يرجع التطبيق لشاشة القفل.", color = FlosiMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(0 to "فوراً", 30 to "30ث", 60 to "1د", 300 to "5د").forEach { (seconds, label) ->
                    FilterChip(
                        selected = autoLock == seconds,
                        onClick = {
                            AppSecurity.setAutoLockSeconds(context, seconds)
                            autoLock = seconds
                            onSecurityChanged()
                        },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (message != null) {
            Surface(color = FlosiPurpleSoft, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Text(message!!, modifier = Modifier.padding(12.dp), color = FlosiPurple)
            }
        }

        OutlinedButton(onClick = onBackups, modifier = Modifier.fillMaxWidth()) {
            Text("إدارة النسخ الاحتياطية المشفرة")
        }

        if (hasPin) {
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
                    message = "تم حفظ PIN بأمان"
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
            error = "تعذر تشغيل شاشة البصمة. استخدم PIN."
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
                    onValueChange = { pin = it.filter(Char::isDigit).take(8); error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("PIN") },
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
                    enabled = pin.length >= 4,
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
                Text(
                    "الحماية Fail-closed: لن يتم فتح Flosi لأن القياسات الحيوية المفعّلة غير متاحة ولا يوجد PIN احتياطي.",
                    color = FlosiRed
                )
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
    val valid = pin.matches(Regex("\\d{4,8}")) && pin == confirm

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعيين PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("استخدم 4 إلى 8 أرقام. يُحفظ PIN كمشتق PBKDF2 مع salt محلي، وليس كنص صريح.", color = FlosiMuted)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter(Char::isDigit).take(8) },
                    label = { Text("PIN الجديد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it.filter(Char::isDigit).take(8) },
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
