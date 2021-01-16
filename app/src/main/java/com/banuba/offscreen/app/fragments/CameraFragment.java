package com.banuba.offscreen.app.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.banuba.offscreen.R;
import com.banuba.offscreen.app.BuffersQueue;
import com.banuba.offscreen.app.DemoApplication;
import com.banuba.offscreen.app.camera.CameraWrapper;
import com.banuba.offscreen.app.render.CameraRenderHandler;
import com.banuba.offscreen.app.render.CameraRenderThread;
import com.banuba.sdk.offscreen.OffscreenEffectPlayer;
import com.banuba.sdk.offscreen.OffscreenEffectPlayerConfig;

import static com.banuba.offscreen.app.DemoApplication.BNB_KEY;
import static com.banuba.offscreen.app.DemoApplication.SIZE;


public class CameraFragment extends Fragment {


    private final Context mContext;

    private final OffscreenEffectPlayer mOffscreenEffectPlayer;
    private final CameraWrapper mCamera;
    private final BuffersQueue mBuffersQueue;
    private final ContentObserver mAccelerometerObserver;

    private CameraRenderHandler mRenderHandler;
    private boolean mEffectLoaded;

    public static Fragment newInstance() {
        return new CameraFragment();
    }

    public CameraFragment() {

        mContext = DemoApplication.getAppContext();
        mBuffersQueue = new BuffersQueue();

        final OffscreenEffectPlayerConfig config = OffscreenEffectPlayerConfig.newBuilder(SIZE).build();
        mOffscreenEffectPlayer = new OffscreenEffectPlayer(mContext, config, BNB_KEY, mBuffersQueue);
        mCamera = new CameraWrapper(mContext, mOffscreenEffectPlayer, SIZE, getLifecycle());

        mAccelerometerObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                mOffscreenEffectPlayer.setUseEffectJSRotation(isAndroidOrientationFixed());
            }
        };

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {

        mOffscreenEffectPlayer.setUseEffectJSRotation(isAndroidOrientationFixed());

        final SurfaceView mSurfaceView = view.findViewById(R.id.camera_fragment_surface_view);
        final SurfaceHolder holder = mSurfaceView.getHolder();
        mRenderHandler = new CameraRenderThread(mContext, SIZE, mOffscreenEffectPlayer, holder, mBuffersQueue).startAndGetHandler();

        final ImageButton btnSwitchCamera = view.findViewById(R.id.fragment_camera_btn_switch_camera);
        btnSwitchCamera.setOnClickListener(v -> mCamera.sendSwitchCamera());

        final Button btnEffect = view.findViewById(R.id.fragment_camera_button_show_effect);
        btnEffect.setOnClickListener(v -> {
            if (mEffectLoaded) {
                mOffscreenEffectPlayer.unloadEffect();
                btnEffect.setText(R.string.show_effect);
            } else {
                mOffscreenEffectPlayer.loadEffect("virtual_bg");
                btnEffect.setText(R.string.hide_effect);
            }
            mEffectLoaded = !mEffectLoaded;
        });

    }

    private boolean isAndroidOrientationFixed() {

        final Activity activity = getActivity();
        if (activity != null) {
            if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR ||
                    activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR) {
                return false;
            } else if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                return Settings.System.getInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0;
            }
        }

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRenderHandler.sendShutdown();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mOffscreenEffectPlayer.setUseEffectJSRotation(isAndroidOrientationFixed());
    }

    private void registerAccelerometerObserver() {
        final Uri setting = Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION);
        mContext.getContentResolver().registerContentObserver(setting, false, mAccelerometerObserver);
    }

    private void unregisterAccelerometerObserver() {
        mContext.getContentResolver().unregisterContentObserver(mAccelerometerObserver);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerAccelerometerObserver();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterAccelerometerObserver();
    }

}
