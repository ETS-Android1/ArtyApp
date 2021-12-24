package com.marichtech.artyy.posts.models;

import java.io.Serializable;

public class SingleProductModel implements Serializable {

    private int no_of_items;
    private String artistmobile, userid, useremail,usermobile,prname,prprice, primage_image, primage_thumb,prdesc;

    public SingleProductModel() {
    }

    public SingleProductModel(String artistmobile, String userid, int no_of_items, String useremail, String usermobile, String prname, String prprice, String primage_image, String primage_thumb, String prdesc) {
       this.artistmobile =artistmobile;
        this.userid = userid;
        this.no_of_items = no_of_items;
        this.useremail = useremail;
        this.usermobile = usermobile;
        this.prname = prname;
        this.prprice = prprice;
        this.primage_image = primage_image;
        this.primage_thumb = primage_thumb;
        this.prdesc = prdesc;
    }

    public String getArtistmobile() {
        return artistmobile;
    }

    public void setArtistmobile(String artistmobile) {
        this.artistmobile = artistmobile;
    }

    public String getUsermobile() {
        return usermobile;
    }

    public void setUsermobile(String usermobile) {
        this.usermobile = usermobile;
    }

    public int getNo_of_items() {
        return no_of_items;
    }

    public void setNo_of_items(int no_of_items) {
        this.no_of_items = no_of_items;
    }

    public String getPrdesc() {
        return prdesc;
    }

    public void setPrdesc(String prdesc) {
        this.prdesc = prdesc;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUseremail() {
        return useremail;
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }

    public String getPrname() {
        return prname;
    }

    public void setPrname(String prname) {
        this.prname = prname;
    }

    public String getPrprice() {
        return prprice;
    }

    public void setPrprice(String prprice) {
        this.prprice = prprice;
    }

    public String getPrimage_image() {
        return primage_image;
    }

    public void setPrimage_image(String primage_image) {
        this.primage_image = primage_image;
    }

    public String getPrimage_thumb() {
        return primage_thumb;
    }

    public void setPrimage_thumb(String primage_thumb) {
        this.primage_thumb = primage_thumb;
    }

}
