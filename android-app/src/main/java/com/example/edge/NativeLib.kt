package com.example.edge

object NativeLib {
    init {
        System.loadLibrary("native-lib")
    }

    /**
     * Process a frame using native OpenCV code.
     * @param data Input frame data (YUV or grayscale)
     * @param width Frame width
     * @param height Frame height
     * @return Processed frame data
     */
    external fun processFrame(data: ByteArray, width: Int, height: Int): ByteArray

    /**
     * Get native library version info
     */
    external fun getVersion(): String
}
