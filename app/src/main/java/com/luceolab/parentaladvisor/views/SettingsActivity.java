package com.luceolab.parentaladvisor.views;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;

import com.luceolab.parentaladvisor.Constants;
import com.luceolab.parentaladvisor.R;
import com.luceolab.parentaladvisor.restclient.AccountsService;
import com.luceolab.parentaladvisor.restclient.ServiceGenerator;
import com.luceolab.parentaladvisor.restclient.models.MonitoringObject;
import com.luceolab.parentaladvisor.restclient.models.Settings;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String mAccessToken;
    private Context mContext;

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setupActionBar();

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, 0);
        mAccessToken = prefs.getString("access_token", null);
        loadCurrentSettings();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new GeneralPreferenceFragment()).commit();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mAccessToken != null) {


        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            Preference button = findPreference("exit");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("access_token", null);
                    editor.apply();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    // Load user settings
    private boolean loadCurrentSettings() {
        if (mAccessToken != null) {

            // Create retrofit service
            AccountsService loginService =
                    ServiceGenerator.createService(AccountsService.class, Constants.REST_API_CLIENT_ID, Constants.REST_API_CLIENT_SECRET);

            Call<Settings> call2 = loginService.saveSettings(
                    mAccessToken,
                    "11",//Boolean.toString(sharedPreferences.getBoolean("monitoring", true)),
                    "11",//Integer.toString(sharedPreferences.getInt("notifications_list", 0)),
                    "11",//sharedPreferences.getString("change_full_name", ""),
                    "11",//sharedPreferences.getString("change_phone", ""),
                    "11"//sharedPreferences.getString("change_password", "")
            );
            call2.enqueue(new Callback<Settings>() {
                @Override
                public void onResponse(Call<Settings> call, Response<Settings> response) {
                }

                @Override
                public void onFailure(Call<Settings> call, Throwable t) {

                }
            });

            // Get single monitoring object
            Call<Settings> call = loginService.getSettings(
                    mAccessToken
            );
            call.enqueue(new Callback<Settings>() {
                @Override
                public void onResponse(Call<Settings> call, Response<Settings> response) {
                    Settings settings = response.body();
                    SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = appPreferences.edit();
                    editor.putBoolean("monitoring", settings.monitoring_status);
                    editor.putString("notifications_list", Integer.toString(settings.notifications));
                    editor.putString("change_full_name", settings.user.full_name);
                    editor.putString("change_phone", settings.user.phone);
                    editor.apply();
                }

                @Override
                public void onFailure(Call<Settings> call, Throwable t) {

                }
            });

            return true;
        }

        return false;
    }
}
