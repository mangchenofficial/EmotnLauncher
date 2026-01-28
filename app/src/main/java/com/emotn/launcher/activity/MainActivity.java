package com.emotn.launcher.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emotn.launcher.R;
import com.emotn.launcher.activity.WallpaperSettingsActivity;
import com.emotn.launcher.service.WallpaperService;
import com.emotn.launcher.utils.PermissionUtil;
import com.emotn.launcher.utils.PerformanceUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridLayout appGrid;
    private List<ResolveInfo> appList;
    private Handler handler;
    private PerformanceUtil performanceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initServices();
        checkPermissions();
    }

    private void initViews() {
        appGrid = findViewById(R.id.app_grid);
        handler = new Handler();
        
        // 初始化性能监控工具
        performanceUtil = new PerformanceUtil(this);
        performanceUtil.startMonitoring();
        performanceUtil.optimizeStartup();
        performanceUtil.checkCompatibility();
    }

    private void initServices() {
        // 启动壁纸服务
        Intent wallpaperIntent = new Intent(this, WallpaperService.class);
        startService(wallpaperIntent);
    }

    private void checkPermissions() {
        if (PermissionUtil.checkAndRequestPermissions(this)) {
            loadApps();
        }
    }

    private void loadApps() {
        handler.post(() -> {
            PackageManager pm = getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            appList = pm.queryIntentActivities(intent, 0);

            runOnUiThread(() -> {
                populateAppGrid();
            });
        });
    }

    private void populateAppGrid() {
        appGrid.removeAllViews();

        for (ResolveInfo info : appList) {
            View appItem = getLayoutInflater().inflate(R.layout.app_item, null);
            ImageView icon = appItem.findViewById(R.id.app_icon);
            TextView label = appItem.findViewById(R.id.app_label);

            icon.setImageDrawable(info.loadIcon(getPackageManager()));
            label.setText(info.loadLabel(getPackageManager()));

            appItem.setOnClickListener(v -> {
                String packageName = info.activityInfo.packageName;
                String className = info.activityInfo.name;
                launchApp(packageName, className);
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = GridLayout.LayoutParams.WRAP_CONTENT;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMargins(24, 24, 24, 24);

            appGrid.addView(appItem, params);
        }
    }

    private void launchApp(String packageName, String className) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(packageName, className);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法启动应用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtil.handlePermissionResult(requestCode, permissions, grantResults)) {
            loadApps();
        } else {
            Toast.makeText(this, "需要权限才能正常使用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                // 处理向上导航
                navigateUp();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // 处理向下导航
                navigateDown();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                // 处理向左导航
                navigateLeft();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // 处理向右导航
                navigateRight();
                return true;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // 处理OK键确认
                handleOkPress();
                return true;
            case KeyEvent.KEYCODE_BACK:
                // 处理返回键
                handleBackPress();
                return true;
            case KeyEvent.KEYCODE_MENU:
                // 处理菜单键
                handleMenuPress();
                return true;
            case KeyEvent.KEYCODE_HOME:
                // 处理Home键，保持默认行为
                return super.onKeyDown(keyCode, event);
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void navigateUp() {
        // 实现向上导航逻辑
        // 例如：移动焦点到上一个应用图标
        Toast.makeText(this, "向上导航", Toast.LENGTH_SHORT).show();
    }

    private void navigateDown() {
        // 实现向下导航逻辑
        // 例如：移动焦点到下一个应用图标
        Toast.makeText(this, "向下导航", Toast.LENGTH_SHORT).show();
    }

    private void navigateLeft() {
        // 实现向左导航逻辑
        // 例如：移动焦点到左侧应用图标
        Toast.makeText(this, "向左导航", Toast.LENGTH_SHORT).show();
    }

    private void navigateRight() {
        // 实现向右导航逻辑
        // 例如：移动焦点到右侧应用图标
        Toast.makeText(this, "向右导航", Toast.LENGTH_SHORT).show();
    }

    private void handleOkPress() {
        // 实现OK键确认逻辑
        // 例如：启动选中的应用
        Toast.makeText(this, "OK键确认", Toast.LENGTH_SHORT).show();
    }

    private void handleBackPress() {
        // 实现返回键逻辑
        // 例如：退出当前菜单或应用
        Toast.makeText(this, "返回键", Toast.LENGTH_SHORT).show();
    }

    private void handleMenuPress() {
        // 实现菜单键逻辑
        // 例如：显示壁纸设置或应用管理菜单
        Intent intent = new Intent(this, WallpaperSettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止壁纸服务
        Intent wallpaperIntent = new Intent(this, WallpaperService.class);
        stopService(wallpaperIntent);
        
        // 停止性能监控
        if (performanceUtil != null) {
            performanceUtil.stopMonitoring();
        }
    }
}