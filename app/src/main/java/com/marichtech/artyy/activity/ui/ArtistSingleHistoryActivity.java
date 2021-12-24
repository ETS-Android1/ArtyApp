package com.marichtech.artyy.activity.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marichtech.artyy.R;
import com.marichtech.artyy.network.CheckInternetConnection;
import com.marichtech.artyy.posts.models.SingleProductModel;
import com.marichtech.artyy.posts.notification.NotificationActivity;
import com.marichtech.artyy.sessions.UserSession;

import java.util.ArrayList;
import java.util.HashMap;

public class ArtistSingleHistoryActivity extends AppCompatActivity {

    //to get user session data
    private UserSession session;
    private HashMap<String,String> user;
    private String name,email,photo,mobile, thumb, userId, OrderId;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;

    //Getting reference to Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();
    private LottieAnimationView tv_no_item;
    private LinearLayout activitycartlist;
    private LottieAnimationView emptycart;

    private float totalcost=0;
    private int totalproducts=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_single_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Items");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        //SharedPreference for Cart Value
        session = new UserSession(getApplicationContext());

        //retrieve session values and display on listviews
        getValues();



        //validating session

        mRecyclerView = findViewById(R.id.recyclerview);
        tv_no_item = findViewById(R.id.tv_no_cards);
        activitycartlist = findViewById(R.id.activity_cart_list);
        emptycart = findViewById(R.id.empty_cart);


        if (mRecyclerView != null) {
            //to enable optimization of recyclerview
            mRecyclerView.setHasFixedSize(true);
        }
        //using staggered grid pattern in recyclerview
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

//        if(session.getCartValue()>0) {
//            populateRecyclerView();
//        }else if(session.getCartValue() == 0)  {
//            tv_no_item.setVisibility(View.GONE);
//            activitycartlist.setVisibility(View.GONE);
//            emptycart.setVisibility(View.VISIBLE);
//        }
        populateRecyclerView();
    }

    private void populateRecyclerView() {

        //Say Hello to our new FirebaseUI android Element, i.e., FirebaseRecyclerAdapter
        final FirebaseRecyclerAdapter<SingleProductModel, ArtistSingleHistoryActivity.MovieViewHolder> adapter = new FirebaseRecyclerAdapter<SingleProductModel, ArtistSingleHistoryActivity.MovieViewHolder>(
                SingleProductModel.class,
                R.layout.cart_item_layout,
                ArtistSingleHistoryActivity.MovieViewHolder.class,
                //referencing the node where we want the database to store the data from our Object

                mDatabaseReference.child("artists").child(mobile).child("items").getRef()
        ) {
            @Override
            protected void populateViewHolder(final ArtistSingleHistoryActivity.MovieViewHolder viewHolder, final SingleProductModel model, final int position) {
                if(tv_no_item.getVisibility()== View.VISIBLE){
                    tv_no_item.setVisibility(View.GONE);
                }
                viewHolder.cardname.setText(model.getPrname());
                viewHolder.cardprice.setText("Ksh "+model.getPrprice());
                viewHolder.cardcount.setText("Quantity : "+model.getNo_of_items());

                Glide.with(ArtistSingleHistoryActivity.this)
                        .load(model.getPrimage_image())
                        .thumbnail(Glide.with(ArtistSingleHistoryActivity.this)
                                .load(model.getPrimage_thumb()))
                        .into(viewHolder.cardimage);



                totalcost += model.getNo_of_items()*Float.parseFloat(model.getPrprice());
                totalproducts += model.getNo_of_items();

                viewHolder.carddelete.setVisibility(View.GONE);
                viewHolder.carddelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        };
        mRecyclerView.setAdapter(adapter);
    }


    //viewHolder for our Firebase UI
    public static class MovieViewHolder extends RecyclerView.ViewHolder{

        TextView cardname;
        ImageView cardimage;
        TextView cardprice;
        TextView cardcount;
        ImageView carddelete;

        View mView;
        public MovieViewHolder(View v) {
            super(v);
            mView = v;
            cardname = v.findViewById(R.id.cart_prtitle);
            cardimage = v.findViewById(R.id.image_cartlist);
            cardprice = v.findViewById(R.id.cart_prprice);
            cardcount = v.findViewById(R.id.cart_prcount);
            carddelete = v.findViewById(R.id.deletecard);
        }
    }


    private void getValues() {

        //create new session object by passing application context
        session = new UserSession(getApplicationContext());
        Bundle bundle = getIntent().getExtras();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        OrderId = bundle.get("orderId").toString();


        //get User details if logged in
        user = session.getUserSession();

        name = user.get(UserSession.KEY_NAME);
        email = user.get(UserSession.KEY_EMAIL);
        mobile = user.get(UserSession.KEY_PHONE);
        photo = user.get(UserSession.KEY_IMAGE);
        thumb = user.get(UserSession.KEY_THUMB);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void viewProfile(View view) {
        startActivity(new Intent(ArtistSingleHistoryActivity.this, ProfileActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

    }

    public void Notifications(View view) {

        startActivity(new Intent(ArtistSingleHistoryActivity.this, NotificationActivity.class));
        finish();
    }
}