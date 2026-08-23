package com.flosi.app.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.flosi.app.data.local.FlosiDatabase
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptedBackupService {
    private val MAGIC="FLOSI_BACKUP_V1".toByteArray(Charsets.UTF_8)
    private val SQLITE_HEADER="SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
    private const val ITERATIONS=180_000
    private const val SALT_SIZE=16
    private const val IV_SIZE=12
    private const val MAX_DATABASE_BYTES=512*1024*1024
    const val NEW_BACKUP_MIN_PASSWORD=8
    const val LEGACY_RESTORE_MIN_PASSWORD=4

    private val requiredTables=setOf(
        "accounts","people","categories","transactions","commitments",
        "budgets","goals","invoices","invoice_items"
    )

    private fun key(password:CharArray,salt:ByteArray):SecretKeySpec {
        val f=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec=PBEKeySpec(password,salt,ITERATIONS,256)
        return try {
            val bytes=f.generateSecret(spec).encoded
            try { SecretKeySpec(bytes,"AES") } finally { bytes.fill(0) }
        } finally {
            spec.clearPassword()
        }
    }

    fun backup(context:Context,db:FlosiDatabase,destination:Uri,password:CharArray) {
        require(password.size>=NEW_BACKUP_MIN_PASSWORD){"كلمة مرور النسخة الجديدة يجب أن تكون 8 أحرف/أرقام على الأقل"}
        var plain:ByteArray?=null
        try {
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
            val dbFile=context.getDatabasePath("flosi.db")
            require(dbFile.isFile){"قاعدة البيانات غير موجودة"}
            require(dbFile.length() in 100..MAX_DATABASE_BYTES.toLong()){"حجم قاعدة البيانات غير صالح للنسخ"}

            plain=dbFile.readBytes()
            require(plain.size<=MAX_DATABASE_BYTES){"قاعدة البيانات أكبر من الحد الآمن للنسخ"}
            requireSqliteHeader(plain)

            val salt=ByteArray(SALT_SIZE).also{SecureRandom().nextBytes(it)}
            val iv=ByteArray(IV_SIZE).also{SecureRandom().nextBytes(it)}
            val cipher=Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE,key(password,salt),GCMParameterSpec(128,iv))
            val encrypted=cipher.doFinal(plain)

            try {
                context.contentResolver.openOutputStream(destination)?.use { output ->
                    DataOutputStream(output).use { d ->
                        d.writeInt(MAGIC.size);d.write(MAGIC)
                        d.writeInt(salt.size);d.write(salt)
                        d.writeInt(iv.size);d.write(iv)
                        d.writeInt(encrypted.size);d.write(encrypted)
                        d.flush()
                    }
                } ?: error("تعذر فتح مكان الحفظ")
            } finally {
                encrypted.fill(0)
                salt.fill(0)
                iv.fill(0)
            }
        } finally {
            plain?.fill(0)
            password.fill('\u0000')
        }
    }

    fun restoreToTemp(context:Context,source:Uri,password:CharArray):ByteArray {
        require(password.size>=LEGACY_RESTORE_MIN_PASSWORD){"كلمة مرور النسخة قصيرة"}
        try {
            val plain=context.contentResolver.openInputStream(source)?.use { input ->
                DataInputStream(input).use { d ->
                    val magicSize=readLength(d,"ترويسة النسخة",1,64)
                    val magic=ByteArray(magicSize).also{d.readFully(it)}
                    require(magic.contentEquals(MAGIC)){"ملف النسخة غير صالح أو إصدار غير مدعوم"}

                    val saltSize=readLength(d,"salt",SALT_SIZE,SALT_SIZE)
                    val salt=ByteArray(saltSize).also{d.readFully(it)}
                    val ivSize=readLength(d,"IV",IV_SIZE,IV_SIZE)
                    val iv=ByteArray(ivSize).also{d.readFully(it)}
                    val encryptedSize=readLength(d,"البيانات المشفرة",16,MAX_DATABASE_BYTES+64)
                    val encrypted=ByteArray(encryptedSize).also{d.readFully(it)}

                    try {
                        val cipher=Cipher.getInstance("AES/GCM/NoPadding")
                        cipher.init(Cipher.DECRYPT_MODE,key(password,salt),GCMParameterSpec(128,iv))
                        cipher.doFinal(encrypted)
                    } finally {
                        salt.fill(0)
                        iv.fill(0)
                        encrypted.fill(0)
                    }
                }
            } ?: error("تعذر قراءة النسخة")

            try {
                require(plain.size in 100..MAX_DATABASE_BYTES){"حجم قاعدة البيانات داخل النسخة غير صالح"}
                validateDatabaseBytes(context,plain)
                return plain
            } catch (e:Throwable) {
                plain.fill(0)
                throw e
            }
        } finally {
            password.fill('\u0000')
        }
    }

    fun applyRestore(context:Context,db:FlosiDatabase,plainDb:ByteArray) {
        require(plainDb.size in 100..MAX_DATABASE_BYTES){"حجم قاعدة البيانات المسترجعة غير صالح"}
        validateDatabaseBytes(context,plainDb)

        val file=context.getDatabasePath("flosi.db")
        val parent=file.parentFile ?: error("مسار قاعدة البيانات غير صالح")
        parent.mkdirs()
        val incoming=File(parent,"flosi.db.restore")
        val previous=File(parent,"flosi.db.pre-restore")
        incoming.delete()
        previous.delete()

        try {
            FileOutputStream(incoming).use { out ->
                out.write(plainDb)
                out.flush()
                out.fd.sync()
            }
            validateDatabaseFile(incoming)

            db.close()
            context.getDatabasePath("flosi.db-wal").delete()
            context.getDatabasePath("flosi.db-shm").delete()

            if(file.exists()&&!file.renameTo(previous)){
                error("تعذر تجهيز قاعدة البيانات الحالية للاسترجاع")
            }
            if(!incoming.renameTo(file)){
                if(previous.exists())previous.renameTo(file)
                error("تعذر تثبيت قاعدة البيانات المسترجعة")
            }

            try {
                validateDatabaseFile(file)
            } catch (e:Throwable) {
                file.delete()
                if(previous.exists())previous.renameTo(file)
                throw e
            }
            previous.delete()
        } finally {
            incoming.delete()
            plainDb.fill(0)
        }
    }

    private fun readLength(d:DataInputStream,label:String,min:Int,max:Int):Int {
        val value=runCatching{d.readInt()}.getOrElse{throw IllegalArgumentException("ملف النسخة ناقص عند $label",it)}
        require(value in min..max){"طول $label غير صالح"}
        return value
    }

    private fun requireSqliteHeader(bytes:ByteArray) {
        require(bytes.size>=SQLITE_HEADER.size&&bytes.copyOfRange(0,SQLITE_HEADER.size).contentEquals(SQLITE_HEADER)){
            "محتوى النسخة ليس قاعدة SQLite صالحة"
        }
    }

    private fun validateDatabaseBytes(context:Context,bytes:ByteArray) {
        requireSqliteHeader(bytes)
        val temp=File(context.cacheDir,"flosi-restore-check-${System.nanoTime()}.db")
        try {
            FileOutputStream(temp).use { out ->
                out.write(bytes)
                out.flush()
                out.fd.sync()
            }
            validateDatabaseFile(temp)
        } finally {
            temp.delete()
            File(temp.path+"-wal").delete()
            File(temp.path+"-shm").delete()
        }
    }

    private fun validateDatabaseFile(file:File) {
        require(file.isFile&&file.length()>=100){"قاعدة البيانات المسترجعة فارغة"}
        val sqlite=SQLiteDatabase.openDatabase(file.path,null,SQLiteDatabase.OPEN_READONLY)
        try {
            sqlite.rawQuery("PRAGMA quick_check(1)",null).use { cursor ->
                require(cursor.moveToFirst()&&cursor.getString(0).equals("ok",ignoreCase=true)){
                    "فشل فحص سلامة قاعدة البيانات المسترجعة"
                }
            }
            val found=mutableSetOf<String>()
            sqlite.rawQuery("SELECT name FROM sqlite_master WHERE type='table'",null).use { cursor ->
                while(cursor.moveToNext())found+=cursor.getString(0)
            }
            val missing=requiredTables-found
            require(missing.isEmpty()){"النسخة ناقصة جداول Flosi المطلوبة: ${missing.joinToString()}"}
        } finally {
            sqlite.close()
        }
    }
}
