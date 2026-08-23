package com.flosi.app.backup

import android.content.Context
import android.net.Uri
import com.flosi.app.data.local.FlosiDatabase
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptedBackupService {
    private val MAGIC="FLOSI_BACKUP_V1".toByteArray(Charsets.UTF_8)
    private const val ITERATIONS=180_000

    private fun key(password:CharArray,salt:ByteArray):SecretKeySpec {
        val f=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val bytes=f.generateSecret(PBEKeySpec(password,salt,ITERATIONS,256)).encoded
        return SecretKeySpec(bytes,"AES")
    }

    fun backup(context:Context,db:FlosiDatabase,destination:Uri,password:CharArray) {
        require(password.size>=4){"كلمة مرور النسخة قصيرة"}
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()
        val dbFile=context.getDatabasePath("flosi.db")
        val plain=dbFile.readBytes()
        val salt=ByteArray(16).also{SecureRandom().nextBytes(it)}
        val iv=ByteArray(12).also{SecureRandom().nextBytes(it)}
        val cipher=Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE,key(password,salt),GCMParameterSpec(128,iv))
        val encrypted=cipher.doFinal(plain)
        context.contentResolver.openOutputStream(destination)?.use { output ->
            DataOutputStream(output).use { d ->
                d.writeInt(MAGIC.size);d.write(MAGIC)
                d.writeInt(salt.size);d.write(salt)
                d.writeInt(iv.size);d.write(iv)
                d.writeInt(encrypted.size);d.write(encrypted)
            }
        } ?: error("تعذر فتح مكان الحفظ")
        password.fill('\u0000')
    }

    fun restoreToTemp(context:Context,source:Uri,password:CharArray):ByteArray {
        val all=context.contentResolver.openInputStream(source)?.use { input ->
            DataInputStream(input).use { d ->
                val magic=ByteArray(d.readInt()).also{d.readFully(it)}
                require(magic.contentEquals(MAGIC)){"ملف النسخة غير صالح"}
                val salt=ByteArray(d.readInt()).also{d.readFully(it)}
                val iv=ByteArray(d.readInt()).also{d.readFully(it)}
                val encrypted=ByteArray(d.readInt()).also{d.readFully(it)}
                val cipher=Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE,key(password,salt),GCMParameterSpec(128,iv))
                cipher.doFinal(encrypted)
            }
        } ?: error("تعذر قراءة النسخة")
        password.fill('\u0000')
        return all
    }

    fun applyRestore(context:Context,db:FlosiDatabase,plainDb:ByteArray) {
        db.close()
        val file=context.getDatabasePath("flosi.db")
        file.parentFile?.mkdirs()
        file.writeBytes(plainDb)
        context.getDatabasePath("flosi.db-wal").delete()
        context.getDatabasePath("flosi.db-shm").delete()
    }
}
