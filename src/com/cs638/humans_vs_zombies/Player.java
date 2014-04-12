package com.cs638.humans_vs_zombies;

import com.google.android.gms.maps.model.LatLng;
import com.cs638.humans_vs_zombies.MainActivity.Status;

import java.util.Random;

public class Player {

    private int id;
    private Status status;
    private LatLng coordinates;

    /**
     * Constructor used when a new player starts playing the game.
     * They will have no id so one will need to be generated for them.
     * @param status
     */
    public Player(Status status){
        id = generateId();
        this.status = status;
    }

    /**
     * Constructor used when pulling data from the back end.
     * All data about this player will already be known.
     * This should only be used for creating data transfer objects.
     * @param id
     * @param status
     * @param coordinates
     */
    public Player(int id, Status status, LatLng coordinates){
        this.id = id;
        this.status = status;
        this.coordinates = coordinates;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id = " + id +
                "status=" + status +
                ", coordinates=" + coordinates +
                '}';
    }

    private int generateId(){
        Random random = new Random();
        id = random.nextInt(2147483647); //0 through max int

        while (id == 0){ // Nobody gets to be 0! It is our default value for session manager.
            id = random.nextInt(2147483647);
        }
        return id;
    }
}
