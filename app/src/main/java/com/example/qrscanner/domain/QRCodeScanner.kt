package com.example.qrscanner.domain

import com.google.mlkit.vision.common.InputImage

interface QRCodeScanner {
  suspend  fun scan(image: InputImage) : String?
}