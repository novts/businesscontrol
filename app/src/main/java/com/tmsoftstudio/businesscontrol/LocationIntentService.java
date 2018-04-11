package com.tmsoftstudio.businesscontrol;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.tmsoftstudio.businesscontrol.backend.myApi.MyApi;
import com.tmsoftstudio.businesscontrol.backend.myApi.model.LocationBean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LocationIntentService extends IntentService implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient mGoogleDriveApiClient;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private Context context;
    private SharedPreferences mSettings;
    private MyApi myApiService = null;
    private LocationBean bean;

    public LocationIntentService() {
        super("LocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            context = this;
            mSettings = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();

        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(1);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getDateTimeInstance().format(new Date());

        double lat= mCurrentLocation.getLatitude();
        double lng = mCurrentLocation.getLongitude();

        String accountName = mSettings.getString("APP_PREFERENCES_ACCOUNT", "");

        List<String> tasksList=new ArrayList<>();

        if(mSettings.contains("APP_PREFERENCES_TASKS")) {
            try {
                String jsonString = mSettings.getString("APP_PREFERENCES_TASKS", "");
                JSONObject json = new JSONObject(jsonString);
                JSONArray jsonArrayTask = json.getJSONArray("tasks");

                for (int i = 0; i < jsonArrayTask.length(); i++) {
                    JSONObject jsonTask = jsonArrayTask.getJSONObject(i);
                    String taskName = jsonTask.getString("name");
                    tasksList.add(taskName);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        bean=new LocationBean();
        bean.setAccount(accountName);
        bean.setDate(mLastUpdateTime);
        bean.setTasks(tasksList);
        bean.setLat(lat);
        bean.setLng(lng);

        if(isOnline()) {

            new Thread(new Runnable() {
                public void run() {
                    if (myApiService == null) {
                        MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                                new AndroidJsonFactory(), null)
                                .setRootUrl("https://buisness-control.appspot.com/_ah/api/")
                                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                                    @Override
                                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                        abstractGoogleClientRequest.setDisableGZipContent(true);
                                    }
                                });
                        myApiService = builder.build();
                    }

                    try {
                        myApiService.setLocation(bean).execute();
                    } catch (IOException e) {
                    }
                }
            }).start();

        }else{
            Handler h = new Handler(context.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context.getApplicationContext(), "Интернет соединение отсутствует", Toast.LENGTH_SHORT).show();
                }
            });
    }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        this.stopSelf();
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


}
