package com.example.hwhong.proximityalert;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

@TargetApi(Build.VERSION_CODES.N)
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private final long MINIMUM_DISTANCE_UPDATE = 1;
    private final long MINIMUM_TIME_UPDATE = 1000;
    private final long POINT_RADIUS = 100;
    private final LatLng DEFAULT = new LatLng(40.764034, -73.974176);

    private final long PROX_ALERT_EXPIRE = -1;
    private final String PROX_ALERT_INTENT = "ProximityAlert";
    private final NumberFormat format = new DecimalFormat("###,###.##");

    private LocationManager manager;
    private ProximityIntentReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_UPDATE,
                MINIMUM_DISTANCE_UPDATE,
                new MyLocationListener()
        );

        addProximityAlert(DEFAULT);

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        drawCircle();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT, 17));
    }

    private void addProximityAlert(LatLng latLng) {

        Intent intent = new Intent(PROX_ALERT_INTENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.addProximityAlert(
                latLng.latitude,
                latLng.longitude,
                POINT_RADIUS,
                PROX_ALERT_EXPIRE,
                pendingIntent
        );

        IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
        receiver = new ProximityIntentReceiver();
        registerReceiver(receiver, filter);

    }

    public class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {

            mMap.clear();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            drawMarkers(latLng);

            drawCircle();

            float distance = getDistanceBetween(latLng);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    private float getDistanceBetween(LatLng latLng) {
        float[] result = new float[1];
        Location.distanceBetween(DEFAULT.latitude, DEFAULT.longitude, latLng.latitude, latLng.longitude, result);
        return result[0];
    }

    private void drawCircle() {
        CircleOptions circle = new CircleOptions();
        circle.center(DEFAULT);
        circle.radius(POINT_RADIUS);
        circle.strokeWidth(5);
        circle.strokeColor(Color.TRANSPARENT);
        circle.fillColor(Color.argb(100, 0 ,0, 255));
        circle.zIndex(3);

        mMap.addCircle(circle);
    }

    private void drawMarkers(LatLng latLng) {

        MarkerOptions marker = new MarkerOptions();
        marker.position(latLng);
        marker.title("Plaze Hotel New York");
        marker.anchor(0.5f, 1.0f);
        marker.draggable(true);

        mMap.addMarker(marker);
    }

    @Override
    protected void onDestroy() {
        if(receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        drawCircle();
        drawMarkers(DEFAULT);

    }

}
