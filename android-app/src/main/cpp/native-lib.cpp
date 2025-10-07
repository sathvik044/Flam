#include <jni.h>
#include <string>
#include <android/log.h>
#include <vector>

#ifdef USE_OPENCV
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
using namespace cv;
#endif

#define TAG "NativeLib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_edge_NativeLib_getVersion(JNIEnv* env, jobject /* this */) {
#ifdef USE_OPENCV
    std::string version = "OpenCV " + std::string(CV_VERSION);
#else
    std::string version = "Fallback (no OpenCV)";
#endif
    return env->NewStringUTF(version.c_str());
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_edge_NativeLib_processFrame(
    JNIEnv* env,
    jobject /* this */,
    jbyteArray data,
    jint width,
    jint height
) {
    jbyte* inputBytes = env->GetByteArrayElements(data, nullptr);
    jsize inputLen = env->GetArrayLength(data);

    if (!inputBytes) {
        LOGE("Failed to get input byte array");
        return data;
    }

#ifdef USE_OPENCV
    // Convert input to OpenCV Mat (assume grayscale or Y plane of YUV)
    Mat inputMat(height, width, CV_8UC1, (unsigned char*)inputBytes);
    Mat outputMat;

    // Apply Canny edge detection
    GaussianBlur(inputMat, outputMat, Size(5, 5), 1.4);
    Canny(outputMat, outputMat, 50, 150);

    // Create output byte array
    jbyteArray result = env->NewByteArray(outputMat.total() * outputMat.elemSize());
    env->SetByteArrayRegion(result, 0, outputMat.total() * outputMat.elemSize(),
                           (jbyte*)outputMat.data);

    env->ReleaseByteArrayElements(data, inputBytes, JNI_ABORT);
    LOGI("Processed frame with OpenCV Canny: %dx%d", width, height);
    return result;

#else
    // Fallback: simple threshold-based edge approximation
    std::vector<unsigned char> output(inputLen);
    unsigned char* input = (unsigned char*)inputBytes;

    for (int y = 1; y < height - 1; y++) {
        for (int x = 1; x < width - 1; x++) {
            int idx = y * width + x;
            int gx = -input[idx - width - 1] + input[idx - width + 1]
                     -2*input[idx - 1] + 2*input[idx + 1]
                     -input[idx + width - 1] + input[idx + width + 1];
            int gy = -input[idx - width - 1] - 2*input[idx - width] - input[idx - width + 1]
                     +input[idx + width - 1] + 2*input[idx + width] + input[idx + width + 1];
            int mag = abs(gx) + abs(gy);
            output[idx] = (mag > 128) ? 255 : 0;
        }
    }

    jbyteArray result = env->NewByteArray(inputLen);
    env->SetByteArrayRegion(result, 0, inputLen, (jbyte*)output.data());
    env->ReleaseByteArrayElements(data, inputBytes, JNI_ABORT);
    LOGI("Processed frame with fallback edge detection: %dx%d", width, height);
    return result;
#endif
}
