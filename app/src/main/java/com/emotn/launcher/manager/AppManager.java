package com.emotn.launcher.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.SharedPreferences;
import android.util.Log;

import com.emotn.launcher.activity.MainActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppManager {

    private static final String TAG = "AppManager";
    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_HIDDEN_APPS = "hidden_apps";
    private static final String KEY_FAVORITE_APPS = "favorite_apps";
    private static final String KEY_APP_ORDER = "app_order";

    private Context context;
    private SharedPreferences preferences;
    private PackageManager packageManager;

    public AppManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.packageManager = context.getPackageManager();
    }

    public List<ResolveInfo> getInstalledApps() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);
        return filterHiddenApps(apps);
    }

    public List<ResolveInfo> getFavoriteApps() {
        Set<String> favoritePackageNames = preferences.getStringSet(KEY_FAVORITE_APPS, new HashSet<>());
        List<ResolveInfo> favoriteApps = new ArrayList<>();
        List<ResolveInfo> allApps = getInstalledApps();

        for (ResolveInfo info : allApps) {
            if (favoritePackageNames.contains(info.activityInfo.packageName)) {
                favoriteApps.add(info);
            }
        }

        return favoriteApps;
    }

    public void addToFavorites(String packageName) {
        Set<String> favoritePackageNames = new HashSet<>(preferences.getStringSet(KEY_FAVORITE_APPS, new HashSet<>()));
        favoritePackageNames.add(packageName);
        preferences.edit().putStringSet(KEY_FAVORITE_APPS, favoritePackageNames).apply();
    }

    public void removeFromFavorites(String packageName) {
        Set<String> favoritePackageNames = new HashSet<>(preferences.getStringSet(KEY_FAVORITE_APPS, new HashSet<>()));
        favoritePackageNames.remove(packageName);
        preferences.edit().putStringSet(KEY_FAVORITE_APPS, favoritePackageNames).apply();
    }

    public boolean isFavorite(String packageName) {
        Set<String> favoritePackageNames = preferences.getStringSet(KEY_FAVORITE_APPS, new HashSet<>());
        return favoritePackageNames.contains(packageName);
    }

    public void hideApp(String packageName) {
        Set<String> hiddenPackageNames = new HashSet<>(preferences.getStringSet(KEY_HIDDEN_APPS, new HashSet<>()));
        hiddenPackageNames.add(packageName);
        preferences.edit().putStringSet(KEY_HIDDEN_APPS, hiddenPackageNames).apply();
    }

    public void unhideApp(String packageName) {
        Set<String> hiddenPackageNames = new HashSet<>(preferences.getStringSet(KEY_HIDDEN_APPS, new HashSet<>()));
        hiddenPackageNames.remove(packageName);
        preferences.edit().putStringSet(KEY_HIDDEN_APPS, hiddenPackageNames).apply();
    }

    public boolean isHidden(String packageName) {
        Set<String> hiddenPackageNames = preferences.getStringSet(KEY_HIDDEN_APPS, new HashSet<>());
        return hiddenPackageNames.contains(packageName);
    }

    private List<ResolveInfo> filterHiddenApps(List<ResolveInfo> apps) {
        Set<String> hiddenPackageNames = preferences.getStringSet(KEY_HIDDEN_APPS, new HashSet<>());
        List<ResolveInfo> filteredApps = new ArrayList<>();

        for (ResolveInfo info : apps) {
            if (!hiddenPackageNames.contains(info.activityInfo.packageName)) {
                filteredApps.add(info);
            }
        }

        return filteredApps;
    }

    public void refreshApps() {
        // 刷新应用列表，通知主界面更新
        Intent intent = new Intent("com.emotn.launcher.APP_LIST_REFRESHED");
        context.sendBroadcast(intent);
    }

    public void uninstallApp(String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(android.net.Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error uninstalling app: " + e.getMessage());
        }
    }

    public void moveApp(String packageName, int newPosition) {
        // 实现应用排序逻辑
        // 这里可以保存应用的排序位置，然后在加载时按照保存的顺序显示
        Log.d(TAG, "Move app " + packageName + " to position " + newPosition);
    }
}
