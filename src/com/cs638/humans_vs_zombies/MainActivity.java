package com.cs638.humans_vs_zombies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

public class MainActivity extends Activity {

    private Player player; // Current player

    // Google Map
    private GoogleMap googleMap;

    private LocationManager locationManager; // Updates player position on the map

    private Marker playerMarker; // Marker that follows the player

    private List<Player> otherPlayers = new ArrayList<Player>();
    private List<Marker> otherPlayerMarkers = new ArrayList<Marker>();

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

        googleMap.setMyLocationEnabled(true); // false to disable
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        // Update camera to general madison area
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(43.0711880,-89.4142350)).zoom(12).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        // Create the new player
        //  In the future, we can store this on an on-device SQLite database
        player = new Player(Status.HUMAN); // New player is human
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
                if (player.getStatus() == Status.HUMAN){
                    player.setStatus(Status.ZOMBIE);
                } else {
                    player.setStatus(Status.HUMAN);
                }
                break;
        }
        return true;
    }

    /**
     * Function to load map. If map is not created it will create it for you
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

        public void onLocationChanged(Location location)
        {
            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

            if (onAppStart){

                onAppStart = false;
                // Move the camera to the given location with a specific zoomLevel.
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 19));

                // Zoom in, animating the camera.
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(19), 2000, null);

                playerMarker = googleMap.addMarker(new MarkerOptions().position(myLocation));
            }

            updateMarkerColor();
            playerMarker.setPosition(myLocation);

            player.setCoordinates(myLocation);

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
                //updateGameData();

                removeMarkers(); // Remove all other players markers before reset

                placeMarkers(); // Update location of player markers
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

    /**
     * Update marker colors based on player status.
     */
    private void updateMarkerColor(){
        if (player.getStatus() == Status.HUMAN){
            playerMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            playerMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
    }

    /**
     * Updates game data with positions of other players.
     * Needs to live outside of the location listener to pass along MainActivity
     */
    private void updateGameData(){
        new UpdateGameData(player, this).execute();
    }


    /**
     * Method to be called by the UpdateGameData class for updating other players information
     * @param otherPlayers
     */
    public void updateOtherPlayers(List<Player> otherPlayers){

        this.otherPlayers = otherPlayers;
    }

    /**
     * Places new markers on the map
     */
    private void placeMarkers(){

        for (Player player : otherPlayers){
            otherPlayerMarkers.add(googleMap.addMarker(new MarkerOptions().position(player.getCoordinates())));
        }
    }

    /**
     * Removes all markers so they can be redrawn
     */
    private void removeMarkers(){
        for (Marker marker : otherPlayerMarkers){
            marker.remove();
        }
    }

    /**
     * Checks for nearby players and determines if a ZombieAttackActivity needs to happen
     */
    private void checkProximity(){

        double playerLatitude = playerMarker.getPosition().latitude;
        double playerLongitude = playerMarker.getPosition().longitude;

        double otherPlayerLatitude;
        double otherPlayerLongitude;

        float[] results = new float[3]; //May contain up to 3 elements if bearings are included

        for (Player otherPlayer : otherPlayers) {

            otherPlayerLatitude = otherPlayer.getCoordinates().latitude;
            otherPlayerLongitude = otherPlayer.getCoordinates().longitude;

            // I don't know why they decided to write the method like this...
            Location.distanceBetween(playerLatitude, playerLongitude,
                    otherPlayerLatitude, otherPlayerLongitude, results);

            // If zombie is close enough (less than 5 meters) to player, start attack.
            //  Will need to edit this to include accuracy and current player status (human or zombie)
            if (results[0] <= 5){
                Intent intent = new Intent(getApplicationContext(), ZombieAttackActivity.class);
                startActivity(intent);
            }
        }
    }
}
