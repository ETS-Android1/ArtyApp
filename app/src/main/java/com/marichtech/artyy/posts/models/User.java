package com.marichtech.artyy.posts.models;


import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
public class User {
    private String id, name, phone, image_url, thumb_url, token_id, email;
    @ServerTimestamp
    private Date created_on;



    public User() {}

    public User(String id, String name, String phone, String image_url, String thumb_url, String token_id, Date created_on, String email) {


        this.id = id;
        this.name = name;
        this.phone = phone;
        this.image_url = image_url;
        this.thumb_url = thumb_url;
        this.token_id = token_id;
        this.created_on = created_on;
        this.email = email;


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }

    public Date getCreated_on() {
        return created_on;
    }

    public void setCreated_on(Date created_on) {
        this.created_on = created_on;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
