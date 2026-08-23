package com.flosi.app.auth

import android.content.Context
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
import com.flosi.app.i18n.LocalFlosiLanguage
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

private val Purple=Color(0xFF7B44EF);private val PurpleSoft=Color(0xFFF5F0FF);private val TextMain=Color(0xFF17131F);private val Muted=Color(0xFF8F8798);private val Green=Color(0xFF18B97D);private val Red=Color(0xFFE84C61)

private object CloudAuth {
    fun configured()=BuildConfig.FLOSI_FIREBASE_API_KEY.isNotBlank()&&BuildConfig.FLOSI_FIREBASE_APP_ID.isNotBlank()&&BuildConfig.FLOSI_FIREBASE_PROJECT_ID.isNotBlank()&&BuildConfig.FLOSI_GOOGLE_WEB_CLIENT_ID.isNotBlank()
    fun auth(context:Context):FirebaseAuth{
        val app=FirebaseApp.getApps(context).firstOrNull{it.name=="flosi-auth"}?:run{
            check(configured()){ "Google/Firebase configuration is incomplete" }
            val options=FirebaseOptions.Builder().setApiKey(BuildConfig.FLOSI_FIREBASE_API_KEY).setApplicationId(BuildConfig.FLOSI_FIREBASE_APP_ID).setProjectId(BuildConfig.FLOSI_FIREBASE_PROJECT_ID).build()
            FirebaseApp.initializeApp(context,options,"flosi-auth")?:error("Could not initialize Flosi authentication")
        }
        return FirebaseAuth.getInstance(app)
    }
    fun hasPassword(user:FirebaseUser?)=user?.providerData?.any{it.providerId==EmailAuthProvider.PROVIDER_ID}==true
    suspend fun google(context:Context):FirebaseUser{
        val option=GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false).setServerClientId(BuildConfig.FLOSI_GOOGLE_WEB_CLIENT_ID).setAutoSelectEnabled(false).build()
        val request=GetCredentialRequest.Builder().addCredentialOption(option).build();val credential=CredentialManager.create(context).getCredential(context,request).credential
        require(credential is CustomCredential&&credential.type==GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){"Google did not return a valid sign-in credential"}
        val token=GoogleIdTokenCredential.createFrom(credential.data).idToken;val result=auth(context).signInWithCredential(GoogleAuthProvider.getCredential(token,null)).await()
        return result.user?:error("Could not create a Flosi session")
    }
    suspend fun linkPassword(context:Context,password:String):FirebaseUser{
        val auth=auth(context);val user=auth.currentUser?:error("Sign in with Google first");val email=user.email?:error("Google account has no valid email")
        if(hasPassword(user)){user.updatePassword(password).await();user.reload().await();return auth.currentUser?:user}
        return user.linkWithCredential(EmailAuthProvider.getCredential(email,password)).await().user?:user
    }
    suspend fun passwordLogin(context:Context,email:String,password:String):FirebaseUser=auth(context).signInWithEmailAndPassword(email.trim(),password).await().user?:error("Sign-in failed")
    suspend fun reset(context:Context,email:String){auth(context).sendPasswordResetEmail(email.trim()).await()}
}

private data class Strength(val len:Boolean,val lower:Boolean,val upper:Boolean,val digit:Boolean,val symbol:Boolean){val strong get()=len&&lower&&upper&&digit&&symbol}
private fun strength(value:String)=Strength(value.length>=12,value.any(Char::isLowerCase),value.any(Char::isUpperCase),value.any(Char::isDigit),value.any{!it.isLetterOrDigit()&&!it.isWhitespace()})
private enum class Stage{LOGIN,SET_PASSWORD,RESET}

@Composable
fun FlosiAuthGate(content:@Composable()->Unit){
    val context=LocalContext.current
    if(!CloudAuth.configured()){MissingConfig();return}
    val auth=remember{CloudAuth.auth(context)};var user by remember{mutableStateOf(auth.currentUser)};var stage by remember{mutableStateOf(if(user!=null&&!CloudAuth.hasPassword(user))Stage.SET_PASSWORD else Stage.LOGIN)}
    DisposableEffect(auth){val listener=FirebaseAuth.AuthStateListener{user=it.currentUser;if(user==null)stage=Stage.LOGIN else if(!CloudAuth.hasPassword(user))stage=Stage.SET_PASSWORD};auth.addAuthStateListener(listener);onDispose{auth.removeAuthStateListener(listener)}}
    if(user!=null&&CloudAuth.hasPassword(user)){content();return}
    when(stage){
        Stage.LOGIN->Login(onGoogle={user=it;stage=if(CloudAuth.hasPassword(it))Stage.LOGIN else Stage.SET_PASSWORD},onPassword={user=it},onReset={stage=Stage.RESET})
        Stage.SET_PASSWORD->SetPassword(user?.email.orEmpty(),{user=it}){auth.signOut();user=null;stage=Stage.LOGIN}
        Stage.RESET->ResetPassword(user?.email.orEmpty()){stage=Stage.LOGIN}
    }
}

@Composable private fun tr(ar:String,en:String)=if(LocalFlosiLanguage.current=="ar")ar else en

@Composable
private fun MissingConfig()=AuthPage{
    Text("Flosi",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp))
    Text(tr("نظام تسجيل Google جاهز، لكنه يحتاج بيانات Firebase وGoogle OAuth الخاصة بالتطبيق.","Google sign-in is ready but requires this app's Firebase and Google OAuth configuration."),color=Muted)
    Spacer(Modifier.height(12.dp));Text(tr("Flosi لا يفتح الحساب بدون مصادقة صحيحة.","Flosi never opens an account without valid authentication."),color=Red)
}

@Composable
private fun Login(onGoogle:(FirebaseUser)->Unit,onPassword:(FirebaseUser)->Unit,onReset:()->Unit){
    val context=LocalContext.current;val scope=rememberCoroutineScope();var email by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
    val lang=LocalFlosiLanguage.current;fun s(ar:String,en:String)=if(lang=="ar")ar else en
    AuthPage{
        Text("Flosi",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold,color=TextMain);Text(s("أموالك، بحساب موثّق","Your money, with a verified account"),color=Muted);Spacer(Modifier.height(22.dp))
        Button(onClick={scope.launch{busy=true;error=null;try{onGoogle(CloudAuth.google(context))}catch(_:GetCredentialException){error=s("تم إلغاء Google أو تعذر إكماله.","Google sign-in was cancelled or could not be completed.")}catch(t:Throwable){error=t.message?:s("تعذر تسجيل الدخول عبر Google","Could not sign in with Google")}finally{busy=false}}},enabled=!busy,modifier=Modifier.fillMaxWidth().height(52.dp),colors=ButtonDefaults.buttonColors(containerColor=Color.White,contentColor=TextMain),shape=RoundedCornerShape(16.dp)){Text("G   ${s("المتابعة باستخدام Google","Continue with Google")}",fontWeight=FontWeight.Bold)}
        Row(Modifier.fillMaxWidth().padding(vertical=16.dp),verticalAlignment=Alignment.CenterVertically){HorizontalDivider(Modifier.weight(1f));Text("  ${s("أو","or")}  ",color=Muted);HorizontalDivider(Modifier.weight(1f))}
        OutlinedTextField(email,{email=it},label={Text(s("بريد Google","Google email"))},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp));Spacer(Modifier.height(10.dp))
        OutlinedTextField(password,{password=it},label={Text(s("كلمة المرور","Password"))},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp))
        TextButton(onClick=onReset,modifier=Modifier.align(Alignment.Start)){Text(s("نسيت كلمة المرور؟","Forgot password?"),color=Purple)}
        Button(onClick={scope.launch{busy=true;error=null;try{onPassword(CloudAuth.passwordLogin(context,email,password))}catch(_:Throwable){error=s("البريد أو كلمة المرور غير صحيحة.","Email or password is incorrect.")}finally{busy=false}}},enabled=!busy&&email.isNotBlank()&&password.isNotBlank(),modifier=Modifier.fillMaxWidth().height(52.dp),colors=ButtonDefaults.buttonColors(containerColor=Purple),shape=RoundedCornerShape(16.dp)){if(busy)CircularProgressIndicator(strokeWidth=2.dp,color=Color.White)else Text(s("تسجيل الدخول","Sign in"))}
        error?.let{Text(it,color=Red,modifier=Modifier.padding(top=12.dp))};Spacer(Modifier.height(14.dp));Text(s("الحساب الجديد يبدأ بالموافقة من Google، ثم يطلب Flosi كلمة مرور قوية لنفس البريد.","A new account starts with Google approval, then Flosi asks for a strong password for the same email."),color=Muted)
    }
}

@Composable
private fun SetPassword(email:String,onDone:(FirebaseUser)->Unit,onCancel:()->Unit){
    val context=LocalContext.current;val scope=rememberCoroutineScope();var pass by remember{mutableStateOf("")};var confirm by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)};val strength=strength(pass);val lang=LocalFlosiLanguage.current;fun s(ar:String,en:String)=if(lang=="ar")ar else en
    AuthPage{
        Text(s("أكمل حماية حسابك","Complete account protection"),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(s("وافق Google على: $email","Google approved: $email"),color=Muted);Spacer(Modifier.height(18.dp))
        OutlinedTextField(pass,{pass=it},label={Text(s("كلمة مرور قوية","Strong password"))},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp));Spacer(Modifier.height(10.dp))
        OutlinedTextField(confirm,{confirm=it},label={Text(s("تأكيد كلمة المرور","Confirm password"))},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp));Spacer(Modifier.height(14.dp))
        Card(colors=CardDefaults.cardColors(containerColor=PurpleSoft),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Rule(s("12 حرفاً على الأقل","At least 12 characters"),strength.len);Rule(s("حرف صغير","Lowercase letter"),strength.lower);Rule(s("حرف كبير","Uppercase letter"),strength.upper);Rule(s("رقم واحد على الأقل","At least one number"),strength.digit);Rule(s("رمز خاص مثل ! @ # $","Special symbol such as ! @ # $"),strength.symbol)}}
        error?.let{Text(it,color=Red,modifier=Modifier.padding(top=10.dp))};Spacer(Modifier.height(16.dp))
        Button(onClick={scope.launch{busy=true;error=null;try{require(pass==confirm){s("كلمتا المرور غير متطابقتين","Passwords do not match")};onDone(CloudAuth.linkPassword(context,pass))}catch(t:Throwable){error=t.message?:s("تعذر حفظ كلمة المرور","Could not save password")}finally{busy=false}}},enabled=!busy&&strength.strong&&pass==confirm,modifier=Modifier.fillMaxWidth().height(52.dp),colors=ButtonDefaults.buttonColors(containerColor=Purple),shape=RoundedCornerShape(16.dp)){if(busy)CircularProgressIndicator(strokeWidth=2.dp,color=Color.White)else Text(s("حفظ والدخول إلى Flosi","Save and enter Flosi"))}
        TextButton(onClick=onCancel,modifier=Modifier.fillMaxWidth()){Text(s("إلغاء والعودة","Cancel and return"))}
    }
}

@Composable private fun Rule(label:String,ok:Boolean){Row(verticalAlignment=Alignment.CenterVertically){Icon(if(ok)Icons.Default.CheckCircle else Icons.Default.Lock,null,tint=if(ok)Green else Muted);Text("  $label",color=if(ok)Green else Muted)}}

@Composable
private fun ResetPassword(initialEmail:String,onBack:()->Unit){
    val context=LocalContext.current;val scope=rememberCoroutineScope();var email by remember(initialEmail){mutableStateOf(initialEmail)};var busy by remember{mutableStateOf(false)};var sent by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)};val lang=LocalFlosiLanguage.current;fun s(ar:String,en:String)=if(lang=="ar")ar else en
    AuthPage{
        Text(s("استعادة كلمة المرور","Reset password"),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(s("استخدم نفس بريد Google المرتبط بحساب Flosi. سيصلك رابط إعادة التعيين على البريد نفسه.","Use the same Google email linked to Flosi. The reset link will be sent to that email."),color=Muted);Spacer(Modifier.height(18.dp))
        OutlinedTextField(email,{email=it},label={Text(s("بريد Google","Google email"))},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp));Spacer(Modifier.height(12.dp))
        Button(onClick={scope.launch{busy=true;error=null;try{CloudAuth.reset(context,email);sent=true}catch(_:Throwable){error=s("تعذر إرسال رسالة الاستعادة. تحقق من البريد والاتصال.","Could not send the reset email. Check the address and connection.")}finally{busy=false}}},enabled=!busy&&email.isNotBlank(),modifier=Modifier.fillMaxWidth().height(52.dp),colors=ButtonDefaults.buttonColors(containerColor=Purple),shape=RoundedCornerShape(16.dp)){if(busy)CircularProgressIndicator(strokeWidth=2.dp,color=Color.White)else Text(s("إرسال رابط الاستعادة","Send reset link"))}
        if(sent)Text(s("تم طلب رسالة الاستعادة. افحص بريدك ثم سجّل الدخول بكلمة المرور الجديدة.","Reset email requested. Check your inbox, then sign in with the new password."),color=Green,modifier=Modifier.padding(top=12.dp));error?.let{Text(it,color=Red,modifier=Modifier.padding(top=12.dp))};TextButton(onClick=onBack,modifier=Modifier.fillMaxWidth()){Text(s("العودة لتسجيل الدخول","Back to sign in"))}
    }
}

@Composable
private fun AuthPage(content:@Composable ColumnScope.()->Unit){
    Surface(Modifier.fillMaxSize(),color=Color(0xFFF7F6FB)){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Card(modifier=Modifier.fillMaxWidth().padding(18.dp),shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(defaultElevation=8.dp)){Column(modifier=Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally,content=content)}}}
}
