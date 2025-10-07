package com.example.edge.camera

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import java.nio.ByteBuffer

class CameraHelper(
    private val context: Context,
    private val onFrameAvailable: (ByteArray, Int, Int) -> Unit
) {
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCaptureSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.e(TAG, "Camera error: $error")
            camera.close()
            cameraDevice = null
        }
    }

    fun openCamera(onReady: (SurfaceTexture) -> Unit) {
        startBackgroundThread()
        try {
            val cameraId = cameraManager.cameraIdList[0]
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = map?.getOutputSizes(ImageFormat.YUV_420_888) ?: emptyArray()
            val previewSize = sizes.firstOrNull { it.width == 640 && it.height == 480 }
                ?: sizes.firstOrNull() ?: Size(640, 480)

            imageReader = ImageReader.newInstance(
                previewSize.width,
                previewSize.height,
                ImageFormat.YUV_420_888,
                2
            ).apply {
                setOnImageAvailableListener({ reader ->
                    val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                    try {
                        val width = image.width
                        val height = image.height
                        val yPlane = image.planes[0]
                        val uPlane = image.planes[1]
                        val vPlane = image.planes[2]

                        val yBuffer = yPlane.buffer
                        val uBuffer = uPlane.buffer
                        val vBuffer = vPlane.buffer

                        val yRowStride = yPlane.rowStride
                        val uvRowStride = uPlane.rowStride
                        val uvPixelStride = uPlane.pixelStride

                        val rgba = ByteArray(width * height * 4)

                        var yp: Int
                        var up: Int
                        var vp: Int
                        var r: Int
                        var g: Int
                        var b: Int

                        for (j in 0 until height) {
                            for (i in 0 until width) {
                                val yIndex = j * yRowStride + i
                                val uvRow = (j / 2) * uvRowStride
                                val uvCol = (i / 2) * uvPixelStride
                                val uIndex = uvRow + uvCol
                                val vIndex = uvRow + uvCol

                                yp = (yBuffer.get(yIndex).toInt() and 0xFF)
                                // For YUV_420_888, U and V are in planes[1] and [2]. Accounts for pixelStride.
                                up = (uBuffer.get(uIndex).toInt() and 0xFF)
                                vp = (vBuffer.get(vIndex).toInt() and 0xFF)

                                val yVal = yp.toFloat()
                                val uVal = up - 128
                                val vVal = vp - 128

                                // Standard YUV to RGB conversion
                                var rf = yVal + 1.370705f * vVal
                                var gf = yVal - 0.337633f * uVal - 0.698001f * vVal
                                var bf = yVal + 1.732446f * uVal

                                r = rf.toInt().coerceIn(0, 255)
                                g = gf.toInt().coerceIn(0, 255)
                                b = bf.toInt().coerceIn(0, 255)

                                val base = (j * width + i) * 4
                                rgba[base] = r.toByte()
                                rgba[base + 1] = g.toByte()
                                rgba[base + 2] = b.toByte()
                                rgba[base + 3] = 0xFF.toByte()
                            }
                        }

                        onFrameAvailable(rgba, width, height)
                    } catch (e: Exception) {
                        Log.e(TAG, "YUV to RGBA conversion failed", e)
                    } finally {
                        image.close()
                    }
                }, backgroundHandler)
            }

            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: SecurityException) {
            Log.e(TAG, "Camera permission not granted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open camera", e)
        }
    }

    private fun createCaptureSession() {
        val device = cameraDevice ?: return
        val reader = imageReader ?: return

        try {
            val captureRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(reader.surface)

            device.createCaptureSession(
                listOf(reader.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        captureRequestBuilder.set(
                            CaptureRequest.CONTROL_MODE,
                            CameraMetadata.CONTROL_MODE_AUTO
                        )
                        session.setRepeatingRequest(
                            captureRequestBuilder.build(),
                            null,
                            backgroundHandler
                        )
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Failed to configure capture session")
                    }
                },
                backgroundHandler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create capture session", e)
        }
    }

    fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        stopBackgroundThread()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, "Interrupted while stopping background thread", e)
        }
    }

    companion object {
        private const val TAG = "CameraHelper"
    }
}
