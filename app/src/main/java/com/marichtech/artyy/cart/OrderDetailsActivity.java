package com.marichtech.artyy.cart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marichtech.artyy.R;
import com.marichtech.artyy.network.CheckInternetConnection;
import com.marichtech.artyy.posts.models.PlacedOrderModel;
import com.marichtech.artyy.posts.models.SingleProductModel;
import com.marichtech.artyy.sessions.UserSession;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.twigafoods.daraja.Daraja;
import com.twigafoods.daraja.DarajaListener;
import com.twigafoods.daraja.model.AccessToken;
import com.twigafoods.daraja.model.LNMExpress;
import com.twigafoods.daraja.model.LNMResult;
import com.whygraphics.multilineradiogroup.MultiLineRadioGroup;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class OrderDetailsActivity extends AppCompatActivity {

    TextView deliveryDate;
    TextView noOfItems;
    TextView totalAmount;
    MaterialEditText ordername;
    MaterialEditText orderemail;
    MaterialEditText ordernumber;
    MaterialEditText orderaddress;
    MaterialEditText orderpincode;

    float Amount;


    private ArrayList<SingleProductModel> cartcollect;
    private String payment_mode="COD",order_reference_id;
    private String placed_user_name,getPlaced_user_email,getPlaced_user_mobile_no;
    private UserSession session;
    private HashMap<String,String> user;
    private DatabaseReference mDatabaseReference;
    private String currdatetime, userId, postId;



    //Declare Daraja :: Global Variable
    Daraja daraja;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        orderpincode = findViewById(R.id.orderpincode);
        orderaddress = findViewById(R.id.orderaddress);
        ordernumber = findViewById(R.id.ordernumber);
        orderemail = findViewById(R.id.orderemail);
        ordername = findViewById(R.id.ordername);
        totalAmount = findViewById(R.id.total_amount);
        noOfItems = findViewById(R.id.no_of_items);
        deliveryDate = findViewById(R.id.delivery_date);


        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //SharedPreference for Cart Value
        session = new UserSession(getApplicationContext());

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();



        //Init Daraja
        //TODO :: REPLACE WITH YOUR OWN CREDENTIALS  :: THIS IS SANDBOX DEMO
        daraja = Daraja.with("Pv1Jfto9J8MARlzFHAqUPcoAFMxV37Ir", "FpJG1YkwyaNYFq8q", new DarajaListener<AccessToken>() {
            @Override
            public void onResult(@NonNull AccessToken accessToken) {
                Log.i(OrderDetailsActivity.this.getClass().getSimpleName(), accessToken.getAccess_token());
                //Toast.makeText(OrderDetailsActivity.this, "TOKEN : " + accessToken.getAccess_token(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e(OrderDetailsActivity.this.getClass().getSimpleName(), error);
            }
        });


        productdetails();

    }


    private void productdetails() {

        Bundle bundle = getIntent().getExtras();

        //setting total price
        totalAmount.setText(bundle.get("totalprice").toString());

        //setting number of products
        noOfItems.setText(bundle.get("totalproducts").toString());

        cartcollect = (ArrayList<SingleProductModel>) bundle.get("cartproducts");

        //delivery date
        SimpleDateFormat formattedDate = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 7);  // number of days to add
        String tomorrow = (formattedDate.format(c.getTime()));
        deliveryDate.setText(tomorrow);

        payment_mode = "M-Pesa";

        user = session.getUserSession();

        placed_user_name=user.get(UserSession.KEY_NAME);
        getPlaced_user_email=user.get(UserSession.KEY_EMAIL);
        getPlaced_user_mobile_no=user.get(UserSession.KEY_PHONE);

        orderemail.setText(getPlaced_user_email);
        ordername.setText(placed_user_name);
        ordernumber.setText(getPlaced_user_mobile_no);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm");
        currdatetime = sdf.format(new Date());
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void PlaceOrder(View view) {

        if (validateFields(view)) {
            Bundle bundle = getIntent().getExtras();

            BigDecimal number = new BigDecimal(bundle.get("totalprice").toString());

            String formattedBalance = number.stripTrailingZeros().toPlainString();

            int roundedBalance = Integer.parseInt(formattedBalance);

            Log.e(OrderDetailsActivity.this.getClass().getSimpleName(), String.valueOf(roundedBalance));

            //TODO :: REPLACE WITH YOUR OWN CREDENTIALS  :: THIS IS SANDBOX DEMO
            LNMExpress lnmExpress = new LNMExpress(
                    "174379",
                    "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919",  //https://developer.safaricom.co.ke/test_credentials
                    String.valueOf(roundedBalance),
                    "254708374149",
                    "174379",
                    bundle.get("phone").toString(),
                    "http://mycallbackurl.com/checkout.php",
                    "Arty Company",
                    "Goods Payment"
            );

            //fieldSet(view);

            daraja.requestMPESAExpress(lnmExpress,
                    new DarajaListener<LNMResult>() {
                        @Override
                        public void onResult(@NonNull LNMResult lnmResult) {
                            Log.e(OrderDetailsActivity.this.getClass().getSimpleName(), lnmResult.ResponseDescription);
                            fieldSet(view);
                        }

                        @Override
                        public void onError(String error) {
                            Log.i(OrderDetailsActivity.this.getClass().getSimpleName(), error);
                        }
                    }
            );
        }

    }

    public void fieldSet(View view){
        Bundle bundle = getIntent().getExtras();

        String artistNumber = bundle.get("ArtistsPhone").toString();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            order_reference_id = getordernumber();

            //adding user details to the database under orders table
            mDatabaseReference.child("orders").child(getPlaced_user_mobile_no).child(userId).child(order_reference_id).setValue(getProductObject());
            mDatabaseReference.child("artists").child(artistNumber).push().setValue(getProductObject());

            //adding products to the order
            for (SingleProductModel model : cartcollect) {
                mDatabaseReference.child("orders").child(getPlaced_user_mobile_no).child(userId).child(order_reference_id).child("items").push().setValue(model);
                mDatabaseReference.child("artists").child(artistNumber).child("items").push().setValue(model);
            }

            mDatabaseReference.child("cart").child(getPlaced_user_mobile_no).removeValue();
            session.setCartValue(0);

            Intent intent = new Intent(OrderDetailsActivity.this, OrderPlacedActivity.class);
            intent.putExtra("orderid", order_reference_id);
            startActivity(intent);
            finish();

    }

    private boolean validateFields(View view) {

        if (ordername.getText().toString().length() == 0 || orderemail.getText().toString().length() == 0 || ordernumber.getText().toString().length() == 0 || orderaddress.getText().toString().length() == 0 ||
                orderpincode.getText().toString().length() == 0) {
            Snackbar.make(view, "Kindly Fill all the fields", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            return false;
        } else if (orderemail.getText().toString().length() < 4 || orderemail.getText().toString().length() > 30) {
            orderemail.setError("Email Must consist of 4 to 30 characters");
            return false;
        } else if (!orderemail.getText().toString().matches("^[A-za-z0-9.@]+")) {
            orderemail.setError("Only . and @ characters allowed");
            return false;
        } else if (!orderemail.getText().toString().contains("@") || !orderemail.getText().toString().contains(".")) {
            orderemail.setError("Email must contain @ and .");
            return false;
        } else if (ordernumber.getText().toString().length() < 4 || ordernumber.getText().toString().length() > 12) {
            ordernumber.setError("Number Must consist of 10 characters");
            return false;
        }

        return true;
    }

    public PlacedOrderModel getProductObject() {
        return new PlacedOrderModel("false",userId,order_reference_id,noOfItems.getText().toString(),totalAmount.getText().toString(),deliveryDate.getText().toString(),payment_mode,ordername.getText().toString(),orderemail.getText().toString(),ordernumber.getText().toString(),orderaddress.getText().toString(),orderpincode.getText().toString(),placed_user_name,getPlaced_user_email,getPlaced_user_mobile_no);
    }

    public String getordernumber() {

        return currdatetime.replaceAll("-","");
    }
}
