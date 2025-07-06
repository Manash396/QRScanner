package com.example.qrscanner.data

import android.util.Log
import com.example.qrscanner.domain.QRCodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class QRCodeScannerImpl(
    private var barcodeScanner: BarcodeScanner
) : QRCodeScanner {


     override suspend fun scan(image: InputImage): String?  = suspendCoroutine { cont ->
         barcodeScanner.process(image)
             .addOnSuccessListener { barcodes ->
                 // list of scanned barcodes
                 if (barcodes.isEmpty()) {
                     Log.d("idebug", "No QR code found")
                 } else {
                     Log.d("idebug", "QR Code found: ${barcodes.firstOrNull()?.rawValue}")
                 }
                 cont.resume(barcodes.firstOrNull()?.rawValue)
             }
             .addOnFailureListener {
                 cont.resume(null)
             }

    }
}