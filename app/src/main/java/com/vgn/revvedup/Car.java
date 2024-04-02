package com.vgn.revvedup;

public class Car {
    String carBrand, carModel, carRegistration, carOwner;

    public Car(String carBrand, String carModel, String carRegistration, String carOwner) {
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.carRegistration = carRegistration;
        this.carOwner = carOwner;
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

    public void setCarOwner(String carOwner) {
        this.carOwner = carOwner;
    }
}
