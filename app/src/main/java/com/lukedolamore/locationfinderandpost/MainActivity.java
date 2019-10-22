package com.lukedolamore.locationfinderandpost;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView textSummary;
    Button buttonSave;
    EditText editTextDescription;
    EditText editTextUserID;
    Location lastLocation = null;
    FusedLocationProviderClient mFusedLocationClient;
    String posturl = "http://developer.kensnz.com/api/addlocdata";
    //://developer.kensnz.com/getlocdata
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonSave = findViewById(R.id.buttonSave);
        textSummary = findViewById(R.id.textSummary);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextUserID = findViewById(R.id.editTextUserID);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        { ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        },101); }
        getLocation();
    }//onCreate

    private void getLocation( ) {
        buttonSave.setEnabled(false);
        textSummary.setText("");
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location)
            {
                if (location != null) {
                    lastLocation = location;
                    buttonSave.setEnabled(true);
                    textSummary.setText(String.format("(%f,%f)",location.getLatitude(), location.getLongitude()));
                }
            }
        });
    }//getLocation

    public void onClickButtonSave(View view) {
        sendDataToWeb( );
    }

    private void sendDataToWeb( ) {
        if (lastLocation == null) return;
        try {
            final String description = editTextDescription.getText().toString();
            StringRequest request
                    = new StringRequest(Request.Method.POST, posturl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            textSummary.setText(s);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            textSummary.setText("Error sending to web service\n"
                                    + volleyError.getMessage());
                        }
                    })
            {
                @Override
                protected Map<String, String> getParams()
                        throws AuthFailureError {
                    Map<String, String> parameters
                            = new HashMap<String, String>();
                    parameters.put("userid", editTextUserID.getText().toString());
                    parameters.put("latitude",
                            Double.toString(lastLocation.getLatitude()));
                    parameters.put("longitude",
                            Double.toString(lastLocation.getLongitude()));
                    parameters.put("description", description);
                    return parameters;
                }
            };
            RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
            rQueue.add(request);
        }
        catch (SecurityException eS) {
            textSummary.setText(eS.getMessage());
        }
    }//sendDataToWeb
}
