package com.bytedance.camera.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bytedance.camera.demo.utils.Utils;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 101;
    private String[] permissions = new String[] {

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TakePictureActivity.class));
        });

        findViewById(R.id.btn_camera).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RecordVideoActivity.class));
        });

        findViewById(R.id.btn_custom).setOnClickListener(v -> {
            if (Utils.isPermissionsReady(MainActivity.this,permissions))
            {
                startActivity(new Intent(MainActivity.this, CustomCameraActivity.class));
            }
            else
            {
                Utils.reuqestPermissions(MainActivity.this,permissions,REQUEST_EXTERNAL_STORAGE);
            }
        });
    }

}
