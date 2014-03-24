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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MyActivity extends Activity {

    // Google Map
    private GoogleMap googleMap;

    private LocationManager mLocManager;


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

        mLocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocListener);
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

    private void moveToLocation(LatLng latlng, int zoomLevel /* Between 2 and 21 */, int animationDuration)
    {
        // Move the camera to the given location with a specific zoomlevel.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomLevel));

        // Zoom in, animating the camera.
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), animationDuration, null);
    }

    LocationListener mLocListener = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

            //googleMap.addMarker(new MarkerOptions().position(myLocation).title("Here I am!"));

            moveToLocation(myLocation, 19, 2000);

            // Preventing repetitive calls to onLocationChanged.
            mLocManager.removeUpdates(this);
            mLocManager.removeUpdates(mLocListener);
        }

        @Override
        public void onProviderDisabled(String provider) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

    };

}
