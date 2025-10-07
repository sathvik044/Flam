# 🎨 Real-Time Edge Detection Viewer

**Android NDK + OpenCV + OpenGL ES + TypeScript Web Viewer**

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-24%2B-green.svg)](https://developer.android.com)
[![OpenCV](https://img.shields.io/badge/OpenCV-4.x-red.svg)](https://opencv.org)

A complete technical assessment project demonstrating real-time camera processing with native C++, OpenGL rendering, and a modern web viewer.

## 🚀 Features

### Android App
- ✅ **Camera2 API** integration with ImageReader
- ✅ **JNI/NDK** bridge for Java ↔ C++ communication
- ✅ **OpenCV C++** processing (Canny edge detection + Gaussian blur)
- ✅ **OpenGL ES 2.0** texture rendering with custom shaders
- ✅ **Real-time toggle** between raw and processed frames
- ✅ **FPS tracking** and performance monitoring
- ✅ **Fallback mode** (works without OpenCV)

### Web Viewer
- ✅ **TypeScript** with strict type checking
- ✅ **Vite** for fast development and building
- ✅ **Canvas rendering** for frame display
- ✅ **Live stats** (FPS, resolution, processing time)
- ✅ **Modern UI** with responsive design
- ✅ **WebSocket stub** for future real-time streaming

Project layout:

```
/android-app          # Android module (Kotlin + NDK + OpenGL ES)
  /src/main
    /java/com/example/edge
    /cpp              # C++ (OpenCV + JNI)
    /res              # UI resources
    /assets/shaders   # GLSL shaders
/web                  # TypeScript web viewer
```

## Features

- Camera2 capture -> JNI -> OpenCV C++ (Canny/Grayscale) -> OpenGL texture render
- Simple toggle: raw vs processed (renderer-side switch)
- FPS measurement and on-screen text overlay (basic)
- Web viewer shows a sample processed frame (base64) and simple FPS text

## Requirements

- Android Studio (Giraffe/Koala or newer), Android SDK 24+
- Android NDK (r25+)
- CMake (via Android Studio)
- A device with a camera (or emulator with virtual cam; physical device recommended)
- Node.js 18+ for the web viewer

## OpenCV Setup (Android)

  1. Download the OpenCV Android SDK from: https://opencv.org/releases/
  2. Unzip, note the path, e.g. `C:/dev/OpenCV-android-sdk`
  3. In `local.properties` (at repo root), add:
   ```
   opencv_sdk_dir=C:/dev/OpenCV-android-sdk
   ```
   Note: Use forward slashes or escaped backslashes on Windows.
4. The CMake config expects `OpenCVConfig.cmake` under `sdk/native/jni/` within that dir. If different, update `CMakeLists.txt` include paths accordingly.

If OpenCV is not configured, the C++ code falls back to a stub (simple luma-based grayscale) so the project still compiles. Enable full OpenCV by ensuring `find_package(OpenCV ...)` succeeds in CMake.
{{ ... }}
## Build & Run (Android)

- Open the root folder in Android Studio.
- Ensure NDK and CMake are installed via SDK Manager.
- Sync Gradle. If OpenCV path is set correctly, it will link to the OpenCV native libraries.
- Run on device. Grant camera permission when prompted.

Controls:
- Tap the toggle button to switch Raw / Processed.
- The view overlays basic FPS and resolution text.

## Web Viewer

The web viewer is a minimal TypeScript app that displays a static processed frame (PNG base64) and updates overlay stats.

- Setup:
  ```bash
  cd web
  npm install
  npm run build
  npm run serve
  ```
- Open http://localhost:5173

You can replace `public/sample.png` with an exported frame from Android and adjust `src/index.ts` to point to your asset or base64 string.

## Architecture

- `android-app/src/main/java/com/example/edge/`
  - `MainActivity.kt`: Camera2 setup, permission handling, connects to `GLSurfaceView` renderer.
  - `CameraRenderer.kt`: OpenGL ES 2.0 renderer. Manages textures, draws camera or processed frame, toggles modes, shows FPS.
  - `CameraHelper.kt`: Camera2 session handling, outputs frames to `ImageReader` or `SurfaceTexture`.
- `android-app/src/main/cpp/`
  - `native-lib.cpp`: JNI bridge. Exposes `processFrame()` that accepts YUV/RGBA and returns processed RGBA. Uses OpenCV if available.
  - `CMakeLists.txt`: Finds OpenCV, builds native lib.
- `android-app/src/main/assets/shaders/`: Simple pass-through vertex and fragment shaders.
- `web/`: TypeScript project using `tsc`; simple DOM updates for image and stats.

Data flow:
```
Camera2 -> YUV -> Java/Kotlin -> JNI (byte[] or direct buffer) -> C++ (OpenCV) -> RGBA -> GL Texture -> Screen
```

## Notes

- This project focuses on integration and rendering performance over UI polish.
- For production, prefer surface sharing (OES external texture) and GPU shaders for efficiency. Here we keep it clear and minimal.
- Frame rate target: 10–15 FPS minimum. Actual performance depends on device and processing path.

## Commit Strategy

- Make small, meaningful commits per module or feature:
  - Scaffold android module
  - Add JNI + CMake
  - Integrate OpenCV
  - Add renderer and shaders
  - Add Camera2 integration
  - Add web viewer
  - Docs and screenshots

## ScreenShots![WhatsApp Image 2025-10-08 at 03 04 13_53b28064](https://github.com/user-attachments/assets/35b81d5a-41db-4e01-bd6a-bfb72d927c9a)


<img width="610" height="1356" alt="image" src="https://github.com/user-attachments/assets/b769d46f-919c-4571-af8e-e542e1f9288a" />


