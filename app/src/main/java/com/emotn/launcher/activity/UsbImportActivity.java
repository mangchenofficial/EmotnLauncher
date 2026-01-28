package com.emotn.launcher.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emotn.launcher.R;
import com.emotn.launcher.manager.WallpaperManager;

import java.util.List;

public class UsbImportActivity extends AppCompatActivity {

    private static final String TAG = "UsbImportActivity";

    private WallpaperManager wallpaperManager;
    private ListView usbDeviceList;
    private ListView wallpaperFileList;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_import);

        initViews();
        initManagers();
        scanUsbDevices();
    }

    private void initViews() {
        usbDeviceList = findViewById(R.id.usb_device_list);
        wallpaperFileList = findViewById(R.id.wallpaper_file_list);
        statusText = findViewById(R.id.status_text);
    }

    private void initManagers() {
        wallpaperManager = new WallpaperManager(this);
    }

    private void scanUsbDevices() {
        // 扫描已连接的USB设备
        // 简化实现，使用模拟数据
        String[] usbDevices = {"USB设备1", "USB设备2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, usbDevices);
        usbDeviceList.setAdapter(adapter);

        usbDeviceList.setOnItemClickListener((parent, view, position, id) -> {
            String deviceName = (String) parent.getItemAtPosition(position);
            scanWallpaperFiles(deviceName);
        });
    }

    private void scanWallpaperFiles(String deviceName) {
        // 扫描USB设备中的壁纸文件
        // 简化实现，使用模拟数据
        String[] wallpaperFiles = {"wallpaper1.jpg", "wallpaper2.mp4", "wallpaper3.gif"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wallpaperFiles);
        wallpaperFileList.setAdapter(adapter);

        statusText.setText("已扫描 " + deviceName + " 中的壁纸文件");

        wallpaperFileList.setOnItemClickListener((parent, view, position, id) -> {
            String fileName = (String) parent.getItemAtPosition(position);
            showWallpaperOptions(fileName);
        });
    }

    private void showWallpaperOptions(String fileName) {
        // 显示壁纸操作选项
        // 简化实现，直接设为壁纸
        Toast.makeText(this, "正在导入壁纸: " + fileName, Toast.LENGTH_SHORT).show();
        // 这里应该实现壁纸导入、预览和设置功能
    }

    public void onBackClick(View view) {
        finish();
    }

    public void onRefreshClick(View view) {
        scanUsbDevices();
        Toast.makeText(this, "刷新设备列表", Toast.LENGTH_SHORT).show();
    }
}
