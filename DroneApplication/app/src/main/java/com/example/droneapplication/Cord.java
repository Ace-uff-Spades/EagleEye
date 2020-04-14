package com.example.droneapplication;

import java.util.Objects;

public class Cord {
    double latitude;
    double longitude;
    double altitude = 4;
    double yaw;
    public Cord(double latitude, double longitude, double altitude, double yaw){
        this.latitude = latitude;
        this.longitude = longitude;
        if(altitude != 0) {
            this.altitude = altitude;
        }
        this.yaw = yaw;
    }

    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public double getAltitude() {
        return altitude;
    }
    public double getYaw() {
        return yaw;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    @Override
    public String toString() {
        return "Cord{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", yaw=" + yaw +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cord cord = (Cord) o;
        return Double.compare(cord.latitude, latitude) == 0 &&
                Double.compare(cord.longitude, longitude) == 0 &&
                Double.compare(cord.altitude, altitude) == 0 &&
                Double.compare(cord.yaw, yaw) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, altitude, yaw);
    }
}
