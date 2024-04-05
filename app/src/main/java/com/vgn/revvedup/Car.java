package com.vgn.revvedup;

import android.net.Uri;

import java.util.List;

public class Car {
    String carBrand, carModel, carRegistration, carOwner;
    List<Uri> carPicturesUri;
    List<String> modsApplied;

    public Car(String carBrand, String carModel, String carRegistration, String carOwner, List<Uri> carPicturesUri, List<String> modsApplied) {
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.carRegistration = carRegistration;
        this.carOwner = carOwner;
        this.carPicturesUri = carPicturesUri;
        this.modsApplied = modsApplied;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarRegistration() {
        return carRegistration;
    }

    public void setCarRegistration(String carRegistration) {
        this.carRegistration = carRegistration;
    }

    public String getCarOwner() {
        return carOwner;
    }

    public List<Uri> getCarPicturesUri() {
        return carPicturesUri;
    }

    public void setCarPicturesUri(List<Uri> carPicturesUri) {
        this.carPicturesUri = carPicturesUri;
    }

    public List<String> getModsApplied() {
        return modsApplied;
    }

    public void setModsApplied(List<String> modsApplied) {
        this.modsApplied = modsApplied;
    }

    public void setCarOwner(String carOwner) {
        this.carOwner = carOwner;
    }
}
