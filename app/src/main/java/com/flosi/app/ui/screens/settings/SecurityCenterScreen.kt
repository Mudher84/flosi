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
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.security.AppSecurity
import com.flosi.app.ui.components.*

@Composable
fun SecurityCenterScreen(onBack:()->Unit,onBackups:()->Unit,onSecurityChanged:()->Unit={}){
    val context=androidx.compose.ui.platform.LocalContext.current;val lang=LocalFlosiLanguage.current;fun s(ar:String,en:String)=if(lang=="ar")ar else en
    var biometric by remember{mutableStateOf(AppSecurity.biometricEnabled(context))};var hasPin by remember{mutableStateOf(AppSecurity.hasPin(context))};var secureScreen by remember{mutableStateOf(AppSecurity.screenSecureEnabled(context))};var autoLock by remember{mutableIntStateOf(AppSecurity.autoLockSeconds(context))};var showPinDialog by remember{mutableStateOf(false)};var message by remember{mutableStateOf<String?>(null)}
    val biometricAvailable=AppSecurity.biometricStatus(context)==BiometricManager.BIOMETRIC_SUCCESS;val activeLayers=listOf(hasPin,biometric,secureScreen).count{it}
    val protectionLabel=if(lang=="ar")when{activeLayers>=3->"حماية قوية";activeLayers==2->"حماية جيدة";activeLayers==1->"حماية أساسية";else->"بدون قفل دخول"}else when{activeLayers>=3->"Strong protection";activeLayers==2->"Good protection";activeLayers==1->"Basic protection";else->"No app lock"}

    FlosiPage(s("أمان Flosi","Flosi security"),flosiText("security_sub"),onBack){
        CardBox{Metric(s("مستوى الحماية","Protection level"),protectionLabel,if(activeLayers>=2)FlosiGreen else FlosiOrange);Text(s("فعّل فقط ما يناسبك. PIN والبصمة/الوجه خيارات مستقلة وليست إلزامية.","Enable only what suits you. PIN and biometrics are independent and optional."),color=FlosiMuted)}
        SectionTitle(s("فتح التطبيق","App access"))
        CardBox{
            SecuritySwitchRow(flosiText("pin_6"),if(hasPin)s("مفعّل ويمكن تغييره متى تريد","Enabled — change it anytime")else s("اختياري — فعّله إذا تريد قفل رقمي سريع","Optional — enable it for a quick numeric lock"),hasPin){enabled->if(enabled)showPinDialog=true else{AppSecurity.clearPin(context);hasPin=false;message=s("تم إيقاف PIN","PIN disabled");onSecurityChanged()}}
            if(hasPin){HorizontalDivider(color=FlosiLine);TextButton(onClick={showPinDialog=true}){Text(s("تغيير PIN","Change PIN"))}}
            HorizontalDivider(color=FlosiLine)
            SecuritySwitchRow(s("البصمة أو الوجه","Fingerprint or face"),if(biometricAvailable)s("اختياري — يستخدم القياسات الحيوية المسجلة في الجهاز","Optional — uses biometrics registered on this device")else s("غير متاحة حالياً على هذا الجهاز","Not currently available on this device"),biometric,biometricAvailable||biometric){enabled->val error=runCatching{AppSecurity.setBiometricEnabled(context,enabled)}.exceptionOrNull()?.message;if(error!=null)message=error else{biometric=enabled;message=if(enabled)s("تم تفعيل البصمة أو الوجه","Biometrics enabled")else s("تم إيقاف البصمة أو الوجه","Biometrics disabled");onSecurityChanged()}}
        }
        SectionTitle(s("القفل والخصوصية","Lock & privacy"))
        CardBox{
            Text(flosiText("auto_lock"),color=FlosiText);Text(s("يعمل فقط عند تفعيل PIN أو البصمة/الوجه.","Works only when PIN or biometrics are enabled."),color=FlosiMuted);Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(7.dp),modifier=Modifier.fillMaxWidth()){listOf(0 to s("فوراً","Now"),30 to s("30ث","30s"),60 to s("1د","1m"),300 to s("5د","5m")).forEach{(seconds,label)->FilterChip(autoLock==seconds,{AppSecurity.setAutoLockSeconds(context,seconds);autoLock=seconds;message=s("تم حفظ مدة القفل التلقائي","Auto-lock duration saved");onSecurityChanged()},{Text(label)},modifier=Modifier.weight(1f))}}
            HorizontalDivider(color=FlosiLine,modifier=Modifier.padding(vertical=8.dp))
            SecuritySwitchRow(flosiText("screen_protection"),s("يمنع تصوير الشاشة ويخفي المحتوى من التطبيقات الأخيرة","Blocks screenshots and hides content from Recent Apps"),secureScreen){AppSecurity.setScreenSecureEnabled(context,it);secureScreen=it;message=if(it)s("تم تفعيل حماية الشاشة","Screen protection enabled")else s("تم إيقاف حماية الشاشة","Screen protection disabled");onSecurityChanged()}
        }
        SectionTitle(s("البيانات","Data"));CardBox{ActionRow(s("النسخ الاحتياطية المشفرة","Encrypted backups"),s("إدارة النسخ والاستعادة بأمان","Manage backup and restore securely"),s("إدارة","Manage"),FlosiPurple,onBackups)}
        message?.let{Surface(color=FlosiPurpleSoft,shape=MaterialTheme.shapes.large,modifier=Modifier.fillMaxWidth()){Text(it,modifier=Modifier.padding(12.dp),color=FlosiPurple)}}
        if(hasPin||biometric)Button(onClick={AppSecurity.lockNow();message=s("سيظهر القفل عند الرجوع أو إعادة فتح التطبيق.","The lock will appear when you return to or reopen the app.");onSecurityChanged()},modifier=Modifier.fillMaxWidth()){Text(s("قفل Flosi الآن","Lock Flosi now"))}
    }
    if(showPinDialog)PinSetupDialog({showPinDialog=false}){pin->val error=runCatching{AppSecurity.setPin(context,pin)}.exceptionOrNull()?.message;if(error==null){hasPin=true;showPinDialog=false;message=s("تم حفظ PIN المكوّن من 6 أرقام","6-digit PIN saved");onSecurityChanged()}else message=error}
}

@Composable
fun FlosiLockScreen(onUnlocked:()->Unit){
    val context=androidx.compose.ui.platform.LocalContext.current;val activity=remember(context){context.findFragmentActivity()};val lang=LocalFlosiLanguage.current;fun s(ar:String,en:String)=if(lang=="ar")ar else en
    var pin by remember{mutableStateOf("")};var error by remember{mutableStateOf<String?>(null)};val biometricEnabled=AppSecurity.biometricEnabled(context);val biometricAvailable=AppSecurity.biometricAvailable(context);val hasPin=AppSecurity.hasPin(context);var prompted by remember{mutableStateOf(false)}
    fun unlockWithBiometric(){val host=activity;if(host==null){error=if(hasPin)s("تعذر تشغيل البصمة. استخدم PIN.","Could not start biometrics. Use PIN.")else s("تعذر تشغيل البصمة أو الوجه.","Could not start biometrics.");return};AppSecurity.authenticateBiometric(host,{error=null;onUnlocked()},{error=it},{error=null})}
    LaunchedEffect(biometricEnabled,biometricAvailable){if(biometricEnabled&&biometricAvailable&&!prompted){prompted=true;unlockWithBiometric()}}
    Surface(color=FlosiBg,modifier=Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){
        Surface(color=FlosiPurpleSoft,shape=MaterialTheme.shapes.extraLarge){Text("◆",modifier=Modifier.padding(horizontal=22.dp,vertical=16.dp),color=FlosiPurple)};Spacer(Modifier.height(18.dp));Text(s("Flosi مقفول","Flosi is locked"),style=MaterialTheme.typography.headlineSmall,color=FlosiText);Spacer(Modifier.height(6.dp));Text(s("تحقق قبل عرض بياناتك المالية","Verify before viewing your financial data"),color=FlosiMuted);Spacer(Modifier.height(22.dp))
        if(hasPin){OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(6);error=null},Modifier.fillMaxWidth(),label={Text(flosiText("pin_6"))},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),visualTransformation=PasswordVisualTransformation(),singleLine=true);Spacer(Modifier.height(10.dp));Button(onClick={if(AppSecurity.verifyPin(context,pin)){AppSecurity.markUnlocked(context);pin="";error=null;onUnlocked()}else error=s("PIN غير صحيح","Incorrect PIN")},enabled=pin.length==6,modifier=Modifier.fillMaxWidth()){Text(s("فتح","Unlock"))}}
        if(biometricEnabled){Spacer(Modifier.height(8.dp));OutlinedButton(onClick=::unlockWithBiometric,enabled=biometricAvailable,modifier=Modifier.fillMaxWidth()){Text(if(biometricAvailable)s("استخدام البصمة أو الوجه","Use fingerprint or face")else s("القياسات الحيوية غير متاحة","Biometrics unavailable"))}}
        if(!hasPin&&biometricEnabled&&!biometricAvailable){Spacer(Modifier.height(12.dp));Text(s("القياسات الحيوية المفعّلة غير متاحة حالياً على الجهاز.","Enabled biometrics are currently unavailable on this device."),color=FlosiRed)};error?.let{Spacer(Modifier.height(12.dp));Text(it,color=FlosiRed)}
    }}
}

@Composable private fun SecuritySwitchRow(title:String,subtitle:String,checked:Boolean,enabled:Boolean=true,onCheckedChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f),horizontalAlignment=Alignment.Start){Text(title,color=FlosiText);Text(subtitle,color=FlosiMuted)};Switch(checked,onCheckedChange,enabled=enabled)}}

@Composable private fun PinSetupDialog(onDismiss:()->Unit,onSave:(String)->Unit){
    val lang=LocalFlosiLanguage.current;fun s(ar:String,en:String)=if(lang=="ar")ar else en;var pin by remember{mutableStateOf("")};var confirm by remember{mutableStateOf("")};val valid=pin.matches(Regex("\\d{6}"))&&pin==confirm
    AlertDialog(onDismissRequest=onDismiss,title={Text(s("تعيين PIN","Set PIN"))},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Text(s("أدخل 6 أرقام فقط. PIN اختياري ويمكن إيقافه لاحقاً من صفحة الأمان.","Enter exactly 6 digits. PIN is optional and can be disabled later."),color=FlosiMuted);OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(6)},label={Text(s("PIN الجديد","New PIN"))},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),visualTransformation=PasswordVisualTransformation(),singleLine=true);OutlinedTextField(confirm,{confirm=it.filter(Char::isDigit).take(6)},label={Text(s("تأكيد PIN","Confirm PIN"))},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword),visualTransformation=PasswordVisualTransformation(),singleLine=true);if(confirm.isNotEmpty()&&pin!=confirm)Text(s("الرمزان غير متطابقين","PINs do not match"),color=FlosiRed)}},confirmButton={Button(onClick={onSave(pin)},enabled=valid){Text(flosiText("save"))}},dismissButton={TextButton(onClick=onDismiss){Text(flosiText("cancel"))}})
}

private tailrec fun Context.findFragmentActivity():FragmentActivity?=when(this){is FragmentActivity->this;is ContextWrapper->baseContext.findFragmentActivity();else->null}
