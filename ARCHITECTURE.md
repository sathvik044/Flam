# Architecture Overview

## System Design

```
┌─────────────────────────────────────────────────────────────┐
│                     Android Application                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐         ┌──────────────┐                  │
│  │  MainActivity │────────▶│ GLSurfaceView│                  │
│  │  (Kotlin)     │         │  + Renderer  │                  │
│  └──────────────┘         └──────┬───────┘                  │
│         │                         │                           │
│         │                         │                           │
│  ┌──────▼──────┐          ┌──────▼───────┐                  │
│  │ CameraHelper│          │CameraRenderer│                  │
│  │ (Camera2)   │          │  (OpenGL ES) │                  │
│  └──────┬──────┘          └──────┬───────┘                  │
│         │                         │                           │
│         │ YUV Frame              │ Texture Update            │
│         │                         │                           │
│  ┌──────▼─────────────────────────▼───────┐                 │
│  │          JNI Bridge (NativeLib)        │                 │
│  └──────────────────┬─────────────────────┘                 │
└─────────────────────┼───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                   Native Layer (C++)                         │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────────────────────────────────────┐          │
│  │         native-lib.cpp (JNI Methods)           │          │
│  │                                                 │          │
│  │  • processFrame(byte[], width, height)        │          │
│  │  • getVersion()                                │          │
│  └────────────────────┬───────────────────────────┘          │
│                       │                                       │
│  ┌────────────────────▼───────────────────────────┐          │
│  │            OpenCV Processing                    │          │
│  │                                                 │          │
│  │  1. Convert YUV → Grayscale Mat                │          │
│  │  2. GaussianBlur (5x5, σ=1.4)                 │          │
│  │  3. Canny Edge Detection (50, 150)            │          │
│  │  4. Return processed buffer                    │          │
│  └─────────────────────────────────────────────────┘          │
│                                                               │
└───────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    Web Viewer (TypeScript)                   │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────────────────────────────────────┐          │
│  │           EdgeViewer Class                      │          │
│  │                                                 │          │
│  │  • Canvas rendering                            │          │
│  │  • Stats display (FPS, resolution, time)       │          │
│  │  • Base64 frame loading                        │          │
│  │  • WebSocket stub (future)                     │          │
│  └─────────────────────────────────────────────────┘          │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

## Data Flow

### Frame Processing Pipeline

1. **Camera Capture** (Camera2 API)
   - `CameraHelper` opens camera with `ImageReader`
   - Captures YUV_420_888 frames at configured resolution (default 640×480)
   - Extracts Y plane (luminance) as byte array

2. **JNI Transfer**
   - Kotlin calls `NativeLib.processFrame(data, width, height)`
   - Byte array transferred to native heap
   - JNI bridge invokes C++ processing function

3. **Native Processing** (C++ + OpenCV)
   - Convert byte array to `cv::Mat` (CV_8UC1)
   - Apply Gaussian blur to reduce noise
   - Run Canny edge detection
   - Return processed buffer to Java

4. **OpenGL Rendering**
   - `CameraRenderer` receives processed byte array
   - Uploads to GL texture (`glTexImage2D`)
   - Renders textured quad with vertex/fragment shaders
   - Displays on `GLSurfaceView`

5. **Toggle Mode**
   - Button switches `processedMode` flag
   - Renderer conditionally calls native processing
   - Raw mode: direct camera data → texture
   - Processed mode: camera → JNI → OpenCV → texture

### Performance Considerations

- **Target**: 10-15 FPS minimum
- **Bottlenecks**:
  - JNI data copy overhead
  - OpenCV processing time (Canny ~30-50ms on mid-range device)
  - Texture upload to GPU

- **Optimizations**:
  - Use direct ByteBuffer to avoid JNI copy
  - Reduce resolution if needed
  - Consider GPU shaders for edge detection (future)
  - Pool buffers to reduce GC pressure

## Module Breakdown

### Android App (`/android-app`)

#### Java/Kotlin Layer
- **`MainActivity.kt`**: Entry point, permission handling, UI setup
- **`CameraGLSurfaceView.kt`**: Custom GLSurfaceView, connects camera and renderer
- **`CameraHelper.kt`**: Camera2 session management, frame capture
- **`CameraRenderer.kt`**: OpenGL ES 2.0 renderer, texture management, FPS tracking
- **`NativeLib.kt`**: JNI interface declaration

#### Native Layer (`/cpp`)
- **`native-lib.cpp`**: JNI implementations, OpenCV integration
- **`CMakeLists.txt`**: Build configuration, OpenCV linking

#### Resources
- **`AndroidManifest.xml`**: Permissions, activity declaration
- **`activity_main.xml`**: UI layout (GLSurfaceView + toggle button)
- **`strings.xml`**, **`styles.xml`**: App resources

### Web Viewer (`/web`)

- **`src/index.ts`**: TypeScript entry point, `EdgeViewer` class
- **`index.html`**: UI with canvas, stats cards, modern styling
- **`package.json`**: Dependencies (TypeScript, Vite)
- **`tsconfig.json`**: TypeScript compiler config
- **`vite.config.ts`**: Vite bundler setup

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Android UI | Kotlin | Activity, views, permissions |
| Camera | Camera2 API | Frame capture |
| Rendering | OpenGL ES 2.0 | GPU texture rendering |
| Processing | OpenCV C++ | Canny edge detection |
| Bridge | JNI/NDK | Java ↔ C++ communication |
| Build | Gradle + CMake | Android + native build |
| Web UI | TypeScript | Type-safe web viewer |
| Web Build | Vite | Fast bundler, dev server |

## Build System

### Gradle (Android)
- Root `build.gradle`: Plugin versions, repositories
- `android-app/build.gradle`: App config, NDK setup, dependencies
- `settings.gradle`: Module inclusion

### CMake (Native)
- `CMakeLists.txt`: C++ compilation, OpenCV linking
- Finds OpenCV via `find_package(OpenCV)`
- Links `libopencv_java4.so` and core libraries

### NPM (Web)
- `package.json`: Scripts, dependencies
- `npm run dev`: Vite dev server
- `npm run build`: Production build

## Extension Points

### Android
- Add more OpenCV filters (Sobel, Laplacian, morphology)
- Implement GPU shaders for real-time effects
- Add recording/export functionality
- Integrate CameraX for simpler API

### Web
- WebSocket server for live streaming
- REST API for frame upload
- Multiple frame comparison view
- Performance graphs

### Integration
- HTTP server in Android app
- Export frames to gallery
- Share processed frames
- Remote control via web interface

## Security Notes

- Camera permission required (runtime permission on Android 6+)
- No network permissions by default
- Local processing only (no cloud upload)
- Web viewer runs locally (no external dependencies)

## Testing Strategy

1. **Unit Tests**: JNI bridge, data conversion
2. **Integration Tests**: Camera → Processing → Render pipeline
3. **Performance Tests**: FPS measurement, memory profiling
4. **Device Tests**: Multiple devices, Android versions
5. **Web Tests**: Browser compatibility, canvas rendering

## Known Limitations

- YUV → Grayscale conversion (color info lost)
- Single-threaded processing (no async pipeline)
- No frame buffering (latest frame only)
- Web viewer is static (no real-time streaming)
- Basic error handling (production needs more robustness)

## Future Improvements

- [ ] GPU-accelerated edge detection (GLSL shaders)
- [ ] Multi-threaded processing pipeline
- [ ] Real-time WebSocket streaming
- [ ] Multiple filter modes (Sobel, Laplacian, etc.)
- [ ] Recording and export
- [ ] Performance profiling UI
- [ ] Automated tests
- [ ] CI/CD pipeline
