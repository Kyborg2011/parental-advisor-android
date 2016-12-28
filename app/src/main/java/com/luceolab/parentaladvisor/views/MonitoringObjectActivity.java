package com.luceolab.parentaladvisor.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luceolab.parentaladvisor.Constants;
import com.luceolab.parentaladvisor.R;
import com.luceolab.parentaladvisor.Utils;
import com.luceolab.parentaladvisor.restclient.AccountsService;
import com.luceolab.parentaladvisor.restclient.ServiceGenerator;
import com.luceolab.parentaladvisor.restclient.models.MonitoringObject;
import com.luceolab.parentaladvisor.restclient.models.State;

import net.danlew.android.joda.DateUtils;
import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonitoringObjectActivity extends AppCompatActivity {

    private String mObjectId;
    private MonitoringObject mMonitoringObject;
    private String mAccessToken;
    private ActionBar actionBar;
    private String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_object);

        // Init date/time library
        JodaTimeAndroid.init(this);

        // ActionBar settings
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();
        mObjectId = bundle.getString("object_id", null);
        mAction = bundle.getString("action", null);

        // Object ID have not passed
        if (mObjectId == null) {
            finish();
        }

        // Access Token
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        mAccessToken = mSharedPreferences.getString("access_token", null);

        getMonitoringObject();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.objects_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_action_settings:
                // Start settings activity
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
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
                        setData();
                    }
                }

                @Override
                public void onFailure(Call<MonitoringObject> call, Throwable t) {
                    showErrorToast();
                }
            });

            return true;
        }

        return false;
    }

    private void showErrorToast() {
        Toast toast = Toast.makeText(getApplicationContext(),
                getString(R.string.simple_error), Toast.LENGTH_LONG);
        toast.show();
    }

    private void setData() {
        if (mMonitoringObject != null) {
            actionBar.setTitle(mMonitoringObject.full_name);

            TextView latitude = (TextView) findViewById(R.id.monitoring_object_latitude);
            TextView longtitude = (TextView) findViewById(R.id.monitoring_object_longtitude);
            TextView lastConnection = (TextView) findViewById(R.id.monitoring_object_last_connection);
            TextView phoneNumber = (TextView) findViewById(R.id.activity_object_phone_number);
            TextView batteryLevel = (TextView) findViewById(R.id.activity_object_battery_level);
            TextView operationSystem = (TextView) findViewById(R.id.activity_object_operation_system);

            LinearLayout coordinatesLayout = (LinearLayout) findViewById(R.id.monitoring_object_coordinates_layout);
            LinearLayout batteryLevelLayout = (LinearLayout) findViewById(R.id.monitoring_object_battery_level_layout);
            LinearLayout commonLayout = (LinearLayout) findViewById(R.id.monitoring_object_common_layout);

            Button callButton = (Button) findViewById(R.id.monitoring_object_action_call);

            phoneNumber.setText(Utils.formatPhoneNumber(mMonitoringObject.phone));
            callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start calling
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mMonitoringObject.phone));
                    startActivity(dialIntent);
                }
            });

            if (mMonitoringObject.operation_system != null) {
                operationSystem.setText(mMonitoringObject.operation_system);
            } else {
                commonLayout.setVisibility(View.INVISIBLE);
            }

            if (mMonitoringObject.states.size() > 0 && mMonitoringObject.states != null) {
                State lastState = mMonitoringObject.states.get(0);

                DateTime dateTime = new DateTime(lastState.timestamp);
                lastConnection.setText(getString(R.string.last_connection_time) + " " +
                        DateUtils.formatDateTime(this, dateTime, DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_SHOW_TIME));

                longtitude.setText(getString(R.string.longtitude) + " " + Utils.round(lastState.lng, 3));
                latitude.setText(getString(R.string.latitude) + " " + Utils.round(lastState.lat, 3));
                batteryLevel.setText(lastState.battery_level.intValue() + "%");

                Button showOnMapButton = (Button) findViewById(R.id.monitoring_object_action_view_on_map);
                if (showOnMapButton != null) {
                    showOnMapButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), ObjectMapsActivity.class);
                            intent.putExtra("object_id", mObjectId);
                            startActivity(intent);
                        }
                    });
                }

                Button showDailyHistoryButton = (Button) findViewById(R.id.monitoring_object_action_show_daily_history);
                if (showDailyHistoryButton != null) {
                    showDailyHistoryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), ObjectMapsActivity.class);
                            intent.putExtra("object_id", mObjectId);
                            intent.putExtra("isDailyChart", true);
                            startActivity(intent);
                        }
                    });
                }
            } else {
                coordinatesLayout.removeAllViews();
                batteryLevelLayout.removeAllViews();
                coordinatesLayout.setVisibility(View.INVISIBLE);
                batteryLevelLayout.setVisibility(View.INVISIBLE);
            }

            Button deleteButton = (Button) findViewById(R.id.monitoring_object_action_remove);
            if (deleteButton != null) {
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(mMonitoringObject._id);
                    }
                });
            }

            Button editButton = (Button) findViewById(R.id.monitoring_object_action_edit);
            if (editButton != null) {
                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditDialog(mMonitoringObject._id);
                    }
                });
            }
            if (mAction != null) {
                if (mAction.equals("remove")) {
                    showDialog(mMonitoringObject._id);
                }
                if (mAction.equals("edit")) {
                    showEditDialog(mMonitoringObject._id);
                }
            }
        }
    }

    public void showDialog(final String objectId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MonitoringObjectActivity.this);
        LayoutInflater inflater = MonitoringObjectActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.delete_object, null);

        builder.setTitle(R.string.deleting)
                .setView(dialogView)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button theButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        theButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creating of retrofit service
                AccountsService loginService =
                        ServiceGenerator.createService(AccountsService.class);

                // User activation request
                Call<MonitoringObject> call = loginService.deleteObject(
                        objectId,
                        mAccessToken
                );

                call.enqueue(new Callback<MonitoringObject>() {
                    @Override
                    public void onResponse(Call<MonitoringObject> call, Response<MonitoringObject> response) {
                        int statusCode = response.code();
                        MonitoringObject result = response.body();

                        if (statusCode == 200 && result.getError() == null) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.successfull), Toast.LENGTH_LONG);
                            toast.show();

                            Intent intent = new Intent(getApplicationContext(), ObjectsListActivity.class);
                            startActivity(intent);

                            finish();
                        } else {
                            showErrorToast();
                        }
                    }

                    @Override
                    public void onFailure(Call<MonitoringObject> call, Throwable t) {
                        showErrorToast();
                    }
                });
            }
        });
    }

    public void showEditDialog(final String objectId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MonitoringObjectActivity.this);
        LayoutInflater inflater = MonitoringObjectActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.edit_object, null);

        EditText fullNameEdit = (EditText) dialogView.findViewById(R.id.editing_object_full_name);
        fullNameEdit.setText(mMonitoringObject.full_name);

        builder.setTitle(R.string.editing)
                .setView(dialogView)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button theButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        theButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText fullNameEdit = (EditText) dialogView.findViewById(R.id.editing_object_full_name);
                String newFullName = fullNameEdit.getText().toString();

                // Creating of retrofit service
                AccountsService loginService =
                        ServiceGenerator.createService(AccountsService.class);

                // User activation request
                Call<MonitoringObject> call = loginService.editObject(
                        objectId,
                        mAccessToken,
                        newFullName
                );

                call.enqueue(new Callback<MonitoringObject>() {
                    @Override
                    public void onResponse(Call<MonitoringObject> call, Response<MonitoringObject> response) {
                        int statusCode = response.code();
                        MonitoringObject result = response.body();

                        if (statusCode == 200 && result.getError() == null) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.successfull), Toast.LENGTH_LONG);
                            toast.show();
                            getMonitoringObject();
                            dialog.cancel();
                        } else {
                            showErrorToast();
                        }
                    }

                    @Override
                    public void onFailure(Call<MonitoringObject> call, Throwable t) {
                        showErrorToast();
                    }
                });
            }
        });
    }
}
