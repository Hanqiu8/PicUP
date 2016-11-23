package com.hq.picup;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by HQ on 11/21/2016.
 */
@IgnoreExtraProperties
public class PictureInfo {
    private String url;
    private double longitude;
    private double latitude;
    private int vote;
    private long time;

    public PictureInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public PictureInfo(String url, double longitude, double latitude, int vote, long time) {
        this.url = url;
        this.longitude = longitude;
        this.latitude = latitude;
        this.vote = vote;
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getUrl() {
        return url;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getVote() {
        return vote;
    }

    public long getTime(){return time;}
}
