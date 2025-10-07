package com.example.edge.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.edge.NativeLib
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL    13→
    14→class CameraRenderer(private val context: Context) : GLSurfaceView.Renderer {
    15→    private var program = 0
    16→    private var textureId = 0
    17→    private var positionHandle = 0
    18→    private var texCoordHandle = 0
    19→    private var textureHandle = 0
    20→
    private var fpsListener: ((Float) -> Unit)? = null

    21→    private var surfaceTexture: SurfaceTexture? = null
    22→    private var frameData: ByteArray? = null
    23→    private var frameWidth = 0
    24→    private var frameHeight = 0
    25→    private var processedMode = false

    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer

    private val vertices = floatArrayOf(
{{ ... }}
        surfaceTexture = texture
    }

    fun toggleProcessed(): Boolean {
        processedMode = !processedMode
    145→        return processedMode
    146→    }
    147→
    148→    private fun updateFps() {
    149→        frameCount++
    150→        val now = System.currentTimeMillis()
    151→        if (now - lastFpsTime >= 1000) {
    152→            fps = frameCount * 1000f / (now - lastFpsTime)
    153→            Log.d(TAG, "FPS: $fps")
            fpsListener?.invoke(fps)
    154→            frameCount = 0
    155→            lastFpsTime = now
    156→        }
    157→    }
    158→
    fun setFpsListener(listener: ((Float) -> Unit)?) {
        fpsListener = listener
    }

    159→    private fun loadShader(type: Int, shaderCode: String): Int {
    160→        return GLES20.glCreateShader(type).also { shader ->
    161→            GLES20.glShaderSource(shader, shaderCode)
    162→            GLES20.glCompileShader(shader)
    163→        }

    companion object {
        private const val TAG = "CameraRenderer"

        private const val VERTEX_SHADER = """
{{ ... }}
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """
    }
}
