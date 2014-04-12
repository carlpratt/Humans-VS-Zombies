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

public class UpdateGameData extends AsyncTask<String, String, String> {

    private static String url_update = "http://cryptic-spire-5519.herokuapp.com/send/";

    private List<Player> players = new ArrayList<Player>();

    JSONParser jsonParser = new JSONParser();

    Player player;
    boolean zombie;

    private MainActivity mainActivity;

    /**
     * Constructor
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
    protected String doInBackground(String... args) {

        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        url_update += player.getId() + ":" +
                zombie + ":" +
                player.getCoordinates().latitude + ":" +
                player.getCoordinates().longitude;

        // getting JSON Object
        JSONObject json = jsonParser.makeHttpRequest(url_update,  "POST");

        try {

            JSONArray jsonPlayers = json.getJSONArray("players");

            for (int i = 0; i < jsonPlayers.length(); i++){
                JSONObject player = jsonPlayers.getJSONObject(i);

                int playerId = player.getInt("id");
                boolean playerStatus = player.getBoolean("zombie");
                double lat = player.getDouble("lat");
                double lon = player.getDouble("lon");

                LatLng playerCoordinates = new LatLng(lat, lon);
                Player p;
                if (playerStatus == true) {
                    p = new Player(playerId, MainActivity.Status.ZOMBIE, playerCoordinates);
                } else if (playerStatus == false){
                    p = new Player(playerId, MainActivity.Status.HUMAN, playerCoordinates);
                } else {
                    p = new Player(MainActivity.Status.HUMAN); // Shouldn't ever get here...
                }

                players.add(p);

            }

            //check log cat for response
            Log.d("Create Response", json.toString());
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
