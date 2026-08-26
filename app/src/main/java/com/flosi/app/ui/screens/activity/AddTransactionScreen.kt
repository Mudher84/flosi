package com.flosi.app.ui.screens.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flosi.app.i18n.FlosiActivityCopy
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.i18n.flosiText
import com.flosi.app.i18n.localizedLegacyText
import com.flosi.app.ui.components.*
import com.flosi.app.ui.viewmodel.EntryViewModel
import com.flosi.app.ui.viewmodel.flosiViewModel

@Composable
fun AddTransactionScreen(onBack:()->Unit,onPickAccount:()->Unit,onPickPerson:(String?)->Unit,onPickCategory:(String)->Unit,pickedAccountId:Long?=null,pickedPersonId:Long?=null,pickedCategoryId:Long?=null){
 val vm:EntryViewModel=flosiViewModel();val accounts by vm.accounts.collectAsState();val people by vm.people.collectAsState();val categories by vm.categories.collectAsState();val lang=LocalFlosiLanguage.current;fun c(key:String,vararg values:Pair<String,Any?>)=FlosiActivityCopy.text(lang,key,*values)
 var amount by remember{mutableStateOf("")};var title by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var kind by remember{mutableStateOf("expense")};var accountId by remember{mutableStateOf(0L)};var personId by remember{mutableStateOf<Long?>(null)};var categoryId by remember{mutableStateOf<Long?>(null)};var saving by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)}
 LaunchedEffect(accounts){if(accounts.none{it.id==accountId})accountId=accounts.firstOrNull()?.id?:0L};LaunchedEffect(pickedAccountId){if(pickedAccountId!=null&&accounts.any{it.id==pickedAccountId}){accountId=pickedAccountId;personId=null}};LaunchedEffect(pickedPersonId){if(pickedPersonId!=null)personId=pickedPersonId};LaunchedEffect(pickedCategoryId){if(pickedCategoryId!=null)categoryId=pickedCategoryId};LaunchedEffect(kind,categories){val selected=categories.firstOrNull{it.id==categoryId};if(selected!=null&&selected.kind!="both"&&selected.kind!=kind)categoryId=null}
 val selectedAccount=accounts.firstOrNull{it.id==accountId};val eligiblePeople=people.filter{selectedAccount==null||it.currency.equals(selectedAccount.currency,true)};LaunchedEffect(accountId,people){if(personId!=null&&eligiblePeople.none{it.id==personId})personId=null}
 val eligibleCategories=categories.filter{it.kind=="both"||when(kind){"income"->it.kind=="income";"expense"->it.kind=="expense";"debt_given","debt_received"->it.kind in setOf("both","expense","income");else->true}};val parsedAmount=amount.toLongOrNull();val canSave=!saving&&parsedAmount!=null&&parsedAmount>0&&title.isNotBlank()&&accountId>0
 FlosiPage(localizedLegacyText("إضافة حركة"),c("capture_seconds"),onBack){
  PremiumCard{
   Text(flosiText("amount"),color=Color.White.copy(alpha=.55f),fontSize=11.sp)
   Row(verticalAlignment=Alignment.Bottom){Text(if(amount.isBlank())"0" else amount,color=Color.White,fontSize=38.sp,fontWeight=FontWeight.Black,letterSpacing=(-1).sp);Spacer(Modifier.width(8.dp));Text(selectedAccount?.currency.orEmpty(),color=Color.White.copy(alpha=.55f),fontSize=12.sp,modifier=Modifier.padding(bottom=7.dp))}
   OutlinedTextField(amount,{value->amount=value.filter{it.isDigit()};error=null},Modifier.fillMaxWidth(),singleLine=true,placeholder={Text(c("enter_amount"))},colors=OutlinedTextFieldDefaults.colors(focusedTextColor=Color.White,unfocusedTextColor=Color.White,focusedBorderColor=Color.White.copy(alpha=.35f),unfocusedBorderColor=Color.White.copy(alpha=.14f),focusedContainerColor=Color.White.copy(alpha=.06f),unfocusedContainerColor=Color.White.copy(alpha=.04f),cursorColor=Color.White))
  }
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("expense" to flosiText("expense"),"income" to flosiText("income"),"debt_given" to c("loan"),"debt_received" to c("received")).forEach{(key,label)->FilterChip(kind==key,{kind=key;error=null},{Text(label)},modifier=Modifier.weight(1f))}}
  OutlinedTextField(title,{title=it;error=null},Modifier.fillMaxWidth(),label={Text(flosiText("description"))},singleLine=true,shape=RoundedCornerShape(18.dp))
  OutlinedTextField(note,{note=it;error=null},Modifier.fillMaxWidth(),label={Text(c("optional_note"))},shape=RoundedCornerShape(18.dp))
  SectionTitle(c("source"))
  CardBox{
   Text(flosiText("account"),fontWeight=FontWeight.ExtraBold)
   if(accounts.isEmpty()){Text(c("no_account"),color=FlosiRed);TextButton(onClick=onPickAccount){Text(c("add_account"))}}else{accounts.take(4).forEach{account->FilterChip(accountId==account.id,{accountId=account.id;personId=null;error=null},{Text("${account.name} • ${account.currency}")})};if(accounts.size>4)TextButton(onClick=onPickAccount){Text(c("all_accounts"))}}
  }
  SectionTitle(c("details"))
  CardBox{
   Text(c("person_optional"),fontWeight=FontWeight.ExtraBold);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(personId==null,{personId=null;error=null},{Text(c("none"))});eligiblePeople.take(3).forEach{person->FilterChip(personId==person.id,{personId=person.id;error=null},{Text(person.name)})}};if(eligiblePeople.size>3)TextButton(onClick={onPickPerson(selectedAccount?.currency)}){Text(c("view_all"))}
   HorizontalDivider(color=MaterialTheme.colorScheme.outline)
   Text(flosiText("category"),fontWeight=FontWeight.ExtraBold);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){eligibleCategories.take(4).forEach{category->FilterChip(categoryId==category.id,{categoryId=category.id;error=null},{Text(category.name)})}};if(eligibleCategories.size>4)TextButton(onClick={onPickCategory(kind)}){Text(c("all_categories"))}
  }
  error?.let{Surface(color=FlosiRed.copy(alpha=.09f),shape=RoundedCornerShape(16.dp)){Text(it,Modifier.padding(12.dp),color=FlosiRed)}}
  Button(onClick={val value=parsedAmount?:return@Button;saving=true;error=null;vm.save(kind,value,title.trim(),note.trim(),accountId,personId,categoryId){message->saving=false;if(message==null)onBack()else error=message}},enabled=canSave,modifier=Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(18.dp)){if(saving)CircularProgressIndicator(strokeWidth=2.dp,modifier=Modifier.size(22.dp))else Text(c("save_transaction"),fontWeight=FontWeight.ExtraBold,fontSize=15.sp)}
 }
}
