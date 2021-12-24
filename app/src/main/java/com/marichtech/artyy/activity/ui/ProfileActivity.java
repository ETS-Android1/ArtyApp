

package com.marichtech.artyy.activity.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.flatdialoglibrary.dialog.FlatDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marichtech.artyy.R;
import com.marichtech.artyy.sessions.UserSession;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    public ProgressDialog mDialog;
    public String dialogMessage ="Please wait....";
    CardView NameCard, PhoneCard;
    CircleImageView profile;
    TextView NameView, EmailView, PhoneView;
    private String name_, email_, phone_, image_, thumb_;

    private StorageReference storageReference;
    FirebaseStorage storage;
    private Bitmap compressedImageFile;
    private Uri postImageUri = null;

    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    private FirebaseFirestore firestore;
    public static String userId;

    public UserSession session;
    private HashMap<String, String> userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        session= new UserSession(getApplicationContext());
        //get User details if logged in
        userData = session.getUserSession();


        InitializeToolbar();
        InitializeViews();
        InitializeCards();
        getUserProfile();

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(dialogMessage);
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

    }

    public void InitializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarProfile);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> finish());

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.nav_profile);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void InitializeViews() {
        NameView = findViewById(R.id.fullName);
        PhoneView = findViewById(R.id.phone);
        EmailView = findViewById(R.id.email);
        profile = findViewById(R.id.profileImage);

        /** Change profile photo from GALLERY */
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open gallery
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(ProfileActivity.this);
            }
        });
    }


    public void getUserProfile() {
        name_ = userData.get(UserSession.KEY_NAME);
        email_ = userData.get(UserSession.KEY_EMAIL);
        phone_ = userData.get(UserSession.KEY_PHONE);
        image_ = userData.get(UserSession.KEY_IMAGE);
        thumb_ = userData.get(UserSession.KEY_THUMB);

        PhoneView.setText(phone_);
        EmailView.setText(email_);
        NameView.setText(name_);

        RequestOptions placeholderOptions = new RequestOptions();
        placeholderOptions.placeholder(R.drawable.default_user_art_g_2);

        if(thumb_ != null && image_ != null){
            Glide.with(getApplication())
                    .applyDefaultRequestOptions(placeholderOptions)
                    .load(image_)
                    .thumbnail(Glide.with(getApplication())
                            .load(thumb_))
                    .into(profile);
        }
    }

    public void InitializeCards() {

        NameCard = findViewById(R.id.nameCard);
        NameCard.setOnClickListener(v -> EditName());

        PhoneCard = findViewById(R.id.phoneCard);
        PhoneCard.setOnClickListener(v -> EditPhone());

    }

    public void EditName() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your name");

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(name_);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogMessage = "Updating Name.....";
                mDialog.show();
                Map<String, Object> postMap = new HashMap<>();
                postMap.put("name", input.getText().toString());
                firestore.collection("Arty/User/Artists").document(userId).update(postMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDialog.dismiss();
                        session.updateName(input.getText().toString());
                        Toasty.info(ProfileActivity.this, "Name Updated", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Log.e(TAG, "Update Name Error"+e.getMessage());
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    public void EditPhone() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your Phone");

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(phone_);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogMessage = "Updating PhoneNumber.....";
                mDialog.show();
                Map<String, Object> postMap = new HashMap<>();
                postMap.put("phone", input.getText().toString());
                firestore.collection("Arty/User/Artists").document(userId).update(postMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDialog.dismiss();
                        session.updatePhone(input.getText().toString());
                        Toasty.info(ProfileActivity.this, "PhoneNumber Updated", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Log.e(TAG, "Update Phonenumber Error"+e.getMessage());
                    }
                });

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void EditProfileImage() {
        final String randomName = UUID.randomUUID().toString();

        final StorageReference filePath = storageReference.child("ArtyProfileImages/Images").child(randomName + ".jpg");
        UploadTask uploadTask = filePath.putFile(postImageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return filePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {

                    final String downloadUri = task.getResult().toString();
                    File newImageFile = new File(postImageUri.getPath());

                    try {
                        compressedImageFile = new Compressor(ProfileActivity.this)
                                .setMaxHeight(200)
                                .setMaxWidth(200)
                                .setQuality(2)
                                .compressToBitmap(newImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] thumbnailData = baos.toByteArray();

                    final StorageReference uploadThumb = storageReference.child("ArtyProfileImages/thumbnails").child(randomName + ".jpg");
                    UploadTask uploadThumbnailTask = uploadThumb.putBytes(thumbnailData);

                     uploadThumbnailTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            return uploadThumb.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String downloadThumbUri = task.getResult().toString();

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("image_url", downloadUri);
                                postMap.put("thumb_url", downloadThumbUri);

                                firestore.collection("Arty/User/Artists").document(userId).update(postMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mDialog.dismiss();
                                        session.updateProfileImage(downloadUri, downloadThumbUri);
                                        Toasty.info(ProfileActivity.this, "Profile Image Updated", Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mDialog.dismiss();
                                        Log.e(TAG, "Add ProfileImage Error"+e.getMessage());
                                    }
                                });

                            } else {
                                mDialog.dismiss();
                                String errorMessage = task.getException().getMessage();
                                Log.e(TAG, errorMessage);
                                Toasty.info(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    mDialog.dismiss();
                }
            }
        });

}

public void openDialog(){
    final FlatDialog flatDialog = new FlatDialog(ProfileActivity.this);
    flatDialog.setTitle("Login")
            .setSubtitle("write your profile info here")
            .setFirstTextFieldHint("email")
            .setSecondTextFieldHint("password")
            .setFirstButtonText("CONNECT")
            .setSecondButtonText("CANCEL")
            .withFirstButtonListner(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(ProfileActivity.this, flatDialog.getFirstTextField(), Toast.LENGTH_SHORT).show();
                }
            })
            .withSecondButtonListner(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    flatDialog.dismiss();
                }
            })
            .show();
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Crop Image Result handler
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                dialogMessage = "Updating Profile Image.....";
                mDialog.show();
                postImageUri = result.getUri();
                profile.setImageURI(postImageUri);
                EditProfileImage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "Selecting profileimage: "+error.toString());
                Toasty.info(ProfileActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

}