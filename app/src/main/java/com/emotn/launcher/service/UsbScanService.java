package com.emotn.launcher.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.emotn.launcher.R;
import com.emotn.launcher.manager.WallpaperManager;

import java.util.List;

public class UsbScanService extends IntentService {

    private static final String TAG = "UsbScanService";

    public UsbScanService() {
        super("UsbScanService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            List<String> usbPaths = intent.getStringArrayListExtra("usb_paths");
            if (usbPaths != null && !usbPaths.isEmpty()) {
                scanUsbWallpapers(usbPaths);
            }
        }
    }

    private void scanUsbWallpapers(List<String> usbPaths) {
        Log.d(TAG, "Scanning USB wallpapers from paths: " + usbPaths);

        WallpaperManager wallpaperManager = new WallpaperManager(this);
        int totalWallpapers = 0;

        for (String path : usbPaths) {
            List<WallpaperManager.WallpaperItem> wallpapers = wallpaperManager.scanWallpapers(path);
            if (wallpapers != null && !wallpapers.isEmpty()) {
                totalWallpapers += wallpapers.size();
                Log.d(TAG, "Found " + wallpapers.size() + " wallpapers in " + path);
            }
        }

        if (totalWallpapers > 0) {
            showScanResult(totalWallpapers);
            // 这里可以发送广播，通知主界面更新壁纸列表
            Intent broadcastIntent = new Intent("com.emotn.launcher.USB_WALLPAPERS_SCANNED");
            broadcastIntent.putExtra("total_wallpapers", totalWallpapers);
            sendBroadcast(broadcastIntent);
        } else {
            showNoWallpapersFound();
        }
    }

    private void showScanResult(int totalWallpapers) {
        String message = getString(R.string.usb_scan_wallpaper) + "\n" + 
                getString(R.string.found) + " " + totalWallpapers + " " + getString(R.string.wallpapers);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showNoWallpapersFound() {
        Toast.makeText(this, R.string.no_wallpapers_found, Toast.LENGTH_SHORT).show();
    }
}
