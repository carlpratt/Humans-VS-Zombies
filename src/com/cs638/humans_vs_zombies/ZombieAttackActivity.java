package com.cs638.humans_vs_zombies;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.Random;

public class ZombieAttackActivity extends Activity {

    SessionManager session;

    TextView battleOutcomeTextView;

    Intent intent;
    private int playerToBeAttackedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.zombie_attack);

        session = new SessionManager(getApplicationContext());
        intent = this.getIntent();

        battle();
    }

    public void onButtonClick(View view){

        switch (view.getId()){

            case R.id.btnZombieAttack:
                finish();
                break;
        }
    }

    /**
     * Random event to determine if human player is infected in a zombie attack
     *  Correct text needs to be set depending on if a human is being attacked or
     *  if a zombie is attacking a human
     */
    private void battle(){
        Random rand = new Random();
        battleOutcomeTextView = (TextView) findViewById(R.id.attackOutcomeTextView);

        if (rand.nextBoolean() == true){ // If attack is successful
            if (session.getPlayerStatus().get(SessionManager.KEY_STATUS) == false) { // If player is human and loses
                battleOutcomeTextView.setText("You were infected!");
                battleOutcomeTextView.setTextColor(Color.RED);
                session.updateInfected(true); // Player is now infected
                new SendInfectedStatus().execute(); // Send id of infected player to server
            } else { // If player is human and wins
                battleOutcomeTextView.setText("You were NOT infected!");
                battleOutcomeTextView.setTextColor(Color.GREEN);
            }
        } else { // If attack fails
            if (session.getPlayerStatus().get(SessionManager.KEY_STATUS) == true) { // If player is zombie and wins
                battleOutcomeTextView.setText("You infected a player!");
                battleOutcomeTextView.setTextColor(Color.GREEN);
                session.updateInfected(true); // Player is now infected
                new SendInfectedStatus().execute(); // Send id of infected player to server
            } else { // If player is zombie and loses
                battleOutcomeTextView.setText("You did NOT infect the player!");
                battleOutcomeTextView.setTextColor(Color.RED);
            }
        }
    }

    /**
     * Upon infection of a human player, this class sends the id of the infected
     *  user to the server.
     */
    public class SendInfectedStatus extends AsyncTask<String, String, String> {

        JSONParser jsonParser = new JSONParser(); // Performs the http request and returns json data

        /**
         * Before starting background thread
         */
        protected void onPreExecute() {

        }

        /**
         * Sending information to server
         */
        protected String doInBackground(String... args) {

            String url_update = "http://cryptic-spire-5519.herokuapp.com/infected/";
            playerToBeAttackedId = intent.getIntExtra("playerToBeAttackedId", 0);
            url_update += Integer.toString(playerToBeAttackedId);
            url_update += ":" + "true";

            jsonParser.makeHttpRequest(url_update,  "GET");

            Log.d("playerToBeAttackedId", Integer.toString(playerToBeAttackedId));

            return null;
        }

        /**
         * After finishing the background thread
         * *
         */
        protected void onPostExecute(String file_url) {

        }
    }
}
