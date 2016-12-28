package com.luceolab.parentaladvisor.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.luceolab.parentaladvisor.Constants;
import com.luceolab.parentaladvisor.R;
import com.luceolab.parentaladvisor.restclient.AccountsService;
import com.luceolab.parentaladvisor.restclient.models.MonitoringObject;
import com.luceolab.parentaladvisor.restclient.ServiceGenerator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddObjectActivity extends AppCompatActivity {

    private ActionBar mActionBar;
    private EditText mPhoneNumber;
    private EditText mFullName;
    private String mAccessToken;
    private boolean mIsLargeLayout;
    private String mPhoneNumberStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_object);

        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
        mActionBar = getSupportActionBar();
        mActionBar.setTitle(getString(R.string.title_activity_add_object));
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mPhoneNumber = (EditText) findViewById(R.id.phone_object);
        mFullName = (EditText) findViewById(R.id.full_name_object);

        mPhoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        mAccessToken = settings.getString("access_token", null);

        Button applyButton = (Button) findViewById(R.id.object_apply_but);
        if (applyButton != null) {
            applyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    send();
                }
            });
        }
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

    public void send() {

        mPhoneNumberStr = mPhoneNumber.getText().toString();
        String full_name = mFullName.getText().toString();

        // Remove all non-digit symbols
        mPhoneNumberStr = mPhoneNumberStr.replaceAll("\\D", "");

        // Creating of retrofit service
        AccountsService loginService =
                ServiceGenerator.createService(AccountsService.class);
        Call<MonitoringObject> call = loginService.addObject(
                full_name,
                mPhoneNumberStr,
                mAccessToken
        );

        call.enqueue(new Callback<MonitoringObject>() {
            @Override
            public void onResponse(Call<MonitoringObject> call, Response<MonitoringObject> response) {
                int statusCode = response.code();
                MonitoringObject result = response.body();

                if (statusCode == 200 && result.getError() == null) {
                    showDialog();
                } else {
                    if (result != null) {
                        showErrorToast(result.getError());
                    } else {
                        showErrorToast();
                    }
                }
            }

            @Override
            public void onFailure(Call<MonitoringObject> call, Throwable t) {
                showErrorToast();
            }
        });
    }

    public void showErrorToast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(),
                text, Toast.LENGTH_LONG);
        toast.show();
    }

    public void showErrorToast() {
        Toast toast = Toast.makeText(getApplicationContext(),
                getString(R.string.simple_error), Toast.LENGTH_LONG);
        toast.show();
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddObjectActivity.this);
        LayoutInflater inflater = AddObjectActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_object_activation, null);
        Button b = (Button) dialogView.findViewById(R.id.resend);
        b.setVisibility(View.INVISIBLE);
        builder.setTitle(R.string.activate_object)
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
                EditText editText = (EditText) dialogView.findViewById(R.id.object_activation_code);
                String enteredCode = editText.getText().toString();

                // Creating of retrofit service
                AccountsService loginService =
                        ServiceGenerator.createService(AccountsService.class);

                // Object activation request
                Call<MonitoringObject> call = loginService.activateObject(
                        mPhoneNumberStr,
                        enteredCode,
                        mAccessToken
                );

                call.enqueue(new Callback<MonitoringObject>() {
                    @Override
                    public void onResponse(Call<MonitoringObject> call, Response<MonitoringObject> response) {
                        int statusCode = response.code();
                        MonitoringObject result = response.body();

                        if (statusCode == 200 && result.getError() == null) {
                            showErrorToast(getString(R.string.object_activate_successfull));

                            // Open dashboard when successful
                            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                            startActivity(intent);

                        } else {
                            if (result != null) {
                                showErrorToast(result.getError());
                            } else {
                                showErrorToast();
                            }
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
