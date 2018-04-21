package com.example.android.prototype;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    GoogleApiClient mGoogleApiClient;
    public LocationRequest mLocationRequest;
    private double latitude=0;
    private double longitude=0;
    public SupportMapFragment mapFragment;
    Location mLastlocation;
    TextView mTextSpeedometer;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(MapActivity.this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mTextSpeedometer = (TextView)findViewById(R.id.SpeedoMeter);
        mTextSpeedometer.setShadowLayer(3,3,3,R.color.black_overlay);

        if(isLocationEnabled(MapActivity.this)){
            mapFragment.getMapAsync(this);
        }else{
            AlertDialog.Builder dialog = new AlertDialog.Builder(MapActivity.this);
            dialog.setMessage(getResources().getString(R.string.msg));
            dialog.setPositiveButton(getResources().getString(R.string.open_setting), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                }
            });
            dialog.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng s = new LatLng(latitude,longitude);
        Log.v("MAPACTIVITY",String.valueOf(longitude)+"  "+String.valueOf(latitude));
        googleMap.clear();

        //googleMap.addCircle(new CircleOptions().fillColor(2).center(s));
        googleMap.addMarker(new MarkerOptions().icon(circle()).position(s).title("Your Current Position"));
        float zoomLevel = 18.0f;
        googleMap.isTrafficEnabled();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(s,zoomLevel));
    }
    public BitmapDescriptor circle(){
        int d = 40; // diameter
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        p.setColor(getResources().getColor(R.color.colorPrimary));
        c.drawCircle(d/2, d/2, d/2, p);
        BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);
        return  bmD;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() &&
                ContextCompat.checkSelfPermission(MapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,MapActivity.this);
        }
    }
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mGoogleApiClient.isConnected() && ContextCompat.checkSelfPermission(MapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

            mLastlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastlocation != null) {
                latitude=mLastlocation.getLatitude();
                longitude=mLastlocation.getLongitude();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        double speed = 0;
        if (location != null) {
            latitude=location.getLatitude();
            longitude=location.getLongitude();
        }
//        if(mLastlocation!=null){
//            speed = Math.sqrt(Math.pow(location.getLatitude()-mLastlocation.getLatitude(),2)
//                    +Math.pow(location.getLongitude()-mLastlocation.getLongitude(),2))/(location.getTime()-mLastlocation.getTime());
//        }
        speed = location.getSpeed();
        int spee = (int) speed;
        String speedText = String.valueOf(spee)+"ft/s";
        mTextSpeedometer.setText(speedText);
        mapFragment.getMapAsync(this);
    }
}
