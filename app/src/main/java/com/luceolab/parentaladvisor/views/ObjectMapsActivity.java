package com.luceolab.parentaladvisor.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.luceolab.parentaladvisor.Constants;
import com.luceolab.parentaladvisor.R;
import com.luceolab.parentaladvisor.restclient.AccountsService;
import com.luceolab.parentaladvisor.restclient.ServiceGenerator;
import com.luceolab.parentaladvisor.restclient.models.MonitoringObject;
import com.luceolab.parentaladvisor.restclient.models.State;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ObjectMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean mIsDailyChart;
    private String mObjectId;
    private String mAccessToken;
    private MonitoringObject mMonitoringObject;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_maps);

        // Init date/time library
        JodaTimeAndroid.init(this);

        // ActionBar settings
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle bundle = getIntent().getExtras();
        mObjectId = bundle.getString("object_id", null);

        // Object ID have not passed
        if (mObjectId == null) {
            finish();
        }
        mIsDailyChart = bundle.getBoolean("isDailyChart", false);

        // Access Token
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        mAccessToken = mSharedPreferences.getString("access_token", null);

        getMonitoringObject();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    // Load monitoring object
    private boolean getMonitoringObject() {
        if (mAccessToken != null) {

            // Create retrofit service
            AccountsService loginService =
                    ServiceGenerator.createService(AccountsService.class, Constants.REST_API_CLIENT_ID, Constants.REST_API_CLIENT_SECRET);

            // Get single monitoring object
            Call<MonitoringObject> call = loginService.getSingleObject(
                    mObjectId,
                    mAccessToken
            );
            call.enqueue(new Callback<MonitoringObject>() {
                @Override
                public void onResponse(Call<MonitoringObject> call, Response<MonitoringObject> response) {
                    MonitoringObject monitoringObject = response.body();
                    if (monitoringObject != null) {
                        mMonitoringObject = monitoringObject;
                        drawMarkers();
                    }
                }

                @Override
                public void onFailure(Call<MonitoringObject> call, Throwable t) {

                }
            });

            return true;
        }

        return false;
    }

    private void drawMarkers() {
        if (!mIsDailyChart) {

            // Show only one last state
            if (mMonitoringObject != null && !mMonitoringObject.states.isEmpty()) {
                actionBar.setTitle(getString(R.string.last_location_of_person) + " " + mMonitoringObject.full_name);
                State lastState = mMonitoringObject.states.get(0);

                if (mMap != null) {
                    // Create new marker
                    LatLng myLocation = new LatLng(lastState.lat, lastState.lng);
                    mMap.addMarker(new MarkerOptions()
                            .position(myLocation)
                            .title(mMonitoringObject.full_name)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                }
            }

        } else {

            // Show daily route of object
            if (mMonitoringObject != null && !mMonitoringObject.states.isEmpty()) {
                actionBar.setTitle(getString(R.string.daily_route_of_person) + " " +
                        mMonitoringObject.full_name);
                List<State> states = mMonitoringObject.states;
                PolylineOptions dailyRoute = new PolylineOptions();
                int i = 0;
                for(State s : states) {
                    LatLng myLocation = new LatLng(s.lat, s.lng);
                    dailyRoute.add(myLocation);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

                    if (i == 0) {
                        mMap.addMarker(new MarkerOptions()
                                .position(myLocation)
                                .title(getString(R.string.finish))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)));
                    }

                    if (i == (states.size() - 1)) {
                        mMap.addMarker(new MarkerOptions()
                                .position(myLocation)
                                .title(getString(R.string.start))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location)));
                    }
                    i++;
                }
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);

                dailyRoute.width(12)
                          .color(ContextCompat.getColor(getApplicationContext(), R.color.colorFirst))
                          .geodesic(true);
                Polyline line = mMap.addPolyline(dailyRoute);
            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
