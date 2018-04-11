package com.tmsoftstudio.businesscontrol;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;


public class ViewMapFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static GoogleMap mMap;
    private SupportMapFragment fragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private static Context context;
    private static View view;
    private static SharedPreferences mSettings;

    public ViewMapFragment() {
        // Required empty public constructor
    }

    public static ViewMapFragment newInstance() {
        ViewMapFragment fragment = new ViewMapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_view_map, container, false);
        context = this.getContext();
        mSettings = context.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, fragment).commit();
        }
        fragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(context.getApplicationContext(), "Location access in Settings is OFF", Toast.LENGTH_SHORT).show();
        } else {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(60000);
            mLocationRequest.setFastestInterval(30000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setNumUpdates(1);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        if(mSettings.contains("APP_PREFERENCES_TASKS")) {
            try {
                String jsonString = mSettings.getString("APP_PREFERENCES_TASKS", "");
                JSONObject json = new JSONObject(jsonString);
                JSONArray jsonArrayTask = json.getJSONArray("tasks");

                for (int i = 0; i < jsonArrayTask.length(); i++) {
                    JSONObject jsonTask = jsonArrayTask.getJSONObject(i);
                    String taskName = jsonTask.getString("name");
                    String route = jsonTask.getString("route");
                    String temp[]=route.replaceAll("lng:", "")
                            .replaceAll("lat:","")
                            .split(",");
                    double [] lat= new double[temp.length];
                    double [] lng=new double[temp.length];
                    for (int j = 0; j<temp.length; j++){
                        lng[j] = Double.valueOf(temp[j]);
                        lat[j]=Double.valueOf(temp[j+1]);

                        String result = null;
                        List<Address> addressList = geocoder.getFromLocation(lat[j], lng[j], 1);
                        if (addressList != null && addressList.size() > 0) {
                            Address address = addressList.get(0);
                            StringBuilder sb = new StringBuilder();
                            String stmp="";
                            for (int k = 0; k < address.getMaxAddressLineIndex(); k++) {
                                if(!stmp.equals(address.getAddressLine(i))) {
                                    sb.append(address.getAddressLine(i)).append(" ");
                                }
                                stmp=address.getAddressLine(i);
                            }

                            result = sb.toString();
                        }
                        LatLng latlng = new LatLng(lat[j], lng[j]);
                        mMap.addMarker(new MarkerOptions().position(latlng).title(taskName+" "+result));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14.0f));
                        j=j+1;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14.0f));
        }

        mGoogleApiClient.disconnect();
        mGoogleApiClient=null;
    }


}
