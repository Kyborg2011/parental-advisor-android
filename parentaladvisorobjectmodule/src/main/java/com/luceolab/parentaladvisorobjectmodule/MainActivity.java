package com.luceolab.parentaladvisorobjectmodule;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.luceolab.parentaladvisorobjectmodule.restclient.AccountsService;
import com.luceolab.parentaladvisorobjectmodule.restclient.ServiceGenerator;
import com.luceolab.parentaladvisorobjectmodule.restclient.models.MonitoringObject;
import com.luceolab.parentaladvisorobjectmodule.restclient.models.State;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private String mPhoneNumber;
    private SharedPreferences mPref;
    private Context mContext;
    final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mPref = getSharedPreferences(Constants.PREFS_NAME, 0);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_action_name);
            actionBar.setLogo(R.drawable.ic_action_name);
        }


        final Button apply = (Button) findViewById(R.id.object_apply_but);
        final EditText eText = (EditText) findViewById(R.id.phone_object);
        final TextView imeiText = (TextView) findViewById(R.id.imei_object);

        String androidId = getAndroidID();
        imeiText.setText(androidId);

        if (apply != null) {
            apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPhoneNumber = eText.getText().toString();
                    mPhoneNumber = mPhoneNumber.replaceAll("\\D", "");

                    // Creating of retrofit service
                    AccountsService loginService =
                            ServiceGenerator.createService(AccountsService.class);
                    Call<MonitoringObject> call = loginService.startRegisteringDevice(
                            mPhoneNumber
                    );

                    call.enqueue(new Callback<MonitoringObject>() {
                        @Override
                        public void onResponse(Call<MonitoringObject> call, Response<MonitoringObject> response) {
                            MonitoringObject obj = response.body();
                            if (obj.getError() == null) {
                                showDialog();
                            } else {
                                // Error toast
                                String error = obj.getError();
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        error, Toast.LENGTH_LONG);
                                toast.show();
                            }

                        }

                        @Override
                        public void onFailure(Call<MonitoringObject> call, Throwable t) {
                        }
                    });
                }
            });
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
    }

    private String getAndroidID() {
        String androidID = android.provider.Settings.System.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return androidID;
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.device_activation_dialog, null);

        builder.setTitle(R.string.device_registration)
                .setView(dialogView)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
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
                EditText editText = (EditText) dialogView.findViewById(R.id.device_registering_code);
                String enteredCode = editText.getText().toString();

                // Creating of retrofit service
                AccountsService loginService =
                        ServiceGenerator.createService(AccountsService.class);

                String androidOS = Build.VERSION.RELEASE;
                String firebaseToken = FirebaseInstanceId.getInstance().getToken();

                // End device registration request
                Call<MonitoringObject> call = loginService.endRegisteringDevice(
                        mPhoneNumber,
                        enteredCode,
                        "Android " + androidOS,
                        getAndroidID(),
                        firebaseToken
                );

                call.enqueue(new Callback<MonitoringObject>() {
                    @Override
                    public void onResponse(Call<MonitoringObject> call, Response<MonitoringObject> response) {
                        int statusCode = response.code();
                        MonitoringObject result = response.body();

                        if (statusCode == 200 && result.getError() == null) {
                            SharedPreferences.Editor editor = mPref.edit();
                            editor.putString("phone", mPhoneNumber);
                            editor.apply();

                            Intent myIntent = new Intent(mContext, BackgroundLocationService.class);
                            mContext.startService(myIntent);

                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.service_started), Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            if (result != null) {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        result.getError(), Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MonitoringObject> call, Throwable t) {

                    }
                });
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mPhoneNumber = mPref.getString("phone", null);

                    if (mPhoneNumber != null) {
                        Intent myIntent = new Intent(mContext, BackgroundLocationService.class);
                        mContext.startService(myIntent);
                    }
                } else {

                }
                return;
            }
        }
    }
}
