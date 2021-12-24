package com.marichtech.artyy.posts.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marichtech.artyy.R;
import com.marichtech.artyy.activity.ui.HomeActivity;
import com.marichtech.artyy.activity.ui.ProfileActivity;
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

public class ItemPostsActivity extends AppCompatActivity {
    private static final String TAG = "ImagePostsActivity";

    private StorageReference storageReference;
    FirebaseStorage storage;

    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    private FirebaseFirestore firestore;
    public static String userId;

    public String dialogMessage ="Please wait....";

    public ProgressDialog mDialog;

    private Bitmap compressedImageFile;
    private Uri postImageUri = null;

    ImageView ImagePost;
    Spinner CategorySpinner;
    Button Submit;
    TextView TitleView, DescriptionView, PriceView;
    private String title_, description_, price_, image_, thumb_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_posts);

        InitializeToolbar();

    }

    public void InitializeToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> finish());

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.nav_profile);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(dialogMessage);
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        InitializeViews();


    }

    public void InitializeViews(){
        CategorySpinner = findViewById(R.id.add_post_title);
        TitleView = findViewById(R.id.itemTitle);
       DescriptionView = findViewById(R.id.itemDescription);
       PriceView = findViewById(R.id.itemPrice);

       ImagePost = findViewById(R.id.add_postimg);

        /** Change profile photo from GALLERY */
        ImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open gallery
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(ItemPostsActivity.this);

            }
        });

        Submit = findViewById(R.id.btnPost);
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPost();
            }
        });



    }

    public void onPost() {
        final String category = CategorySpinner.getSelectedItem().toString();
        final String title = TitleView.getText().toString();
        final String desc = DescriptionView.getText().toString();
        final String price = PriceView.getText().toString();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc) && postImageUri != null) {


            dialogMessage = "Uploading Post..";
            mDialog.show();

            final String randomName = UUID.randomUUID().toString();

            final StorageReference filePath = storageReference.child("ArtyPostsImages/post_images").child(randomName + ".jpg");
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
                            compressedImageFile = new Compressor(ItemPostsActivity.this)
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

                        final StorageReference uploadThumb = storageReference.child("ArtyPostsImages/post_images/thumbnails").child(randomName + ".jpg");
                        UploadTask uploadThumbnailTask = uploadThumb.putBytes(thumbnailData);

                        Task<Uri> thumbUrlTask = uploadThumbnailTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                                    postMap.put("title", title);
                                    postMap.put("desc", desc);
                                    postMap.put("category", category);
                                    postMap.put("price", price);
                                    postMap.put("user_id", userId);
                                    postMap.put("created_on", FieldValue.serverTimestamp());

                                    firestore.collection("Arty/Posts/Data").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ItemPostsActivity.this, "Post added successfully", Toast.LENGTH_LONG).show();
                                                Intent mainIntent = new Intent(ItemPostsActivity.this, HomeActivity.class);
                                                startActivity(mainIntent);
                                                finish();
                                            } else {
                                                String errorMessage = task.getException().getMessage();
                                                Toast.makeText(ItemPostsActivity.this, "Image Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                            }

                                            mDialog.dismiss();
                                        }
                                    });
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(ItemPostsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        mDialog.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(ItemPostsActivity.this, "Text Fields and Image must not be empty", Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Crop Image Result handler
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                ImagePost.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "Selecting profileimage: "+error.toString());
                Toasty.info(ItemPostsActivity.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

}