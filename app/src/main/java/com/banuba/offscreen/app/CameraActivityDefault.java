package com.banuba.offscreen.app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.banuba.offscreen.app.fragments.CameraFragment;

public class CameraActivityDefault extends BaseCameraActivity {

    @NonNull
    @Override
    protected Fragment getCameraFragment() {
        return CameraFragment.newInstance();
    }
}
