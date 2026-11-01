package com.delta.assist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private TextView status;
    private final ActivityResultLauncher<Intent> projLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
        if (res.getResultCode() != RESULT_OK || res.getData() == null) { status.setText("Yakalama reddedildi"); return; }
        Intent svc = new Intent(this, CaptureService.class).putExtra(CaptureService.EXTRA_RESULT_CODE, res.getResultCode()).putExtra(CaptureService.EXTRA_RESULT_DATA, res.getData());
        startForegroundService(svc);
        status.setText("Yakalama aktif");
    });

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        status = findViewById(R.id.tv_status);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        findViewById(R.id.btn_overlay).setOnClickListener(v -> {
            if (Settings.canDrawOverlays(this)) { status.setText("Overlay hazir"); return; }
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        });
        findViewById(R.id.btn_access).setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        findViewById(R.id.btn_start).setOnClickListener(v -> {
            MediaProjectionManager m = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            projLauncher.launch(m.createScreenCaptureIntent());
        });
    }
}
