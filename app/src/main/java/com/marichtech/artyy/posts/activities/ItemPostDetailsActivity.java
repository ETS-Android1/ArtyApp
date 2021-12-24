package com.marichtech.artyy.posts.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marichtech.artyy.R;
import com.marichtech.artyy.activity.ui.ProfileActivity;
import com.marichtech.artyy.cart.CartActivity;
import com.marichtech.artyy.network.CheckInternetConnection;
import com.marichtech.artyy.posts.models.SingleProductModel;
import com.marichtech.artyy.posts.notification.NotificationActivity;
import com.marichtech.artyy.sessions.UserSession;

import es.dmoral.toasty.Toasty;

public class ItemPostDetailsActivity extends AppCompatActivity {


    ImageView productimage;
    TextView productname, productprice, addToCart, buyNow, productdesc;
    EditText quantityProductPage;
    LottieAnimationView addToWishlist;


    private String usermobile, useremail, title, desc, price, user_id, imageUri, thumbUri, artistPhone;
    private  float priceFloat;


    private int quantity = 1;
    private UserSession session;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_post_details);

        addToWishlist = findViewById(R.id.add_to_wishlist);
        quantityProductPage = findViewById(R.id.quantityProductPage);
        productdesc = findViewById(R.id.productdesc);
        buyNow = findViewById(R.id.buy_now);
        addToCart = findViewById(R.id.add_to_cart);
        productprice = findViewById(R.id.productprice);
        productname = findViewById(R.id.productname);
        productimage = findViewById(R.id.productimage);

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initialize();


    }

    private void initialize() {
        // Get Extras
        Intent postDetailIntent = getIntent();
        title = postDetailIntent.getExtras().getString("Title");
        desc = postDetailIntent.getExtras().getString("Desc");
        price = postDetailIntent.getExtras().getString("Price");
        priceFloat= Float.parseFloat(postDetailIntent.getExtras().getString("Price"));
        user_id = postDetailIntent.getExtras().getString("UserId");
        imageUri = postDetailIntent.getExtras().getString("ImageUrl");
        thumbUri = postDetailIntent.getExtras().getString("ThumbUrl");
        artistPhone= postDetailIntent.getExtras().getString("ArtistsNumber");

        productprice.setText("Ksh " + price);

        productname.setText(title);
        productdesc.setText(desc);
        quantityProductPage.setText("1");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.drawerback);

        Glide.with(this)
                .applyDefaultRequestOptions(requestOptions)
                .load(imageUri)
                .thumbnail(Glide.with(this)
                        .load(thumbUri))
                .into(productimage);

        //SharedPreference for Cart Value
        session = new UserSession(getApplicationContext());

        //validating session
        usermobile = session.getUserSession().get(UserSession.KEY_PHONE);
        useremail = session.getUserSession().get(UserSession.KEY_EMAIL);

        //setting textwatcher for no of items field
        quantityProductPage.addTextChangedListener(productcount);

        //get firebase instance
        //initializing database reference
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private SingleProductModel getProductObject() {

        return new SingleProductModel(artistPhone, user_id, Integer.parseInt(quantityProductPage.getText().toString()), useremail, usermobile, title, Float.toString(priceFloat), imageUri, thumbUri, desc);

    }


    public void Notifications(View view) {
        startActivity(new Intent(ItemPostDetailsActivity.this, ProfileActivity.class));
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void shareProduct(View view) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Found amazing " + productname.getText().toString() + "on Arty App";
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void similarProduct(View view) {
        finish();
    }

    public void decrement(View view) {
        if (quantity > 1) {
            quantity--;
            quantityProductPage.setText(String.valueOf(quantity));
        }
    }

    public void increment(View view) {
        if (quantity < 500) {
            quantity++;
            quantityProductPage.setText(String.valueOf(quantity));
        } else {
            Toasty.error(ItemPostDetailsActivity.this, "Product Count Must be less than 500", Toast.LENGTH_LONG).show();
        }
    }

    //check that product count must not exceed 500
    TextWatcher productcount = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //none
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (quantityProductPage.getText().toString().equals("")) {
                quantityProductPage.setText("0");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            //none
            if (Integer.parseInt(quantityProductPage.getText().toString()) >= 500) {
                Toasty.error(ItemPostDetailsActivity.this, "Product Count Must be less than 500", Toast.LENGTH_LONG).show();
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        //check Internet Connection
       new CheckInternetConnection(this).checkConnection();
    }

    public void addToCart(View view) {


            mDatabaseReference.child("cart").child(usermobile).push().setValue(getProductObject());
            session.increaseCartValue();
            Log.e("Cart Value IP", session.getCartValue() + " ");
            Toasty.success(ItemPostDetailsActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();

    }

    public void addToWishList(View view) {

        addToWishlist.playAnimation();
        mDatabaseReference.child("wishlist").child(usermobile).push().setValue(getProductObject());
        session.increaseWishlistValue();
    }

    public void goToCart(View view) {


            mDatabaseReference.child("cart").child(usermobile).push().setValue(getProductObject());
            session.increaseCartValue();
            startActivity(new Intent(ItemPostDetailsActivity.this, CartActivity.class));
            finish();

    }
}