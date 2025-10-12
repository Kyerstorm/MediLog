package com.healthcalendar.app.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.work.*
import com.google.gson.Gson
import com.healthcalendar.app.data.database.entities.Appointment
import com.healthcalendar.app.data.database.entities.Medication
import com.healthcalendar.app.data.database.entities.MedicationLog
import java.io.File
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object BackupManager {
    
    private const val KEYSTORE_ALIAS = "health_calendar_backup_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val BACKUP_WORK_NAME = "auto_backup_work"
    
    data class BackupData(
        val appointments: List<Appointment>,
        val medications: List<Medication>,
        val logs: List<MedicationLog>,
        val timestamp: Long,
        val version: String = "1.0"
    )
    
    // Create encrypted backup
    fun createEncryptedBackup(
        context: Context,
        appointments: List<Appointment>,
        medications: List<Medication>,
        logs: List<MedicationLog>
    ): File {
        val backupData = BackupData(
            appointments = appointments,
            medications = medications,
            logs = logs,
            timestamp = System.currentTimeMillis()
        )
        
        val gson = Gson()
        val jsonData = gson.toJson(backupData)
        
        // Encrypt data
        val (encryptedData, iv) = encryptData(jsonData.toByteArray())
        
        // Save to file
        val timestamp = System.currentTimeMillis()
        val backupFile = File(context.getExternalFilesDir("backups"), "backup_$timestamp.hcb")
        backupFile.parentFile?.mkdirs()
        
        // Write IV and encrypted data
        backupFile.outputStream().use { output ->
            output.write(iv.size)
            output.write(iv)
            output.write(encryptedData)
        }
        
        return backupFile
    }
    
    // Restore from encrypted backup
    fun restoreFromBackup(backupFile: File): BackupData? {
        return try {
            val fileData = backupFile.readBytes()
            
            // Read IV
            val ivSize = fileData[0].toInt()
            val iv = fileData.copyOfRange(1, 1 + ivSize)
            val encryptedData = fileData.copyOfRange(1 + ivSize, fileData.size)
            
            // Decrypt data
            val decryptedData = decryptData(encryptedData, iv)
            val jsonData = String(decryptedData)
            
            // Parse JSON
            val gson = Gson()
            gson.fromJson(jsonData, BackupData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // List available backups
    fun listBackups(context: Context): List<File> {
        val backupDir = File(context.getExternalFilesDir("backups"), "")
        return backupDir.listFiles()?.filter { it.extension == "hcb" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    // Delete old backups (keep last N)
    fun cleanOldBackups(context: Context, keepCount: Int = 10) {
        val backups = listBackups(context)
        backups.drop(keepCount).forEach { it.delete() }
    }
    
    // Schedule auto-backup
    fun scheduleAutoBackup(context: Context, frequencyHours: Long) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            frequencyHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BACKUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            backupRequest
        )
    }
    
    // Cancel auto-backup
    fun cancelAutoBackup(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME)
    }
    
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        return if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    private fun encryptData(data: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encryptedData = cipher.doFinal(data)
        val iv = cipher.iv
        return Pair(encryptedData, iv)
    }
    
    private fun decryptData(encryptedData: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), IvParameterSpec(iv))
        return cipher.doFinal(encryptedData)
    }
}

// WorkManager worker for auto-backup
class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // This would need to be implemented with proper dependency injection
        // For now, return success
        return Result.success()
    }
}
