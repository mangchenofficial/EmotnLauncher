package com.emotn.launcher.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.emotn.launcher.R;
import com.emotn.launcher.manager.WallpaperManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UsbReceiver extends BroadcastReceiver {

    private static final String TAG = "UsbReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            // U盘已连接
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                Log.d(TAG, "USB device attached: " + device.getDeviceName());
                handleUsbAttached(context);
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            // U盘已断开
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                Log.d(TAG, "USB device detached: " + device.getDeviceName());
                handleUsbDetached(context);
            }
        }
    }

    private void handleUsbAttached(Context context) {
        // 延迟一秒，确保系统已经挂载U盘
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 扫描所有挂载的USB设备
            List<String> usbPaths = getUsbPaths();
            if (!usbPaths.isEmpty()) {
                // 显示提示信息，询问是否扫描壁纸
                showUsbScanPrompt(context, usbPaths);
            }
        }).start();
    }

    private void handleUsbDetached(Context context) {
        // 显示设备已断开的提示
        Toast.makeText(context, R.string.device_disconnected, Toast.LENGTH_SHORT).show();
    }

    private List<String> getUsbPaths() {
        List<String> usbPaths = new ArrayList<>();

        // 检查所有可能的USB挂载点
        String[] possiblePaths = {
                "/storage/usb0",
                "/storage/usb1",
                "/storage/usbotg",
                "/mnt/usb0",
                "/mnt/usb1",
                "/mnt/usbotg"
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists() && file.isDirectory() && file.canRead()) {
                usbPaths.add(path);
            }
        }

        // 也检查外部存储目录，可能包含USB设备
        File externalStorage = Environment.getExternalStorageDirectory();
        if (externalStorage != null && externalStorage.exists() && externalStorage.isDirectory()) {
            File[] files = externalStorage.getParentFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.exists() && file.isDirectory() && file.canRead() && 
                            (file.getName().startsWith("usb") || file.getName().startsWith("usbotg"))) {
                        usbPaths.add(file.getAbsolutePath());
                    }
                }
            }
        }

        return usbPaths;
    }

    private void showUsbScanPrompt(Context context, List<String> usbPaths) {
        // 这里应该显示一个对话框，询问用户是否扫描U盘中的壁纸
        // 由于是广播接收器，我们需要通过Intent启动一个Activity或Service来显示对话框
        // 简化实现，直接开始扫描
        startUsbScan(context, usbPaths);
    }

    private void startUsbScan(Context context, List<String> usbPaths) {
        // 启动扫描服务
        Intent intent = new Intent(context, UsbScanService.class);
        intent.putStringArrayListExtra("usb_paths", new ArrayList<>(usbPaths));
        context.startService(intent);
    }
}
