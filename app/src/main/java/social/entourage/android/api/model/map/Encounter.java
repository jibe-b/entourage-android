package social.entourage.android.api.model.map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("unused")
public class Encounter implements Serializable{

    private long id;

    @Expose(serialize = false, deserialize = true)
    private long tourId;

    @SerializedName("date")
    private Date creationDate;

    private double longitude;

    private double latitude;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("street_person_name")
    private String streetPersonName;

    private String message;

    @SerializedName("voice_message")
    private String voiceMessageUrl;

    private String soundCloudPermalinkUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTourId() {
        return tourId;
    }

    public void setTourId(long tourId) {
        this.tourId = tourId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
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

    public String getSoundCloudPermalinkUrl() {
        return soundCloudPermalinkUrl;
    }

    public void setSoundCloudPermalinkUrl(final String permakink) {
        this.soundCloudPermalinkUrl = permakink;
    }
}
