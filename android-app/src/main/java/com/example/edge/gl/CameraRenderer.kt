package com.example.edge.gl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private var program = 0
    private var textureId = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureHandle = 0
    private var modeHandle = 0
    private var texelHandle = 0
    private var lowThreshHandle = 0
    private var highThreshHandle = 0
    private var flipXHandle = 0

    private var surfaceTexture: SurfaceTexture? = null

    private var frameData: ByteArray? = null
    private var frameWidth: Int = 0
    private var frameHeight: Int = 0
    @Volatile private var hasNewFrame = false

    private var fpsListener: ((Float) -> Unit)? = null
    private var lastFpsTime = 0L
    private var frameCount = 0

    private var shaderMode: ShaderMode = ShaderMode.PASS
    private var cannyLow: Float = 0.08f
    private var cannyHigh: Float = 0.20f

    private val vertexData: FloatBuffer = ByteBuffer.allocateDirect(VERTICES.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(VERTICES)
        .apply { position(0) }

    private val texCoordData: FloatBuffer = ByteBuffer.allocateDirect(TEX_COORDS.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(TEX_COORDS)
        .apply { position(0) }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (program == 0) throw RuntimeException("Failed to create GL program")

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        modeHandle = GLES20.glGetUniformLocation(program, "uMode")
        texelHandle = GLES20.glGetUniformLocation(program, "uTexel")
        lowThreshHandle = GLES20.glGetUniformLocation(program, "uLow")
        highThreshHandle = GLES20.glGetUniformLocation(program, "uHigh")
        flipXHandle = GLES20.glGetUniformLocation(program, "uFlipX")

        textureId = genTexture()
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        lastFpsTime = System.currentTimeMillis()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Upload new frame if present
        if (hasNewFrame) {
            val data = frameData
            if (data != null && frameWidth > 0 && frameHeight > 0) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
                // Upload RGBA texture from camera frame
                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_RGBA,
                    frameWidth,
                    frameHeight,
                    0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    ByteBuffer.wrap(data)
                )
                hasNewFrame = false
            }
        }

        GLES20.glUseProgram(program)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexData)

        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordData)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        val modeInt = when (shaderMode) {
            ShaderMode.PASS -> 0
            ShaderMode.GRAYSCALE -> 1
            ShaderMode.INVERT -> 2
            ShaderMode.CANNY -> 3
        }
        GLES20.glUniform1i(modeHandle, modeInt)

        // Set uniforms for Canny when frame size known
        if (frameWidth > 0 && frameHeight > 0) {
            GLES20.glUniform2f(texelHandle, 1.0f / frameWidth.toFloat(), 1.0f / frameHeight.toFloat())
        }
        // Tunable thresholds for canny (low/high)
        GLES20.glUniform1f(lowThreshHandle, cannyLow)
        GLES20.glUniform1f(highThreshHandle, cannyHigh)
        // Disable horizontal flip (no mirror)
        GLES20.glUniform1i(flipXHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        updateFps()
    }

    fun updateFrame(imageData: ByteArray, width: Int, height: Int) {
        frameData = imageData
        frameWidth = width
        frameHeight = height
        hasNewFrame = true
    }

    fun setSurfaceTexture(texture: SurfaceTexture) {
        surfaceTexture = texture
    }

    fun toggleProcessed(): Boolean {
        shaderMode = if (shaderMode == ShaderMode.PASS) ShaderMode.GRAYSCALE else ShaderMode.PASS
        return shaderMode != ShaderMode.PASS
    }

    fun cycleShaderMode(): ShaderMode {
        shaderMode = when (shaderMode) {
            ShaderMode.PASS -> ShaderMode.GRAYSCALE
            ShaderMode.GRAYSCALE -> ShaderMode.INVERT
            ShaderMode.INVERT -> ShaderMode.CANNY
            ShaderMode.CANNY -> ShaderMode.PASS
        }
        return shaderMode
    }

    fun setFpsListener(listener: ((Float) -> Unit)?) {
        fpsListener = listener
    }

    fun setCannyThresholds(low: Float, high: Float) {
        cannyLow = low.coerceIn(0f, 1f)
        cannyHigh = high.coerceIn(0f, 1f)
        if (cannyHigh < cannyLow) {
            val tmp = cannyLow
            cannyLow = cannyHigh
            cannyHigh = tmp
        }
    }

    private fun updateFps() {
        frameCount++
        val now = System.currentTimeMillis()
        if (now - lastFpsTime >= 1000) {
            val fps = frameCount * 1000f / (now - lastFpsTime).coerceAtLeast(1)
            fpsListener?.invoke(fps)
            frameCount = 0
            lastFpsTime = now
        }
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        val program = GLES20.glCreateProgram()
        if (program == 0) return 0
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ${'$'}{GLES20.glGetProgramInfoLog(program)}")
            GLES20.glDeleteProgram(program)
            return 0
        }
        return program
    }

    private fun loadShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) return 0
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader $type: ${'$'}{GLES20.glGetShaderInfoLog(shader)}")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    private fun genTexture(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        val texId = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        return texId
    }

    enum class ShaderMode { PASS, GRAYSCALE, INVERT, CANNY }

    companion object {
        private const val TAG = "CameraRenderer"

        // Fullscreen quad
        private val VERTICES = floatArrayOf(
            -1f, -1f,
             1f, -1f,
            -1f,  1f,
             1f,  1f,
        )
        // Texture coordinates: 90° CW rotation WITHOUT mirror
        // Order corresponds to vertices: BL, BR, TL, TR
        private val TEX_COORDS = floatArrayOf(
            1f, 1f,  // BL -> right-top
            1f, 0f,  // BR -> right-bottom
            0f, 1f,  // TL -> left-top
            0f, 0f,  // TR -> left-bottom
        )

        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
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
            uniform int uMode; // 0=pass,1=gray,2=invert,3=canny
            uniform vec2 uTexel; // 1/width, 1/height
            uniform float uLow;   // low threshold
            uniform float uHigh;  // high threshold
            uniform int uFlipX;   // 1 to flip horizontally

            // Sobel kernels
            mat3 sobelX = mat3(-1.0, 0.0, 1.0,
                               -2.0, 0.0, 2.0,
                               -1.0, 0.0, 1.0);
            mat3 sobelY = mat3(-1.0, -2.0, -1.0,
                                0.0,  0.0,  0.0,
                                1.0,  2.0,  1.0);

            float luminance(vec3 color) {
                return 0.299 * color.r + 0.587 * color.g + 0.114 * color.b;
            }

            // 3x3 Gaussian blur of luminance
            float blurLuma(vec2 uv) {
                float kernel[9];
                kernel[0]=1.0; kernel[1]=2.0; kernel[2]=1.0;
                kernel[3]=2.0; kernel[4]=4.0; kernel[5]=2.0;
                kernel[6]=1.0; kernel[7]=2.0; kernel[8]=1.0;
                float sum = 0.0;
                float wsum = 0.0;
                int idx = 0;
                for (int j=-1; j<=1; j++) {
                    for (int i=-1; i<=1; i++) {
                        vec2 o = vec2(float(i), float(j)) * uTexel;
                        float w = kernel[idx++];
                        sum += luminance(texture2D(uTexture, uv + o).rgb) * w;
                        wsum += w;
                    }
                }
                return sum / wsum;
            }

            void main() {
                vec2 uv = vTexCoord;
                if (uFlipX == 1) {
                    uv.x = 1.0 - uv.x;
                }
                vec3 color = texture2D(uTexture, uv).rgb;

                if (uMode == 1) {
                    // Grayscale
                    float gray = luminance(color);
                    gl_FragColor = vec4(gray, gray, gray, 1.0);
                } else if (uMode == 2) {
                    // Invert
                    gl_FragColor = vec4(1.0 - color, 1.0);
                } else if (uMode == 3) {
                    // Canny Edge Detection (improved): blur -> sobel -> NMS -> double threshold
                    float G[9];
                    int idx = 0;
                    float gx = 0.0;
                    float gy = 0.0;

                    // Blur luminance 3x3 and apply sobel
                    for (int j=-1; j<=1; j++) {
                        for (int i=-1; i<=1; i++) {
                            vec2 o = vec2(float(i), float(j)) * uTexel;
                            float l = blurLuma(uv + o);
                            int k = (j+1)*3 + (i+1);
                            // apply sobel with pre-defined matrices
                            gx += l * sobelX[j+1][i+1];
                            gy += l * sobelY[j+1][i+1];
                        }
                    }

                    float mag = length(vec2(gx, gy));
                    float ang = atan(gy, gx);

                    // Non-maximum suppression: sample along gradient direction
                    vec2 dir = normalize(vec2(cos(ang), sin(ang))) * uTexel;
                    float mag1 = length(vec2(0.0));
                    float mag2 = length(vec2(0.0));
                    // sample previous and next
                    {
                        // previous
                        float gx1 = 0.0; float gy1 = 0.0;
                        float gx2 = 0.0; float gy2 = 0.0;
                        // approximate by sampling magnitudes at offsets
                        float mPrev = luminance(texture2D(uTexture, vec2(uFlipX == 1 ? 1.0 - uv.x : uv.x, uv.y) - dir).rgb);
                        float mNext = luminance(texture2D(uTexture, vec2(uFlipX == 1 ? 1.0 - uv.x : uv.x, uv.y) + dir).rgb);
                        mag1 = mPrev; mag2 = mNext;
                    }

                    float nms = (mag >= mag1 && mag >= mag2) ? mag : 0.0;

                    // Double threshold + simple hysteresis
                    float strong = step(uHigh, nms);
                    float weak = step(uLow, nms) * (1.0 - strong);

                    // Promote weak if neighbor is strong
                    float linked = 0.0;
                    for (int j=-1; j<=1; j++) {
                        for (int i=-1; i<=1; i++) {
                            vec2 o = vec2(float(i), float(j)) * uTexel;
                            float lmag = luminance(texture2D(uTexture, vec2(uFlipX == 1 ? 1.0 - uv.x : uv.x, uv.y) + o).rgb);
                            linked = max(linked, step(uHigh, lmag));
                        }
                    }
                    float edge = max(strong, min(weak * linked, 1.0));
                    gl_FragColor = vec4(vec3(edge), 1.0);
                } else {
                    // Pass through
                    gl_FragColor = vec4(color, 1.0);
                }
            }
        """
    }
}
