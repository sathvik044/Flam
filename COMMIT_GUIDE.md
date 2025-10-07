# Git Commit Strategy Guide

⚠️ **CRITICAL**: Proper Git usage is mandatory for evaluation. Single "final commit" submissions will be rejected.

## Recommended Commit Sequence

### 1. Initial Scaffold
```bash
git add README.md .gitignore settings.gradle build.gradle gradle.properties
git commit -m "docs: add README and project configuration"
```

### 2. Android Module Setup
```bash
git add android-app/build.gradle android-app/proguard-rules.pro
git add android-app/src/main/AndroidManifest.xml
git add android-app/src/main/res/
git commit -m "feat: add Android app module with Gradle config"
```

### 3. NDK/JNI Integration
```bash
git add android-app/src/main/cpp/CMakeLists.txt
git add android-app/src/main/cpp/native-lib.cpp
git add android-app/src/main/java/com/example/edge/NativeLib.kt
git commit -m "feat: add JNI bridge and C++ native processing stub"
```

### 4. OpenCV Integration
```bash
git add android-app/src/main/cpp/native-lib.cpp
git add local.properties.template
git commit -m "feat: integrate OpenCV for Canny edge detection"
```

### 5. OpenGL Renderer
```bash
git add android-app/src/main/java/com/example/edge/gl/
git commit -m "feat: implement OpenGL ES 2.0 renderer with texture mapping"
```

### 6. Camera Integration
```bash
git add android-app/src/main/java/com/example/edge/camera/
git commit -m "feat: add Camera2 API integration with ImageReader"
```

### 7. UI and Activity
```bash
git add android-app/src/main/java/com/example/edge/MainActivity.kt
git add android-app/src/main/java/com/example/edge/ui/
git commit -m "feat: add MainActivity with GLSurfaceView and toggle button"
```

### 8. Web Viewer
```bash
git add web/
git commit -m "feat: add TypeScript web viewer with Vite"
```

### 9. Documentation
```bash
git add SETUP.md COMMIT_GUIDE.md
git commit -m "docs: add setup instructions and commit guide"
```

### 10. Testing & Polish
```bash
git add <files>
git commit -m "fix: improve frame processing performance"

git add <files>
git commit -m "feat: add FPS counter overlay"

git add <files>
git commit -m "docs: add screenshots and architecture diagram"
```

## Commit Message Format

Use conventional commits:

- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation only
- `refactor:` - Code refactoring
- `perf:` - Performance improvement
- `test:` - Adding tests
- `chore:` - Build/tooling changes

## Example Good Commits

✅ `feat: add Canny edge detection with OpenCV`
✅ `fix: resolve camera permission crash on Android 13`
✅ `perf: optimize frame buffer allocation in JNI`
✅ `docs: update README with OpenCV setup instructions`

## Example Bad Commits

❌ `update`
❌ `final version`
❌ `changes`
❌ `asdfasdf`

## Pushing to GitHub

```bash
# Create repo on GitHub first, then:
git remote add origin https://github.com/yourusername/edge-viewer.git
git branch -M main
git push -u origin main
```

## Verification

Before submission, verify:

```bash
# Check commit history
git log --oneline

# Should show multiple meaningful commits, not just 1-2
```

## Tips

- Commit after each working feature/module
- Write clear, descriptive messages
- Don't commit generated files (build/, .gradle/, node_modules/)
- Use .gitignore properly
- Push regularly to avoid losing work
