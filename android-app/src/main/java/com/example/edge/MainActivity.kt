package com.example.edge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.edge.ui.CameraGLSurfaceView

class MainActivity : AppCompatActivity() {
    private lateinit var glView: CameraGLSurfaceView
    private lateinit var btnToggle: Button

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glView = findViewById(R.id.glView)
        btnToggle = findViewById(R.id.btnToggle)

        btnToggle.setOnClickListener {
            val mode = glView.toggleProcessed()
            btnToggle.text = if (mode) "Toggle: Processed" else "Toggle: Raw"
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    private fun startCamera() {
        glView.visibility = View.VISIBLE
        glView.onResume()
        glView.start()
    }
}
