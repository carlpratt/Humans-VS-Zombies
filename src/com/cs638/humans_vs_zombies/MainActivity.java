package com.cs638.humans_vs_zombies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
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

    private Player player; // Current player
    private Marker playerMarker; // Marker that follows the player

    private List<Player> otherPlayers = new ArrayList<Player>();
    private List<Marker> otherPlayerMarkers = new ArrayList<Marker>();
    
    private String locationServiceProvider = LocationManager.NETWORK_PROVIDER; // Location service provider (gps or network)
    private int updatePeriod = 5; // How often user receives location updates

    private SessionManager session;

    private MediaPlayer mPlayer; // Controls sound playback

    private List<Integer> inventory; // Player's inventory containing weapons and first-aid

    private Player playerToBeAttacked; // Player that is going to be attacked by zombie

    private boolean showDevInfo = false; // Lists info like current coordinates and player id

    public enum Status {
        HUMAN,
        ZOMBIE
    }

    /**
     * Creates the map and sets up LocationListener to monitor changing position
     * @param savedInstanceState
     */
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

        // Check which location provider (gps or network) we should be using
        Intent intent = getIntent();

        if (intent.hasExtra("locationServiceProvider")){
            locationServiceProvider = intent.getStringExtra("locationServiceProvider");
        }

        if (locationServiceProvider == LocationManager.NETWORK_PROVIDER){
            updatePeriod = 1; // Network doesn't update location as often as gps
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Toast.makeText(this, "Service Provider: " + locationServiceProvider, Toast.LENGTH_LONG).show(); // For debug

        locationManager.requestLocationUpdates(locationServiceProvider, 0, 0, locationListener);

        // Create player session so they don't get a new id each time they open the app
        session = new SessionManager(getApplicationContext());

        // If no session has been created, make a new one and create a new player
        if (session.getPlayerId().get(SessionManager.KEY_ID) == 0) {
            // Create the new player
            player = new Player(Status.HUMAN); // New player is human
            session.createSession(player.getId(), false);
        } else {
            if (session.getPlayerStatus().get(SessionManager.KEY_STATUS) == false){
                player = new Player(Status.HUMAN);
            } else if (session.getPlayerStatus().get(SessionManager.KEY_STATUS) == true){
                player = new Player(Status.ZOMBIE);
            }
            player.setId(session.getPlayerId().get(SessionManager.KEY_ID));
        }

        ImageView weapons = (ImageView) findViewById(R.id.weaponsImageView);
        ImageView firstAid = (ImageView) findViewById(R.id.firstAidImageView);
        weapons.setImageResource(R.drawable.axe);
        firstAid.setImageResource(R.drawable.antidote);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Options menu. Right now this only allows player to switch status between human and zombie for testing
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_change_status:
                if (player.getStatus() == Status.HUMAN){
                    player.setStatus(Status.ZOMBIE);
                    session.updateStatus(true);
                } else {
                    player.setStatus(Status.HUMAN);
                    session.updateStatus(false);
                }
                break;

            case R.id.action_change_service_provider:
                if (locationServiceProvider == LocationManager.GPS_PROVIDER) {
                    locationServiceProvider = LocationManager.NETWORK_PROVIDER;
                } else {
                    locationServiceProvider = LocationManager.GPS_PROVIDER;
                }
                Intent newServiceProviderIntent = new Intent(getApplicationContext(), MainActivity.class);
                newServiceProviderIntent.putExtra("locationServiceProvider", locationServiceProvider);
                newServiceProviderIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(newServiceProviderIntent);
                break;

            case R.id.action_show_developer_info:
                showDevInfo = true;
                break;

            case R.id.action_hide_developer_info:
                showDevInfo = false;
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
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //initializeMap();
        locationManager.requestLocationUpdates(locationServiceProvider, 0, 0, locationListener);
    }

    public void onButtonClick(View v){
        switch (v.getId()){
            case R.id.btnAttackHuman:
                Intent intent = new Intent(getApplicationContext(), ZombieAttackActivity.class);
                intent.putExtra("playerToBeAttackedId", playerToBeAttacked.getId());
                startActivity(intent);
                hideAttackHumanButton();
                break;
        }
    }

    /**
     * Object to monitor position and provide new coordinate updates.
     * We update the map and other parts of the game when onLocationChanged updates.
     */
    LocationListener locationListener = new LocationListener()
    {
        double latitude;
        double longitude;
        boolean onAppStart = true;
        int updateCounter = 0;

        public void onLocationChanged(Location location)
        {
            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

            // Gotta do the fancy stuff like zoom animate the camera to Madison only
            //  when the app first starts.
            if (onAppStart){

                onAppStart = false;
                // Move the camera to the given location with a specific zoomLevel.
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 19));

                // Zoom in, animating the camera.
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(19), 2000, null);

                playerMarker = googleMap.addMarker(new MarkerOptions().position(myLocation)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.zombie)));
            }

            updatePlayerData();
            playerMarker.setPosition(myLocation);

            player.setCoordinates(myLocation);

            updateCounter++;
            if (updateCounter >= updatePeriod){ // Only show toast message and update backend every n calls to onLocationChanged
                updateCounter = 0;
                latitude = locationManager.getLastKnownLocation(locationServiceProvider).getLatitude();
                longitude = locationManager.getLastKnownLocation(locationServiceProvider).getLongitude();

                Float accuracy = locationManager.getLastKnownLocation(locationServiceProvider).getAccuracy();
                Double altitude = locationManager.getLastKnownLocation(locationServiceProvider).getAltitude();

                if (showDevInfo == true) {
                    String coordinates =
                            "Latitude: " + latitude +
                                    "\nLongitude: " + longitude +
                                    "\nAccuracy: " + accuracy +
                                    "\nAltitude: " + altitude +
                                    "\nID: " + player.getId();
                    Toast.makeText(getApplicationContext(), coordinates, Toast.LENGTH_LONG).show();
                }

                // Once location is given, we pull data from the back end, and update other player markers
                updateGameData();

                removeMarkers(); // Remove all other players markers before reset

                placeMarkers(); // Update location of player markers

                if (player.getStatus() == Status.HUMAN) {
                    humanCheckProximity(); // Play sound effects if zombies are close enough
                } else {
                    zombieCheckProximity(); // Start zombie attack if necessary
                }
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
     * Update player status and maker color
     */
    private void updatePlayerData(){

        if (session.getPlayerStatus().get(SessionManager.KEY_STATUS) == false){
            player.setStatus(Status.HUMAN);
            playerMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.player));
        } else {
            player.setStatus(Status.ZOMBIE);
            playerMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.zombie));
        }
    }

    /**
     * Updates game data with positions of other players.
     * Needs to live outside of the location listener to pass along MainActivity
     */
    private void updateGameData(){
        new UpdateGameData(player, this).execute(player);
    }


    /**
     * Method to be called by the UpdateGameData class for updating other players information
     * @param otherPlayers
     */
    public void updateOtherPlayers(List<Player> otherPlayers){

        this.otherPlayers = otherPlayers;
    }

    /**
     * Places new markers on the map and sets the color according to status
     */
    private void placeMarkers(){

        Marker marker;

        for (Player player : otherPlayers){

            // We already add our own marker to the map. Don't want to do it twice.
            if (player.getId() != session.getPlayerId().get(SessionManager.KEY_ID)) {

                marker = googleMap.addMarker(new MarkerOptions().position(player.getCoordinates()));

                if (player.getStatus() == Status.HUMAN) {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.player));
                } else if (player.getStatus() == Status.ZOMBIE){
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.zombie));
                } else {
                    // Should never get here...
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }

                otherPlayerMarkers.add(marker);
            }
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
     * or if sound effects need to be played
     */
    private void humanCheckProximity(){

        double playerLatitude = playerMarker.getPosition().latitude;
        double playerLongitude = playerMarker.getPosition().longitude;

        double otherPlayerLatitude;
        double otherPlayerLongitude;

        boolean playSound = false; // Play sound if zombies are close enough
        int zombieIntensity = 0; // Number of zombies less than 15 meters from player

        float[] results = new float[3]; //May contain up to 3 elements if bearings are included

        for (Player otherPlayer : otherPlayers) {

            otherPlayerLatitude = otherPlayer.getCoordinates().latitude;
            otherPlayerLongitude = otherPlayer.getCoordinates().longitude;

            // I don't know why they decided to write the method like this...
            Location.distanceBetween(playerLatitude, playerLongitude,
                    otherPlayerLatitude, otherPlayerLongitude, results);

            // Check for longer distance and play sound
            if (results[0] <= 15
                    && otherPlayer.getStatus() == Status.ZOMBIE
                    && otherPlayer.getId() != session.getPlayerId().get(SessionManager.KEY_ID)){

                playSound = true;
                zombieIntensity++;
            }
        }

        // Only play sound if zombies are within 15 meters of human player
        if (playSound && player.getStatus() == Status.HUMAN){
            playZombieSound(zombieIntensity);
        }
    }

    /**
     * Checks for nearby human players and initiates an attack if they are close enough
     */
    private void zombieCheckProximity(){

        List<Player> humanPlayersNearby = new ArrayList<Player>();

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

            // If human is close enough (less than 5 meters) to player, start attack.
            //  Don't compare locations with ourselves! Check for our own player id.
            if (results[0] <= 100
                    && otherPlayer.getStatus() == Status.HUMAN
                    && otherPlayer.getId() != session.getPlayerId().get(SessionManager.KEY_ID)){

                humanPlayersNearby.add(otherPlayer);
            }
        }

        // Display button to start zombie attack if human(s) are close enough
        if (humanPlayersNearby.size() > 0){
            displayAttackHumanButton();

            // Randomly select a player to attack from the list
            Random rand = new Random();
            playerToBeAttacked = humanPlayersNearby.get(rand.nextInt(humanPlayersNearby.size()));
        } else {
            hideAttackHumanButton();
        }
    }

    /**
     * Plays a sound based on how many zombies are nearby the player
     * @param zombieIntensity: number of zombies less than 15 meters from player
     */
    private void playZombieSound(int zombieIntensity){

        if (zombieIntensity > 1) {
            mPlayer = MediaPlayer.create(this, R.raw.enraged_zombies);
        } else {
            mPlayer = MediaPlayer.create(this, pickRandomSound());
        }

        mPlayer.setLooping(false);
        mPlayer.start();
    }

    /**
     * Randomly picks a sound to allow game to have a variety of audio effects
     * @return id of the sound to be played
     */
    private int pickRandomSound(){
        Random random = new Random();

        if (random.nextInt(2) == 0){
            return R.raw.zombie_moan;
        } else {
            return R.raw.zombie_talking;
        }
    }

    /**
     * Puts the 'Attack Human!' button on the fragment
     */
    private void displayAttackHumanButton(){
        final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
        animation.setDuration(500); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
        final Button btn = (Button) findViewById(R.id.btnAttackHuman);
        btn.startAnimation(animation);
    }

    /**
     * Hides the 'Attack Human!' button
     */
    private void hideAttackHumanButton(){
        Button btn = (Button) findViewById(R.id.btnAttackHuman);
        btn.setVisibility(View.GONE);
    }
}
