package com.cs638.humans_vs_zombies;

import com.google.android.gms.maps.model.LatLng;
import com.cs638.humans_vs_zombies.MainActivity.Status;

import java.util.Random;

public class Player {

    private int id;
    private Status status;
    private LatLng coordinates;

    public Player(Status status){
        id = generateId();
        this.status = status;
    }

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
                "status=" + status +
                ", coordinates=" + coordinates +
                '}';
    }

    private int generateId(){
        Random random = new Random();
        id = random.nextInt(2147483647); //0 through max int
        return id;
    }
}
