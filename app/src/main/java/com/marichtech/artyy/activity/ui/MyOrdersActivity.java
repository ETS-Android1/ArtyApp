package com.marichtech.artyy.activity.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.marichtech.artyy.posts.models.Orders;
import com.marichtech.artyy.posts.models.PlacedOrderModel;
import com.marichtech.artyy.posts.models.SingleProductModel;
import com.marichtech.artyy.posts.notification.NotificationActivity;
import com.marichtech.artyy.sessions.UserSession;

import java.util.HashMap;

public class MyOrdersActivity extends AppCompatActivity {

    //to get user session data
    private UserSession session;
    private HashMap<String,String> user;
    private String name,email,photo,mobile, thumb, userId;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;

    //Getting reference to Firebase Database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabaseReference = database.getReference();
    private LottieAnimationView tv_no_item;
    private FrameLayout activitycartlist;
    private LottieAnimationView emptycart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("History");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        //retrieve session values and display on listviews
        getValues();

        //SharedPreference for Cart Value
        session = new UserSession(getApplicationContext());

        //validating session
        // session.isLoggedIn();

        mRecyclerView = findViewById(R.id.recyclerview);
        tv_no_item = findViewById(R.id.tv_no_cards);
        activitycartlist = findViewById(R.id.frame_container);
        emptycart = findViewById(R.id.empty_cart);

        if (mRecyclerView != null) {
            //to enable optimization of recyclerview
            mRecyclerView.setHasFixedSize(true);
        }
        //using staggered grid pattern in recyclerview
        mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        populateRecyclerView();
    }

    private void populateRecyclerView() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Toast.makeText(MyOrdersActivity.this,mobile, Toast.LENGTH_SHORT).show();
        Toast.makeText(MyOrdersActivity.this,userId, Toast.LENGTH_SHORT).show();

        //Say Hello to our new FirebaseUI android Element, i.e., FirebaseRecyclerAdapter
        final FirebaseRecyclerAdapter<PlacedOrderModel, MyOrdersActivity.MovieViewHolder> adapter = new FirebaseRecyclerAdapter<PlacedOrderModel, MyOrdersActivity.MovieViewHolder>(
                PlacedOrderModel.class,
                R.layout.history_item,
                MyOrdersActivity.MovieViewHolder.class,
                //referencing the node where we want the database to store the data from our Object

                mDatabaseReference.child("orders").child(mobile).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef()

        ) {
            @Override
            protected void populateViewHolder(final MyOrdersActivity.MovieViewHolder viewHolder, final PlacedOrderModel model, final int position) {
                if(tv_no_item.getVisibility()== View.VISIBLE){
                    tv_no_item.setVisibility(View.GONE);
                }

                viewHolder.cardorderid.setText(model.getOrderid());
                viewHolder.carditemnumber.setText(model.getNo_of_items());
                viewHolder.cardtotalamount.setText(model.getTotal_amount());
                viewHolder.carddeliverydate.setText(model.getDelivery_date());
                viewHolder.carddeliverystatus.setText(model.getDelivered());


                viewHolder.carddelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(MyOrdersActivity.this,getItem(position).getPrname(), Toast.LENGTH_SHORT).show();
//                        getRef(position).removeValue();
//                        session.decreaseWishlistValue();
//                        startActivity(new Intent(MyOrdersActivity.this,WishListActivity.class));
//                        finish();
                    }
                });

                if(model.getDelivered().equalsIgnoreCase("true")){
                    viewHolder.mView.setBackgroundColor(Color.parseColor("#ace5ee"));
                }


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MyOrdersActivity.this,SingleHistoryActivity.class);
                        intent.putExtra("orderId",model.getOrderid());
                        intent.putExtra("phone",model.getPlaced_user_mobile_no());
                        intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        startActivity(intent);
                    }
                });
            }

        };
        mRecyclerView.setAdapter(adapter);
    }

    //viewHolder for our Firebase UI
    public static class MovieViewHolder extends RecyclerView.ViewHolder{

        TextView carditemnumber;
        TextView cardtotalamount;
        TextView carddeliverydate, cardorderid;
        TextView carddeliverystatus;
        ImageView carddelete;

        View mView;
        public MovieViewHolder(View v) {
            super(v);
            mView = v;
            carditemnumber = v.findViewById(R.id.no_of_items);
            cardtotalamount = v.findViewById(R.id.total_amount);
            carddeliverydate = v.findViewById(R.id.delivery_date);
            carddeliverystatus = v.findViewById(R.id.delivery_status);
            carddelete = v.findViewById(R.id.deletecard);
            cardorderid = v.findViewById(R.id.order_id);

        }
    }


    private void getValues() {

        //create new session object by passing application context
        session = new UserSession(getApplicationContext());


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
        startActivity(new Intent(MyOrdersActivity.this,ProfileActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

    }

    public void Notifications(View view) {

        startActivity(new Intent(MyOrdersActivity.this, NotificationActivity.class));
        finish();
    }
}
