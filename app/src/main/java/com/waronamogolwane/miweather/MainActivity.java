package com.waronamogolwane.miweather;

import static android.icu.lang.UCharacter.toUpperCase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView dateTimeDisplay, results, currentTempTextView, currentDescriptionTextView;
    TextView cityTextView, countryTextView;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;
    private double currentLatitude = 0, currentLongitude = 0;
    public String currentCountryCode, currentZipCode, currentCity ="", currentCountry, currentDescription;
    private double currentTemperature;
    public FusedLocationProviderClient fusedLocationProviderClient;
    List<Address> addresses;

    String tempUrl;
    private final String url = "https://api.openweathermap.org/data/2.5/weather?";
    private final String appId = "7d414118c2b4eb64e75c1e5a05eb523b";
    DecimalFormat df = new DecimalFormat("#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initViews();
        setCurrentDate();
        getAppPermissions();
        getCurrentLocation();


    }

    private void getAppPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(MainActivity.this,
                                Locale.getDefault());

                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1
                            );


                            currentCountryCode = addresses.get(0).getCountryCode();
                            currentZipCode = addresses.get(0).getPostalCode();
                            currentCountry = addresses.get(0).getCountryName();
                            currentCity = addresses.get(0).getLocality();

                            currentLatitude = addresses.get(0).getLatitude();
                            currentLongitude = addresses.get(0).getLongitude();

                            countryTextView.setText(currentCountry);
                            cityTextView.setText(currentCity);

                            setCurrentWeather();




                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        } else {
            ActivityCompat.requestPermissions(MainActivity.this
                    , new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
            getAppPermissions();
        }

    }


    private void getCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void initViews() {
        dateTimeDisplay = (TextView)findViewById(R.id.dateTextView);
        currentTempTextView = (TextView) findViewById(R.id.temperatureTextView);
        cityTextView = (TextView) findViewById(R.id.cityTextView);
        countryTextView = (TextView) findViewById(R.id.countryTextView);
        currentDescriptionTextView = (TextView) findViewById(R.id.descriptionTextView);


    }

    private void setCurrentWeather() {


        tempUrl = url  + "q=" +currentCity + "," + currentCountry + "&APPID=" + appId;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                String output = "";
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);

                    String description = jsonObjectWeather.getString("description");

                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                    double temp = jsonObjectMain.getDouble("temp") - 273.15;
                    currentTemperature = temp;
                    currentDescription = description;
                    currentDescriptionTextView.setText(toUpperCase(currentDescription));
                    currentTempTextView.setText(df.format(currentTemperature));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void setCurrentDate() {
        calendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("EEE, MMM d");
        date = dateFormat.format(calendar.getTime());
        dateTimeDisplay.setText(date);
    }

}