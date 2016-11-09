package com.example.martinhuang.mapgps;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.Manifest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.firebase.client.Firebase;
import com.firebase.client.snapshot.DoubleNode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        GoogleMap.InfoWindowAdapter {


    public static final String USER_ID = "USER_ID";
    public static final String EMAIL = "EMAIL";
    public static final String FIREBASE_ID = "FIREBASE_ID";

    Firebase firebase;
    private DatabaseReference mDatabase;
    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    double currentLat;
    double currentLong;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationDrawer;
    private ActionBarDrawerToggle drawerToggle;

    private TextView tvLat;
    private TextView tvLng;

    private String messageText;
    private String userID;
    private String email;
    private String firebaseUserID;
    private String messageKey;
    private LatLng latLng;

    SupportMapFragment mapFragment;
    GoogleMap googleMap;

    private static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_maps);

        init();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
    }

    private void init() {

        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvLat = (TextView)findViewById(R.id.tv_lat);
        tvLng = (TextView)findViewById(R.id.tv_lng);

        //setup toolbar
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //drawer menu layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        navigationDrawer = (NavigationView)findViewById(R.id.nvView);
        setupDrawerContent(navigationDrawer);

        //animate the hamburger icon turning
        drawerLayout.addDrawerListener(drawerToggle);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        Intent intent = getIntent();
        userID = intent.getStringExtra(USER_ID);
        email = intent.getStringExtra(EMAIL);
        firebaseUserID = intent.getStringExtra(FIREBASE_ID);

        //tested getting info from loginactivity ok
//        Toast.makeText(MapsActivity.this, "Facebook UserID is : " + userID + "\nEmail: " + email +
//                "\n Firebase: " + firebaseUserID, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //permission to gps
        //you want gps and not giving permission to use gps
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);

                //reposition the current location button on map
                mMap.setPadding(0,dpToPx(80),0,0);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setInfoWindowAdapter(this);

        initListenDataChange();


    }

    private void initListenDataChange() {

        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //user firebase ID
                for(DataSnapshot firebaseID: dataSnapshot.getChildren()) {
                    //get Posts
                    for(DataSnapshot posts: firebaseID.getChildren()) {

                        if(posts.getKey().equals("Posts")) {

                            Toast.makeText(MapsActivity.this,posts.getKey(),Toast.LENGTH_LONG).show();

                        }
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        ValueEventListener postListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.hasChild("Posts")) {
//                    Message message = dataSnapshot.getValue(Message.class);
//                    String text = message.getMessage();
//
//                    double tempLat = Double.parseDouble(message.getLatitude());
//                    double tempLng = Double.parseDouble(message.getLongitude());
//
//                    LatLng tempPosition = new LatLng(tempLat, tempLng);
//                    dropRetrievedMessages(text, tempPosition);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//        mDatabase.addValueEventListener(postListener);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker.remove();
//        }

//        firebase = new Firebase("https://mapgps-145221.firebaseio.com/");

        currentLat = location.getLatitude();
        currentLong = location.getLongitude();

        //for writing to firebase
        Map<String, String> coordinates = new HashMap<String, String>();
        coordinates.put("Long", Double.toString(currentLong));
        coordinates.put("Lat", Double.toString(currentLat));

//        firebase.child("test").setValue(coordinates);

        Log.d("TAG","firebasechild ");

        //Place current location marker
        latLng = new LatLng(currentLat, currentLong);
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Location");
//        markerOptions.snippet("Long: " + currentLong + "\nLat: " + currentLat);
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//
//        mCurrLocationMarker = mMap.addMarker(markerOptions);
//

        //move map camera / zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //TODO:
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.create_memu, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.search:
                startSearchLocationActivity(findViewById(R.id.search));
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(21)
    private void startSearchLocationActivity(View view) {
        Intent intent = new Intent(MapsActivity.this, SearchLocationActivity.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int[] coordinates = new int[2];
            view.getLocationInWindow(coordinates);

            int cx = (int) (coordinates[0] + view.getWidth() / 2.0);
            int cy = (int) (coordinates[1] + view.getHeight() / 2.0);

            intent.putExtra(SearchLocationActivity.CENTER_X, cx);
            intent.putExtra(SearchLocationActivity.CENTER_Y, cy);

            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());

        } else {
            startActivity(intent);
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    private void selectDrawerItem(MenuItem menuItem) {

        switch(menuItem.getItemId()) {

            //post message
            case R.id.action_message:

                drawerLayout.closeDrawers();

                final EditText editText = (EditText)findViewById(R.id.et);
                editText.setSingleLine();
                int backgroundHeight = (int)editText.getTextSize()*(int)1.2;
               // editText.setHeight(backgroundHeight);
                editText.getText().clear();
                editText.setVisibility(View.VISIBLE);
                editText.setHint("Write something cool");

                //so keyboard can auto show
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();

                final InputMethodManager inputManager =
                        (InputMethodManager) MapsActivity.this.
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(editText,InputMethodManager.SHOW_IMPLICIT);

                editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if(actionId == EditorInfo.IME_ACTION_DONE) {

                            if(editText.getText().toString().trim().length() != 0) {

                                Toast.makeText(MapsActivity.this,editText.getText(),Toast.LENGTH_LONG).show();

                                Message message = new Message();
                                latLng = new LatLng(currentLat, currentLong);
                                message.setMessage(editText.getText().toString());
                                message.setLatitude(Double.toString(currentLat));
                                message.setLongitude(Double.toString(currentLong));


                                messageKey = mDatabase.child("users").child(firebaseUserID).child("Posts").child("Messages")
                                        .push().getKey();
//                                mDatabase.child("users").child(firebaseUserID).child("Posts").child("Messages")
//                                        .child("Message").setValue(message).push();

                                mDatabase.child("users").child(firebaseUserID).child("Posts").child("Messages")
                                        .push().child("Message").setValue(message);

                                editText.clearFocus();
                                editText.setVisibility(View.INVISIBLE);

                                hideKeyboard(inputManager);

                                messageText = editText.getText().toString();
                                dropMessage(editText.getText().toString());
                            }
                            else {

                                hideKeyboard(inputManager);
                                editText.setVisibility(View.INVISIBLE);

                                Toast.makeText(MapsActivity.this, "ERROR: Have to enter text to post!", Toast.LENGTH_SHORT)
                                        .show();
                            }

                            return true;
                        }
                        return false;
                    }
                });

                break;
            case R.id.logout:

                LoginManager.getInstance().logOut();
                Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.left_to_right ,R.anim.right_to_left);
                finish();

        }
    }

    private void hideKeyboard(InputMethodManager inputManager) {

        inputManager.hideSoftInputFromWindow(
                MapsActivity.this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

    }

    private void dropMessage(String message) {
        latLng = new LatLng(currentLat, currentLong);
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(false).title(message));
    }

    private void dropRetrievedMessages(String message, LatLng position) {
        mMap.addMarker(new MarkerOptions().position(position).draggable(false).title(message));
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return customMessageWindow(marker);
    }

    private View customMessageWindow(Marker marker) {

        LinearLayout infoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams infoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoView.setOrientation(LinearLayout.HORIZONTAL);
        infoView.setLayoutParams(infoViewParams);

        LinearLayout subInfoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams subInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subInfoView.setOrientation(LinearLayout.VERTICAL);
        subInfoView.setLayoutParams(subInfoViewParams);

        TextView subInfoMessage = new TextView(MapsActivity.this);
        subInfoMessage.setText(messageText);
        subInfoView.addView(subInfoMessage);

        infoView.addView(subInfoView);

        return infoView;
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}
