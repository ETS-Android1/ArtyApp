package com.marichtech.artyy.activity.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.marichtech.artyy.R;

public class HelpActivity extends AppCompatActivity {

    CardView FaqCard, ContactCard, TermsPrivacy, AboutCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);


        InitializeToolbar();
        InitializeCards();

    }

    public void InitializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> finish());

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.nav_help);
            ab.setDisplayHomeAsUpEnabled(true);
        }

    }

    public void InitializeCards() {

       FaqCard = findViewById(R.id.faqCard);
       FaqCard.setOnClickListener(v -> FaqCard());

        ContactCard = findViewById(R.id.contactCard);
        ContactCard.setOnClickListener(v -> ContactCard());

        TermsPrivacy = findViewById(R.id.termsPrivacyCard);
        TermsPrivacy.setOnClickListener(v -> TermsPrivacy());

        AboutCard = findViewById(R.id.aboutCard);
        AboutCard.setOnClickListener(v -> AboutCard());

    }

    private void AboutCard() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    private void TermsPrivacy() {
        String url = "https://github.com/MarichTech/Terms-Condition/blob/main/terms.md";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void ContactCard() {
    }

    public void FaqCard(){

    }
}