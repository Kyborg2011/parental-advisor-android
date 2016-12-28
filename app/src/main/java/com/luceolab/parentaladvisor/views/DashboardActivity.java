package com.luceolab.parentaladvisor.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.luceolab.parentaladvisor.Constants;
import com.luceolab.parentaladvisor.R;
import com.luceolab.parentaladvisor.Utils;
import com.luceolab.parentaladvisor.restclient.AccountsService;
import com.luceolab.parentaladvisor.restclient.ServiceGenerator;
import com.luceolab.parentaladvisor.restclient.models.MonitoringObject;
import com.luceolab.parentaladvisor.restclient.models.State;
import com.luceolab.parentaladvisor.views.map_custom_views.CustomStateMarker;

import net.danlew.android.joda.DateUtils;
import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<CustomStateMarker>,
        ClusterManager.OnClusterInfoWindowClickListener<CustomStateMarker>,
        ClusterManager.OnClusterItemClickListener<CustomStateMarker>,
        ClusterManager.OnClusterItemInfoWindowClickListener<CustomStateMarker> {

    private LocationManager mLocationManager;
    private Context mContext;
    private GoogleMap googleMap = null;
    private CustomStateMarker mMyLocationMarker;
    private ClusterManager<CustomStateMarker> mClusterManager;
    private CustomStateMarker clickedClusterItem;

    private String mAccessToken;
    private HashMap<MonitoringObject, CustomStateMarker> mObjectsMarkers = new HashMap<>();

    private boolean mZoomed = false;

    @Override
    public boolean onClusterClick(Cluster<CustomStateMarker> cluster) {
        return false;
    }

    private class CustomStateMarkerRenderer extends DefaultClusterRenderer<CustomStateMarker> {
        private Context mContext;
        private GoogleMap mGoogleMap;

        private final IconGenerator mIconGenerator;
        private TextView mFullNameView;
        private final ImageView mImageView;

        public CustomStateMarkerRenderer() {
            super(getApplicationContext(), googleMap, mClusterManager);
            mContext = getApplicationContext();
            mGoogleMap = googleMap;
            mIconGenerator = new IconGenerator(mContext);
            mImageView = new ImageView(mContext);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(CustomStateMarker state, MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(state, markerOptions);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<CustomStateMarker> cluster, MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        @Override
        protected void onClusterItemRendered(CustomStateMarker clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
            if (clusterItem.getMyLocationIcon()) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location));
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker));
            }
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Never render clusters.
            return false;
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            LatLng myLocation = new LatLng(lat, lng);

            if (null != googleMap) {

                State state = new State();
                state.object_name = getString(R.string.my_current_location);
                state.lat = lat;
                state.lng = lng;

                if (mMyLocationMarker == null) {
                    mMyLocationMarker = new CustomStateMarker(lat,
                            lng, state);
                    mMyLocationMarker.setMyLocationIcon(true);
                    mClusterManager.addItem(mMyLocationMarker);
                } else {
                    mMyLocationMarker.setState(state);
                }

                // Update cluster manager
                mClusterManager.cluster();

                // Zoom to level 15 (streets and buildings)
                if (mZoomed == false) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                    mZoomed = true;
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Init date/time library
        JodaTimeAndroid.init(this);

        mContext = this;

        // ActionBar settings
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_dashboard));
            actionBar.setIcon(R.drawable.ic_action_bar);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        ImageButton addNewObjectButton = (ImageButton) findViewById(R.id.add_object_but);
        if (addNewObjectButton != null) {
            addNewObjectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, AddObjectActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Working with Google Maps
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        // Init Location Manager to get and update "My current location"
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000,
                100, mLocationListener);

        // Access Token
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        mAccessToken = mSharedPreferences.getString("access_token", null);
        boolean sentTokenToServer = mSharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false);

        if (!sentTokenToServer) {
            String firebaseToken = FirebaseInstanceId.getInstance().getToken();
            Utils.sendFirebaseToken(firebaseToken, mAccessToken);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("firebase_token", firebaseToken);
            editor.putBoolean(Constants.SENT_TOKEN_TO_SERVER, true);
            editor.apply();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("oncreate", "launch");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_action_objects:
                // Start objects list activity
                Intent intent = new Intent(this, ObjectsListActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_action_settings:
                // Start settings activity
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        mClusterManager = new ClusterManager<>(this, googleMap);
        mClusterManager.setRenderer(new CustomStateMarkerRenderer());

        // Setting a custom info window adapter for the google map
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker args) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker args) {

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.custom_maps_info_window, null);

                TextView title = (TextView) v.findViewById(R.id.custom_marker_window_title);
                TextView phone = (TextView) v.findViewById(R.id.custom_marker_window_phone);
                TextView time = (TextView) v.findViewById(R.id.custom_marker_window_time);

                if (clickedClusterItem != null) {

                    title.setText(clickedClusterItem.getState().object_name);
                    phone.setText(Utils.formatPhoneNumber(clickedClusterItem.getState().object_phone));

                    if (clickedClusterItem.getState().timestamp != null) {
                        DateTime dateTime = new DateTime(clickedClusterItem.getState().timestamp);
                        long secondsElapsed = Utils.printTimeDifference(dateTime);
                        String interval = "";
                        if (secondsElapsed < 60) {
                            Double seconds = new Double(secondsElapsed);
                            interval = getString(R.string.sended) + " " + seconds.intValue() + " " +
                                    getString(R.string.seconds);
                        } else if (secondsElapsed < 3600) {
                            Double minutes = new Double(secondsElapsed / 60);
                            interval = getString(R.string.sended) + " " + minutes.intValue() + " " +
                                    getString(R.string.minutes);
                        } else {
                            interval = DateUtils.formatDateTime(getApplicationContext(), dateTime,
                                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);

                        }

                        time.setText(interval);
                    }
                }

                return v;

            }
        });

        googleMap.setOnCameraChangeListener(mClusterManager);
        googleMap.setOnMarkerClickListener(mClusterManager);
        googleMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<CustomStateMarker>() {
            @Override
            public boolean onClusterItemClick(CustomStateMarker item) {
                clickedClusterItem = item;
                return false;
            }
        });

        // Create markers
        loadMarkers();
        mClusterManager.cluster();
    }

    private Location getLocation() {
        Location location = null;

        try {

            mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled || isNetworkEnabled) {

                // First get location from Network Provider
                if (isNetworkEnabled) {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 40000, 10, mLocationListener);
                    if (mLocationManager != null) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        }
                        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }

                //get the location by gps
                if (isGPSEnabled) {
                    if (location == null) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 40000, 10, mLocationListener);
                        if (mLocationManager != null) {
                            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    private boolean updateMonitoringObjects() {

        if (mAccessToken != null && googleMap != null) {

            // Create retrofit service
            AccountsService loginService =
                    ServiceGenerator.createService(AccountsService.class, Constants.REST_API_CLIENT_ID, Constants.REST_API_CLIENT_SECRET);

            // Get all user's monitoring objects
            Call<List<MonitoringObject>> call = loginService.getMonitoringObjects(
                    mAccessToken
            );
            call.enqueue(new Callback<List<MonitoringObject>>() {
                @Override
                public void onResponse(Call<List<MonitoringObject>> call, Response<List<MonitoringObject>> response) {
                    List<MonitoringObject> monitoringObjects = response.body();

                    if (monitoringObjects != null) {
                        for (MonitoringObject monObject : monitoringObjects) {

                            if (monObject.states.size() > 0) {
                                State lastState = monObject.states.get(0);

                                // Create new marker
                                LatLng myLocation = new LatLng(lastState.lat, lastState.lng);

                                lastState.object_phone = monObject.phone;
                                lastState.object_name = monObject.full_name;

                                CustomStateMarker marker = new CustomStateMarker(lastState.lat,
                                        lastState.lng, lastState);
                                mClusterManager.addItem(marker);

                                mObjectsMarkers.put(monObject, marker);
                            }
                        }

                        mClusterManager.cluster();
                    } else {
                        showErrorToast();
                    }
                }

                @Override
                public void onFailure(Call<List<MonitoringObject>> call, Throwable t) {
                    showErrorToast();
                }
            });

            return true;
        }

        return false;
    }

    private void loadMarkers() {
        // Get current location
        Location currentLocation = getLocation();
        // Remove old markers
        googleMap.clear();

        if (currentLocation != null) {
            // Marker with current user's location
            LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            State myState = new State();
            myState.object_name = getString(R.string.my_current_location);
            myState.lat = myLocation.latitude;
            myState.lng = myLocation.longitude;

            if (mMyLocationMarker == null) {
               mMyLocationMarker = new CustomStateMarker(currentLocation.getLatitude(),
                        currentLocation.getLongitude(), myState);
               mMyLocationMarker.setMyLocationIcon(true);
               mClusterManager.addItem(mMyLocationMarker);
            } else {
                mMyLocationMarker.setState(myState);
            }

            // Zoom to level 15 (streets and buildings)
            if (mZoomed == false) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                mZoomed = true;
            }
        }

        // Update cluster manager
        mClusterManager.cluster();

        // Update monitoring objects
        updateMonitoringObjects();
    }

    private void showErrorToast() {
        Toast toast = Toast.makeText(getApplicationContext(),
                getString(R.string.simple_error), Toast.LENGTH_LONG);
        toast.show();
    }

    public void startProgressBar(final ProgressBar bar) {
        bar.setIndeterminate(true);
        bar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bar.setIndeterminate(false);
                        bar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("binding", data.getStringExtra("battery"));
    }

    @Override
    protected void onNewIntent(Intent data) {
        super.onNewIntent(data);

        CustomStateMarker m;
        Bundle bundle = data.getExtras();

        int action = bundle.getInt("action");

        if (action == Constants.NEW_STATE_ACTION) {

            final ProgressBar bar = (ProgressBar) findViewById(R.id.dashboardProgressBar);

            if (bar != null) {
                startProgressBar(bar);
            }

            LatLng pos = new LatLng(bundle.getDouble("lat"), bundle.getDouble("lng"));

            for (MonitoringObject monObj : mObjectsMarkers.keySet()) {
                if (monObj._id.equals(bundle.getString("object"))) {
                    mClusterManager.removeItem(mObjectsMarkers.get(monObj));
                }
            }

            MonitoringObject monObj = new MonitoringObject();
            monObj._id = bundle.getString("object");

            State state = new State();
            state.object_name = bundle.getString("object_name");
            state.timestamp = bundle.getString("timestamp");
            state.object_phone = bundle.getString("object_phone");
            state.battery_level = bundle.getDouble("battery_level");
            state.lat = bundle.getDouble("lat");
            state.lng = bundle.getDouble("lng");
            state.object = bundle.getString("object");

            // New clustered marker
            m = new CustomStateMarker(bundle.getDouble("lat"), bundle.getDouble("lng"), state);
            mClusterManager.addItem(m);
            mObjectsMarkers.put(monObj, m);

            // Update clusters
            mClusterManager.cluster();
        }
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<CustomStateMarker> cluster) {
    }

    @Override
    public boolean onClusterItemClick(CustomStateMarker item) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(CustomStateMarker item) {
    }
}
