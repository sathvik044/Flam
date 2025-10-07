package com.example.edge.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.edge.NativeLib
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL    13→
    14→class CameraRenderer(private val context: Context) : GLSurfaceView.Renderer {
    15→    private var program = 0
    16→    private var textureId = 0
    17→    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0
    private var modeHandle = 0
    20→
    private var fpsListener: ((Float) -> Unit)? = null

    21→    private var surfaceTexture: SurfaceTexture? = null
    22→    private var frameData: ByteArray? = null
{{ ... }}
        surfaceTexture = texture
    }

    fun toggleProcessed(): Boolean {
        processedMode = !processedMode
        return processedMode
    }

    fun cycleShaderMode(): ShaderMode {
        shaderMode = when (shaderMode) {
            ShaderMode.PASS -> ShaderMode.GRAYSCALE
            ShaderMode.GRAYSCALE -> ShaderMode.INVERT
            ShaderMode.INVERT -> ShaderMode.PASS
        }
        return shaderMode
    }
146→    }
    147→
    148→    private fun updateFps() {
    149→        frameCount++
    150→        val now = System.currentTimeMillis()
    151→        if (now - lastFpsTime >= 1000) {
{{ ... }}
    fun setFpsListener(listener: ((Float) -> Unit)?) {
        fpsListener = listener
    }

    159→    private fun loadShader(type: Int, shaderCode: String): Int {
    160→        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        modeHandle = GLES20.glGetUniformLocation(program, "uMode")
.also { shader ->
    161→            GLES20.glShaderSource(shader, shaderCode)
    162→            GLES20.glUniform1i(textureHandle, 0)
            // 0 = PASS, 1 = GRAYSCALE, 2 = INVERT
            val modeInt = when (shaderMode) {
                ShaderMode.PASS -> 0
                ShaderMode.GRAYSCALE -> 1
                ShaderMode.INVERT -> 2
            }
            GLES20.glUniform1i(modeHandle, modeInt)
r(shader)
    163→        }

    companion object {
        private const val TAG = "CameraRenderer"

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
            uniform int uMode; // 0=pass,1=gray,2=invert
            void main() {
                vec4 c = texture2D(uTexture, vTexCoord);
                if (uMode == 1) {
                    float g = c.r; // source is luminance
                    gl_FragColor = vec4(g, g, g, 1.0);
                } else if (uMode == 2) {
                    gl_FragColor = vec4(1.0 - c.rgb, 1.0);
                } else {
                    gl_FragColor = c;
                }
            }
        """
    }
}
