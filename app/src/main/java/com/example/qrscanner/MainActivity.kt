package com.example.qrscanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.Surface
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.OnNewIntentProvider
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.lifecycle.lifecycleScope
import com.example.qrscanner.databinding.ActivityMainBinding
import com.example.qrscanner.data.CameraControlManager
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.logging.Handler
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var appBar: BottomAppBar
    private var isFabcenter: Boolean =  true
    private  var lastScrollY: Int = 0
//    field injection
    @Inject lateinit var cameraControlManager: CameraControlManager
      @Inject lateinit var cameraProvider : ListenableFuture<ProcessCameraProvider>
      private lateinit var provider: ProcessCameraProvider

      private var permission : Boolean = false

      private lateinit var preview: PreviewView
      private lateinit var btn : FloatingActionButton
      private lateinit var webView: WebView

      private lateinit var progressBar : ProgressBar

      private val viewModel : QRViewModel by viewModels()

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       // initailised the view
        preview = binding.previewView
        btn = binding.fabBtn
        webView =binding.webView
        progressBar = binding.progressBar
        appBar = binding.bottomAppBar

        setUpwebView()

        //  listenner for srcolling down down

        webView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = webView.scrollY
            val layoutparams = appBar.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = layoutparams.behavior as? HideBottomViewOnScrollBehavior<*>
            // Scroll down
            if (scrollY > lastScrollY && isFabcenter ) {
                appBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
                isFabcenter = false

                // hide appbar
                appBar.animate()
                    .translationY(appBar.height.toFloat())
                    .setDuration(200)
                    .start()

                // Scroll up
            } else if (scrollY < lastScrollY && !isFabcenter) {
                appBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
                isFabcenter = true

//             show appbar
                appBar.animate()
                    .translationY(0f)
                    .setDuration(200)
                    .start()

            }

            lastScrollY = scrollY
        }

        // checking the camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
            ){
            permission = true
            cameraProvider.addListener(
                {provider = cameraProvider.get()},
                ContextCompat.getMainExecutor(this)
            )
        }else{
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA),101)
        }


        // observing the
        viewModel.result.observe(this){ url ->
            url?.let{
                   loadWebContent(webView,url)
             }
        }

      btn.setOnClickListener {
          if (permission) {
              webView.visibility = View.GONE
              preview.visibility = View.VISIBLE
             bindCameraPreview(provider)
          }
      }


    }

    private fun bindCameraPreview(cameraProvider: ProcessCameraProvider){
        val prev: Preview = Preview.Builder()
            .build()
        val cameraSelector : CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


        val imageAnalysis : ImageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(
                    ContextCompat.getMainExecutor(this@MainActivity), QRCodeAnalyzer(viewModel)
                )
            }
// setting preview surface with preview view
        prev.surfaceProvider = preview.surfaceProvider

        cameraProvider.unbindAll()

        val camera = cameraProvider.bindToLifecycle(this,cameraSelector,prev,imageAnalysis)
        cameraControlManager.setTCameraControl(camera.cameraControl)
    }

    private fun setUpwebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                progressBar.visibility = View.GONE
//                webView.visibility = View.VISIBLE
            }
        }
        webView.webChromeClient = WebChromeClient()

        webView.settings.apply {
            domStorageEnabled = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            allowFileAccess = false
        }
    }

    // now after requesting if the permission is granted

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED){
            permission = true
            cameraProvider.addListener(
                {provider = cameraProvider.get()},
                ContextCompat.getMainExecutor(this)
            )
        }
    }


    private fun loadWebContent(webView: WebView, url: String) {

        preview.visibility = View.GONE
        progressBar.visibility = View.VISIBLE

        
        lifecycleScope.launch {
            delay(2000) // wait for 2 seconds
            if (Patterns.WEB_URL.matcher(url).matches()) {
                val validUrl = if (url.startsWith("http")) url else "https://$url"
                webView.loadUrl(validUrl)
            } else {
                val html = "<html><body><p><h1>$url</h1></p></body></html>"
                webView.loadData(html, "text/html", "UTF-8")
            }
        }


    }
}

