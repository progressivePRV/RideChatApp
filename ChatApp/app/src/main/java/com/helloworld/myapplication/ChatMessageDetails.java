package com.helloworld.myapplication;

import java.util.ArrayList;
import java.util.Date;

public class ChatMessageDetails {
    String Uid;
    String firstname;
    String Message;
    String date;
    ArrayList<String> likedUsers = new ArrayList<>();
    String imageUrl;

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<String> getLikedUsers() {
        return likedUsers;
    }

    public void setLikedUsers(ArrayList<String> likedUsers) {
        this.likedUsers = likedUsers;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "ChatMessageDetails{" +
                "Uid='" + Uid + '\'' +
                ", firstname='" + firstname + '\'' +
                ", Message='" + Message + '\'' +
                ", date='" + date + '\'' +
                ", likedUsers=" + likedUsers +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
