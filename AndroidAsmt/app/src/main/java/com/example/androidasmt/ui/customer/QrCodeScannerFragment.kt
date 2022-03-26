package com.example.androidasmt.ui.customer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentQrCodeScannerBinding
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.ProceedBookingBody
import com.example.androidasmt.network.response.UserResponse
import com.example.androidasmt.rv.ClinicAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrCodeScannerFragment : Fragment() {

    private lateinit var binding: FragmentQrCodeScannerBinding
    private val nav by lazy { findNavController() }

    private val vm: QrCodeScannerFragmentModel by viewModels()

    // camera
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val TAG = "CameraXBasic"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            // to show all the permission available ------------------------------------------
            // For Debugging Purposes
            permissions.entries.forEach {
                Log.e(TAG, "${it.key} = ${it.value}")
            }

            // check if is granted -----------------------------------------------------------
            val granted = permissions.entries.all {
                it.value == true
            }

            // if permission granted, open camera --------------------------------------------
            // else navigateUp with error alert dialog
            if (granted) {
                startCamera()
            }
            else {
                // display error alert dialog
                AlertDialog.Builder(requireContext())
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Error")
                    .setMessage("Please request camera permissions in your setting.")
                    .setPositiveButton("Dismiss", null)
                    .show()

                nav.navigateUp()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentQrCodeScannerBinding.inflate(inflater, container, false)

        vm.id = requireArguments().getInt("id", 0)

        // binding.button.setOnClickListener { scan() }

        // if permission granted, open camera -----------------------------------------------
        // else request permission
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        return binding.root
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner -------------------
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview ------------------------------------------------------------------------
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default ------------------------------------------------
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // build ImageAnalyzer ------------------------------------------------------------
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QRCodeAnalyzer() {
                        scan(it)
                    })
                }

            // Bind CameraProvider ------------------------------------------------------------
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalyzer, preview)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private class QRCodeAnalyzer(
        // Callback function
        val fn: (String?) -> Unit = { _ ->}
    ) : ImageAnalysis.Analyzer {

        // configure barcode scanner to scan what kind of format of code
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        override fun analyze(imageProxy: ImageProxy) {
            Log.e(TAG, "Scanning")
            scanBarcode(imageProxy)
        }

        @SuppressLint("UnsafeOptInUsageError")
        private fun scanBarcode(imageProxy: ImageProxy) {
            imageProxy.image?.let { image ->
                val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
                val scanner = BarcodeScanning.getClient(options)
                scanner.process(inputImage)
                    .addOnCompleteListener {
                        imageProxy.close()
                        if (it.isSuccessful) {
                            readBarcodeData(it.result as List<Barcode>)
                        } else {
                            it.exception?.printStackTrace()
                        }
                    }
            }
        }

        private fun readBarcodeData(barcodes: List<Barcode>) {
            for (barcode in barcodes) {
                when (barcode.valueType) {
                    //you can check if the barcode has other values
                    //For now I am using it just for URL
                    Barcode.TYPE_TEXT -> {
                        //we have the URL here
                        val id = barcode.rawValue

                        fn(id)
                    }
                }
            }
        }
    }

    private fun scan(code: String?) {

        // if still loading,
        // or code is empty
        // then do not proceed
        if (vm.isLoading.value == true || code.isNullOrEmpty()) return
        Log.e(TAG, "Scanned QR Code, $code")
        vm.isLoading.value = true  // display loading state in ui

        lifecycleScope.launch {

            // prepare body data
            val body = ProceedBookingBody (
                vm.id,
                code,
            )

            // send api request
            val response = withContext(Dispatchers.IO) {
                VaccineApi.retrofitService.proceedBooking(body)
            }

            // check the statusCode
            when (response.code()) {
                in 200..299 -> status200to299() // success status
                404 -> status404() // NotFound Status
                else -> statusOther(response.code().toString()) // Other Status
            }
        }
    }

    private fun status200to299() {
        Snackbar.make(binding.root, "QR Code Scanned!", Snackbar.LENGTH_SHORT).show()
        nav.navigateUp()
    }

    private fun status404() {
        // display error alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Invalid QR Code")
            .setPositiveButton("Try again", null)
            .setOnDismissListener() {
                vm.isLoading.value = false
            }
            .show()
    }

    private fun statusOther(code: String) {
        // display error alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Unexpected Error Occurred: $code")
            .setPositiveButton("Try again later", null)
            .setOnDismissListener() {
                nav.navigateUp()
            }
            .show()
    }
}