package com.emotn.launcher.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.emotn.launcher.service.WallpaperService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WallpaperManager {

    private static final String TAG = "WallpaperManager";
    private static final String PREF_NAME = "wallpaper_prefs";
    private static final String KEY_CURRENT_WALLPAPER = "current_wallpaper";
    private static final String KEY_WALLPAPER_TYPE = "wallpaper_type";
    private static final String KEY_TRANSPARENCY = "transparency";
    private static final String KEY_BRIGHTNESS = "brightness";
    private static final String KEY_IS_MUTED = "is_muted";
    private static final String KEY_IS_LOOPING = "is_looping";
    private static final String KEY_WALLPAPER_MODE = "wallpaper_mode";

    public static final int WALLPAPER_MODE_CENTER = 0;
    public static final int WALLPAPER_MODE_STRETCH = 1;
    public static final int WALLPAPER_MODE_TILE = 2;

    private Context context;
    private SharedPreferences preferences;
    private File cacheDir;

    public WallpaperManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.cacheDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "wallpaper_cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public void setCurrentWallpaper(String path, int type) {
        preferences.edit()
                .putString(KEY_CURRENT_WALLPAPER, path)
                .putInt(KEY_WALLPAPER_TYPE, type)
                .apply();
    }

    public String getCurrentWallpaper() {
        return preferences.getString(KEY_CURRENT_WALLPAPER, null);
    }

    public int getCurrentWallpaperType() {
        return preferences.getInt(KEY_WALLPAPER_TYPE, WallpaperService.WALLPAPER_TYPE_STATIC);
    }

    public void setTransparency(int value) {
        preferences.edit().putInt(KEY_TRANSPARENCY, value).apply();
    }

    public int getTransparency() {
        return preferences.getInt(KEY_TRANSPARENCY, 100);
    }

    public void setBrightness(int value) {
        preferences.edit().putInt(KEY_BRIGHTNESS, value).apply();
    }

    public int getBrightness() {
        return preferences.getInt(KEY_BRIGHTNESS, 100);
    }

    public void setMuted(boolean muted) {
        preferences.edit().putBoolean(KEY_IS_MUTED, muted).apply();
    }

    public boolean isMuted() {
        return preferences.getBoolean(KEY_IS_MUTED, true);
    }

    public void setLooping(boolean looping) {
        preferences.edit().putBoolean(KEY_IS_LOOPING, looping).apply();
    }

    public boolean isLooping() {
        return preferences.getBoolean(KEY_IS_LOOPING, true);
    }

    public void setWallpaperMode(int mode) {
        preferences.edit().putInt(KEY_WALLPAPER_MODE, mode).apply();
    }

    public int getWallpaperMode() {
        return preferences.getInt(KEY_WALLPAPER_MODE, WALLPAPER_MODE_CENTER);
    }

    public List<WallpaperItem> scanWallpapers(String directory) {
        List<WallpaperItem> wallpapers = new ArrayList<>();
        File dir = new File(directory);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (isSupportedWallpaper(file)) {
                        WallpaperItem item = createWallpaperItem(file);
                        if (item != null) {
                            wallpapers.add(item);
                        }
                    }
                }
            }
        }
        return wallpapers;
    }

    private boolean isSupportedWallpaper(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
                name.endsWith(".bmp") || name.endsWith(".webp") || name.endsWith(".gif") ||
                name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".avi");
    }

    private WallpaperItem createWallpaperItem(File file) {
        try {
            String path = file.getAbsolutePath();
            String name = file.getName();
            long size = file.length();
            int type = detectWallpaperType(file);
            Bitmap thumbnail = generateThumbnail(file, type);

            return new WallpaperItem(path, name, size, type, thumbnail);
        } catch (Exception e) {
            Log.e(TAG, "Error creating wallpaper item: " + e.getMessage());
            return null;
        }
    }

    private int detectWallpaperType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".avi")) {
            return WallpaperService.WALLPAPER_TYPE_VIDEO;
        } else if (name.endsWith(".gif")) {
            return WallpaperService.WALLPAPER_TYPE_GIF;
        } else {
            return WallpaperService.WALLPAPER_TYPE_STATIC;
        }
    }

    private Bitmap generateThumbnail(File file, int type) {
        try {
            if (type == WallpaperService.WALLPAPER_TYPE_VIDEO) {
                return ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating thumbnail: " + e.getMessage());
            return null;
        }
    }

    public Bitmap getCachedThumbnail(String wallpaperPath) {
        try {
            String fileName = getThumbnailFileName(wallpaperPath);
            File thumbnailFile = new File(cacheDir, fileName);
            if (thumbnailFile.exists()) {
                return BitmapFactory.decodeFile(thumbnailFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting cached thumbnail: " + e.getMessage());
        }
        return null;
    }

    public void cacheThumbnail(String wallpaperPath, Bitmap thumbnail) {
        try {
            String fileName = getThumbnailFileName(wallpaperPath);
            File thumbnailFile = new File(cacheDir, fileName);
            FileOutputStream fos = new FileOutputStream(thumbnailFile);
            thumbnail.compress(Bitmap.CompressFormat.PNG, 80, fos);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Error caching thumbnail: " + e.getMessage());
        }
    }

    private String getThumbnailFileName(String wallpaperPath) {
        return "thumb_" + wallpaperPath.hashCode() + ".png";
    }

    public void clearCache() {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static class WallpaperItem {
        public String path;
        public String name;
        public long size;
        public int type;
        public Bitmap thumbnail;

        public WallpaperItem(String path, String name, long size, int type, Bitmap thumbnail) {
            this.path = path;
            this.name = name;
            this.size = size;
            this.type = type;
            this.thumbnail = thumbnail;
        }
    }
}
