package com.cs638.humans_vs_zombies;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {

    // Google Map
    private GoogleMap googleMap;

    private LocationManager locationManager; // Updates player position on the map

    private Marker marker; // Marker that follows the player

    private int id;

    private Status playerStatus = Status.HUMAN; // Human or zombie status

    private List<LatLng> coordinates = new ArrayList<LatLng>();

    public enum Status {
        HUMAN,
        ZOMBIE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        try {
            // Loading map
            initializeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Random random = new Random();
        id = random.nextInt(2147483647); //0 through max int


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_change_status:
                if (playerStatus == Status.HUMAN){
                    playerStatus = Status.ZOMBIE;
                } else {
                    playerStatus = Status.HUMAN;
                }
                break;
        }
        return true;
    }

    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void initializeMap() {
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
        initializeMap();
    }

    LocationListener locationListener = new LocationListener()
    {
        double latitude;
        double longitude;
        boolean onAppStart = true;
        int i = 0;
        //Marker marker;

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
            }

            updateMarkerColor();
            marker.setPosition(myLocation);

            i++;
            if (i >= 5){ // Only show toast message and update backend every 5 calls to onLocationChanged
                i = 0;
                latitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
                longitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();

                Float accuracy = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getAccuracy();
                Double altitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getAltitude();

                String coordinates =
                        "Latitude: " + latitude +
                        "\nLongitude: " + longitude +
                        "\nAccuracy: " + accuracy +
                        "\nAltitude: " + altitude;
                Toast.makeText(getApplicationContext(), coordinates, Toast.LENGTH_LONG).show();

                // Can uncomment once backend is working
                updateGameData();
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

    private void updateMarkerColor(){
        if (playerStatus == Status.HUMAN){
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
    }

    /**
     * Method to be called by the UpdateGameData class for updating other players coordinates
     * @param coordinates
     */
    public void setCoordinates(List<LatLng> coordinates){
        this.coordinates = coordinates;
    }

    /**
     * Grabs new data about other players from the backend
     */
    private void updateGameData(){
        new UpdateGameData(this).execute();
    }
}
