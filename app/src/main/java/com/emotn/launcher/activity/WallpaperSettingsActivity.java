package com.emotn.launcher.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.emotn.launcher.R;
import com.emotn.launcher.manager.WallpaperManager;
import com.emotn.launcher.service.WallpaperService;

import java.util.List;

public class WallpaperSettingsActivity extends AppCompatActivity {

    private static final String TAG = "WallpaperSettingsActivity";

    private WallpaperService wallpaperService;
    private boolean isServiceBound = false;
    private WallpaperManager wallpaperManager;

    private ListView wallpaperList;
    private SeekBar transparencyBar;
    private SeekBar brightnessBar;
    private Switch muteSwitch;
    private Switch loopSwitch;
    private TextView transparencyValue;
    private TextView brightnessValue;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            WallpaperService.WallpaperBinder binder = (WallpaperService.WallpaperBinder) service;
            wallpaperService = binder.getService();
            isServiceBound = true;
            updateWallpaperSettings();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_settings);

        initViews();
        initManagers();
        loadWallpapers();
        setupListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, WallpaperService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    private void initViews() {
        wallpaperList = findViewById(R.id.wallpaper_list);
        transparencyBar = findViewById(R.id.transparency_bar);
        brightnessBar = findViewById(R.id.brightness_bar);
        muteSwitch = findViewById(R.id.mute_switch);
        loopSwitch = findViewById(R.id.loop_switch);
        transparencyValue = findViewById(R.id.transparency_value);
        brightnessValue = findViewById(R.id.brightness_value);
    }

    private void initManagers() {
        wallpaperManager = new WallpaperManager(this);
    }

    private void loadWallpapers() {
        // 加载已导入的壁纸列表
        // 这里应该从壁纸管理器中获取壁纸列表
        // 简化实现，使用模拟数据
        String[] wallpapers = {"默认壁纸", "壁纸1", "壁纸2", "壁纸3"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wallpapers);
        wallpaperList.setAdapter(adapter);

        wallpaperList.setOnItemClickListener((parent, view, position, id) -> {
            // 选择壁纸并设置
            String wallpaperName = (String) parent.getItemAtPosition(position);
            setWallpaper(wallpaperName);
        });
    }

    private void setupListeners() {
        transparencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isServiceBound) {
                    wallpaperService.setTransparency(progress);
                    wallpaperManager.setTransparency(progress);
                    transparencyValue.setText(progress + "%");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    wallpaperManager.setBrightness(progress);
                    brightnessValue.setText(progress + "%");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        muteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isServiceBound) {
                wallpaperService.setMuted(isChecked);
                wallpaperManager.setMuted(isChecked);
            }
        });

        loopSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isServiceBound) {
                wallpaperService.setLooping(isChecked);
                wallpaperManager.setLooping(isChecked);
            }
        });
    }

    private void updateWallpaperSettings() {
        // 更新壁纸设置UI
        int transparency = wallpaperManager.getTransparency();
        int brightness = wallpaperManager.getBrightness();
        boolean isMuted = wallpaperManager.isMuted();
        boolean isLooping = wallpaperManager.isLooping();

        transparencyBar.setProgress(transparency);
        brightnessBar.setProgress(brightness);
        muteSwitch.setChecked(isMuted);
        loopSwitch.setChecked(isLooping);

        transparencyValue.setText(transparency + "%");
        brightnessValue.setText(brightness + "%");
    }

    private void setWallpaper(String wallpaperName) {
        // 设置壁纸逻辑
        // 这里应该根据壁纸名称找到对应的壁纸文件路径
        // 简化实现，使用默认壁纸
        if (isServiceBound) {
            // 这里应该设置实际的壁纸路径
            wallpaperService.setWallpaper(null);
        }
    }

    public void onBackClick(View view) {
        finish();
    }

    public void onImportFromUsbClick(View view) {
        // 从U盘导入壁纸
        Intent intent = new Intent(this, UsbImportActivity.class);
        startActivity(intent);
    }
}
