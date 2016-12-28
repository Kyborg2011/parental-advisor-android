package com.luceolab.parentaladvisor.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.luceolab.parentaladvisor.Constants;
import com.luceolab.parentaladvisor.R;
import com.luceolab.parentaladvisor.Utils;
import com.luceolab.parentaladvisor.restclient.AccountsService;
import com.luceolab.parentaladvisor.restclient.ServiceGenerator;
import com.luceolab.parentaladvisor.restclient.models.MonitoringObject;
import com.luceolab.parentaladvisor.restclient.models.State;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ObjectsListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private MonitoringObjectsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String mAccessToken;
    private ArrayList<MonitoringObject> myDataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objects_list);

        // ActionBar settings
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.title_activity_objects_list));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        ImageButton addNewObjectButton = (ImageButton) findViewById(R.id.add_object_but);
        if (addNewObjectButton != null) {
            addNewObjectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), AddObjectActivity.class);
                    startActivity(intent);
                }
            });
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.objects_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MonitoringObjectsAdapter(new ArrayList<MonitoringObject>(), getApplicationContext());
        mAdapter.setOnItemClickListener(new MonitoringObjectsAdapter.OnItemClickListener () {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getApplicationContext(), MonitoringObjectActivity.class);
                String objectId = mAdapter.mDataset.get(position)._id;
                intent.putExtra("object_id", objectId);
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        // Access Token
        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        mAccessToken = mSharedPreferences.getString("access_token", null);

        getMonitoringObjects();
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

    // Load monitoring objects
    private boolean getMonitoringObjects() {
        if (mAccessToken != null) {

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
                    myDataset = new ArrayList<>();

                    if (monitoringObjects != null) {
                        myDataset = (ArrayList<MonitoringObject>) monitoringObjects;
                    } else {
                        showErrorToast();
                    }

                    mAdapter = new MonitoringObjectsAdapter(myDataset, getApplicationContext());
                    mAdapter.setOnItemClickListener(new MonitoringObjectsAdapter.OnItemClickListener () {
                        @Override
                        public void onItemClick(View view, int position) {
                            Intent intent = new Intent(getApplicationContext(), MonitoringObjectActivity.class);
                            String objectId = mAdapter.mDataset.get(position)._id;
                            intent.putExtra("object_id", objectId);
                            startActivity(intent);
                        }
                    });
                    mRecyclerView.setAdapter(mAdapter);
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

    private void showErrorToast() {
        Toast toast = Toast.makeText(getApplicationContext(),
                getString(R.string.simple_error), Toast.LENGTH_LONG);
        toast.show();
    }

    public static class MonitoringObjectsAdapter extends RecyclerView.Adapter<MonitoringObjectsAdapter.ViewHolder> {
        private List<MonitoringObject> mDataset;
        private OnItemClickListener mOnItemClickListener;
        private Context mContext;

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View mLayout;
            public ImageButton editActionButton;
            public ImageButton deleteActionButton;


            public ViewHolder(View v) {
                super(v);
                mLayout = v;
                editActionButton = (ImageButton) v.findViewById(R.id.object_card_view_edit_button);
                deleteActionButton = (ImageButton) v.findViewById(R.id.object_card_view_delete_button);
            }
        }

        public MonitoringObjectsAdapter(List<MonitoringObject> myDataset, Context context) {
            mDataset = myDataset;
            mContext = context;
        }

        @Override
        public MonitoringObjectsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.monitoring_object_card_view, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            TextView tV = (TextView) holder.mLayout.findViewById(R.id.object_card_view_full_name);
            tV.setText(mDataset.get(position).full_name);

            TextView tPhone = (TextView) holder.mLayout.findViewById(R.id.object_card_view_phone_number);
            tPhone.setText(Utils.formatPhoneNumber(mDataset.get(position).phone));

            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            });

            holder.editActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MonitoringObjectActivity.class);
                    String objectId = mDataset.get(position)._id;
                    intent.putExtra("object_id", objectId);
                    intent.putExtra("action", "edit");
                    mContext.startActivity(intent);
                    System.out.println("Edit button pressed. Position - " + position);
                }
            });

            holder.deleteActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MonitoringObjectActivity.class);
                    String objectId = mDataset.get(position)._id;
                    intent.putExtra("object_id", objectId);
                    intent.putExtra("action", "remove");
                    mContext.startActivity(intent);
                    System.out.println("Delete button pressed. Position - " + position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            mOnItemClickListener = listener;
        }


    }
}
