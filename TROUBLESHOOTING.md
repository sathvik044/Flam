# Troubleshooting Guide

## Common Issues and Solutions

### 1. Gradle Sync Failed

**Symptom**: "Could not resolve dependencies" or "Plugin not found"

**Solutions**:
- Check internet connection
- Update Gradle wrapper: `gradle wrapper --gradle-version 8.7`
- Invalidate caches: File → Invalidate Caches → Invalidate and Restart
- Check `local.properties` has correct `sdk.dir`

### 2. OpenCV Not Found

**Symptom**: CMake error "Could not find OpenCV"

**Solutions**:
- Download OpenCV Android SDK from https://opencv.org/releases/
- Extract to known location (e.g., `C:/dev/OpenCV-android-sdk`)
- Update `local.properties`:
  ```
  opencv_sdk_dir=C\:/dev/OpenCV-android-sdk
  ```
- Ensure `OpenCVConfig.cmake` exists in `sdk/native/jni/`
- Clean and rebuild project

**Fallback**: Project builds without OpenCV using simple edge detection

### 3. NDK Not Found

**Symptom**: "NDK not configured" or "CMake error"

**Solutions**:
- Open SDK Manager: Tools → SDK Manager → SDK Tools
- Check "NDK (Side by side)" and "CMake"
- Install version 25.x or newer
- Restart Android Studio
- Sync Gradle again

### 4. Camera Permission Denied

**Symptom**: App crashes on launch or black screen

**Solutions**:
- Check `AndroidManifest.xml` has camera permission
- Grant permission manually:
  - Settings → Apps → Edge Viewer → Permissions → Camera → Allow
- On Android 13+, ensure runtime permission handling works
- Check Logcat for permission errors

### 5. Black Screen / No Camera Feed

**Symptom**: App runs but shows black screen

**Solutions**:
- Check camera permission granted
- Verify device has working camera
- Check Logcat for camera errors:
  ```
  adb logcat | grep -i camera
  ```
- Try different camera ID in `CameraHelper.kt` (change `cameraIdList[0]` to `[1]`)
- Test on physical device (emulator cameras are unreliable)

### 6. JNI UnsatisfiedLinkError

**Symptom**: "java.lang.UnsatisfiedLinkError: No implementation found for..."

**Solutions**:
- Ensure native library builds successfully
- Check CMakeLists.txt is correct
- Verify library name matches: `System.loadLibrary("native-lib")`
- Clean and rebuild: Build → Clean Project → Rebuild Project
- Check APK contains .so files: Build → Analyze APK → lib/

### 7. Low FPS / Poor Performance

**Symptom**: Frame rate below 10 FPS

**Solutions**:
- Reduce camera resolution in `CameraHelper.kt`:
  ```kotlin
  val previewSize = Size(320, 240) // Lower resolution
  ```
- Disable OpenCV processing temporarily (test raw mode)
- Profile with Android Profiler: View → Tool Windows → Profiler
- Test on physical device (emulator is slow)
- Optimize OpenCV parameters (smaller blur kernel, simpler detection)

### 8. Web Viewer Not Loading

**Symptom**: Blank page or "Cannot GET /"

**Solutions**:
- Ensure Node.js installed: `node --version`
- Install dependencies: `cd web && npm install`
- Start dev server: `npm run dev`
- Check port 5173 not in use
- Clear browser cache
- Check browser console for errors (F12)

### 9. TypeScript Compilation Errors

**Symptom**: "Cannot find module" or type errors

**Solutions**:
- Install dependencies: `npm install`
- Check `tsconfig.json` is correct
- Update TypeScript: `npm install -D typescript@latest`
- Clean build: `rm -rf dist node_modules && npm install`

### 10. Git Issues

**Symptom**: "fatal: not a git repository" or commit fails

**Solutions**:
- Initialize repo: `git init`
- Configure user:
  ```bash
  git config user.name "Your Name"
  git config user.email "your@email.com"
  ```
- Check .gitignore excludes build files
- Force add if needed: `git add -f <file>`

### 11. Build Artifacts in Git

**Symptom**: `.gradle/`, `build/`, `node_modules/` tracked

**Solutions**:
- Ensure `.gitignore` is correct
- Remove from tracking:
  ```bash
  git rm -r --cached .gradle build node_modules
  git commit -m "Remove build artifacts"
  ```
- Add to `.gitignore` if missing

### 12. OpenGL Rendering Issues

**Symptom**: Distorted image, wrong colors, or crash

**Solutions**:
- Check texture format matches data (GL_LUMINANCE for grayscale)
- Verify texture dimensions are powers of 2 or use GL_CLAMP_TO_EDGE
- Check shader compilation: Look for GL errors in Logcat
- Enable GL error checking:
  ```kotlin
  val error = GLES20.glGetError()
  if (error != GLES20.GL_NO_ERROR) Log.e(TAG, "GL Error: $error")
  ```

## Debugging Tips

### Enable Verbose Logging

Add to `MainActivity.onCreate()`:
```kotlin
if (BuildConfig.DEBUG) {
    Log.d("EdgeViewer", "Debug mode enabled")
}
```

### Check Native Logs

```bash
adb logcat | grep -i "NativeLib"
```

### Profile Performance

1. Open Android Profiler: View → Tool Windows → Profiler
2. Select app process
3. Monitor CPU, Memory, and GPU usage
4. Identify bottlenecks

### Inspect APK

1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Build → Analyze APK
3. Check `lib/` folder has `.so` files for target ABIs
4. Verify OpenCV libraries included (if configured)

### Test on Multiple Devices

- Different Android versions (6.0, 10, 13+)
- Different screen sizes and resolutions
- Different camera capabilities
- Physical devices preferred over emulator

## Getting Help

1. **Check Documentation**:
   - README.md - Overview and quick start
   - SETUP.md - Detailed setup instructions
   - ARCHITECTURE.md - System design
   - This file - Troubleshooting

2. **Search Logs**:
   ```bash
   adb logcat > logcat.txt
   # Search for errors in logcat.txt
   ```

3. **Clean Build**:
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

4. **Reset Environment**:
   - Close Android Studio
   - Delete `.gradle/`, `.idea/`, `build/` folders
   - Reopen project
   - Sync Gradle

5. **Verify Prerequisites**:
   - Android Studio (latest stable)
   - Android SDK (API 24+)
   - NDK (r25+)
   - CMake (3.22+)
   - OpenCV Android SDK (4.x)
   - Node.js (18+)
   - Git

## Still Stuck?

If none of the above helps:

1. Create a minimal reproducible example
2. Check Android Studio Event Log (bottom-right corner)
3. Review full Logcat output
4. Verify all prerequisites installed correctly
5. Try on a different device/emulator
6. Check GitHub Issues (after repo creation)

## Performance Benchmarks

Expected performance on mid-range device (Snapdragon 660, 4GB RAM):

| Metric | Expected | Actual (Your Device) |
|--------|----------|---------------------|
| FPS (Raw) | 25-30 | ___ |
| FPS (Processed) | 12-18 | ___ |
| Processing Time | 35-55ms | ___ |
| Memory Usage | <100MB | ___ |
| APK Size | ~15-25MB | ___ |

If your results differ significantly, investigate:
- Device capabilities
- OpenCV configuration
- Resolution settings
- Background processes
