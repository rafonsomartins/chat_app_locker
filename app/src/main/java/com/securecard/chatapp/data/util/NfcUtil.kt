package com.securecard.chatapp.data.util

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import java.io.IOException

object NfcUtil {
    
    private const val TIMEOUT = 5000 // 5 seconds timeout for NFC operations
    
    // Check if NFC is available and enabled
    fun isNfcAvailable(activity: Activity): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        return nfcAdapter != null
    }
    
    fun isNfcEnabled(activity: Activity): Boolean {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        return nfcAdapter != null && nfcAdapter.isEnabled
    }
    
    // Enable foreground dispatch for NFC reading
    fun enableNfcForegroundDispatch(activity: Activity, pendingIntent: android.app.PendingIntent) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null)
    }
    
    // Disable foreground dispatch
    fun disableNfcForegroundDispatch(activity: Activity) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        nfcAdapter.disableForegroundDispatch(activity)
    }
    
    // Process NFC intent
    fun processNfcIntent(intent: Intent): Tag? {
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            return intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }
        return null
    }
    
    // Connect to NFC card and send data for encryption
    fun encryptWithCard(tag: Tag, data: ByteArray): ByteArray? {
        val isoDep = IsoDep.get(tag) ?: return null
        
        try {
            isoDep.connect()
            isoDep.timeout = TIMEOUT
            
            // This is a placeholder for the actual APDU commands to send to the card
            // Real implementation would depend on the specific NFC card's protocol
            val apduCommand = buildEncryptApduCommand(data)
            val response = isoDep.transceive(apduCommand)
            
            isoDep.close()
            return processEncryptionResponse(response)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (isoDep.isConnected) {
                try {
                    isoDep.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }
    
    // Connect to NFC card and send encrypted data for decryption
    fun decryptWithCard(tag: Tag, encryptedData: ByteArray): ByteArray? {
        val isoDep = IsoDep.get(tag) ?: return null
        
        try {
            isoDep.connect()
            isoDep.timeout = TIMEOUT
            
            // This is a placeholder for the actual APDU commands to send to the card
            // Real implementation would depend on the specific NFC card's protocol
            val apduCommand = buildDecryptApduCommand(encryptedData)
            val response = isoDep.transceive(apduCommand)
            
            isoDep.close()
            return processDecryptionResponse(response)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (isoDep.isConnected) {
                try {
                    isoDep.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }
    
    // Check if the card supports the required encryption operations
    fun verifyCardSupport(tag: Tag): Boolean {
        val isoDep = IsoDep.get(tag) ?: return false
        
        try {
            isoDep.connect()
            isoDep.timeout = TIMEOUT
            
            // Check card support by sending a test command
            val testCommand = buildTestApduCommand()
            val response = isoDep.transceive(testCommand)
            
            isoDep.close()
            return isCardSupported(response)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (isoDep.isConnected) {
                try {
                    isoDep.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }
    
    // These methods would contain the actual implementation for your specific NFC card
    // These are placeholders that would need to be replaced with real implementations
    
    private fun buildEncryptApduCommand(data: ByteArray): ByteArray {
        // Replace with actual APDU command for encryption
        // Example: CLA INS P1 P2 Lc Data Le
        return byteArrayOf(0x00, 0x20, 0x00, 0x00) + byteArrayOf(data.size.toByte()) + data + byteArrayOf(0x00)
    }
    
    private fun buildDecryptApduCommand(encryptedData: ByteArray): ByteArray {
        // Replace with actual APDU command for decryption
        return byteArrayOf(0x00, 0x22, 0x00, 0x00) + byteArrayOf(encryptedData.size.toByte()) + encryptedData + byteArrayOf(0x00)
    }
    
    private fun buildTestApduCommand(): ByteArray {
        // Replace with actual APDU command to test card support
        return byteArrayOf(0x00, 0xA4, 0x04, 0x00, 0x00)
    }
    
    private fun processEncryptionResponse(response: ByteArray): ByteArray? {
        // Process the response from the encryption command
        // Check status words (last 2 bytes) for success/failure
        return if (response.size >= 2 && response[response.size - 2] == 0x90.toByte() && response[response.size - 1] == 0x00.toByte()) {
            // Remove status words from response
            response.copyOfRange(0, response.size - 2)
        } else {
            null
        }
    }
    
    private fun processDecryptionResponse(response: ByteArray): ByteArray? {
        // Process the response from the decryption command
        return if (response.size >= 2 && response[response.size - 2] == 0x90.toByte() && response[response.size - 1] == 0x00.toByte()) {
            // Remove status words from response
            response.copyOfRange(0, response.size - 2)
        } else {
            null
        }
    }
    
    private fun isCardSupported(response: ByteArray): Boolean {
        // Check if the card supports the required operations
        return response.size >= 2 && response[response.size - 2] == 0x90.toByte() && response[response.size - 1] == 0x00.toByte()
    }
} 