 package com.custom.webwallpaper;

import android.service.wallpaper.WallpaperService;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.app.Presentation;
import android.view.SurfaceHolder;
import android.view.MotionEvent;
import android.webkit.WebView;

public class WebWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new WebEngine();
    }

    private class WebEngine extends Engine {
        private VirtualDisplay virtualDisplay;
        private Presentation presentation;
        private WebView webView;

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            
            // Clean up previous instances if the screen rotates or resizes
            if (presentation != null) presentation.dismiss();
            if (virtualDisplay != null) virtualDisplay.release();

            // 1. Create a Virtual Display tied to the Wallpaper's Canvas Surface
            DisplayManager dm = (DisplayManager) getSystemService(DISPLAY_SERVICE);
            virtualDisplay = dm.createVirtualDisplay("WebWallpaper", 
                width, height, 300, 
                holder.getSurface(), 0);

            // 2. Create a Presentation targeting that Virtual Display
            presentation = new Presentation(WebWallpaperService.this, virtualDisplay.getDisplay());
            
            // 3. Initialize the hardware-accelerated WebView
            webView = new WebView(presentation.getContext());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            
            // Read the URL saved by MainActivity
            SharedPreferences prefs = getSharedPreferences("WallpaperConfig", MODE_PRIVATE);
            String targetUrl = prefs.getString("target_url", "https://threejs.org/");
            
            webView.loadUrl(targetUrl);
            
            // Pipe the WebView into the Presentation layer
            presentation.setContentView(webView);
            presentation.show();
        }

        // Route touch events from the Home Screen directly to the WebView (for interactive particles, etc.)
        @Override
        public void onTouchEvent(MotionEvent event) {
            if (webView != null) {
                webView.dispatchTouchEvent(event);
            }
            super.onTouchEvent(event);
        }

        // Pause rendering when an app is opened to save battery
        @Override
        public void onVisibilityChanged(boolean visible) {
            if (webView != null) {
                if (visible) {
                    webView.onResume();
                } else {
                    webView.onPause(); 
                }
            }
        }
        
        // Clean up memory when a new wallpaper is applied
        @Override
        public void onDestroy() {
            if (presentation != null) presentation.dismiss();
            if (virtualDisplay != null) virtualDisplay.release();
            if (webView != null) webView.destroy();
            super.onDestroy();
        }
    }
}
