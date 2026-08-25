package com.flosi.app.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
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
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

private val Purple = Color(0xFF7B44EF)
private val PurpleSoft = Color(0xFFF5F0FF)
private val TextMain = Color(0xFF17131F)
private val Muted = Color(0xFF8F8798)
private val Green = Color(0xFF18B97D)
private val Red = Color(0xFFE84C61)
private const val AUTH_TIMEOUT_MS = 45_000L

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

private object CloudAuth {
    fun configured(): Boolean =
        BuildConfig.FLOSI_FIREBASE_API_KEY.isNotBlank() &&
            BuildConfig.FLOSI_FIREBASE_APP_ID.isNotBlank() &&
            BuildConfig.FLOSI_FIREBASE_PROJECT_ID.isNotBlank() &&
            BuildConfig.FLOSI_GOOGLE_WEB_CLIENT_ID.isNotBlank()

    private suspend fun <T> timed(label: String, block: suspend () -> T): T = try {
        withTimeout(AUTH_TIMEOUT_MS) { block() }
    } catch (_: TimeoutCancellationException) {
        error("$label لم يكتمل خلال 45 ثانية. تحقق من الإنترنت وإعداد Firebase/Google ثم حاول مرة أخرى.")
    }

    fun auth(context: Context): FirebaseAuth {
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == "flosi-auth" } ?: run {
            check(configured()) { "إعداد Firebase أو Google غير مكتمل" }
            val options = FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FLOSI_FIREBASE_API_KEY)
                .setApplicationId(BuildConfig.FLOSI_FIREBASE_APP_ID)
                .setProjectId(BuildConfig.FLOSI_FIREBASE_PROJECT_ID)
                .build()
            FirebaseApp.initializeApp(context.applicationContext, options, "flosi-auth")
                ?: error("تعذر تهيئة تسجيل الدخول في Flosi")
        }
        return FirebaseAuth.getInstance(app)
    }

    suspend fun google(context: Context): FirebaseUser = timed("تسجيل Google") {
        val activity = context.activity() ?: error("تعذر العثور على شاشة Android النشطة لتسجيل Google")
        val option = GetSignInWithGoogleOption.Builder(
            serverClientId = BuildConfig.FLOSI_GOOGLE_WEB_CLIENT_ID
        ).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val credential = CredentialManager.create(activity)
            .getCredential(activity, request)
            .credential
        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "لم يرجع Google بيانات دخول صالحة" }
        val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val result = auth(activity)
            .signInWithCredential(GoogleAuthProvider.getCredential(token, null))
            .await()
        result.user ?: error("تعذر إنشاء جلسة Flosi")
    }

    suspend fun emailLogin(context: Context, email: String, password: String): FirebaseUser = timed("تسجيل الدخول") {
        auth(context).signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("تعذر تسجيل الدخول")
    }

    suspend fun emailRegister(context: Context, email: String, password: String): FirebaseUser = timed("إنشاء الحساب") {
        auth(context).createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("تعذر إنشاء الحساب")
    }

    suspend fun reset(context: Context, email: String) {
        timed("استعادة كلمة المرور") {
            auth(context).sendPasswordResetEmail(email.trim()).await()
        }
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

private enum class Stage { CHOICE, EMAIL, REGISTER, RESET }

@Composable
fun FlosiAuthGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    if (!CloudAuth.configured()) {
        MissingConfig()
        return
    }

    val auth = remember { CloudAuth.auth(context) }
    var user by remember { mutableStateOf(auth.currentUser) }
    var stage by remember { mutableStateOf(Stage.CHOICE) }

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
        Stage.CHOICE -> Choice(
            onSignedIn = { user = it },
            onEmail = { stage = Stage.EMAIL },
        )
        Stage.EMAIL -> EmailLogin(
            onSignedIn = { user = it },
            onRegister = { stage = Stage.REGISTER },
            onReset = { stage = Stage.RESET },
            onBack = { stage = Stage.CHOICE },
        )
        Stage.REGISTER -> EmailRegister(
            onSignedIn = { user = it },
            onBack = { stage = Stage.EMAIL },
        )
        Stage.RESET -> ResetPassword("") { stage = Stage.EMAIL }
    }
}

@Composable
private fun MissingConfig() = AuthPage {
    Text("Flosi", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text("إعداد Firebase أو Google OAuth غير مكتمل.", color = Muted)
    Spacer(Modifier.height(12.dp))
    Text("Flosi لا يفتح الحساب بدون مصادقة صحيحة.", color = Red)
}

@Composable
private fun Choice(
    onSignedIn: (FirebaseUser) -> Unit,
    onEmail: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AuthPage {
        Text("Flosi", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TextMain)
        Text("اختر طريقة الدخول", color = Muted)
        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                scope.launch {
                    busy = true
                    error = null
                    try {
                        onSignedIn(CloudAuth.google(context))
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
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TextMain),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (busy) CircularProgressIndicator(strokeWidth = 2.dp)
            else Text("G   المتابعة باستخدام Google", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onEmail,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("الدخول بالبريد الإلكتروني")
        }

        error?.let { Text(it, color = Red, modifier = Modifier.padding(top = 14.dp)) }
    }
}

@Composable
private fun EmailLogin(
    onSignedIn: (FirebaseUser) -> Unit,
    onRegister: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AuthPage {
        Text("الدخول بالبريد الإلكتروني", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(email, { email = it }, label = { Text("البريد الإلكتروني") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(password, { password = it }, label = { Text("كلمة المرور") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        TextButton(onClick = onReset, modifier = Modifier.align(Alignment.Start)) { Text("نسيت كلمة المرور؟", color = Purple) }
        Button(
            onClick = {
                scope.launch {
                    busy = true
                    error = null
                    try {
                        onSignedIn(CloudAuth.emailLogin(context, email, password))
                    } catch (t: Throwable) {
                        error = t.message ?: "البريد أو كلمة المرور غير صحيحة."
                    } finally {
                        busy = false
                    }
                }
            },
            enabled = !busy && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White) else Text("تسجيل الدخول")
        }
        OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("إنشاء حساب جديد") }
        error?.let { Text(it, color = Red, modifier = Modifier.padding(top = 12.dp)) }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("العودة") }
    }
}

@Composable
private fun EmailRegister(
    onSignedIn: (FirebaseUser) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val s = strength(pass)

    AuthPage {
        Text("إنشاء حساب جديد", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(email, { email = it }, label = { Text("البريد الإلكتروني") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("كلمة مرور قوية") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(confirm, { confirm = it }, label = { Text("تأكيد كلمة المرور") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = PurpleSoft), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Rule("12 حرفاً على الأقل", s.len)
                Rule("حرف صغير", s.lower)
                Rule("حرف كبير", s.upper)
                Rule("رقم واحد على الأقل", s.digit)
                Rule("رمز خاص مثل ! @ # $", s.symbol)
            }
        }
        error?.let { Text(it, color = Red, modifier = Modifier.padding(top = 10.dp)) }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = {
                scope.launch {
                    busy = true
                    error = null
                    try {
                        require(pass == confirm) { "كلمتا المرور غير متطابقتين" }
                        onSignedIn(CloudAuth.emailRegister(context, email, pass))
                    } catch (t: Throwable) {
                        error = t.message ?: "تعذر إنشاء الحساب"
                    } finally {
                        busy = false
                    }
                }
            },
            enabled = !busy && email.isNotBlank() && s.strong && pass == confirm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White) else Text("إنشاء الحساب والدخول")
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("لدي حساب بالفعل") }
    }
}

@Composable
private fun Rule(label: String, ok: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(if (ok) Icons.Default.CheckCircle else Icons.Default.Lock, null, tint = if (ok) Green else Muted)
        Text("  $label", color = if (ok) Green else Muted)
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
        Text("أدخل البريد المرتبط بحساب Flosi.", color = Muted)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(email, { email = it }, label = { Text("البريد الإلكتروني") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                scope.launch {
                    busy = true
                    error = null
                    try {
                        CloudAuth.reset(context, email)
                        sent = true
                    } catch (t: Throwable) {
                        error = t.message ?: "تعذر إرسال رسالة الاستعادة. تحقق من البريد والاتصال."
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
            if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White) else Text("إرسال رابط الاستعادة")
        }
        if (sent) Text("تم إرسال طلب الاستعادة. افحص بريدك.", color = Green, modifier = Modifier.padding(top = 12.dp))
        error?.let { Text(it, color = Red, modifier = Modifier.padding(top = 12.dp)) }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("العودة") }
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = content,
                )
            }
        }
    }
}
