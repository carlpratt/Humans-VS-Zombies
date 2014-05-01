package com.cs638.humans_vs_zombies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {

    // Shared Preferences
    SharedPreferences preferences;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared preferences mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "Humans_VS_Zombies_Preferences";

    // User keys (make variables public to access from outside)
    public static final String KEY_ID = "id";
    public static final String KEY_STATUS = "status";
    public static final String KEY_INFECTED = "infected";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        preferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }

    /**
     * Create login session
     * @param id: player id
     * @param status: player zombie status (true for zombie)
     * */
    public void createSession(int id, boolean status){
        // Storing login value as TRUE
        editor.putInt(KEY_ID, id);
        editor.putBoolean(KEY_STATUS, status);

        // commit changes
        editor.commit();
    }

    /**
     * Saves the player's status as either human (false) or zombie (true)
     * @param status
     */
    public void updateStatus(boolean status){
        editor.putBoolean(KEY_STATUS, status);
        editor.commit();
    }

    /**
     * Saves the player's infected status as either true or false
     * @param infected
     */
    public void updateInfected (boolean infected) {
        editor.putBoolean(KEY_INFECTED, infected);
        editor.commit();
    }

    /**
     * Get session player id
     * */
    public HashMap<String, Integer> getPlayerId(){
        HashMap<String, Integer> id = new HashMap<String, Integer>();
        // Player id. Default is 0
        id.put(KEY_ID, preferences.getInt(KEY_ID, 0));

        // return user
        return id;
    }

    /**
     * Get session player status
     */
    public HashMap<String, Boolean> getPlayerStatus(){
        HashMap<String, Boolean> status = new HashMap<String, Boolean>();
        // Player status. Default is not zombie
        status.put(KEY_STATUS, preferences.getBoolean(KEY_STATUS, false));

        // return status
        return status;
    }

    /**
     * Get infection status of player
     */
    public HashMap<String, Boolean> getPlayerInfected(){
        HashMap<String, Boolean> infected = new HashMap<String, Boolean>();
        infected.put(KEY_INFECTED, preferences.getBoolean(KEY_INFECTED, false));
        return infected;
    }

    /**
     * Clear session details
     * */
    public void clearSession(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }
}

