@echo off
echo ========================================
echo Edge Viewer - Quick Start Script
echo ========================================
echo.

echo [1/5] Checking prerequisites...
where git >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Git not found. Please install Git first.
    pause
    exit /b 1
)
echo ✓ Git found

where node >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo WARNING: Node.js not found. Web viewer will not work.
) else (
    echo ✓ Node.js found
)

echo.
echo [2/5] Initializing Git repository...
if not exist .git (
    git init
    echo ✓ Git repository initialized
) else (
    echo ✓ Git repository already exists
)

echo.
echo [3/5] Creating local.properties...
if not exist local.properties (
    echo Please enter your Android SDK path:
    echo Example: C:\Users\YourName\AppData\Local\Android\Sdk
    set /p SDK_PATH="SDK Path: "
    
    echo Please enter your OpenCV Android SDK path:
    echo Example: C:\dev\OpenCV-android-sdk
    set /p OPENCV_PATH="OpenCV Path: "
    
    (
        echo sdk.dir=%SDK_PATH:\=\\%
        echo opencv_sdk_dir=%OPENCV_PATH:\=\\%
    ) > local.properties
    
    echo ✓ local.properties created
) else (
    echo ✓ local.properties already exists
)

echo.
echo [4/5] Setting up web viewer...
if exist web\package.json (
    cd web
    if not exist node_modules (
        echo Installing npm dependencies...
        call npm install
        if %ERRORLEVEL% equ 0 (
            echo ✓ Web dependencies installed
        ) else (
            echo WARNING: npm install failed
        )
    ) else (
        echo ✓ Web dependencies already installed
    )
    cd ..
) else (
    echo WARNING: web/package.json not found
)

echo.
echo [5/5] Making initial Git commit...
git add .
git status --short
echo.
set /p COMMIT_MSG="Enter commit message (or press Enter for default): "
if "%COMMIT_MSG%"=="" set COMMIT_MSG=Initial project scaffold

git commit -m "%COMMIT_MSG%"
if %ERRORLEVEL% equ 0 (
    echo ✓ Initial commit created
) else (
    echo ℹ No changes to commit or already committed
)

echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Open project in Android Studio
echo 2. Sync Gradle and install NDK/CMake if prompted
echo 3. Run on device (Shift+F10)
echo 4. For web viewer: cd web ^&^& npm run dev
echo.
echo See SETUP.md for detailed instructions.
echo See COMMIT_GUIDE.md for Git workflow.
echo.
pause
