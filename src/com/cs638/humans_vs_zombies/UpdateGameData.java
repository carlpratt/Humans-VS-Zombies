package com.cs638.humans_vs_zombies;

import android.os.AsyncTask;
import com.google.android.gms.maps.model.LatLng;
import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

public class UpdateGameData extends AsyncTask<String, String, String> {

    private MainActivity mainActivity;

    private static String url_update = "";

    private List<LatLng> coordinates = new ArrayList<LatLng>();

    JSONParser jsonParser = new JSONParser();

    /**
     * Constructor
     * @param mainActivity
     */
    public UpdateGameData(MainActivity mainActivity){
        this.mainActivity = mainActivity;
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

        // getting JSON Object
//        JSONObject json = jsonParser.makeHttpRequest(url_update,
//                "POST", params);

        // check log cat for response
        //Log.d("Create Response", json.toString());

        return null;
    }

    /**
     * After completing background task update the list of coordinates in Main Activity
     * *
     */
    protected void onPostExecute(String file_url) {

        mainActivity.setCoordinates(coordinates);
    }
}
