package com.marichtech.artyy.activity.signin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.marichtech.artyy.activity.ui.CustomerActivity;
import com.marichtech.artyy.activity.ui.HomeActivity;
import com.marichtech.artyy.R;
import com.marichtech.artyy.sessions.UserSession;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    public ProgressDialog mDialog;
    public String email_, pass_;
    private EditText EmailText, PasswordText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    public static String userId;

    public UserSession session;
    private HashMap<String, String> userData;

    private String name, email, phone, image, thumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Initialize();
    }

    public void Initialize() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        EmailText = findViewById(R.id.email);
        PasswordText = findViewById(R.id.password);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Please wait..");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        session= new UserSession(getApplicationContext());

    }


    public void onLogin(View view) {

        email_ = EmailText.getText().toString();
        pass_ = PasswordText.getText().toString();

        if (!TextUtils.isEmpty(email_) && !TextUtils.isEmpty(pass_)) {
            mDialog.show();

            mAuth.signInWithEmailAndPassword(email_, pass_).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    final String token_id = FirebaseInstanceId.getInstance().getToken();
                    final String current_id = mAuth.getCurrentUser().getUid();

                    mFirestore.collection("Arty/User/Artists").document(current_id).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.getString("token_id").equals("")) {

                            Map<String, Object> tokenMap = new HashMap<>();
                            tokenMap.put("token_id", token_id);

                            mFirestore.collection("Arty/User/Artists").document(current_id).update(tokenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    FirebaseFirestore.getInstance().collection("Arty/User/Artists").document(current_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                                            checkVerifiedEmail();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Error", ".." + e.getMessage());
                                        }
                                    });

                                }
                            }).addOnFailureListener(e -> {
                                mDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            mDialog.dismiss();
//                            new BottomDialog.Builder(LoginActivity.this)
//                                    .setTitle("Information")
//                                    .setContent("This account is being used in another device, please logout from that device and try again.")
//                                    .setPositiveText("Ok")
//                                    .setPositiveBackgroundColorResource(R.color.purple_200)
//                                    .setCancelable(true)
//                                    .onPositive(dialog ->
//                                            dialog.dismiss())
//                                    .show();

                            if (mAuth.getCurrentUser() != null) {
                                signOut();
                            }

                        }
                    });


                } else {
                    if (task.getException().getMessage().contains("The password is invalid")) {
                        Toast.makeText(LoginActivity.this, "Error: The password you have entered is invalid.", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    } else if (task.getException().getMessage().contains("There is no user record")) {
                        Toast.makeText(LoginActivity.this, "Error: Invalid user, Please register using the button below.", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }

                }

            });

        } else if (TextUtils.isEmpty(email_)) {

            Toasty.error(this, "Enter Your Email ", Toasty.LENGTH_SHORT, true).show();

        } else if (TextUtils.isEmpty(pass_)) {
            Toasty.error(this, "Enter Your Password ", Toasty.LENGTH_SHORT, true).show();

        } else {
            Toasty.error(this, "Please fill all fields ", Toasty.LENGTH_SHORT, true).show();
        }

    }


    /** checking email verified or NOT */
    private void checkVerifiedEmail() {
        boolean isVerified = false;
        if (mAuth.getCurrentUser() != null) {
            isVerified = mAuth.getCurrentUser().isEmailVerified();
        }
        if (isVerified){
            String UID = mAuth.getCurrentUser().getUid();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("email_verified", "true");
            mFirestore.collection("Arty/User/Artists").document(UID ).update(userMap).addOnSuccessListener(aVoid -> {
                if (mAuth.getCurrentUser() != null) {
                    mAuth.getCurrentUser().sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    getUserProfile();

                                } else {
                                    mDialog.dismiss();
                                    signOut();
                                }
                            });
                }}).addOnFailureListener(e -> {
                mDialog.dismiss();
                Log.e(TAG,e.getMessage());
                Toasty.info(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } else {
            mDialog.dismiss();
            Toasty.info(this, "Email is not verified. Please verify first", Toasty.LENGTH_LONG,true).show();
            signOut();
        }
    }
    private void signOut(){
        final String userUid = mAuth.getCurrentUser().getUid();
        Map<String, Object> tokenRemove = new HashMap<>();
        tokenRemove.put("token_id", "");
       mFirestore.collection("Arty/User/Artists").document(userUid).update(tokenRemove).addOnSuccessListener(aVoid ->
                mAuth.signOut())
               .addOnFailureListener(e -> {
                   Toasty.info(LoginActivity.this, e.getMessage(), Toasty.LENGTH_LONG,true).show();
                   Log.e(TAG, "Logout Error"+e.getMessage());
               });
    }

    public void getUserProfile() {
        userId = mAuth.getCurrentUser().getUid();
        mFirestore.collection("Arty/User/Artists").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    phone = documentSnapshot.getString("phone");
                    email = documentSnapshot.getString("email");
                    name = documentSnapshot.getString("name");
                    image = documentSnapshot.getString("image_url");
                    thumb = documentSnapshot.getString("thumb_url");

                    session.createUserSession(name, email, phone,image, thumb);
                    mDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, CustomerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                }).addOnFailureListener(e -> {
                    Toasty.info(LoginActivity.this, e.toString(), Toasty.LENGTH_LONG,true).show();
                    Log.e(TAG, "Get user data"+e.getMessage());
                });
    }

    public void onRegister(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void onForgotPassword(View view) {
    }

}