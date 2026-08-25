package com.flosi.app.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flosi.app.settings.FlosiPreferencesState
import com.flosi.app.ui.components.FlosiGreen
import com.flosi.app.ui.components.FlosiPurple
import com.flosi.app.ui.components.FlosiPurpleDeep
import com.flosi.app.ui.viewmodel.AccountsViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel
import com.flosi.app.ui.viewmodel.rememberFlosiPreferences
import kotlinx.coroutines.launch

@Composable
fun FlosiOnboardingGate(content:@Composable()->Unit){
    val prefs=rememberFlosiPreferences()
    val state by prefs.state.collectAsState(initial=FlosiPreferencesState())
    if(state.onboardingCompleted) content() else FlosiOnboardingScreen()
}

@Composable
private fun FlosiOnboardingScreen(){
    val prefs=rememberFlosiPreferences()
    val accountVm:AccountsViewModel=flosiViewModel()
    val accounts by accountVm.accounts.collectAsState()
    val state by prefs.state.collectAsState(initial=FlosiPreferencesState())
    val scope=rememberCoroutineScope()
    var step by remember{mutableIntStateOf(0)}
    var currency by remember(state.currency){mutableStateOf(state.currency)}
    var accountName by remember{mutableStateOf("")}
    var openingBalance by remember{mutableStateOf("")}
    var monthlyIncome by remember(state.expectedMonthlyIncome){mutableStateOf(if(state.expectedMonthlyIncome>0) state.expectedMonthlyIncome.toString() else "")}
    var saving by remember{mutableStateOf(false)}
    var error by remember{mutableStateOf<String?>(null)}
    val currencies=listOf("IQD","USD","SAR","AED","KWD","QAR","BHD","OMR","EUR","GBP")

    Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background){
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal=22.dp,vertical=18.dp)){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Text("Flosi",fontSize=30.sp,fontWeight=FontWeight.Black,color=MaterialTheme.colorScheme.onBackground,modifier=Modifier.weight(1f))
                Text("${step+1}/4",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=11.sp,fontWeight=FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(progress={((step+1)/4f).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth().height(7.dp),color=FlosiPurple,trackColor=FlosiPurple.copy(alpha=.10f),strokeCap=androidx.compose.ui.graphics.StrokeCap.Round)
            Spacer(Modifier.height(26.dp))

            when(step){
                0->WelcomeStep()
                1->AccountStep(currency,currencies,{currency=it},accountName,{accountName=it},openingBalance,{openingBalance=it})
                2->IncomeStep(currency,monthlyIncome,{monthlyIncome=it})
                else->ReadyStep(currency,accountName.ifBlank{accounts.firstOrNull()?.name.orEmpty()},monthlyIncome)
            }

            Spacer(Modifier.weight(1f))
            error?.let{Text(it,color=MaterialTheme.colorScheme.error,fontSize=11.sp,modifier=Modifier.padding(bottom=8.dp))}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                if(step>0) OutlinedButton(onClick={step--;error=null},modifier=Modifier.weight(1f).height(54.dp),shape=RoundedCornerShape(18.dp)){Text("رجوع")}
                Button(
                    onClick={
                        when(step){
                            0->step=1
                            1->{
                                if(accountName.isBlank()&&accounts.isEmpty()){error="اكتب اسم أول حساب";return@Button}
                                val balance=openingBalance.toLongOrNull()?:0L
                                saving=true;error=null
                                scope.launch{
                                    runCatching{prefs.setCurrency(currency)}.onFailure{error=it.message;saving=false;return@launch}
                                    if(accounts.isEmpty()){
                                        accountVm.add(accountName.ifBlank{"المحفظة"},"wallet",balance,currency){message->
                                            if(message!=null){error=message;saving=false}else{saving=false;step=2}
                                        }
                                    }else{saving=false;step=2}
                                }
                            }
                            2->{
                                val income=monthlyIncome.toLongOrNull()?:0L
                                saving=true;error=null
                                scope.launch{prefs.setExpectedMonthlyIncome(income);saving=false;step=3}
                            }
                            3->{
                                saving=true;scope.launch{prefs.setOnboardingCompleted(true);saving=false}
                            }
                        }
                    },
                    enabled=!saving,
                    modifier=Modifier.weight(if(step>0)1.4f else 1f).height(54.dp),
                    shape=RoundedCornerShape(18.dp)
                ){
                    if(saving) CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp) else Text(if(step==3)"ابدأ ويا Flosi" else "التالي",fontWeight=FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable private fun WelcomeStep(){
    Column(verticalArrangement=Arrangement.spacedBy(16.dp)){
        Box(Modifier.fillMaxWidth().height(230.dp).background(Brush.linearGradient(listOf(FlosiPurpleDeep,FlosiPurple)),RoundedCornerShape(34.dp)),contentAlignment=Alignment.Center){
            Column(horizontalAlignment=Alignment.CenterHorizontally){
                Box(Modifier.size(82.dp).background(Color.White.copy(alpha=.12f),CircleShape),contentAlignment=Alignment.Center){Text("ف",fontSize=38.sp,fontWeight=FontWeight.Black,color=Color.White)}
                Spacer(Modifier.height(16.dp));Text("فلوسك أوضح من اليوم",color=Color.White,fontSize=23.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(6.dp));Text("إدارة ذكية • خصوصية • بدون تعقيد",color=Color.White.copy(alpha=.72f),fontSize=11.sp)
            }
        }
        Text("خل نجهز Flosi إلك",fontSize=25.sp,fontWeight=FontWeight.Black)
        Text("خلال أقل من دقيقة نحدد عملتك، أول حساب، ودخلك الشهري حتى تكون الصفحة الرئيسية مفيدة من أول فتح.",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=13.sp,lineHeight=20.sp)
    }
}

@Composable private fun AccountStep(currency:String,currencies:List<String>,onCurrency:(String)->Unit,accountName:String,onName:(String)->Unit,balance:String,onBalance:(String)->Unit){
    Column(verticalArrangement=Arrangement.spacedBy(14.dp)){
        Text("أولاً، وين تخلي فلوسك؟",fontSize=25.sp,fontWeight=FontWeight.Black)
        Text("اختَر العملة الأساسية وأنشئ أول محفظة أو حساب.",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=12.sp)
        Text("العملة",fontWeight=FontWeight.Bold)
        Column(verticalArrangement=Arrangement.spacedBy(8.dp)){currencies.chunked(5).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){row.forEach{code->FilterChip(selected=currency==code,onClick={onCurrency(code)},label={Text(code)},modifier=Modifier.weight(1f))}}}}
        OutlinedTextField(accountName,onName,Modifier.fillMaxWidth(),label={Text("اسم الحساب")},placeholder={Text("مثلاً: الكاش أو الرافدين")},singleLine=true,shape=RoundedCornerShape(18.dp))
        OutlinedTextField(balance,{onBalance(it.filter(Char::isDigit))},Modifier.fillMaxWidth(),label={Text("الرصيد الحالي — اختياري")},suffix={Text(currency)},singleLine=true,shape=RoundedCornerShape(18.dp))
    }
}

@Composable private fun IncomeStep(currency:String,income:String,onIncome:(String)->Unit){
    Column(verticalArrangement=Arrangement.spacedBy(16.dp)){
        Text("شنو دخلك الشهري التقريبي؟",fontSize=25.sp,fontWeight=FontWeight.Black)
        Text("هذا مو شرط يكون راتب فقط. نستخدمه حتى Flosi يحسب نسبة الصرف ومساحتك الآمنة بشكل أذكى.",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=12.sp,lineHeight=19.sp)
        OutlinedTextField(income,{onIncome(it.filter(Char::isDigit))},Modifier.fillMaxWidth(),label={Text("الدخل الشهري")},suffix={Text(currency)},singleLine=true,shape=RoundedCornerShape(20.dp))
        Surface(color=FlosiPurple.copy(alpha=.08f),shape=RoundedCornerShape(22.dp)){Text("تگدر تخليها صفر وتضيفها من الإعدادات بأي وقت.",Modifier.padding(16.dp),color=FlosiPurpleDeep,fontSize=11.sp,fontWeight=FontWeight.SemiBold)}
    }
}

@Composable private fun ReadyStep(currency:String,accountName:String,income:String){
    Column(verticalArrangement=Arrangement.spacedBy(16.dp)){
        Text("جاهزين ✦",fontSize=28.sp,fontWeight=FontWeight.Black)
        Text("Flosi صار مهيأ إلك. من هسه كل حركة تدخلها راح تبني صورة أوضح عن وضعك المالي.",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=13.sp,lineHeight=20.sp)
        SummaryRow("العملة الأساسية",currency)
        SummaryRow("أول حساب",accountName.ifBlank{"تم إنشاؤه"})
        SummaryRow("الدخل الشهري",if(income.isBlank()||income=="0")"غير محدد" else "$income $currency")
        Surface(color=FlosiGreen.copy(alpha=.09f),shape=RoundedCornerShape(22.dp)){Text("الخطوة الجاية: سجّل أول حركة وخلي Flosi يبدي يفهم نمط صرفك.",Modifier.padding(16.dp),color=FlosiGreen,fontSize=11.sp,fontWeight=FontWeight.Bold)}
    }
}

@Composable private fun SummaryRow(label:String,value:String){Surface(color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(20.dp),tonalElevation=1.dp){Row(Modifier.fillMaxWidth().padding(16.dp)){Text(label,Modifier.weight(1f),color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=11.sp);Text(value,fontWeight=FontWeight.ExtraBold,fontSize=12.sp)}}}
