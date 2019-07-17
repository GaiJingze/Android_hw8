package com.bytedance.camera.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import com.bytedance.camera.demo.utils.Utils;

public class RecordVideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_EXTERNAL_CAMERA = 101;
    String[] permissions = new String[] {

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_video);
        videoView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {

            if (Utils.isPermissionsReady(this, permissions)) {
                openVideoRecordApp();
            } else {
                Utils.reuqestPermissions(this, permissions, REQUEST_EXTERNAL_CAMERA);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            videoView.setVideoURI(videoUri);
            videoView.start();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_CAMERA: {
                if (Utils.isPermissionsReady(this, permissions)) {
                    openVideoRecordApp();
                }
                break;
            }
        }
    }

    private void openVideoRecordApp() {
        Intent takeViderIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(takeViderIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takeViderIntent,REQUEST_VIDEO_CAPTURE);
        }
    }
}
