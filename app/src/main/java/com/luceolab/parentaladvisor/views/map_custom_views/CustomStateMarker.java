package com.luceolab.parentaladvisor.views.map_custom_views;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.luceolab.parentaladvisor.restclient.models.State;

public class CustomStateMarker implements ClusterItem {
    private LatLng mPosition;
    private State mState;
    private String mTitle;
    private String mSnippet;
    private boolean mMyLocation;

    public CustomStateMarker(double lat, double lng, State state) {
        mPosition = new LatLng(lat, lng);
        mState = state;
        mTitle = state.object_name;
        mSnippet = state.object_phone;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public State getState() {
        return mState;
    }

    public String getTitle(){
        return mTitle;
    }

    public String getSnippet(){
        return mSnippet;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setState(State state) {
        mState = state;
        mPosition = new LatLng(state.lat, state.lng);
    }

    public void setMyLocationIcon(boolean flag) {
        mMyLocation = flag;
    }

    public boolean getMyLocationIcon() {
        return mMyLocation;
    }
}
