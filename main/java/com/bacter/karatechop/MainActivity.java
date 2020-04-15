package com.bacter.karatechop;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    private static final int TIPO_SENSOR = Sensor.TYPE_ACCELEROMETER;
    private pl.droidsonroids.gif.GifImageButton image_button_liga_e_desliga_processo;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Boolean temFlash;
    private Boolean flashDesligado = true;
    private Boolean shakeServiceDesligado;
    private int count = 0;
    private static final String SERVICE_STATE = "serviceState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        image_button_liga_e_desliga_processo = findViewById(R.id.image_button_liga_e_desliga_processo);
        temFlash = getApplicationContext().getPackageManager().hasSystemFeature( PackageManager.FEATURE_CAMERA_FLASH);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(TIPO_SENSOR);

        if (savedInstanceState != null) {
            shakeServiceDesligado = savedInstanceState.getBoolean(SERVICE_STATE);
            if (shakeServiceDesligado) {
                image_button_liga_e_desliga_processo.setImageResource(R.drawable.karate_chop);
            } else {
                image_button_liga_e_desliga_processo.setImageResource(R.drawable.karate_chop);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (MyService.running) {
                shakeServiceDesligado = false;
                image_button_liga_e_desliga_processo.setImageResource(R.drawable.karate_chop);
            } else {
                shakeServiceDesligado = true;
                image_button_liga_e_desliga_processo.setImageResource(R.drawable.karate_chop);
            }
        }

        if (sensor == null) {
            Toast.makeText(MainActivity.this, "Sensor Not Available", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!temFlash) {
            Toast.makeText(MainActivity.this, "Your Device Does Not Have A Flash (lol)", Toast.LENGTH_SHORT).show();
            finish();
        }

        image_button_liga_e_desliga_processo.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (shakeServiceDesligado) {
                    startService(new Intent (MainActivity.this, MyService.class));
                    Toast.makeText(MainActivity.this, "Karate Chop is On", Toast.LENGTH_SHORT).show();
                    image_button_liga_e_desliga_processo.setImageResource(R.drawable.karate_chop);
                    shakeServiceDesligado = false;
                    Log.e("service", "Service Started");
                } else {
                    stopService(new Intent(MainActivity.this, MyService.class));
                    Toast.makeText(MainActivity.this, "Karate Chop is Off", Toast.LENGTH_SHORT).show();
                    image_button_liga_e_desliga_processo.setImageResource(R.drawable.karate_chop);
                    shakeServiceDesligado = true;
                    Log.e("service", "Service Has Stopped");
                }
            }

        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        count++;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    if (count == 2) {
                        onFlashlight();
                        count = 0;
                    }
                }
            }, 500);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SERVICE_STATE, shakeServiceDesligado);
        super.onSaveInstanceState(outState);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void onFlashlight() {
        if (flashDesligado) {
            try {
                CameraManager cameraManager = (CameraManager) getApplicationContext().getSystemService( Context.CAMERA_SERVICE);
                for (String id : cameraManager.getCameraIdList()) {

                    // Turn on the flash if camera has one
                    if (cameraManager.getCameraCharacteristics(id)
                            .get( CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
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
