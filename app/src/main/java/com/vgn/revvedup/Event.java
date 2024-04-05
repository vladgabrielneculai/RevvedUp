package com.vgn.revvedup;

import java.util.List;

public class Event {

    private String name, details, startDate, endDate, location, eventImage, eventOwner, eventType;
    private int noLikes, noCars;
    private List<String> modsAllowed, eventCompetitions;

    public Event(String name, String details, String startDate, String endDate, String location, String eventType, String eventImage, String eventOwner, List<String> modsAllowed, List<String> eventCompetitions, int noLikes, int noCars) {
        this.name = name;
        this.details = details;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.eventType = eventType;
        this.eventImage = eventImage;
        this.eventOwner = eventOwner;
        this.modsAllowed = modsAllowed;
        this.eventCompetitions = eventCompetitions;
        this.noLikes = noLikes;
        this.noCars = noCars;
    }

    public Event() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public String getEventOwner() {
        return eventOwner;
    }

    public void setEventOwner(String eventOwner) {
        this.eventOwner = eventOwner;
    }

    public int getNoLikes() {
        return noLikes;
    }

    public void setNoLikes(int noLikes) {
        this.noLikes = noLikes;
    }

    public int getNoCars() {
        return noCars;
    }

    public void setNoCars(int noCars) {
        this.noCars = noCars;
    }

    public List<String> getModsAllowed() {
        return modsAllowed;
    }

    public void setModsAllowed(List<String> modsAllowed) {
        this.modsAllowed = modsAllowed;
    }

    public List<String> getEventCompetitions() {
        return eventCompetitions;
    }

    public void setEventCompetitions(List<String> eventCompetitions) {
        this.eventCompetitions = eventCompetitions;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
