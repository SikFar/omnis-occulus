package com.omnisoculus;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.CompoundButton.OnCheckedChangeListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , LocationListener {


    private boolean mRequestingLocationUpdates = true;
    private GoogleMap mMap;
    private static final String TAG = "EmailPassword";

    private FloatingActionButton fabFrien, fabSet;
    private Button btnSignOut, btnFriends,btnLocate;

    private Marker markerFriend = null;

    private User currentUser;

    private LatLng user1;

    public final static String EXTRA_MESSAGE = "com.omnisoculus.MESSAGE";

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersRef = mRootRef.child("Users");
    private DatabaseReference mCurrentUserRef = null;
    private DatabaseReference mLastKnownLocRef;
    private DatabaseReference mLastKnownLatRef;// = mCurrentUserRef.child("Last known location");
    private DatabaseReference mLastKnownLonRef;// = mCurrentUserRef.child("Last known location");
    private DatabaseReference mUserFriendRef;
    private DatabaseReference mWatchingRef;

    private Location mLastLocation, mCurrentLocation = null;
    private LocationRequest mLocationRequest = null;
    private String mLatitudeText, mLongitudeText;
    private Switch swUpdateLoc;

    private ArrayMap<String,DatabaseReference> friends;
    private ArrayMap<String,MarkerOptions> markers;

    private PendingResult<LocationSettingsResult> result;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    Animation animScale,animScaleStop;

    private FirebaseUser user = null;
    private String uid;
    GoogleApiClient mGoogleApiClient = null;

    LocationSettingsRequest.Builder builder = null;

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener;

    /*public MapsActivity(Animation animScale) {
        this.animScale = animScale;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        animScale = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        animScaleStop = AnimationUtils.loadAnimation(this, R.anim.anim_scale_stop);


        if (mGoogleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        };


        createLocationRequest();



        builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        Firebase.setAndroidContext(this);

        mAuth = FirebaseAuth.getInstance();

        fabFrien = (FloatingActionButton) findViewById(R.id.fabFriends);
        fabFrien.setOnClickListener(onClickListener);
        fabFrien.setOnHoverListener(hoverListener);

        fabSet = (FloatingActionButton)findViewById(R.id.fabSettings);
        fabSet.setOnClickListener(onClickListener);
        fabSet.setOnHoverListener(hoverListener);

        btnSignOut = (Button) findViewById(R.id.btnSignout);
        btnSignOut.setOnClickListener(onClickListener);
        btnSignOut.setOnHoverListener(hoverListener);

        //btnFriends = (Button) findViewById(R.id.btnFriends);
        //btnFriends.setOnClickListener(onClickListener);

        //btnLocate = (Button)findViewById(R.id.btnLocate);
        //btnLocate.setOnClickListener(onClickListener);



        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                uid = user.getUid();
                mCurrentUserRef = mUsersRef.child(user.getUid());

                DatabaseReference friend1 = mRootRef.child("Users").child("z1xqs3idQWcRNdeLLcsdGE3qUnW2");

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:s" +
                            "    igned_in:" + uid);
                    Toast.makeText(getApplicationContext(), "Logged in.",Toast.LENGTH_LONG).show();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        // [END auth_state_listener]

    }



    private void plotFriends() {
        DatabaseReference myFriends = mCurrentUserRef.child("Friends");

        mWatchingRef = mCurrentUserRef.child("Friends activity").child("You are watching");
        friends = new ArrayMap<String,DatabaseReference>();
        myFriends.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {


                //Iterable<DataSnapshot> friends = dataSnapshot.getChildren();
                //DatabaseReference tmp = dataSnapshot.getRef();
                friends.put(dataSnapshot.getKey(),mRootRef.child(dataSnapshot.getValue(String.class)));
                //Log.i("Latitude", "" + dataSnapshot.child("Latitude").getValue(double.class));


                //DatabaseReference user_friend = mRootRef.child(dataSnapshot.getValue(String.class));
                //user_friend.child("Last known location").addChildEventListener(childEventListener);

                friends.get(dataSnapshot.getKey()).child("Name").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.i("afadf","adgadg");
                        Log.i(dataSnapshot.getKey(),dataSnapshot.getKey());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.i("afadf","adgadg");
                        Log.i(dataSnapshot.getKey(),dataSnapshot.getKey());
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                friends.get(dataSnapshot.getKey()).child("Name").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i("Test",String.valueOf(dataSnapshot.getValue()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("Test",String.valueOf(dataSnapshot.getValue()));
                    }
                });

                //user_friend.child("Last known location").child("Latitude").addValueEventListener(valueEventListener);

                //user_friend.child("Name").addValueEventListener(valueEventListener);
                //user_friend.child("Name").addChildEventListener(childEventListener);

                //user_friend.child("Last known location").addChildEventListener(childEventListener);
                mWatchingRef.child(dataSnapshot.getKey()).setValue(true);

                Log.i("Friends",dataSnapshot.getValue(String.class));


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    double mLat = 0.0, mLng = 0.0;

    View.OnHoverListener hoverListener = new View.OnHoverListener() {
        @Override
        public boolean onHover(View v, MotionEvent event) {
            Log.i("Hover","Hover start");

            switch (event.getAction()){
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.startAnimation(animScale);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.startAnimation(animScaleStop);
                    break;
                case MotionEvent.ACTION_HOVER_MOVE:
                    v.startAnimation(animScale);
                    break;
                    /*v.startAnimation(animScaleStop);
                    break;*/
            }
            return true;
        }
    };

    ValueEventListener latEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.i("Test",String.valueOf(dataSnapshot.getValue()));
            mLat = Double.parseDouble(dataSnapshot.getValue(String.class));

            if(mLat != 0.0 && mLng != 0.0) {
                updateMarker(new LatLng(mLat, mLng),"Friend");
            }
            //String msg = ("Friend location added: " + mLat + ", " + mLng);
            //Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
            /*switch (dataSnapshot.getKey()){
                case "Name":
                    Log.i("Change", "Name: "+ String.valueOf(dataSnapshot.getValue(String.class)));
                    break;
                case "Longitude":
                    Log.i("Change", "Longitude: "+ String.valueOf(dataSnapshot.getValue(String.class)));
                    break;
                case "Latitude":
                    Log.i("Change",  "Latitude: "+ String.valueOf(dataSnapshot.getValue(double.class))  );
                    break;
            }*/
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    ValueEventListener longEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.i("Test",String.valueOf(dataSnapshot.getValue()));

            mLng = Double.parseDouble(dataSnapshot.getValue(String.class));
            if(mLat != 0.0 && mLng != 0.0) {
                updateMarker(new LatLng(mLat, mLng),"Friend");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    ChildEventListener childEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i("afadf","adgadg");
            Log.i(dataSnapshot.getKey(),dataSnapshot.getKey());
            /*String mLat = dataSnapshot.child("Latitude").getValue(String.class);
            String mLng = dataSnapshot.child("Longitude").getValue(String.class);
            Log.i("Info",mLng +"  " + mLat);/*
            updateMarker(new LatLng(mLat, mLng));
            String msg = ("Friend location added: " + mLat + ", " + mLng);
            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();*/
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            String mLng = "",mLat = "";
                    mLat = dataSnapshot.getValue(String.class);
                    mLng = dataSnapshot.getValue(String.class);


            Log.i("Kommer hit5","Kommer hit5");
            Log.i(dataSnapshot.getKey(),dataSnapshot.getKey());
            Log.i("Info",mLng +"  " + mLat);/*
            updateMarker(new LatLng(mLat, mLng));
            String msg = ("Friend location changed: " + mLat + ", " + mLng);
            Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();*/
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }



    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                /*case R.id.btnFriends:
                    Intent friends = new Intent(getApplicationContext(), Friends.class);
                    friends.putExtra(EXTRA_MESSAGE, uid);
                    startActivity(friends);
                    break;*/
                case R.id.btnSignout:
                    mAuth.signOut();
                    currentUser.setLoggedInStatus(false);
                    Intent signin = new Intent(getApplicationContext(), SignIn.class);
                    startActivity(signin);
                    finish();
                    break;
                case R.id.fabFriends:
                    Log.i("Floating Action Button","Friends");
                    Intent i = new Intent(getApplicationContext(), Friends.class);
                    i.putExtra(EXTRA_MESSAGE, uid);
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;
                case R.id.fabSettings:
                    Log.i("Floating Action Button","Settings");
                    break;
                /*case R.id.btnLocate:
                    plotFriends();
                    break;*/
                default:
                    break;
            }
        }
    };

    private void updateMarker(LatLng mLatLng, String name) {
        //MIDLERTIDIG
        if(markerFriend == null){
            markerFriend = mMap.addMarker(new MarkerOptions().position(mLatLng));
            markerFriend.setTitle(name);
            mMap.setOnMarkerClickListener(markerClickListener);
        }
        else{
            markerFriend.setPosition(mLatLng);
        }
    }

    private GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerFriend.getPosition(), 17));
            markerFriend.showInfoWindow();
            return true;
        }
    };


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(mRequestingLocationUpdates) {
            //mRequestingLocationUpdates = false;
            startLocationUpdates();

        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient
                , mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    @Override
    public void onLocationChanged(Location location) {
        String msg = ("Location: " + location.getLatitude() + ", " + location.getLongitude());
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();


        mLastKnownLocRef = mCurrentUserRef.child("Last known location");
        //mLastKnownLocRef.addChildEventListener(childEventListener2);
        mLastKnownLatRef = mLastKnownLocRef.child("Latitude");
        mLastKnownLonRef = mLastKnownLocRef.child("Longitude");

        mCurrentLocation = location;

        double latitude = mCurrentLocation.getLatitude();
        double longitude = mCurrentLocation.getLongitude();

        mLastKnownLonRef.setValue(String.valueOf(longitude));
        mLastKnownLatRef.setValue(String.valueOf(latitude));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            this.moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.omnisoculus/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
    }

    @Override
    public void onStart() {

        super.onStart();
        mGoogleApiClient.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.omnisoculus/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
        if(user == null){
            user = mAuth.getCurrentUser();
            uid = user.getUid();

        }
        currentUser = new User(uid);
        mCurrentUserRef = mUsersRef.child(user.getUid());


        //Midlertitid
        DatabaseReference friend1 = mRootRef.child("Users").child("l6SEYUvFwkaJFOpoVRqtM79OPpl1");

        friend1.child("Last known location").child("Latitude").addValueEventListener(latEventListener);
        friend1.child("Last known location").child("Longitude").addValueEventListener(longEventListener);


    }

}
