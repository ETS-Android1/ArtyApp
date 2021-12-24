package com.marichtech.artyy.sessions;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;

public class UserSession {

    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "UserSessionPref";

    // First name (make variable public to access from outside)
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";

    public static final String KEY_ARTIST = "false";

    // user avatar (make variable public to access from outside)
    public static final String KEY_IMAGE = "image";
    public static final String KEY_THUMB = "thumb";

    // First time logic Check
    public static final String FIRST_TIME = "firsttime";

    // number of items in our cart
    public static final String KEY_CART = "cartvalue";

    // number of items in our wishlist
    public static final String KEY_WISHLIST = "wishlistvalue";

    // check first time app launch
    public static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";



    // Constructor
    public UserSession(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public Boolean  getRole() {
        return pref.getBoolean(KEY_ARTIST, true);
    }

    public void setRole(Boolean n){
        editor.putBoolean(KEY_ARTIST,n);
        editor.commit();
    }


    /**
     * Create login session
     * */
    public void createUserSession(String name,  String email, String number, String image, String thumb){

        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, number);
        editor.putString(KEY_NAME, name);

        // Storing image url in pref
        editor.putString(KEY_IMAGE, image);
        editor.putString(KEY_THUMB, thumb);

        // commit changes
        editor.commit();
    }

    public void updateName(String name){
        editor.putString(KEY_NAME, name);
        // commit changes
        editor.commit();
    }

    public void updatePhone(String number){
        editor.putString(KEY_PHONE, number);
        // commit changes
        editor.commit();
    }

    public void updateProfileImage(String image, String thumb){
        // Storing image url in pref
        editor.putString(KEY_IMAGE, image);
        editor.putString(KEY_THUMB, thumb);
        // commit changes
        editor.commit();
    }



    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserSession(){
        HashMap<String, String> user = new HashMap<>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, "name"));

        user.put(KEY_PHONE, pref.getString(KEY_PHONE, "phone"));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, "email"));

        // user avatar
        user.put(KEY_IMAGE, pref.getString(KEY_IMAGE, "image")) ;
        user.put(KEY_THUMB, pref.getString(KEY_THUMB, "thumb")) ;

        // return user
        return user;
    }


    public int getCartValue(){
        return pref.getInt(KEY_CART,0);
    }

    public int getWishlistValue(){
        return pref.getInt(KEY_WISHLIST,0);
    }

    public Boolean  getFirstTime() {
        return pref.getBoolean(FIRST_TIME, true);
    }

    public void setFirstTime(Boolean n){
        editor.putBoolean(FIRST_TIME,n);
        editor.commit();
    }


    public void increaseCartValue(){
        int val = getCartValue()+1;
        editor.putInt(KEY_CART,val);
        editor.commit();
        Log.e("Cart Value PE", "Var value : "+val+"Cart Value :"+getCartValue()+" ");
    }

    public void increaseWishlistValue(){
        int val = getWishlistValue()+1;
        editor.putInt(KEY_WISHLIST,val);
        editor.commit();
        Log.e("Cart Value PE", "Var value : "+val+"Cart Value :"+getCartValue()+" ");
    }

    public void decreaseCartValue(){
        int val = getCartValue()-1;
        editor.putInt(KEY_CART,val);
        editor.commit();
        Log.e("Cart Value PE", "Var value : "+val+"Cart Value :"+getCartValue()+" ");
    }

    public void decreaseWishlistValue(){
        int val = getWishlistValue()-1;
        editor.putInt(KEY_WISHLIST,val);
        editor.commit();
        Log.e("Cart Value PE", "Var value : "+val+"Cart Value :"+getCartValue()+" ");
    }

    public void setCartValue(int count){
        editor.putInt(KEY_CART,count);
        editor.commit();
    }

    public void setWishlistValue(int count){
        editor.putInt(KEY_WISHLIST,count);
        editor.commit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

}
