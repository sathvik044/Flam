# Setup Instructions

## Prerequisites

1. **Android Studio** (Giraffe or newer)
2. **Android SDK** (API 24+)
3. **Android NDK** (r25+)
4. **CMake** (via Android Studio SDK Manager)
5. **OpenCV Android SDK** (4.x)
6. **Node.js** (18+) for web viewer
7. **Git** for version control

## Step 1: Clone/Initialize Repository

```bash
cd Flam
git init
git add .
git commit -m "Initial project scaffold"
```

## Step 2: Download OpenCV Android SDK

1. Visit https://opencv.org/releases/
2. Download **OpenCV Android SDK** (e.g., opencv-4.9.0-android-sdk.zip)
3. Extract to a known location (e.g., `C:/dev/OpenCV-android-sdk`)

## Step 3: Configure local.properties

1. Copy `local.properties.template` to `local.properties`
2. Edit paths:
   ```properties
   sdk.dir=C\:/Users/YourUsername/AppData/Local/Android/Sdk
   opencv_sdk_dir=C\:/dev/OpenCV-android-sdk
   ```

## Step 4: Open in Android Studio

1. Open Android Studio
2. File → Open → Select the `Flam` folder
3. Wait for Gradle sync
4. Install NDK and CMake if prompted (Tools → SDK Manager → SDK Tools)

## Step 5: Build Android App

1. Connect a physical device (recommended) or start emulator
2. Click **Run** (green play button)
3. Grant camera permission when prompted
4. Tap "Toggle" button to switch between raw and processed frames

## Step 6: Setup Web Viewer

```bash
cd web
npm install
npm run dev
```

Opens at http://localhost:5173

## Step 7: Git Workflow (IMPORTANT)

Make meaningful commits as you develop:

```bash
# After initial scaffold
git add .
git commit -m "Add Android module structure and Gradle config"

# After adding JNI
git add android-app/src/main/cpp/
git commit -m "Add NDK C++ bridge and OpenCV integration"

# After renderer
git add android-app/src/main/java/com/example/edge/gl/
git commit -m "Implement OpenGL ES renderer with texture mapping"

# After camera
git add android-app/src/main/java/com/example/edge/camera/
git commit -m "Add Camera2 integration with ImageReader"

# After web
git add web/
git commit -m "Add TypeScript web viewer with Vite"

# Push to GitHub
git remote add origin https://github.com/yourusername/edge-viewer.git
git branch -M main
git push -u origin main
```

## Troubleshooting

### OpenCV Not Found
- Verify `opencv_sdk_dir` path in `local.properties`
- Check that `OpenCVConfig.cmake` exists in `sdk/native/jni/`
- Project will build with fallback edge detection if OpenCV is missing

### Camera Permission Denied
- Check AndroidManifest.xml has `<uses-permission android:name="android.permission.CAMERA" />`
- Grant permission manually in Settings → Apps → Edge Viewer → Permissions

### NDK Build Fails
- Ensure NDK is installed via SDK Manager
- Check CMake version (3.22.1+)
- Clean and rebuild: Build → Clean Project → Rebuild

### Web Viewer Blank
- Replace `web/public/sample.png` with actual frame
- Check browser console for errors
- Ensure Vite dev server is running

## Performance Tips

- Use physical device for best performance
- Target 10-15 FPS minimum
- Reduce resolution if needed (modify CameraHelper.kt)
- Profile with Android Profiler (View → Tool Windows → Profiler)

## Next Steps

1. Test on device
2. Capture screenshots/GIF for README
3. Export a processed frame and add to `web/public/sample.png`
4. Document architecture and commit history
5. Push to GitHub with proper commits
