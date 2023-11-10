package com.vgn.revvedup;

public class HelperClass {

    String fname, lname, email, username, password, role;

    public HelperClass(String fname, String lname, String username, String email, String password, String role) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public HelperClass() {
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
