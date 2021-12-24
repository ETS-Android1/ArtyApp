package com.marichtech.artyy.posts.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.marichtech.artyy.R;

public class ImagePreviewActivity extends AppCompatActivity {

    PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        // Adding this line will prevent taking screenshot in your app
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.blacklist);
        TextView appname = findViewById(R.id.appname);
        appname.setTypeface(typeface);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> finish());

        photoView = findViewById(R.id.photo_view);

        Intent imageIntent = getIntent();
        String title = imageIntent.getExtras().getString("Title");
        String imageUri = imageIntent.getExtras().getString("ImageUrl");
        String thumbUri = imageIntent.getExtras().getString("ThumbUrl");

        ActionBar ab = getSupportActionBar();
        if (ab != null && title != null) {
            ab.setTitle(title);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.default_image);

        Glide.with(this)
                .applyDefaultRequestOptions(requestOptions)
                .load(imageUri)
                .thumbnail(Glide.with(this)
                        .load(thumbUri))
                .into(photoView);

    }
}