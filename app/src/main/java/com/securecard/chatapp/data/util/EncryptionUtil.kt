package com.securecard.chatapp.data.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import android.content.Context

object EncryptionUtil {
    
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 16
    
    // Standard encryption without NFC card
    fun encrypt(plainText: String, secretKey: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = SecretKeySpec(secretKey, "AES")
        
        // Generate a random IV
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec)
        val cipherText = cipher.doFinal(plainText.toByteArray())
        
        // Combine IV and cipherText
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
    
    fun decrypt(encryptedText: String, secretKey: ByteArray): String {
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)
        
        // Extract IV
        val iv = ByteArray(GCM_IV_LENGTH)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        
        // Extract cipherText
        val cipherText = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, iv.size, cipherText, 0, cipherText.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = SecretKeySpec(secretKey, "AES")
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec)
        val decryptedText = cipher.doFinal(cipherText)
        
        return String(decryptedText)
    }
    
    // Generate a random encryption key
    fun generateKey(): ByteArray {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(KEY_SIZE, SecureRandom())
        return keyGenerator.generateKey().encoded
    }
    
    // For secure storage of keys and preferences
    fun createEncryptedSharedPreferences(context: Context, fileName: String): EncryptedSharedPreferences {
        val masterKeyAlias = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        return EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }
    
    // Prepare data for NFC card encryption
    fun prepareForCardEncryption(plainText: String): ByteArray {
        return plainText.toByteArray()
    }
    
    // Process data decrypted from NFC card
    fun processCardDecryptedData(decryptedData: ByteArray): String {
        return String(decryptedData)
    }
} 