package com.cti.fmi.licentaapk.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cti.fmi.licentaapk.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.cti.fmi.licentaapk.R.id.map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback
{

    private final static String PREFERENCES = "MySharedPrefs";
    private final static String LOG_TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private LatLng selectedLocation;
    private String selectedLocationString = "";
    private String selectedLocality;
    private String selectedCountryName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        Button confirmLocationButton = findViewById(R.id.confirm_location_button);
        confirmLocationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                attemptConfirmLocation();
            }
        });

        Toolbar toolbar = findViewById(R.id.maps_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void attemptConfirmLocation()
    {
        if(selectedLocation == null)
        {
            Toast.makeText(getBaseContext(), R.string.provide_location, Toast.LENGTH_LONG).show();
        }
        else
        {
            if(!selectedLocationString.equals(""))
            {
                Intent intent = new Intent();
                intent.putExtra("location", selectedLocationString);
                intent.putExtra("locality", selectedLocality);
                intent.putExtra("countryName", selectedCountryName);
                setResult(RESULT_OK, intent);
                finish();
            }
            else
            {
                Toast.makeText(getBaseContext(), R.string.provide_valid_location, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        LatLng bucharest = new LatLng(44.427741, 26.103312);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bucharest));

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMinZoomPreference(5.0f);
        mMap.setMaxZoomPreference(15.0f);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point)
            {
                selectedLocation = point;

                mMap.clear();

                new ReverseGeocodingTask(getApplicationContext()).execute(selectedLocation);
            }
        });
    }

    private class ReverseGeocodingTask extends AsyncTask<LatLng, Void, HashMap<String, String>>
    {
        Context mContext;

        public ReverseGeocodingTask(Context context)
        {
            super();
            mContext = context;
        }

        // Finding address using reverse geocoding
        @Override
        protected HashMap<String, String> doInBackground(LatLng... params)
        {
            Geocoder geocoder = new Geocoder(mContext);
            double latitude = params[0].latitude;
            double longitude = params[0].longitude;
            HashMap<String, String> map = new HashMap<>();

            List<Address> addresses = null;

            try
            {
                addresses = geocoder.getFromLocation(latitude, longitude,1);
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            if(addresses != null && addresses.size() > 0 )
            {
                Address address = addresses.get(0);
                Log.e(LOG_TAG, "addresses.get : " + address.toString());

                String locality = address.getLocality();

                if (locality != null && !locality.equals(""))
                {
                    map.put("locality",locality);
                }
                else
                {
                    map.put("locality","");
                }

                String countryName = address.getCountryName();

                if (countryName != null && !countryName.equals(""))
                {
                    map.put("countryName",countryName);
                }
                else
                {
                    map.put("countryName","");
                }
            }

            Log.e(LOG_TAG, "addressText: " + map.get("locality") + "" + map.get("countryName"));

            return map;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> map)
        {
            String locality = map.get("locality");
            String countryName = map.get("countryName");

            StringBuilder sb = new StringBuilder();

            if (locality != null && !locality.equals(""))
            {
                sb.append(locality);
                sb.append(", ");
            }

            if (countryName != null && !countryName.equals(""))
            {
                sb.append(countryName);
            }

            mMap.addMarker(
                new MarkerOptions()
                        .position(selectedLocation)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title(sb.toString()))
                .showInfoWindow();

            if(sb.toString().equals(""))
            {
                selectedLocationString = "";
            }
            else
            {
                sb.delete(0, sb.length());

                sb.append(selectedLocation.latitude);
                sb.append(",");
                sb.append(selectedLocation.longitude);

                selectedLocationString = sb.toString();

                selectedLocality = locality;
                selectedCountryName = countryName;
            }
        }
    }
}
