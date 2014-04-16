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

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        preferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }

    /**
     * Create login session
     * */
    public void createSession(int id){
        // Storing login value as TRUE
        editor.putInt(KEY_ID, id);

        // commit changes
        editor.commit();
    }

    /**
     * Get session player id
     * */
    public HashMap<String, Integer> getPlayerId(){
        HashMap<String, Integer> player = new HashMap<String, Integer>();
        // Player id. Default is 0
        player.put(KEY_ID, preferences.getInt(KEY_ID, 0));

        // return user
        return player;
    }

    /**
     * Get session player status
     */
    public HashMap<String, Boolean> getPlayerStatus(){
        HashMap<String, Boolean> player = new HashMap<String, Boolean>();
        // Player id. Default is 0
        player.put(KEY_STATUS, preferences.getBoolean(KEY_STATUS, false));

        // return status
        return player;
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
