package com.banuba.offscreen.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.banuba.offscreen.R;
import com.banuba.offscreen.app.fragments.NoCameraFragment;


public abstract class BaseCameraActivity extends AppCompatActivity {

    static final int REQUEST_CAMERA_PERMISSION = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_main);
        checkPermissionAndTakeAction();
    }

    private boolean getCameraPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            showNoCameraFragment();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showNoCameraFragment();
            } else {
                showCameraFragment();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void showCameraFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, getCameraFragment())
                .commitAllowingStateLoss();
    }

    @NonNull
    protected abstract Fragment getCameraFragment();

    private void showNoCameraFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, NoCameraFragment.newInstance())
                .commitAllowingStateLoss();
    }

    private void checkPermissionAndTakeAction() {
        final boolean hasCameraPermission = getCameraPermission();
        if (hasCameraPermission) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, getCameraFragment())
                    .commitAllowingStateLoss();
        } else {
            requestCameraPermission();
        }
    }

}