package com.emotn.launcher.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.emotn.launcher.R;

import java.io.File;

public class WallpaperService extends Service implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "WallpaperService";

    private WindowManager windowManager;
    private WindowManager.LayoutParams wallpaperParams;
    private View wallpaperView;
    private MediaPlayer mediaPlayer;

    private String currentWallpaperPath;
    private int wallpaperType = WALLPAPER_TYPE_STATIC;
    private int transparency = 100;
    private boolean isMuted = true;
    private boolean isLooping = true;

    public static final int WALLPAPER_TYPE_STATIC = 0;
    public static final int WALLPAPER_TYPE_VIDEO = 1;
    public static final int WALLPAPER_TYPE_GIF = 2;

    private final IBinder binder = new WallpaperBinder();

    public class WallpaperBinder extends Binder {
        public WallpaperService getService() {
            return WallpaperService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initWallpaperWindow();
    }

    private void initWallpaperWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        wallpaperParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                PixelFormat.TRANSLUCENT
        );

        wallpaperParams.gravity = Gravity.TOP | Gravity.START;
        wallpaperParams.windowAnimations = android.R.style.Animation_Dialog;
    }

    public void setWallpaper(String path) {
        currentWallpaperPath = path;
        wallpaperType = detectWallpaperType(path);
        updateWallpaperView();
    }

    public void setTransparency(int value) {
        transparency = Math.max(0, Math.min(100, value));
        if (wallpaperView != null) {
            wallpaperView.setAlpha(transparency / 100f);
        }
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(isMuted ? 0f : 1f);
        }
    }

    public void setLooping(boolean looping) {
        isLooping = looping;
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(isLooping);
        }
    }

    private int detectWallpaperType(String path) {
        if (path == null) return WALLPAPER_TYPE_STATIC;

        String extension = path.toLowerCase();
        if (extension.endsWith(".mp4") || extension.endsWith(".webm") || extension.endsWith(".avi")) {
            return WALLPAPER_TYPE_VIDEO;
        } else if (extension.endsWith(".gif")) {
            return WALLPAPER_TYPE_GIF;
        } else {
            return WALLPAPER_TYPE_STATIC;
        }
    }

    private void updateWallpaperView() {
        // 移除旧的壁纸视图
        if (wallpaperView != null) {
            windowManager.removeView(wallpaperView);
            wallpaperView = null;
        }

        // 停止旧的媒体播放器
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // 创建新的壁纸视图
        switch (wallpaperType) {
            case WALLPAPER_TYPE_VIDEO:
                createVideoWallpaper();
                break;
            case WALLPAPER_TYPE_GIF:
                createGifWallpaper();
                break;
            case WALLPAPER_TYPE_STATIC:
            default:
                createStaticWallpaper();
                break;
        }

        // 添加壁纸视图到窗口管理器
        if (wallpaperView != null) {
            wallpaperView.setAlpha(transparency / 100f);
            windowManager.addView(wallpaperView, wallpaperParams);
        }
    }

    private void createVideoWallpaper() {
        SurfaceView surfaceView = new SurfaceView(this);
        surfaceView.getHolder().addCallback(this);
        wallpaperView = surfaceView;
    }

    private void createStaticWallpaper() {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(Uri.fromFile(new File(currentWallpaperPath)));
        wallpaperView = imageView;
    }

    private void createGifWallpaper() {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(Uri.fromFile(new File(currentWallpaperPath)));
        wallpaperView = imageView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (wallpaperType == WALLPAPER_TYPE_VIDEO && currentWallpaperPath != null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(holder);
            try {
                mediaPlayer.setDataSource(currentWallpaperPath);
                mediaPlayer.setVolume(isMuted ? 0f : 1f);
                mediaPlayer.setLooping(isLooping);
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.e(TAG, "Error preparing media player: " + e.getMessage());
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 处理表面变化
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!isLooping) {
            // 单次播放模式，播放完成后停止
            mp.stop();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wallpaperView != null) {
            windowManager.removeView(wallpaperView);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}