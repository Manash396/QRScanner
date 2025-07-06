package com.example.qrscanner.di

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.qrscanner.data.CameraControlManager
import com.example.qrscanner.data.QRCodeScannerImpl
import com.example.qrscanner.domain.QRCodeScanner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    custom dependency created by me
    @Provides
    @Singleton
    fun provideCameraControlManager(): CameraControlManager = CameraControlManager()

    // by google mlkit
    @Provides
    @Singleton
    fun provideBarcodeScanner(cameraControlManager: CameraControlManager): BarcodeScanner {
        val zoomCallback = ZoomSuggestionOptions.ZoomCallback { zoomRatio ->
            cameraControlManager.setApplyZoom(zoomRatio)
        }

        val zoomOptions = ZoomSuggestionOptions.Builder(zoomCallback)
            .setMaxSupportedZoomRatio(5.0f)
            .build()

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .setZoomSuggestionOptions(zoomOptions)
            .build()

        return BarcodeScanning.getClient(options)
    }

//    provide QRCodeScanner dependency (repository)
    @Provides
    @Singleton
    fun provideQRCodeScanner(scanner: BarcodeScanner): QRCodeScanner {
        return QRCodeScannerImpl(scanner)
    }

//    camera provider using listenable so that it did not block main thread
    
    @Provides
    @Singleton
         fun provideCameraProvider(@ApplicationContext context: Context) :  ListenableFuture<ProcessCameraProvider>{
         return ProcessCameraProvider.getInstance(context)
     }

}