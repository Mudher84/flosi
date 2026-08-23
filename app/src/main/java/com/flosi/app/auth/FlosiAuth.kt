package com.flosi.app.auth

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val Purple = Color(0xFF7B44EF)
private val PurpleSoft = Color(0xFFF5F0FF)
private val TextMain = Color(0xFF17131F)
private val Muted = Color(0xFF8F8798)
private val Green = Color(0xFF18B97D)
private val Red = Color(0xFFE84C61)

private object CloudAuth {
    fun configured() =
        BuildConfig.FLOSI_FIREBASE_API_KEY.isNotBlank() &&
            BuildConfig.FLOSI_FIREBASE_APP_ID.isNotBlank() &&
            BuildConfig.FLOSI_FIREBASE_PROJECT_ID.isNotBlank() &&
            BuildConfig.FLOSI_GOOGLE_WEB_CLIENT_ID.isNotBlank()

    fun auth(context: Context): FirebaseAuth {
        val app = FirebaseApp.getApps(context).firstOrNull { it.name == "flosi-auth" } ?: run {
            check(configured()) { "إعداد Google/Firebase غير مكتمل" }
            val options = FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FLOSI_FIREBASE_API_KEY)
                .setApplicationId(BuildConfig.FLOSI_FIREBASE_APP_ID)
                .setProjectId(BuildConfig.FLOSI_FIREBASE_PROJECT_ID)
                .build()
            FirebaseApp.initializeApp(context, options, "flosi-auth")
                ?: error("تعذر تهيئة مصادقة Flosi")
        }
        return FirebaseAuth.getInstance(app)
    }

    fun hasPassword(user: FirebaseUser?) =
        user?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true

    suspend fun google(context: Context): FirebaseUser {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.FLOSI_GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "لم يرجع Google بيانات دخول صالحة" }
        val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val result = auth(context).signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
        return result.user ?: error("تعذر إنشاء جلسة Flosi")
    }

    suspend fun linkPassword(context: Context, password: String): FirebaseUser {
        val auth = auth(context)
        val user = auth.currentUser ?: error("سجّل الدخول عبر Google أولاً")
        val email = user.email ?: error("حساب Google لا يحتوي بريداً صالحاً")
        if (hasPassword(user)) {
            user.updatePassword(password).await()
            user.reload().await()
            return auth.currentUser ?: user
        }
        return user.linkWithCredential(EmailAuthProvider.getCredential(email, password)).await().user ?: user
    }

    suspend fun passwordLogin(context: Context, email: String, password: String): FirebaseUser =
        auth(context).signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("تعذر تسجيل الدخول")

    suspend fun reset(context: Context, email: String) {
        auth(context).sendPasswordResetEmail(email.trim()).await()
    }
}

private data class Strength(
    val len: Boolean,
    val lower: Boolean,
    val upper: Boolean,
    val digit: Boolean,
    val symbol: Boolean
) {
    val strong get() = len && lower && upper && digit && symbol
}

private fun strength(value: String) = Strength(
    len = value.length >= 12,
    lower = value.any(Char::isLowerCase),
    upper = value.any(Char::isUpperCase),
    digit = value.any(Char::isDigit),
    symbol = value.any { !it.isLetterOrDigit() && !it.isWhitespace() }
)

private enum class Stage { LOGIN, SET_PASSWORD, RESET }

@Composable
fun FlosiAuthGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    if (!CloudAuth.configured()) {
        MissingConfig()
        return
    }

    val auth = remember { CloudAuth.auth(context) }
    var user by remember { mutableStateOf(auth.currentUser) }
    var stage by remember {
        mutableStateOf(if (user != null && !CloudAuth.hasPassword(user)) Stage.SET_PASSWORD else Stage.LOGIN)
    }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            user = it.currentUser
            if (user == null) stage = Stage.LOGIN
            else if (!CloudAuth.hasPassword(user)) stage = Stage.SET_PASSWORD
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    if (user != null && CloudAuth.hasPassword(user)) {
        content()
        return
    }

    when (stage) {
        Stage.LOGIN -> Login(
            onGoogle = {
                user = it
                stage = if (CloudAuth.hasPassword(it)) Stage.LOGIN else Stage.SET_PASSWORD
            },
            onPassword = { user = it },
            onReset = { stage = Stage.RESET }
        )
        Stage.SET_PASSWORD -> SetPassword(
            email = user?.email.orEmpty(),
            onDone = { user = it },
            onCancel = {
                auth.signOut()
                user = null
                stage = Stage.LOGIN
            }
        )
        Stage.RESET -> ResetPassword(user?.email.orEmpty()) { stage = Stage.LOGIN }
    }
}

@Composable
private fun MissingConfig() = AuthPage {
    Text("Flosi", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(8.dp))
    Text("نظام تسجيل Google جاهز، لكنه يحتاج بيانات Firebase وGoogle OAuth الخاصة بالتطبيق.", color = Muted)
    Spacer(Modifier.height(12.dp))
    Text("Flosi لا يفتح الحساب بدون مصادقة صحيحة.", color = Red)
}

@Composable
private fun Login(
    onGoogle: (FirebaseUser) -> Unit,
    onPassword: (FirebaseUser) -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AuthPage {
        Text("Flosi", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TextMain)
        Text("أموالك، بحساب موثّق", color = Muted)
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = {
                scope.launch {
                    busy = true; error = null
                    try { onGoogle(CloudAuth.google(context)) }
                    catch (_: GetCredentialException) { error = "تم إلغاء Google أو تعذر إكماله." }
                    catch (t: Throwable) { error = t.message ?: "تعذر تسجيل الدخول عبر Google" }
                    finally { busy = false }
                }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TextMain),
            shape = RoundedCornerShape(16.dp)
        ) { Text("G   المتابعة باستخدام Google", fontWeight = FontWeight.Bold) }

        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f)); Text("  أو  ", color = Muted); HorizontalDivider(Modifier.weight(1f))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("بريد Google") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        TextButton(onClick = onReset, modifier = Modifier.align(Alignment.Start)) {
            Text("نسيت كلمة المرور؟", color = Purple)
        }
        Button(
            onClick = {
                scope.launch {
                    busy = true; error = null
                    try { onPassword(CloudAuth.passwordLogin(context, email, password)) }
                    catch (_: Throwable) { error = "البريد أو كلمة المرور غير صحيحة." }
                    finally { busy = false }
                }
            },
            enabled = !busy && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            shape = RoundedCornerShape(16.dp)
        ) { if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White) else Text("تسجيل الدخول") }
        error?.let { Text(it, color = Red, modifier = Modifier.padding(top = 12.dp)) }
        Spacer(Modifier.height(14.dp))
        Text("الحساب الجديد يبدأ بالموافقة من Google، ثم يطلب Flosi كلمة مرور قوية لنفس البريد.", color = Muted)
    }
}

@Composable
private fun SetPassword(email: String, onDone: (FirebaseUser) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val s = strength(pass)

    AuthPage {
        Text("أكمل حماية حسابك", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("وافق Google على: $email", color = Muted)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("كلمة مرور قوية") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text("تأكيد كلمة المرور") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(14.dp))
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
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    busy = true; error = null
                    try {
                        require(pass == confirm) { "كلمتا المرور غير متطابقتين" }
                        onDone(CloudAuth.linkPassword(context, pass))
                    } catch (t: Throwable) { error = t.message ?: "تعذر حفظ كلمة المرور" }
                    finally { busy = false }
                }
            },
            enabled = !busy && s.strong && pass == confirm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            shape = RoundedCornerShape(16.dp)
        ) { if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White) else Text("حفظ والدخول إلى Flosi") }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("إلغاء والعودة") }
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
        Text("استخدم نفس بريد Google المرتبط بحساب Flosi. سيصلك رابط إعادة التعيين على البريد نفسه.", color = Muted)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("بريد Google") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                scope.launch {
                    busy = true; error = null
                    try { CloudAuth.reset(context, email); sent = true }
                    catch (_: Throwable) { error = "تعذر إرسال رسالة الاستعادة. تحقق من البريد والاتصال." }
                    finally { busy = false }
                }
            },
            enabled = !busy && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Purple),
            shape = RoundedCornerShape(16.dp)
        ) { if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White) else Text("إرسال رابط الاستعادة") }
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
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content
                )
            }
        }
    }
}
