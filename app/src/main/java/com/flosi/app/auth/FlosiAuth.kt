package com.flosi.app.auth

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.flosi.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val Purple = Color(0xFF7B44EF)
private val TextMain = Color(0xFF17131F)
private val Muted = Color(0xFF8F8798)
private val Green = Color(0xFF18B97D)
private val Red = Color(0xFFE84C61)

private object CloudAuth {
    fun configured(): Boolean =
        BuildConfig.FLOSI_FIREBASE_API_KEY.isNotBlank() &&
            BuildConfig.FLOSI_FIREBASE_APP_ID.isNotBlank() &&
            BuildConfig.FLOSI_FIREBASE_PROJECT_ID.isNotBlank()

    fun googleConfigured(): Boolean = BuildConfig.FLOSI_GOOGLE_WEB_CLIENT_ID.isNotBlank()

    fun auth(context: Context): FirebaseAuth {
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == "flosi-auth" } ?: run {
            check(configured()) { "إعداد Firebase غير مكتمل" }
            val options = FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FLOSI_FIREBASE_API_KEY)
                .setApplicationId(BuildConfig.FLOSI_FIREBASE_APP_ID)
                .setProjectId(BuildConfig.FLOSI_FIREBASE_PROJECT_ID)
                .build()
            FirebaseApp.initializeApp(context, options, "flosi-auth")
                ?: error("تعذر تهيئة تسجيل الدخول في Flosi")
        }
        return FirebaseAuth.getInstance(app)
    }

    suspend fun google(context: Context): FirebaseUser {
        check(googleConfigured()) { "إعداد Google OAuth غير مكتمل" }
        val option = GetSignInWithGoogleOption.Builder(BuildConfig.FLOSI_GOOGLE_WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "لم يرجع Google بيانات دخول صالحة" }
        val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val result = auth(context)
            .signInWithCredential(GoogleAuthProvider.getCredential(token, null))
            .await()
        return result.user ?: error("تعذر إنشاء جلسة Flosi")
    }

    suspend fun passwordLogin(context: Context, email: String, password: String): FirebaseUser =
        auth(context).signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("تعذر تسجيل الدخول")

    suspend fun createEmailAccount(context: Context, email: String, password: String): FirebaseUser =
        auth(context).createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("تعذر إنشاء الحساب")

    suspend fun reset(context: Context, email: String) {
        auth(context).sendPasswordResetEmail(email.trim()).await()
    }
}

private data class Strength(
    val len: Boolean,
    val lower: Boolean,
    val upper: Boolean,
    val digit: Boolean,
    val symbol: Boolean,
) {
    val strong: Boolean get() = len && lower && upper && digit && symbol
}

private fun strength(value: String) = Strength(
    len = value.length >= 12,
    lower = value.any(Char::isLowerCase),
    upper = value.any(Char::isUpperCase),
    digit = value.any(Char::isDigit),
    symbol = value.any { !it.isLetterOrDigit() && !it.isWhitespace() },
)

private enum class Stage { LOGIN, RESET }

@Composable
fun FlosiAuthGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    if (!CloudAuth.configured()) {
        MissingConfig()
        return
    }

    val auth = remember { CloudAuth.auth(context) }
    var user by remember { mutableStateOf(auth.currentUser) }
    var stage by remember { mutableStateOf(Stage.LOGIN) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { user = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (user != null) {
        content()
        return
    }

    when (stage) {
        Stage.LOGIN -> Login(
            onAuthenticated = { user = it },
            onReset = { stage = Stage.RESET },
        )
        Stage.RESET -> ResetPassword("") { stage = Stage.LOGIN }
    }
}

@Composable
private fun MissingConfig() = AuthPage {
    Text("Flosi", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text("نظام الحساب يحتاج إعداد Firebase الخاص بالتطبيق.", color = Muted)
    Spacer(Modifier.height(12.dp))
    Text("Flosi لا يفتح الحساب بدون مصادقة صحيحة.", color = Red)
}

@Composable
private fun Login(
    onAuthenticated: (FirebaseUser) -> Unit,
    onReset: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var useEmail by remember { mutableStateOf(false) }
    var createMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val passwordStrength = strength(password)

    AuthPage {
        Text("Flosi", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TextMain)
        Text("اختر الطريقة التي تناسبك للدخول", color = Muted)
        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                scope.launch {
                    busy = true
                    error = null
                    try {
                        onAuthenticated(CloudAuth.google(context))
                    } catch (e: GetCredentialException) {
                        val detail = e.message?.trim().orEmpty()
                        error = buildString {
                            append("تعذر تسجيل الدخول عبر Google")
                            append("\nالنوع: ")
                            append(e.type)
                            if (detail.isNotBlank()) {
                                append("\nالتفاصيل: ")
                                append(detail)
                            }
                        }
                    } catch (t: Throwable) {
                        error = t.message ?: "تعذر تسجيل الدخول عبر Google"
                    } finally {
                        busy = false
                    }
                }
            },
            enabled = !busy && CloudAuth.googleConfigured(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TextMain),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("G   المتابعة باستخدام Google", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                useEmail = !useEmail
                error = null
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(if (useEmail) "إخفاء تسجيل البريد الإلكتروني" else "الدخول بالبريد الإلكتروني", fontWeight = FontWeight.Bold)
        }

        if (useEmail) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(Modifier.weight(1f))
                Text("  البريد الإلكتروني  ", color = Muted)
                HorizontalDivider(Modifier.weight(1f))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !createMode,
                    onClick = {
                        createMode = false
                        confirm = ""
                        error = null
                    },
                    label = { Text("تسجيل دخول") },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = createMode,
                    onClick = {
                        createMode = true
                        error = null
                    },
                    label = { Text("إنشاء حساب") },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("البريد الإلكتروني") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("كلمة المرور") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )

            if (createMode) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("تأكيد كلمة المرور") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "لإنشاء الحساب: 12 حرفاً على الأقل مع حرف كبير وصغير ورقم ورمز خاص.",
                    color = if (passwordStrength.strong) Green else Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                TextButton(onClick = onReset, modifier = Modifier.align(Alignment.Start)) {
                    Text("نسيت كلمة المرور؟", color = Purple)
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        error = null
                        try {
                            val result = if (createMode) {
                                require(passwordStrength.strong) { "كلمة المرور لا تحقق شروط الأمان" }
                                require(password == confirm) { "كلمتا المرور غير متطابقتين" }
                                CloudAuth.createEmailAccount(context, email, password)
                            } else {
                                CloudAuth.passwordLogin(context, email, password)
                            }
                            onAuthenticated(result)
                        } catch (t: Throwable) {
                            error = t.message ?: if (createMode) "تعذر إنشاء الحساب" else "البريد أو كلمة المرور غير صحيحة"
                        } finally {
                            busy = false
                        }
                    }
                },
                enabled = !busy && email.isNotBlank() && password.isNotBlank() &&
                    (!createMode || (passwordStrength.strong && password == confirm)),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple),
                shape = RoundedCornerShape(16.dp),
            ) {
                if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White)
                else Text(if (createMode) "إنشاء الحساب والدخول" else "تسجيل الدخول")
            }
        }

        error?.let { Text(it, color = Red, modifier = Modifier.padding(top = 12.dp)) }
        Spacer(Modifier.height(14.dp))
        Text("يمكنك استخدام Google مباشرة أو إنشاء حساب مستقل بالبريد الإلكتروني.", color = Muted)
    }
}

@Composable
private fun ResetPassword(initialEmail: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var busy by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AuthPage {
        Text("استعادة كلمة المرور", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("أدخل البريد الإلكتروني المرتبط بحساب Flosi وسنرسل إليه رابط إعادة التعيين.", color = Muted)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            email,
            { email = it },
            label = { Text("البريد الإلكتروني") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                scope.launch {
                    busy = true
                    error = null
                    try {
                        CloudAuth.reset(context, email)
                        sent = true
                    } catch (_: Throwable) {
                        error = "تعذر إرسال رسالة الاستعادة. تحقق من البريد والاتصال."
                    } finally {
                        busy = false
                    }
                }
            },
            enabled = !busy && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White)
            else Text("إرسال رابط الاستعادة")
        }
        if (sent) Text("تم طلب رسالة الاستعادة. افحص بريدك ثم سجّل الدخول بكلمة المرور الجديدة.", color = Green, modifier = Modifier.padding(top = 12.dp))
        error?.let { Text(it, color = Red, modifier = Modifier.padding(top = 12.dp)) }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("العودة لتسجيل الدخول") }
    }
}

@Composable
private fun AuthPage(content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxSize(), color = Color(0xFFF7F6FB)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content,
                )
            }
        }
    }
}
