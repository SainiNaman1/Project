package com.railway.model;

import java.util.Objects;

/**
 * Represents a railway station in the railway network.
 * Demonstrates ENCAPSULATION.
 */
public class Station {
    private long id;
    private String stationCode;
    private String stationName;
    private String city;
    private String state;

    public Station() {
    }

    public Station(long id, String stationCode, String stationName, String city, String state) {
        this.id = id;
        this.stationCode = stationCode;
        this.stationName = stationName;
        this.city = city;
        this.state = state;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station station)) return false;
        return id == station.id || Objects.equals(stationCode, station.stationCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, stationCode);
    }

    @Override
    public String toString() {
        return stationName + " (" + stationCode + ")";
    }
}
