package com.cs638.humans_vs_zombies;

import com.google.android.gms.maps.model.LatLng;
import com.cs638.humans_vs_zombies.MainActivity.Status;

public class Player {

    private Status status;
    private LatLng coordinates;

    public Player(Status status, LatLng coordinates){
        this.status = status;
        this.coordinates = coordinates;
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
}
