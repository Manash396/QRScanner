package com.example.qrscanner.data

import androidx.camera.core.CameraControl
import javax.inject.Inject
import javax.inject.Singleton

// this is my custom dependency

@Singleton
class CameraControlManager @Inject constructor() {
    var cameraControl : CameraControl? =null

    fun setTCameraControl(control: CameraControl){
        this.cameraControl = control
    }

    fun setApplyZoom(zoomRatio: Float): Boolean{
       return try {
            cameraControl?.setZoomRatio(zoomRatio)
           true
        } catch (e: Exception){
          false
        }


    }
}