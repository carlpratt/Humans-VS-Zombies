package com.cs638.humans_vs_zombies;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs the http operations on our back end server.
 * The primary function of this class is to push up the current player's status and coordinates, and
 * to pull in the status and coordinates of all other players so MainActivity can update the map.
 */
public class UpdateGameData extends AsyncTask<Player, String, String> {


    private List<Player> players = new ArrayList<Player>(); // Pulled data will be parsed into this list

    JSONParser jsonParser = new JSONParser(); // Performs the http request and returns json data

    Player player;
    boolean zombie;

    private MainActivity mainActivity;

    /**
     * Constructor
     * @param player: Current player's data to push up to the back end.
     * @param mainActivity: Activity to send the pulled data to.
     */
    public UpdateGameData(Player player, MainActivity mainActivity){

        this.player = player;
        this.mainActivity = mainActivity;

        if (player.getStatus() == MainActivity.Status.ZOMBIE){
            zombie = true;
        } else {
            zombie = false;
        }
    }

    /**
     * Before starting background thread
     */
    protected void onPreExecute() {

    }

    /**
     * Performing query to database
     */
    protected String doInBackground(Player... args) {

        String url_update = "http://cryptic-spire-5519.herokuapp.com/send/";

        Player playerArgs = args[0];

        url_update += player.getId() + ":" +
                zombie + ":" +
                playerArgs.getCoordinates().latitude + ":" +
                playerArgs.getCoordinates().longitude;

        // getting JSON Object
        JSONObject json = jsonParser.makeHttpRequest(url_update,  "GET");

        try {

            //check log cat for response
            Log.d("Create Response", json.toString());

            JSONArray jsonPlayers = json.getJSONArray("data");

            for (int i = 0; i < jsonPlayers.length(); i++){
                JSONObject player = jsonPlayers.getJSONObject(i);

                int playerId = player.getInt("userID");
                boolean playerZombieStatus = player.getBoolean("zombie");
                double lat = player.getDouble("lat");
                double lon = player.getDouble("lon");

                LatLng playerCoordinates = new LatLng(lat, lon);
                Player p;
                if (playerZombieStatus == true) {
                    p = new Player(playerId, MainActivity.Status.ZOMBIE, playerCoordinates);
                } else if (playerZombieStatus == false){
                    p = new Player(playerId, MainActivity.Status.HUMAN, playerCoordinates);
                } else {
                    p = new Player(MainActivity.Status.HUMAN); // Shouldn't ever get here...
                }

                players.add(p);

            }
        }
        catch (JSONException e){

        }

        return null;
    }

    /**
     * After completing background task update the list of coordinates in Main Activity
     * *
     */
    protected void onPostExecute(String file_url) {

        mainActivity.updateOtherPlayers(players);
    }
}
