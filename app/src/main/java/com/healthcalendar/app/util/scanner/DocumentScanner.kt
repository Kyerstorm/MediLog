package com.healthcalendar.app.util.scanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.healthcalendar.app.data.database.entities.DocumentType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "DocumentScanner"
        const val REQUEST_CODE_SCAN = 1001
    }
    
    private var scanner: GmsDocumentScanner? = null
    private var onScanComplete: ((List<Uri>) -> Unit)? = null
    private var onScanError: ((Exception) -> Unit)? = null
    
    /**
     * Initialize the document scanner with options
     */
    fun initializeScanner(
        pageLimit: Int = 10,
        galleryImportAllowed: Boolean = true
    ) {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(galleryImportAllowed)
            .setPageLimit(pageLimit)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        
        scanner = GmsDocumentScanning.getClient(options)
        Log.d(TAG, "Document scanner initialized")
    }
    
    /**
     * Start the document scanning process
     */
    fun startScan(
        activity: Activity,
        onComplete: (List<Uri>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (scanner == null) {
            initializeScanner()
        }
        
        this.onScanComplete = onComplete
        this.onScanError = onError
        
        scanner?.getStartScanIntent(activity)
            ?.addOnSuccessListener { intentSender ->
                try {
                    activity.startIntentSenderForResult(
                        intentSender,
                        REQUEST_CODE_SCAN,
                        null,
                        0,
                        0,
                        0
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting scan", e)
                    onError(e)
                }
            }
            ?.addOnFailureListener { e ->
                Log.e(TAG, "Failed to get scan intent", e)
                onError(e)
            }
    }
    
    /**
     * Handle the result from the document scanner
     * Call this from your Activity's onActivityResult
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SCAN) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let { intent ->
                        val result = GmsDocumentScanningResult.fromActivityResultIntent(intent)
                        result?.let { scanResult ->
                            val uris = mutableListOf<Uri>()
                            
                            // Get PDF if available
                            scanResult.pdf?.let { pdf ->
                                uris.add(pdf.uri)
                                Log.d(TAG, "PDF scanned: ${pdf.uri}")
                            }
                            
                            // Get individual page images
                            scanResult.pages?.forEach { page ->
                                uris.add(page.imageUri)
                                Log.d(TAG, "Page scanned: ${page.imageUri}")
                            }
                            
                            onScanComplete?.invoke(uris)
                        } ?: run {
                            onScanError?.invoke(Exception("No scan result"))
                        }
                    }
                }
                Activity.RESULT_CANCELED -> {
                    Log.d(TAG, "Scan cancelled")
                    onScanError?.invoke(Exception("Scan cancelled"))
                }
                else -> {
                    Log.e(TAG, "Scan failed with result code: $resultCode")
                    onScanError?.invoke(Exception("Scan failed"))
                }
            }
        }
    }
}

/**
 * Data class to represent scanned document info
 */
data class ScannedDocument(
    val uri: Uri,
    val type: DocumentType,
    val timestamp: Long = System.currentTimeMillis()
)
