package com.bacter.karatechop;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyService extends Service {

    static boolean running;
    private Boolean flashDesligado = true;
    private Vibrator vibe;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    public MyService() {
    }

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        new ThreadService().start();
        return super.onStartCommand(intent, flags, startId);
    }

    public class ThreadService extends Thread {
        @Override
        public void run() {
            vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onShake(int count) {

                    if(count == 2){
                        onFlashlight();
                        mShakeDetector.setmShakeCount(0);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        running = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void onFlashlight() {
        if (flashDesligado) {
            vibe.vibrate(300);
            try {
                CameraManager cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
                for (String id : cameraManager.getCameraIdList()) {

                    // Turn on the flash if camera has one
                    if (cameraManager.getCameraCharacteristics(id)
                            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            cameraManager.setTorchMode(id, true);
                        }
                        flashDesligado = false;
                    }
                }
            } catch (CameraAccessException e) {
                Log.e("tag", "Failed to interact with camera.", e);
                Toast.makeText(getApplicationContext(), "Torch Failed: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            vibe.vibrate(300);
            try {
                CameraManager cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
                for (String id : cameraManager.getCameraIdList()) {

                    // Turn on the flash if camera has one
                    if (cameraManager.getCameraCharacteristics(id)
                            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            cameraManager.setTorchMode(id, false);
                        }
                        flashDesligado = true;
                    }
                }
            } catch (CameraAccessException e) {
                Log.e("tag", "Failed to interact with camera.", e);
                Toast.makeText(getApplicationContext(), "Torch Failed: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
