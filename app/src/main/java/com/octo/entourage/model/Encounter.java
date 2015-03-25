package com.octo.entourage.model;

import org.joda.time.DateTime;

public class Encounter {

    private long id;

    private DateTime creationDate;

    private double longitude;

    private double latitude;

    private String userId;

    private String userName;

    private String streetPersonName;

    private String message;

    private String voiceMessageUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStreetPersonName() {
        return streetPersonName;
    }

    public void setStreetPersonName(String streetPersonName) {
        this.streetPersonName = streetPersonName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVoiceMessageUrl() {
        return voiceMessageUrl;
    }

    public void setVoiceMessageUrl(String voiceMessageUrl) {
        this.voiceMessageUrl = voiceMessageUrl;
    }
}
