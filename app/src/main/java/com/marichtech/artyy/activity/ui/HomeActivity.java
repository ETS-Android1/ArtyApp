package com.marichtech.artyy.activity.ui;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.marichtech.artyy.R;
import com.marichtech.artyy.activity.signin.LoginActivity;
import com.marichtech.artyy.cart.CartActivity;
import com.marichtech.artyy.fragments.dashboard.DashboardFragment;
import com.marichtech.artyy.posts.activities.ItemPostsActivity;
import com.marichtech.artyy.posts.activities.TopBarActivity;
import com.marichtech.artyy.posts.notification.NotificationActivity;
import com.marichtech.artyy.sessions.UserSession;
import com.marichtech.artyy.side_nav_ui.DrawerAdapter;
import com.marichtech.artyy.side_nav_ui.DrawerItem;
import com.marichtech.artyy.side_nav_ui.SimpleItem;
import com.marichtech.artyy.side_nav_ui.SpaceItem;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class HomeActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {

    private static final int POS_DASHBOARD = 0;
    private static final int POS_PROFILE = 1;
    private static final int POS_ABOUT = 2;
    private static final int POS_LOGOUT = 4;

    DrawerAdapter adapter;
    private String[] screenTitles;
    private Drawable[] screenIcons;
    private SlidingRootNav slidingRootNav;
    public static Toolbar toolbar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentuser;
    private FirebaseFirestore firestore;
    public static String userId;

    //Side Nav Details
    CircleImageView profile;
    TextView NavNameView, NavEmailView, NavViewProfile;
    private String name_, email_, phone_, image_, thumb_;

    //create user session
    public UserSession session;
    private HashMap<String, String> userData;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());


        mAuth = FirebaseAuth.getInstance();
        currentuser=mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();


        toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        if(currentuser==null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }else{
            userId = currentuser.getUid();
            session= new UserSession(getApplicationContext());
            //get User details if logged in
            userData = session.getUserSession();

        }

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.activity_drawer)
                .inject();

        InitializeSlideRoot();

        if(currentuser != null){getProfileInfo();}


    }



    public void InitializeSlideRoot(){


        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        adapter = new DrawerAdapter(Arrays.asList(
                createItemFor(POS_DASHBOARD).setChecked(true),
                createItemFor (POS_PROFILE),
                createItemFor(POS_ABOUT),
                new SpaceItem(40),
                createItemFor(POS_LOGOUT)));
        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_DASHBOARD);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.side_nav, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.items:
                startActivity(new Intent(this, ItemPostsActivity.class));
                break;
        }
        return true;
    }

    @NonNull
    private String[] loadScreenTitles() {

        String[] selectedTile;

        selectedTile =  getResources().getStringArray(R.array.ld_activityScreenTitles);

        return selectedTile;

    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }
    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.minimal_black))
                .withTextTint(color(R.color.minimal_black))
                .withSelectedIconTint(color(R.color.colorPrimaryDark))
                .withSelectedTextTint(color(R.color.colorPrimaryDark));
    }
    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }


    @Override
    public void onItemSelected(int position) {

        Fragment selectedScreen;

        switch (position) {

            case POS_DASHBOARD:
                slidingRootNav.closeMenu(true);
                selectedScreen = new DashboardFragment();
                showFragment(selectedScreen);
                return;


            case POS_PROFILE:
                slidingRootNav.closeMenu(true);
                startActivity(new Intent(this, ProfileActivity.class));
                return;

            case POS_ABOUT:
                slidingRootNav.closeMenu(true);
                startActivity(new Intent(this, HelpActivity.class));
                return;

            case POS_LOGOUT:
                slidingRootNav.closeMenu(true);
                if (currentuser != null && isOnline()) {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("Are you sure do you want to logout from this account?")
                            .positiveText("Yes")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    logout();
                                    dialog.dismiss();
                                }
                            }).negativeText("No")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            }).show();

                } else {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("A technical occurred while logging you out, Check your network connection and try again.")
                            .positiveText("Done")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    logout();
                                    dialog.dismiss();
                                }
                            }).show();

                }
                return;
        }

        slidingRootNav.closeMenu(true);
    }

    public void logout() {

        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage("Logging you out...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        Map<String, Object> tokenRemove = new HashMap<>();
        tokenRemove.put("token_id", "");

        firestore.collection("Arty/User/Artists").document(userId).update(tokenRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mAuth.signOut();
                session.createUserSession("name", "email", "number","", "");
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                mDialog.dismiss();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Logout Error", e.getMessage());
            }
        });

    }


    private void showFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    public void viewProfile(View view) {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentuser==null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    public void getProfileInfo(){
//Nav Side View
        NavNameView = findViewById(R.id.navName1);
        NavEmailView = findViewById(R.id.navEmail1);
        profile = findViewById(R.id.navProfile1);
        NavViewProfile = findViewById(R.id.viewProfile);
        NavViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                finish();
            }
        });

        name_ = userData.get(UserSession.KEY_NAME);
        email_ = userData.get(UserSession.KEY_EMAIL);
        image_ = userData.get(UserSession.KEY_IMAGE);
        thumb_ = userData.get(UserSession.KEY_THUMB);

            NavEmailView.setText(email_);
            NavNameView.setText(name_);

        RequestOptions placeholderOptions = new RequestOptions();
        placeholderOptions.placeholder(R.drawable.default_user_art_g_2);


        Glide.with(getApplication())
                .applyDefaultRequestOptions(placeholderOptions)
                    .load(image_)
                    .thumbnail(Glide.with(getApplication())
                            .load(thumb_))
                    .into(profile);

    }

    public void Notifications(View view) {
        startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
    }

    public void viewCart(View view) {
        startActivity(new Intent(HomeActivity.this, CartActivity.class));
    }
}
