package com.flosi.app.ui.screens.settings

import android.app.Activity
import android.widget.Toast
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
import com.flosi.app.i18n.LocalFlosiLanguage
import com.flosi.app.ui.components.*

@Composable
fun BackupManagerScreen(onBack:()->Unit){
    val context=LocalContext.current;val app=context.applicationContext as FlosiApplication;val lang=LocalFlosiLanguage.current;fun s(ar:String,en:String)=if(lang=="ar")ar else en
    var password by remember{mutableStateOf("")};var status by remember{mutableStateOf("")};var success by remember{mutableStateOf(true)}
    val createBackup=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")){uri->uri?.let{runCatching{EncryptedBackupService.backup(context,app.database,it,password.toCharArray())}.onSuccess{status=s("تم إنشاء نسخة مشفرة AES-GCM بنجاح","AES-GCM encrypted backup created successfully");success=true;password=""}.onFailure{e->status=e.message?:s("فشل النسخ","Backup failed");success=false}}}
    val restoreBackup=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->uri?.let{runCatching{val bytes=EncryptedBackupService.restoreToTemp(context,it,password.toCharArray());EncryptedBackupService.applyRestore(context,app.database,bytes);app.reloadAfterRestore()}.onSuccess{success=true;password="";status=s("تم فحص النسخة واسترجاعها وإعادة فتح قاعدة البيانات.","Backup verified, restored, and database reopened.");Toast.makeText(context,s("تم استرجاع نسخة Flosi بنجاح","Flosi backup restored successfully"),Toast.LENGTH_LONG).show();(context as? Activity)?.recreate()}.onFailure{e->status=e.message?:s("فشل الاسترجاع","Restore failed");success=false}}}

    FlosiPage(s("إدارة النسخ الاحتياطية","Manage backups"),s("نسخ مشفرة مع فحص سلامة قبل الاسترجاع","Encrypted backups with integrity checks before restore"),onBack){
        OutlinedTextField(password,{password=it;status=""},Modifier.fillMaxWidth(),label={Text(s("كلمة مرور النسخة","Backup password"))},visualTransformation=PasswordVisualTransformation(),singleLine=true)
        Text(s("النسخة الجديدة تحتاج 8 أحرف/أرقام أو أكثر. لاسترجاع نسخ Flosi القديمة ما زال التطبيق يقبل كلمات المرور القديمة من 4 أحرف فأكثر.","New backups require at least 8 characters. Existing legacy Flosi backups can still be restored with their original 4+ character password."),color=FlosiMuted)
        Button(onClick={createBackup.launch("flosi-backup.flosi")},enabled=password.length>=EncryptedBackupService.NEW_BACKUP_MIN_PASSWORD,modifier=Modifier.fillMaxWidth()){Text(s("إنشاء نسخة مشفرة","Create encrypted backup"))}
        OutlinedButton(onClick={restoreBackup.launch(arrayOf("application/octet-stream","*/*"))},enabled=password.length>=EncryptedBackupService.LEGACY_RESTORE_MIN_PASSWORD,modifier=Modifier.fillMaxWidth()){Text(s("فحص واسترجاع نسخة","Verify and restore backup"))}
        if(status.isNotBlank())CardBox{Text(status,color=if(success)FlosiGreen else FlosiRed)}
        CardBox{Text(s("قبل استبدال بياناتك، Flosi يتحقق من ترويسة SQLite، سلامة quick_check، وجود جداول التطبيق الأساسية، وحدود أحجام الملف. الاستبدال يتم بملف مؤقت مع رجوع للنسخة السابقة إذا فشل التثبيت.","Before replacing data, Flosi verifies the SQLite header, quick_check integrity, required app tables, and file-size limits. Restore uses a temporary file and rolls back if installation fails."),color=FlosiMuted)}
        CardBox{Text(s("هذه النسخة تحفظ قاعدة بياناتك المالية. إعدادات الجهاز الحساسة مثل PIN والبصمة لا تُنسخ لأسباب أمنية.","This backup stores your financial database. Device-sensitive settings such as PIN and biometrics are intentionally not copied for security."),color=FlosiMuted)}
        CardBox{Text(s("من نافذة اختيار الملف تقدر تختار Google Drive مباشرة؛ ما يحتاج OAuth خاص للتخزين اليدوي عبر Android.","Google Drive can be chosen directly from Android's file picker; manual storage does not require separate OAuth."),color=FlosiMuted)}
    }
}
