# Edge Viewer - Web Component

Minimal TypeScript web viewer for displaying processed frames from the Android app.

## Setup

```bash
npm install
```

## Development

```bash
npm run dev
```

Opens at http://localhost:5173

## Build

```bash
npm run build
```

Output in `dist/`

## Usage

- Replace `public/sample.png` with an actual processed frame from the Android app
- The viewer displays the frame and simulates FPS/stats updates
- Extend `EdgeViewer.connectWebSocket()` for real-time streaming

## Architecture

- `src/index.ts`: Main viewer class, handles canvas rendering and stats updates
- `index.html`: UI layout with modern styling
- `vite.config.ts`: Vite bundler configuration
