package com.cs638.humans_vs_zombies;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;

public class MyActivity extends Activity {

    // Google Map
    private GoogleMap googleMap;

    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        try {
            // Loading map
            initilizeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap.setMyLocationEnabled(true); // false to disable
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        // Update camera to general madison area
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(43.0711880,-89.4142350)).zoom(12).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
    }

    LocationListener locationListener = new LocationListener()
    {
        double latitude;
        double longitude;
        boolean onAppStart = true;
        int i = 0;
        Marker marker;

        public void onLocationChanged(Location location)
        {
            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

            if (onAppStart){

                onAppStart = false;
                // Move the camera to the given location with a specific zoomLevel.
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 19));

                // Zoom in, animating the camera.
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(19), 2000, null);

                marker = googleMap.addMarker(new MarkerOptions().position(myLocation));
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }

            marker.setPosition(myLocation);

            i++;
            if (i >= 5){ // Only show toast message every 5 calls to onLocationChanged
                i = 0;
                latitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
                longitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();

                String coordinates = "Latitude: " + latitude + "\nLongitude: " + longitude;
                Toast.makeText(getApplicationContext(), coordinates, Toast.LENGTH_SHORT).show();
            }

            // Preventing repetitive calls to onLocationChanged.
            //locationManager.removeUpdates(this);
            //locationManager.removeUpdates(locationListener);
        }

        @Override
        public void onProviderDisabled(String provider) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

    };
}
