# Quick Reference Card

## 🚀 Essential Commands

### Android Build
```bash
# Open in Android Studio
# File → Open → Select project folder

# Build from command line
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Clean build
./gradlew clean build
```

### Web Viewer
```bash
cd web
npm install          # Install dependencies
npm run dev          # Start dev server (http://localhost:5173)
npm run build        # Build for production
npm run preview      # Preview production build
```

### Git Workflow
```bash
# Initialize
git init
git add .
git commit -m "Initial project scaffold"

# Modular commits (see COMMIT_GUIDE.md)
git add android-app/src/main/cpp/
git commit -m "feat: add JNI bridge and OpenCV integration"

# Push to GitHub
git remote add origin https://github.com/yourusername/edge-viewer.git
git push -u origin main
```

## 📁 Key Files

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Entry point, permissions |
| `CameraHelper.kt` | Camera2 integration |
| `CameraRenderer.kt` | OpenGL ES renderer |
| `native-lib.cpp` | JNI + OpenCV processing |
| `CMakeLists.txt` | Native build config |
| `index.ts` | Web viewer logic |
| `local.properties` | SDK/OpenCV paths |

## 🔧 Configuration

### local.properties
```properties
sdk.dir=C\:/Users/YourName/AppData/Local/Android/Sdk
opencv_sdk_dir=C\:/dev/OpenCV-android-sdk
```

### Adjust Camera Resolution
Edit `CameraHelper.kt`:
```kotlin
val previewSize = Size(640, 480)  // Change here
```

### Adjust OpenCV Parameters
Edit `native-lib.cpp`:
```cpp
GaussianBlur(inputMat, outputMat, Size(5, 5), 1.4);  // Blur
Canny(outputMat, outputMat, 50, 150);                // Thresholds
```

## 🐛 Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| OpenCV not found | Check `opencv_sdk_dir` in `local.properties` |
| NDK not found | Install via SDK Manager → SDK Tools |
| Camera black screen | Grant permission in Settings → Apps |
| Low FPS | Reduce resolution in `CameraHelper.kt` |
| Web viewer blank | Run `npm install` then `npm run dev` |
| Git issues | Run `git init` and configure user |

## 📊 Performance Targets

- **FPS**: 10-15 minimum
- **Processing**: 30-50ms per frame
- **Resolution**: 640×480 default
- **Memory**: <100MB

## 🎯 Feature Checklist

- [x] Camera2 integration
- [x] JNI bridge
- [x] OpenCV processing
- [x] OpenGL rendering
- [x] TypeScript web viewer
- [x] Toggle raw/processed
- [x] FPS tracking
- [x] Documentation

## 📚 Documentation Map

```
README.md           → Start here
SETUP.md            → Detailed setup
ARCHITECTURE.md     → System design
COMMIT_GUIDE.md     → Git workflow
EVALUATION.md       → Assessment mapping
TROUBLESHOOTING.md  → Common issues
PROJECT_SUMMARY.md  → High-level overview
QUICK_REFERENCE.md  → This file
```

## 🔗 Useful Links

- [OpenCV Android SDK](https://opencv.org/releases/)
- [Android Camera2 API](https://developer.android.com/training/camera2)
- [OpenGL ES 2.0](https://developer.android.com/guide/topics/graphics/opengl)
- [TypeScript Docs](https://www.typescriptlang.org/docs/)
- [Vite Guide](https://vitejs.dev/guide/)

## 💡 Pro Tips

1. **Use physical device** for best performance (emulator is slow)
2. **Check Logcat** for debugging: `adb logcat | grep -i edge`
3. **Profile performance**: View → Tool Windows → Profiler
4. **Make small commits**: Don't dump everything at once
5. **Test without OpenCV**: Fallback mode should work
6. **Document as you go**: Don't wait until the end

## 🎬 Quick Start (30 seconds)

```bash
# 1. Run setup script
quick-start.bat

# 2. Open in Android Studio
# File → Open → Select folder

# 3. Run on device
# Click green play button

# 4. Start web viewer
cd web && npm run dev
```

## 📝 Commit Message Templates

```bash
# Feature
git commit -m "feat: add Canny edge detection with OpenCV"

# Fix
git commit -m "fix: resolve camera permission crash on Android 13"

# Performance
git commit -m "perf: optimize frame buffer allocation in JNI"

# Documentation
git commit -m "docs: update README with OpenCV setup instructions"

# Refactor
git commit -m "refactor: extract renderer logic to separate class"
```

## 🧪 Testing Commands

```bash
# Check APK contents
./gradlew assembleDebug
# Build → Analyze APK → app-debug.apk

# View logs
adb logcat | grep -i "NativeLib\|CameraRenderer\|EdgeViewer"

# Check native libraries
adb shell run-as com.example.edge ls -la lib/

# Monitor FPS
adb logcat | grep -i "FPS:"
```

## 🎨 Color Codes (for UI)

```
Primary: #667eea
Secondary: #764ba2
Success: #4caf50
Error: #f44336
Text: #333333
Background: #f8f9fa
```

## 📱 Supported Devices

- **Min Android**: 7.0 (API 24)
- **Target Android**: 14 (API 34)
- **ABIs**: arm64-v8a, armeabi-v7a
- **Camera**: Camera2 API compatible

## 🔐 Permissions Required

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## 🏁 Final Checklist

- [ ] Code compiles without errors
- [ ] App runs on device
- [ ] Camera permission granted
- [ ] Toggle works
- [ ] FPS meets target
- [ ] Web viewer displays
- [ ] Git commits are meaningful
- [ ] Documentation complete
- [ ] Screenshots added
- [ ] Pushed to GitHub

---

**Quick Help**: See TROUBLESHOOTING.md for detailed solutions
