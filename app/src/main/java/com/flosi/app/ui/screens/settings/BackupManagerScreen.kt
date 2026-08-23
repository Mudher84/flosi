package com.flosi.app.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.flosi.app.FlosiApplication
import com.flosi.app.backup.EncryptedBackupService
import com.flosi.app.ui.components.*

@Composable
fun BackupManagerScreen(onBack:()->Unit){
    val context=LocalContext.current
    val app=context.applicationContext as FlosiApplication
    var password by remember{mutableStateOf("")}
    var status by remember{mutableStateOf("")}

    val createBackup=rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ){uri->
        uri?.let{
            runCatching{EncryptedBackupService.backup(context,app.database,it,password.toCharArray())}
                .onSuccess{status="تم إنشاء نسخة احتياطية مشفرة"}
                .onFailure{e->status=e.message?:"فشل النسخ"}
        }
    }

    val restoreBackup=rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ){uri->
        uri?.let{
            runCatching{
                val bytes=EncryptedBackupService.restoreToTemp(context,it,password.toCharArray())
                EncryptedBackupService.applyRestore(context,app.database,bytes)
            }.onSuccess{status="تم الاسترجاع. أغلق التطبيق وافتحه من جديد."}
             .onFailure{e->status=e.message?:"فشل الاسترجاع"}
        }
    }

    FlosiPage("إدارة النسخ الاحتياطية","مشفر ويمكن حفظه في Google Drive",onBack){
        OutlinedTextField(password,{password=it},Modifier.fillMaxWidth(),label={Text("كلمة مرور النسخة")})
        Text("استخدم 4 أحرف/أرقام أو أكثر، ولا تنسَها؛ بدونها لا يمكن فك النسخة.",color=FlosiMuted)
        Button(onClick={createBackup.launch("flosi-backup.flosi")},enabled=password.length>=4,modifier=Modifier.fillMaxWidth()){Text("إنشاء نسخة مشفرة")}
        OutlinedButton(onClick={restoreBackup.launch(arrayOf("application/octet-stream","*/*"))},enabled=password.length>=4,modifier=Modifier.fillMaxWidth()){Text("استرجاع نسخة")}
        if(status.isNotBlank())CardBox{Text(status)}
        CardBox{Text("من نافذة اختيار الملف تقدر تختار Google Drive مباشرة؛ ما يحتاج OAuth خاص للتخزين اليدوي عبر Android.",color=FlosiMuted)}
    }
}
