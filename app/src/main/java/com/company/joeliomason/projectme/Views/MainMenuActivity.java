package com.company.joeliomason.projectme.Views;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.company.joeliomason.projectme.POJOs.User;
import com.company.joeliomason.projectme.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class MainMenuActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    BootstrapPagerAdapter mSectionsPagerAdapter;
    String date;

    private String mUsername;
    private String mPhotoUrl;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DrawerLayout mDrawerLayout;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu_setup);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        Uri firebaseImage = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginView.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            date = extras.getString("date");
        }
        if(date == null) {
            DateTime today = DateTime.now(TimeZone.getDefault());
            int day = today.getDay();
            int month = today.getMonth();
            int year = today.getYear();
            if(day < 10) {
                date ="0" + day + "/";
            } else {
                date = day + "/";
            }
            if(month < 10) {
                date+="0" + month + "/";
            } else {
                date += month + "/";
            }
            if(year < 10) {
                date+="00" + year;
            } else {
                date += year + "";
            }
        }

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        // Creating new user node, which returns the unique key value
        // new user node would be /users/$userid/
        final String userId = mFirebaseUser.getUid();
        // creating user object

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    User user = new User(mFirebaseAuth.getCurrentUser().getDisplayName(), mFirebaseAuth.getCurrentUser().getEmail());

                    // pushing user to 'users' node using the userId
                    mDatabase.child(userId).setValue(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.show();


        try {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionbar.setTitle(mFirebaseUser.getDisplayName());
        } catch(NullPointerException n) {
            Log.v("nullPointerCaught", n.toString());
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.name);
        TextView navEmail = headerView.findViewById(R.id.email);
        ImageView navImage = headerView.findViewById(R.id.imageView);
        navUsername.setText(mFirebaseUser.getDisplayName());
        navEmail.setText(mFirebaseUser.getEmail());
        navImage.setImageURI(firebaseImage);
        Picasso.get().load(firebaseImage.toString()).into(navImage);


        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new BootstrapPagerAdapter(getResources(), getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(5000, false);
        mViewPager.post(new Runnable() {
            public void run() {
                mViewPager.setCurrentItem(5000, false);
            }
        });
        mViewPager.getAdapter().notifyDataSetChanged();
        mViewPager.setOffscreenPageLimit(1);

        FloatingActionButton fab = (FloatingActionButton) this.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenuActivity.this, CategoryListView.class);
                intent.putExtra("date", date);
                startActivity(intent);
            }
        });





    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_cal:
                Intent intent = new Intent(this, CalendarActivityView.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public class BootstrapPagerAdapter extends FragmentStatePagerAdapter {

        /**
         * Create pager adapter
         *
         * @param resources
         * @param fragmentManager
         */

        public BootstrapPagerAdapter(Resources resources, FragmentManager fragmentManager) {
            super(fragmentManager);

        }

        @Override
        public int getCount() {
            return 10000;
        }

        @Override
        public int getItemPosition(Object object) {
            return FragmentStatePagerAdapter.POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            DateTime pagerdate = DateTime.now(TimeZone.getDefault());
            if(date != null) {
                int day = Integer.parseInt(date.substring(0, 2));
                int month = Integer.parseInt(date.substring(3, 5));
                int year = Integer.parseInt(date.substring(6));
                pagerdate = new DateTime(year, month, day, 0, 0, 0, 0);
            }
            DateTime today = DateTime.now(TimeZone.getDefault());
            DateTime yesterday = today.minusDays(1);
            DateTime tomorrow = today.plusDays(1);
            DateTime days = pagerdate.plusDays(position - 5000);
            String date = days.format("DD/MM/YYYY").toString();
            if(days.isSameDayAs(today)){
                date = "Today";
            } else if(days.isSameDayAs(yesterday)) {
                date = "Yesterday";
            } else if(days.isSameDayAs(tomorrow)){
                date = "Tomorrow";
            }
            return date;
        }

        @Override
        public Fragment getItem(int position) {

            DateTime pagerdate = DateTime.now(TimeZone.getDefault());
            if(date != null) {
                int day = Integer.parseInt(date.substring(0, 2));
                int month = Integer.parseInt(date.substring(3, 5));
                int year = Integer.parseInt(date.substring(6));
                pagerdate = new DateTime(year, month, day, 0, 0, 0, 0);
            }
            DateTime days = pagerdate.plusDays(position - 5000);

            Bundle bundle = new Bundle();
            bundle.putString("date", days.format("DD/MM/YYYY").toString());
            bundle.putInt("position", position);
            Log.v("date", days.format("DD/MM/YYYY").toString());
            Log.v("position", position + "");
            MainMenuView2 mainMenuView2 = new MainMenuView2();
            mainMenuView2.setArguments(bundle);
            //mainMenuView2.newInstance(position);
            return mainMenuView2;
        }



    }

    @Override
    public void onBackPressed() {
        //do nothing
    }

}
