package com.marichtech.artyy.cart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.marichtech.artyy.R;
import com.marichtech.artyy.network.CheckInternetConnection;
import com.twigafoods.daraja.Daraja;
import com.twigafoods.daraja.DarajaListener;
import com.twigafoods.daraja.model.AccessToken;
import com.twigafoods.daraja.model.LNMExpress;
import com.twigafoods.daraja.model.LNMResult;

public class OrderPlacedActivity extends AppCompatActivity {

    TextView orderidview;
    private String orderid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_placed);

        orderidview = findViewById(R.id.orderid);

        //check Internet Connection
        new CheckInternetConnection(this).checkConnection();



        initialize();

    }

    private void initialize() {


        Bundle bundle = getIntent().getExtras();
        // bundle.get("phone").toString()
        //  bundle.get("totalprice").toString()

        //Toast.makeText(OrderPlacedActivity.this, "RESult : " + lnmResult.ResponseDescription, Toast.LENGTH_SHORT).show();
        orderid = getIntent().getStringExtra("orderid");
        orderidview.setText(orderid);



    }

    public void finishActivity(View view) {
        finish();
    }
}
