 package com.custom.webwallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText urlInput;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Open our secure storage to save/read the URL
        prefs = getSharedPreferences("WallpaperConfig", MODE_PRIVATE);
        urlInput = findViewById(R.id.url_input);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnSet = findViewById(R.id.btn_set_wallpaper);

        // Load saved URL or default to a cool 3D demo
        urlInput.setText(prefs.getString("target_url", "https://threejs.org/"));

        btnSave.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            // Auto-fix URLs missing http://
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://")) {
                url = "https://" + url;
            }
            prefs.edit().putString("target_url", url).apply();
            Toast.makeText(this, "Wallpaper URL Saved!", Toast.LENGTH_SHORT).show();
        });

        // Launch the native Android Live Wallpaper Picker
        btnSet.setOnClickListener(v -> {
            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(this, WebWallpaperService.class));
            startActivity(intent);
        });
    }
}
