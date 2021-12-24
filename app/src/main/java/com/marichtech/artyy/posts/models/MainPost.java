package com.marichtech.artyy.posts.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class MainPost extends PostId {

    private String user_id, image_url, title, desc, price, thumb_url;
    @ServerTimestamp
    private Date created_on;

    public MainPost() {}

    public MainPost(String user_id, String image_url, String title, String desc,String price, String thumb_url, Date created_on) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.title = title;
        this.desc = desc;
        this.price = price;
        this.thumb_url = thumb_url;
        this.created_on = created_on;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }



    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public Date getCreated_on() {
        return created_on;
    }

    public void setCreated_on(Date created_on) {
        this.created_on = created_on;
    }
}
