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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}