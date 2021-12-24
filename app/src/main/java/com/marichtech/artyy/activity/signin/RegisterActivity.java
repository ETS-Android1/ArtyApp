package com.marichtech.artyy.activity.signin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.chrisdisdero.crdcrypt.CRDCrypt;
import com.chrisdisdero.crdcrypt.CRDCryptException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.marichtech.artyy.R;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    public ProgressDialog mDialog;
    public String name_, phone_, email_, pass_, confirmPass_;
    private EditText NameText, PhoneText, EmailText, PasswordText, ConfirmPasswordText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private byte[] initializationVector = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Initialize();

    }

    public void Initialize() {
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        NameText = (EditText) findViewById(R.id.fullName);
        EmailText = (EditText) findViewById(R.id.email);
        PhoneText = findViewById(R.id.number);
        PasswordText = (EditText) findViewById(R.id.password);
        ConfirmPasswordText = (EditText) findViewById(R.id.confirmPassword);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);


        try {

            initializationVector = CRDCrypt.generateInitializationVector();

        } catch (CRDCryptException e) {

            Log.e(TAG, "Failed encryption initialization vector", e);
        }
    }

    public void onRegister(View view) {

        name_ = NameText.getText().toString();
        email_ = EmailText.getText().toString();
        phone_ = PhoneText.getText().toString();
        pass_ = PasswordText.getText().toString();
        confirmPass_ = ConfirmPasswordText.getText().toString();

        mDialog.show();

        if (TextUtils.isEmpty(name_)) {
            Toasty.error(this, "Enter Your FullName ", Toasty.LENGTH_SHORT, true).show();
            mDialog.dismiss();
        }
        else if (TextUtils.isEmpty(email_)) {
            Toasty.error(this, "Enter Your Email ", Toasty.LENGTH_SHORT, true).show();
            mDialog.dismiss();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email_).matches()){
            Toasty.error(this, "Your Email is not valid ", Toasty.LENGTH_SHORT, true).show();
            mDialog.dismiss();
        }
        else if (TextUtils.isEmpty(phone_)) {
            Toasty.error(this, "Enter Your PhoneNumber ", Toasty.LENGTH_SHORT, true).show();
            mDialog.dismiss();
        }
        else if (TextUtils.isEmpty(pass_)) {
            Toasty.error(this, "Enter Your Password ", Toasty.LENGTH_SHORT, true).show();
            mDialog.dismiss();
        }
        else if (TextUtils.isEmpty(confirmPass_)) {
            Toasty.error(this, "Enter Password Confirmation ", Toasty.LENGTH_SHORT, true).show();
            mDialog.dismiss();
        }


        else if(!TextUtils.isEmpty(pass_) && !TextUtils.isEmpty(confirmPass_) && !pass_.equals(confirmPass_)){
            Toasty.error(this, "Your Password and Confirmation password dont match ", Toasty.LENGTH_SHORT, true).show();
            mDialog.dismiss();
        }

        else if (!TextUtils.isEmpty(name_) || !TextUtils.isEmpty(phone_) || !TextUtils.isEmpty(email_) || !TextUtils.isEmpty(pass_) || !TextUtils.isEmpty(confirmPass_)) {


            registerUser();
        } else {
            Toasty.error(this, "Please fill all fields ", Toasty.LENGTH_SHORT, true).show();
            mDialog.dismiss();
        }
    }

    public void registerUser(){

        mAuth.createUserWithEmailAndPassword(email_, pass_).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String myKey = "TheThirtyTwoByteKeyForEncryption";

                    String dataToEncrypt = pass_;

                    byte[] encrypted = null;
                    try {

                        encrypted = CRDCrypt.aes256Encrypt(myKey, dataToEncrypt.getBytes(StandardCharsets.UTF_8), initializationVector);

                    } catch (CRDCryptException e) {

                        Log.e(TAG, "onCreate: failed to encrypt data", e);
                    }

                    final String userUid = mAuth.getCurrentUser().getUid();

                    String token_id = FirebaseInstanceId.getInstance().getToken();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", userUid);
                    userMap.put("name", name_);
                    userMap.put("email", email_);
                    userMap.put("phone", phone_);
                    userMap.put("image_url", "");
                    userMap.put("thumb_url", "");
                    userMap.put("password", Arrays.toString(encrypted));
                    userMap.put("token_id", token_id);
                    userMap.put("created_on", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Arty/User/Artists").document(userUid).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // SENDING VERIFICATION EMAIL TO THE REGISTERED USER'S EMAIL

                            if (mAuth.getCurrentUser() != null) {
                                mAuth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    registerSuccessPopUp();

                                                    // LAUNCH activity after certain time period
                                                    new Timer().schedule(new TimerTask() {
                                                        public void run() {
                                                            RegisterActivity.this.runOnUiThread(new Runnable() {
                                                                public void run() {
                                                                    signOut();
                                                                    Intent mainIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(mainIntent);
                                                                    finish();

                                                                    Toasty.info(RegisterActivity.this, "A verification link has been sent to your email. Check your email and verify", Toasty.LENGTH_SHORT, true).show();
                                                                    mDialog.dismiss();
                                                                }
                                                            });
                                                        }
                                                    }, 4000);


                                                } else {
                                                    mDialog.dismiss();
                                                    signOut();
                                                }
                                            }
                                        });

                            }}
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Log.e(TAG,e.getMessage());
                            Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                } else {
                    mDialog.dismiss();
                    Log.e(TAG, task.getException().getMessage());
                    Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signOut(){
        final String userUid = mAuth.getCurrentUser().getUid();
        Map<String, Object> tokenRemove = new HashMap<>();
        tokenRemove.put("token_id", "");
        firebaseFirestore.collection("Users").document(userUid).update(tokenRemove).addOnSuccessListener(aVoid ->
                mAuth.signOut())
                .addOnFailureListener(e ->
                        Log.e(TAG, "Logout Error"+e.getMessage()));
    }


    private void registerSuccessPopUp() {
        // Custom Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        View view = LayoutInflater.from(RegisterActivity.this).inflate(R.layout.register_success_popup, null);

        //ImageButton imageButton = view.findViewById(R.id.successIcon);
        //imageButton.setImageResource(R.drawable.logout);
        builder.setCancelable(false);

        builder.setView(view);
        builder.show();
    }
    public void openPolicy(View view) {
        String url = "https://github.com/MarichTech/Terms-Condition/blob/main/policy.md";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void openTerms(View view) {
        String url = "https://github.com/MarichTech/Terms-Condition/blob/main/terms.md";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void openLogin(View view) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}