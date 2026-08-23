package com.flosi.app.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.flosi.app.FlosiApplication
import com.flosi.app.backup.EncryptedBackupService
import com.flosi.app.ui.components.*

@Composable
fun BackupManagerScreen(onBack:()->Unit){
    val context=LocalContext.current
    val app=context.applicationContext as FlosiApplication
    var password by remember{mutableStateOf("")}
    var status by remember{mutableStateOf("")}
    var success by remember{mutableStateOf(true)}

    val createBackup=rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ){uri->
        uri?.let{
            runCatching{EncryptedBackupService.backup(context,app.database,it,password.toCharArray())}
                .onSuccess{
                    status="تم إنشاء نسخة مشفرة AES-GCM بنجاح"
                    success=true
                    password=""
                }
                .onFailure{e->
                    status=e.message?:"فشل النسخ"
                    success=false
                }
        }
    }

    val restoreBackup=rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ){uri->
        uri?.let{
            runCatching{
                val bytes=EncryptedBackupService.restoreToTemp(context,it,password.toCharArray())
                EncryptedBackupService.applyRestore(context,app.database,bytes)
            }.onSuccess{
                status="تم فك النسخة وفحص SQLite وquick_check والجداول ثم الاسترجاع بأمان. أغلق التطبيق وافتحه من جديد."
                success=true
                password=""
            }.onFailure{e->
                status=e.message?:"فشل الاسترجاع"
                success=false
            }
        }
    }

    FlosiPage("إدارة النسخ الاحتياطية","نسخ مشفرة مع فحص سلامة قبل الاسترجاع",onBack){
        OutlinedTextField(
            value=password,
            onValueChange={password=it;status=""},
            modifier=Modifier.fillMaxWidth(),
            label={Text("كلمة مرور النسخة")},
            visualTransformation=PasswordVisualTransformation(),
            singleLine=true
        )
        Text("استخدم 4 أحرف/أرقام أو أكثر، ولا تنسَها؛ بدونها لا يمكن فك النسخة.",color=FlosiMuted)
        Button(
            onClick={createBackup.launch("flosi-backup.flosi")},
            enabled=password.length>=4,
            modifier=Modifier.fillMaxWidth()
        ){Text("إنشاء نسخة مشفرة")}
        OutlinedButton(
            onClick={restoreBackup.launch(arrayOf("application/octet-stream","*/*"))},
            enabled=password.length>=4,
            modifier=Modifier.fillMaxWidth()
        ){Text("فحص واسترجاع نسخة")}
        if(status.isNotBlank())CardBox{Text(status,color=if(success)FlosiGreen else FlosiRed)}
        CardBox{
            Text("قبل استبدال بياناتك، Flosi يتحقق من ترويسة SQLite، سلامة quick_check، وجود جداول التطبيق الأساسية، وحدود أحجام الملف. الاستبدال يتم بملف مؤقت مع رجوع للنسخة السابقة إذا فشل التثبيت.",color=FlosiMuted)
        }
        CardBox{Text("من نافذة اختيار الملف تقدر تختار Google Drive مباشرة؛ ما يحتاج OAuth خاص للتخزين اليدوي عبر Android.",color=FlosiMuted)}
    }
}
