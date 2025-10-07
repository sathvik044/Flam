/**
 * Edge Viewer Web - TypeScript Entry Point
 * Displays processed frames and stats from Android app
 */

interface FrameStats {
  fps: number;
  width: number;
  height: number;
  processingTimeMs: number;
}

class EdgeViewer {
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private fpsElement: HTMLElement;
  private resolutionElement: HTMLElement;
  private processingTimeElement: HTMLElement;

  constructor() {
    this.canvas = document.getElementById('frameCanvas') as HTMLCanvasElement;
    this.ctx = this.canvas.getContext('2d')!;
    this.fpsElement = document.getElementById('fpsValue')!;
    this.resolutionElement = document.getElementById('resolutionValue')!;
    this.processingTimeElement = document.getElementById('processingTime')!;

    this.init();
  }

  private async init(): Promise<void> {
    console.log('Edge Viewer initialized');
    
    // Load sample frame (replace with actual frame from Android)
    await this.loadSampleFrame();
    
    // Simulate stats update (in real app, receive via WebSocket or HTTP)
    this.updateStats({
      fps: 15.2,
      width: 640,
      height: 480,
      processingTimeMs: 42
    });

    // Start animation loop for demo
    this.startDemo();
  }

  private async loadSampleFrame(): Promise<void> {
    try {
      // Try to load sample image
      const img = new Image();
      img.onload = () => {
        this.canvas.width = img.width;
        this.canvas.height = img.height;
        this.ctx.drawImage(img, 0, 0);
      };
      img.onerror = () => {
        // Fallback: draw gradient placeholder
        this.drawPlaceholder();
      };
      img.src = '/sample.png';
    } catch (error) {
      console.error('Failed to load sample frame:', error);
      this.drawPlaceholder();
    }
  }

  private drawPlaceholder(): void {
    const w = this.canvas.width;
    const h = this.canvas.height;
    
    // Draw gradient background
    const gradient = this.ctx.createLinearGradient(0, 0, w, h);
    gradient.addColorStop(0, '#667eea');
    gradient.addColorStop(1, '#764ba2');
    this.ctx.fillStyle = gradient;
    this.ctx.fillRect(0, 0, w, h);

    // Draw edge-like pattern
    this.ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)';
    this.ctx.lineWidth = 2;
    for (let i = 0; i < 20; i++) {
      this.ctx.beginPath();
      this.ctx.moveTo(Math.random() * w, Math.random() * h);
      this.ctx.lineTo(Math.random() * w, Math.random() * h);
      this.ctx.stroke();
    }

    // Draw text
    this.ctx.fillStyle = 'white';
    this.ctx.font = '24px sans-serif';
    this.ctx.textAlign = 'center';
    this.ctx.fillText('Sample Edge Detection Frame', w / 2, h / 2);
    this.ctx.font = '16px sans-serif';
    this.ctx.fillText('Replace sample.png with real frame', w / 2, h / 2 + 30);
  }

  private updateStats(stats: FrameStats): void {
    this.fpsElement.textContent = stats.fps.toFixed(1);
    this.resolutionElement.textContent = `${stats.width}×${stats.height}`;
    this.processingTimeElement.textContent = `${stats.processingTimeMs}ms`;
  }

  private startDemo(): void {
    // Simulate varying FPS for demo
    setInterval(() => {
      const fps = 12 + Math.random() * 6; // 12-18 FPS
      const processingTime = 35 + Math.random() * 20; // 35-55ms
      
      this.updateStats({
        fps,
        width: 640,
        height: 480,
        processingTimeMs: Math.round(processingTime)
      });
    }, 1000);
  }

  /**
   * Update frame from base64 string (for future integration)
   */
  public updateFrameFromBase64(base64: string): void {
    const img = new Image();
    img.onload = () => {
      this.canvas.width = img.width;
      this.canvas.height = img.height;
      this.ctx.drawImage(img, 0, 0);
    };
    img.src = `data:image/png;base64,${base64}`;
  }

  /**
   * Connect to WebSocket for real-time updates (stub for future)
   */
  public connectWebSocket(url: string): void {
    console.log(`WebSocket connection stub: ${url}`);
    // Implementation for real-time frame streaming
    // const ws = new WebSocket(url);
    // ws.onmessage = (event) => { ... };
  }
}

// Initialize viewer when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
  const viewer = new EdgeViewer();
  
  // Expose to window for debugging
  (window as any).edgeViewer = viewer;
});

export { EdgeViewer };
