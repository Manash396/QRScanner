package com.example.qrscanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrscanner.domain.QRCodeScanner
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRViewModel @Inject constructor(
    private val qrCodeScanner: QRCodeScanner
) : ViewModel() {

    private var _result = MutableLiveData<String?>()

    // can be accessed  a val
    val result : LiveData<String?> = _result  // read-only

     fun scan(image: InputImage) : Job{
        return viewModelScope.launch {

        _result.postValue(qrCodeScanner.scan(image))

         }
    }

}