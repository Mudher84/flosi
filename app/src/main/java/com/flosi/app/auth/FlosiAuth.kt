package com.flosi.app.auth

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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

private val FlosiPurple = Color(0xFF7B44EF)
private val FlosiPurpleSoft = Color(0xFFF5F0FF)
private val FlosiText = Color(0xFF17131F)
private val FlosiMuted = Color(0xFF8F8798)
private val FlosiGreen = Color(0xFF18B97D)
private val FlosiRed = Color(0xFFE84C61)

private object FlosiCloudAuth {
    fun configured(): Boolean =
        BuildConfig.FLOSI_FIREBASE_API_KEY.isNotBlank() &&
            BuildConfig.FLOSI_FIREBASE_APP_ID.isNotBlank() &&
            BuildConfig.FLOSI_FIREBASE_PROJECT_ID.isNotBlank() &&
            BuildConfig.FLOSI_GOOGLE_WEB_CLIENT_ID.isNotBlank()

    fun auth(context: Context): FirebaseAuth {
        val app = FirebaseApp.getApps(context).firstOrNull() ?: run {
            check(configured()) { "Flosi cloud authentication is not configured" }
            val options = FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FLOSI_FIREBASE_API_KEY)
                .setApplicationId(BuildConfig.FLOSI_FIREBASE_APP_ID)
                .setProjectId(BuildConfig.FLOSI_FIREBASE_PROJECT_ID)
                .build()
            FirebaseApp.initializeApp(context, options, "flosi-auth")
                ?: error("Unable to initialize Flosi Firebase authentication")
        }
        return FirebaseAuth.getInstance(app)
    }

    fun hasPassword(user: FirebaseUser?): Boolean =
        user?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true

    suspend fun signInWithGoogle(context: Context): FirebaseUser {
        check(configured()) { "إعداد Google/Firebase غير مكتمل" }
        val credentialManager = CredentialManager.create(context)
        val googleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.FLOSI_GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "لم يرجع Google بيانات دخول صالحة" }
        val google = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(google.idToken, null)
        return auth(context).signInWithCredential(firebaseCredential).await().user
            ?: error("تعذر إنشاء جلسة Flosi")
    }

    suspend fun linkStrongPassword(context: Context, password: String): FirebaseUser {
        val current = auth(context).currentUser ?: error("سجّل الدخول عبر Google أولاً")
        val email = current.email ?: error("حساب Google لا يحتوي بريداً قابلاً للاستخدام")
        val credential = EmailAuthProvider.getCredential(email, password)
        return if (hasPassword(current)) {
            current.updatePassword(password).await()
            current.reload().await()
            auth(context).currentUser ?: current
        } else {
            current.linkWithCredential(credential).await().user ?: current
        }
    }

    suspend fun signInWithPassword(context: Context, email: String, password: String): FirebaseUser {
        return auth(context).signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("تعذر تسجيل الدخول")
    }

    suspend fun sendReset(context: Context, email: String) {
        auth(context).sendPasswordResetEmail(email.trim()).await()
    }
}

private data class PasswordStrength(
    val length: Boolean,
    val lower: Boolean,
    val upper: Boolean,
    val digit: Boolean,
    val symbol: Boolean
) {
    val strong: Boolean get() = length && lower && upper && digit && symbol
}

private fun passwordStrength(value: String) = PasswordStrength(
    length = value.length >= 12,
    lower = value.any { it.isLowerCase() },
    upper = value.any { it.isUpperCase() },
    digit = value.any { it.isDigit() },
    symbol = value.any { !it.isLetterOrDigit() && !it.isWhitespace() }
)

private enum class AuthStage { LOGIN, SET_PASSWORD, RESET }

@Composable
fun FlosiAuthGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var authUser by remember { mutableStateOf<FirebaseUser?>(null) }
    var stage by remember { mutableStateOf(AuthStage.LOGIN) }

    if (!FlosiCloudAuth.configured()) {
        AuthConfigurationRequired()
        return
    }

    val auth = remember { FlosiCloudAuth.auth(context) }
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebase ->
            authUser = firebase.currentUser
            stage = when {
                firebase.currentUser == null -> AuthStage.LOGIN
                FlosiCloudAuth.hasPassword(firebase.currentUser) -> AuthStage.LOGIN
                else -> AuthStage.SET_PASSWORD
            }
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val user = authUser ?: auth.currentUser
    if (user != null && FlosiCloudAuth.hasPassword(user)) {
        content()
        return
    }

    when (stage) {
        AuthStage.LOGIN -> LoginScreen(
            onGoogleSucceeded = { googleUser ->
                authUser = googleUser
                stage = if (FlosiCloudAuth.hasPassword(googleUser)) AuthStage.LOGIN else AuthStage.SET_PASSWORD
            },
            onPasswordSucceeded = { signed -> authUser = signed },
            onForgot = { stage = AuthStage.RESET }
        )
        AuthStage.SET_PASSWORD -> SetStrongPasswordScreen(
            email = user?.email.orEmpty(),
            onDone = { linked -> authUser = linked },
            onCancel = {
                auth.signOut()
                authUser = null
                stage = AuthStage.LOGIN
            }
        )
        AuthStage.RESET -> ResetPasswordScreen(
            initialEmail = user?.email.orEmpty(),
            onBack = { stage = AuthStage.LOGIN }
        )
    }
}

@Composable
private fun AuthConfigurationRequired() {
    Surface(Modifier.fillMaxSize(), color = Color(0xFFF7F6FB)) {
        Box(Modifier.fillMaxSize().padding(22.dp), contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Flosi", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("تسجيل Google جاهز برمجياً لكنه يحتاج بيانات مشروع Firebase وGoogle OAuth الخاصة بـ Flosi.", color = FlosiMuted)
                    Spacer(Modifier.height(14.dp))
                    Text("لا يتم فتح التطبيق بوضع غير محمي عند غياب إعدادات المصادقة.", color = FlosiRed)
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(
    onGoogleSucceeded: (FirebaseUser) -> Unit,
    onPasswordSucceeded: (FirebaseUser) -> Unit,
    onForgot: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AuthPage {
        Text("Flosi", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = FlosiText)
        Text("أموالك، بحساب واحد موثّق", color = FlosiMuted)
        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                scope.launch {
                    busy = true; error = null
                    try {
                        onGoogleSucceeded(FlosiCloudAuth.signInWithGoogle(context))
                    } catch (_: GetCredentialException) {
                        error = "تم إلغاء تسجيل Google أو تعذر إكماله."
                    } catch (t: Throwable) {
                        error = t.message ?: "تعذر تسجيل الدخول عبر Google"
                    } finally { busy = false }
                }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = FlosiText),
            shape = RoundedCornerShape(16.dp)
        ) { Text("G   المتابعة باستخدام Google", fontWeight = FontWeight.Bold) }

        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Divider(Modifier.weight(1f)); Text("  أو  ", color = FlosiMuted); Divider(Modifier.weight(1f))
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
        TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.Start)) {
            Text("نسيت كلمة المرور؟", color = FlosiPurple)
        }
        Button(
            onClick = {
                scope.launch {
                    busy = true; error = null
                    try { onPasswordSucceeded(FlosiCloudAuth.signInWithPassword(context, email, password)) }
                    catch (_: Throwable) { error = "البريد أو كلمة المرور غير صحيحة." }
                    finally { busy = false }
                }
            },
            enabled = !busy && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FlosiPurple),
            shape = RoundedCornerShape(16.dp)
        ) { if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White) else Text("تسجيل الدخول") }

        error?.let { Text(it, color = FlosiRed, modifier = Modifier.padding(top = 12.dp)) }
        Spacer(Modifier.height(14.dp))
        Text("إنشاء حساب جديد يبدأ دائماً بالموافقة من Google، وبعدها يطلب Flosi كلمة مرور قوية مرتبطة بنفس البريد.", color = FlosiMuted)
    }
}

@Composable
private fun SetStrongPasswordScreen(
    email: String,
    onDone: (FirebaseUser) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val strength = passwordStrength(password)

    AuthPage {
        Text("أكمل حماية حسابك", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("وافق Google على الحساب: $email", color = FlosiMuted)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
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
        Card(colors = CardDefaults.cardColors(containerColor = FlosiPurpleSoft), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Requirement("12 حرفاً على الأقل", strength.length)
                Requirement("حرف إنكليزي صغير", strength.lower)
                Requirement("حرف إنكليزي كبير", strength.upper)
                Requirement("رقم واحد على الأقل", strength.digit)
                Requirement("رمز خاص مثل ! @ # $", strength.symbol)
            }
        }
        error?.let { Text(it, color = FlosiRed, modifier = Modifier.padding(top = 10.dp)) }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    busy = true; error = null
                    try {
                        if (password != confirm) error("كلمتا المرور غير متطابقتين")
                        onDone(FlosiCloudAuth.linkStrongPassword(context, password))
                    } catch (t: Throwable) {
                        error = t.message ?: "تعذر حفظ كلمة المرور"
                    } finally { busy = false }
                }
            },
            enabled = !busy && strength.strong && password == confirm,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FlosiPurple),
            shape = RoundedCornerShape(16.dp)
        ) { if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White) else Text("حفظ والدخول إلى Flosi") }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("إلغاء والعودة") }
    }
}

@Composable
private fun Requirement(label: String, ok: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.material3.Icon(
            imageVector = if (ok) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = null,
            tint = if (ok) FlosiGreen else FlosiMuted
        )
        Text("  $label", color = if (ok) FlosiGreen else FlosiMuted)
    }
}

@Composable
private fun ResetPasswordScreen(initialEmail: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }
    var busy by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AuthPage {
        Text("استعادة كلمة المرور", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("أدخل نفس البريد الذي وافقت عليه في Google. سيرسل Firebase رابط إعادة تعيين إلى ذلك البريد.", color = FlosiMuted)
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
                    try {
                        FlosiCloudAuth.sendReset(context, email)
                        sent = true
                    } catch (_: Throwable) {
                        error = "تعذر إرسال رسالة الاستعادة. تحقق من البريد والاتصال."
                    } finally { busy = false }
                }
            },
            enabled = !busy && email.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FlosiPurple),
            shape = RoundedCornerShape(16.dp)
        ) { if (busy) CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White) else Text("إرسال رابط الاستعادة") }
        if (sent) Text("تم طلب رسالة الاستعادة. افحص بريدك ثم ارجع وسجّل الدخول بكلمة المرور الجديدة.", color = FlosiGreen, modifier = Modifier.padding(top = 12.dp))
        error?.let { Text(it, color = FlosiRed, modifier = Modifier.padding(top = 12.dp)) }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("العودة لتسجيل الدخول") }
    }
}

@Composable
private fun AuthPage(content: @Composable Column.() -> Unit) {
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
