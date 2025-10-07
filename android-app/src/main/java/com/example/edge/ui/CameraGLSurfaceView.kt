package com.example.edge.ui

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.edge.camera.CameraHelper
import com.example.edge.gl.CameraRenderer

class CameraGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer: CameraRenderer
    private val cameraHelper: CameraHelper

    init {
        setEGLContextClientVersion(2)
        renderer = CameraRenderer(context)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY

        cameraHelper = CameraHelper(context) { imageData, width, height ->
            renderer.updateFrame(imageData, width, height)
        }
    }

    fun start() {
        cameraHelper.openCamera { surfaceTexture ->
            renderer.setSurfaceTexture(surfaceTexture)
        }
    }

    fun toggleProcessed(): Boolean {
        return renderer.toggleProcessed()
    }

    fun cycleShaderMode(): com.example.edge.gl.CameraRenderer.ShaderMode {
        return renderer.cycleShaderMode()
    }

    fun setFpsListener(listener: ((Float) -> Unit)?) {
        renderer.setFpsListener(listener)
    }

    override fun onPause() {
        super.onPause()
        cameraHelper.closeCamera()
    }

    override fun onResume() {
        super.onResume()
    }
}
