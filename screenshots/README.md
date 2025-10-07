# Screenshots

Add your app screenshots here after testing on device.

## Recommended Screenshots

1. **Raw Camera Feed** - `raw_feed.png`
   - Shows the unprocessed camera view
   
2. **Processed Feed** - `processed_feed.png`
   - Shows the edge-detected output
   
3. **Toggle Button** - `toggle_ui.png`
   - Shows the UI with toggle button
   
4. **Web Viewer** - `web_viewer.png`
   - Shows the TypeScript web viewer

5. **Demo GIF** (optional) - `demo.gif`
   - Shows toggle functionality in action

## How to Capture

### Android Screenshots
```bash
# Take screenshot
adb shell screencap -p /sdcard/screenshot.png

# Pull to computer
adb pull /sdcard/screenshot.png ./screenshots/raw_feed.png
```

### Web Screenshots
- Use browser screenshot tool (F12 → ... → Capture screenshot)
- Or use Windows Snipping Tool

### GIF Recording
- Use screen recorder app on Android
- Convert video to GIF using online tool or ffmpeg:
  ```bash
  ffmpeg -i video.mp4 -vf "fps=10,scale=320:-1:flags=lanczos" demo.gif
  ```

## Update README

After adding screenshots, update the main README.md:

```markdown
## Screenshots

### Android App
![Raw Feed](screenshots/raw_feed.png)
![Processed Feed](screenshots/processed_feed.png)

### Web Viewer
![Web Viewer](screenshots/web_viewer.png)

### Demo
![Demo](screenshots/demo.gif)
```
