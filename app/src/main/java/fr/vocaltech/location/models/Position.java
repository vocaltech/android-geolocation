package fr.vocaltech.location.models;

import androidx.annotation.NonNull;

public class Position {
    private Coordinates coordinates;
    private long timestamp;
    private String trackId;
    private String userId;

    public Position(Coordinates coordinates, long timestamp, String trackId, String userId) {
        this.coordinates = coordinates;
        this.timestamp = timestamp;
        this.trackId = trackId;
        this.userId = userId;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @NonNull
    @Override
    public String toString() {
        return "Position {" +
                "coordinates: " + coordinates +
                ", timestamp: " + timestamp +
                ", trackId: '" + trackId + '\'' +
                ", userId: '" + userId + '\'' +
                '}';
    }
}
