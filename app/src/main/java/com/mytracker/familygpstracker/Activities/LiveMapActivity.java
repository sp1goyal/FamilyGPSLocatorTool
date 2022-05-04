package com.mytracker.familygpstracker.Activities;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mytracker.familygpstracker.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class LiveMapActivity extends AppCompatActivity implements OnMapReadyCallback {


    @BindView(R.id.toolbarLiveMap) Toolbar toolbar;

    GoogleMap mMap;
    LatLng friendLatLng;
    String latitude,longitude,name,userid,prevdate,prevImage,myImage,myName,myLat,myLng,myDate;
    Marker marker;
    DatabaseReference reference;
    ;
    ArrayList<String> mKeys;
    MarkerOptions myOptions;
    private ValueAnimator lastPulseAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);
        ButterKnife.bind(this);


        mKeys = new ArrayList<>();
        Intent intent = getIntent();

        if(intent!=null)
        {
            latitude=intent.getStringExtra("latitude");
            longitude = intent.getStringExtra("longitude");
            name = intent.getStringExtra("name");
            userid = intent.getStringExtra("userid");
            prevdate = intent.getStringExtra("date");
            prevImage = intent.getStringExtra("image");

            toolbar.setTitle(name +"'s" + " Location");
            setSupportActionBar(toolbar);
            if(getSupportActionBar()!=null)
            {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

        }

        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);


        SupportMapFragment mapFragment =  (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        myName = dataSnapshot.child("name").getValue(String.class);
                        myLat = dataSnapshot.child("lat").getValue(String.class);
                        myLng = dataSnapshot.child("lng").getValue(String.class);
                        myDate = dataSnapshot.child("date").getValue(String.class);
                        myImage = dataSnapshot.child("profile_image").getValue(String.class);


                        friendLatLng = new LatLng(Double.parseDouble(myLat),Double.parseDouble(myLng));

                        myOptions = new MarkerOptions();
                        myOptions.position(friendLatLng);
                        myOptions.snippet("Last seen: "+myDate);
                        myOptions.title(myName);

                        if(marker == null)
                        {
                            marker = mMap.addMarker(myOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(friendLatLng,15));
                            createRippleEffect(friendLatLng);
                        }
                        else
                        {
                            marker.setPosition(friendLatLng);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View row = getLayoutInflater().inflate(R.layout.custom_snippet,null);
                TextView nameTxt = row.findViewById(R.id.snippetName);
                TextView dateTxt = row.findViewById(R.id.snippetDate);
                CircleImageView imageTxt = row.findViewById(R.id.snippetImage);
                if(myName == null && myDate == null)
                {
                    nameTxt.setText(name);
                    dateTxt.setText(dateTxt.getText().toString() + prevdate);
                    Picasso.get().load(prevImage).placeholder(R.drawable.defaultprofile).into(imageTxt);
                }
                else
                {
                    nameTxt.setText(myName);
                    dateTxt.setText(dateTxt.getText().toString() + myDate);
                    Picasso.get().load(myImage).placeholder(R.drawable.defaultprofile).into(imageTxt);
                }


                return row;
            }
        });

        friendLatLng = new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
        MarkerOptions optionsnew = new MarkerOptions();

        optionsnew.position(friendLatLng);
        optionsnew.title(name);
        optionsnew.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


        if(marker == null)
        {
            marker = mMap.addMarker(optionsnew);
        }
        else
        {
            marker.setPosition(friendLatLng);
        }


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(friendLatLng,15));
        createRippleEffect(friendLatLng);



    }

    private void createRippleEffect(final LatLng sourceLocation) {

        // First ripple effect
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final GroundOverlay groundOverlay11 = mMap.addGroundOverlay(new
                        GroundOverlayOptions()
                        .position(sourceLocation, 2000)
                        .transparency(0.6f)
                        .image(BitmapDescriptorFactory.fromResource(R.drawable.riple_icon)));
                OverLay(groundOverlay11, 6000);
            }
        }, 0);

        //Second ripple effect
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final GroundOverlay groundOverlay12 = mMap.addGroundOverlay(new
                        GroundOverlayOptions()
                        .position(sourceLocation, 2000)
                        .transparency(0.6f)
                        .image(BitmapDescriptorFactory.fromResource(R.drawable.riple_icon)));
                OverLay(groundOverlay12, 5000);
            }
        }, 0);

        //Third ripple effect
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final GroundOverlay groundOverlay13 = mMap.addGroundOverlay(new
                        GroundOverlayOptions()
                        .position(sourceLocation, 200)
                        .transparency(0.6f)
                        .image(BitmapDescriptorFactory.fromResource(R.drawable.riple_icon)));
                OverLay(groundOverlay13, 4000);
            }
        }, 0);

    }

    public void OverLay(final GroundOverlay groundOverlay, int duration) {
        lastPulseAnimator = ValueAnimator.ofInt(0, 1500);
        int r = 99999;
        lastPulseAnimator.setRepeatCount(r);
        //lastPulseAnimator.setIntValues(0, 500);
        lastPulseAnimator.setDuration(duration);
        lastPulseAnimator.setEvaluator(new IntEvaluator());
        lastPulseAnimator.setInterpolator(new LinearInterpolator());
        lastPulseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                Integer i = (Integer) valueAnimator.getAnimatedValue();
                groundOverlay.setDimensions(i);
                groundOverlay.setTransparency(animatedFraction);
            }
        });
        lastPulseAnimator.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
