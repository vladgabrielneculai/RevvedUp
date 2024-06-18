package com.vgn.revvedup;

import java.util.HashMap;
import java.util.List;

public class Event {

    private String name, details, startDate, endDate, location, eventImage, eventOwner, eventType;
    private int noLikes, noCars;
    private List<String> modsAllowed, eventCompetitions;
    private HashMap<String, Boolean> userLikes;
    private List<String> participantsWaitingList, participantsAcceptedList, participantsRejectedList;
    private Double score;


    private Double latitude, longitude;

    public Event(String name, String details, String startDate, String endDate, String location, String eventImage, String eventOwner, String eventType, int noLikes, int noCars, List<String> modsAllowed, List<String> eventCompetitions, HashMap<String, Boolean> userLikes, List<String> participantsWaitingList, List<String> participantsAcceptedList, List<String> participantsRejectedList, Double latitude, Double longitude, Double score) {
        this.name = name;
        this.details = details;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.eventImage = eventImage;
        this.eventOwner = eventOwner;
        this.eventType = eventType;
        this.noLikes = noLikes;
        this.noCars = noCars;
        this.modsAllowed = modsAllowed;
        this.eventCompetitions = eventCompetitions;
        this.userLikes = userLikes;
        this.participantsWaitingList = participantsWaitingList;
        this.participantsAcceptedList = participantsAcceptedList;
        this.participantsRejectedList = participantsRejectedList;
        this.latitude = latitude;
        this.longitude = longitude;
        this.score = score;
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

    public HashMap<String, Boolean> getUserLikes() {
        return userLikes;
    }

    public void setUserLikes(HashMap<String, Boolean> userLikes) {
        this.userLikes = userLikes;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<String> getParticipantsWaitingList() {
        return participantsWaitingList;
    }

    public void setParticipantsWaitingList(List<String> participantsWaitingList) {
        this.participantsWaitingList = participantsWaitingList;
    }

    public List<String> getParticipantsAcceptedList() {
        return participantsAcceptedList;
    }

    public void setParticipantsAcceptedList(List<String> participantsAcceptedList) {
        this.participantsAcceptedList = participantsAcceptedList;
    }

    public List<String> getParticipantsRejectedList() {
        return participantsRejectedList;
    }

    public void setParticipantsRejectedList(List<String> participantsRejectedList) {
        this.participantsRejectedList = participantsRejectedList;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }


}
