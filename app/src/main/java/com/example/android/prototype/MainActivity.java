package com.example.android.prototype;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wonderkiln.camerakit.CameraView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private float last_x,last_y,last_z;
    private static final int SHAKING_THRESHOLD=1500;
    private long lastUpdate=0;
    CameraView camera;
    ImageView locationImageView;
    final int PERMISSION_CAMERA_GRANTED=1;
    final int PERMISSION_GRANTED_RECORD=2;
    ImageView imageView;
    ImageView cameraFlip;
    TextView mTrackingText;
    SensorManager sensorManager;
    Sensor Accelerator;
    TextView mCameraFlipText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camera = (CameraView) findViewById(R.id.cameraview);
        camera.start();
        camera.setCropOutput(true);
        camera.setPinchToZoom(true);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            camera.setFocusedByDefault(true);
        }
        camera.setFocusable(true);
        camera.setFocusableInTouchMode(true);
        mCameraFlipText = (TextView)findViewById(R.id.text_flip);
        mCameraFlipText.setShadowLayer(3,3,3,R.color.black_overlay);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Accelerator = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,Accelerator,SensorManager.SENSOR_DELAY_NORMAL);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},PERMISSION_CAMERA_GRANTED);
            }
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},PERMISSION_GRANTED_RECORD);
            }
        }
        mTrackingText = (TextView)findViewById(R.id.text_location);
        mTrackingText.setShadowLayer(3,3,3,R.color.black_overlay);
        cameraFlip = (ImageView)findViewById(R.id.CameraFlip);
        cameraFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.toggleFacing();
                setImageWithFlip();
            }
        });
        imageView = (ImageView) findViewById(R.id.CameraButton);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              camera.captureImage();
            }
        });
        locationImageView = (ImageView)findViewById(R.id.location_gps);
        locationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void setImageWithFlip(){
        if(camera.isFacingFront()){
            cameraFlip.setImageResource(R.drawable.ic_undo_white_36dp);
        }
        if(camera.isFacingBack()){
            cameraFlip.setImageResource(R.drawable.ic_redo_white_36dp);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera.start();
        camera.setCropOutput(true);
        sensorManager.registerListener(this,Accelerator,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        camera.stop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
      Sensor mysensor = sensorEvent.sensor;
        float x=0;
        float y=0;
        float z=0;
      if(mysensor.getType()==Sensor.TYPE_ACCELEROMETER){
          x = sensorEvent.values[0];
          y = sensorEvent.values[1];
          z = sensorEvent.values[2];
      }
      long CurrentTime = System.currentTimeMillis();
      if((CurrentTime-lastUpdate)>100){
          long TimeDifference = CurrentTime-lastUpdate;
          lastUpdate=CurrentTime;
          float speed= Math.abs(x+y+z-last_x-last_y-last_z)/TimeDifference*10000;
          if(speed>SHAKING_THRESHOLD){
              camera.toggleFacing();
              setImageWithFlip();
          }
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
