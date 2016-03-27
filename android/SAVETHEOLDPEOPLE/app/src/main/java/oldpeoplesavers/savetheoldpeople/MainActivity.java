package oldpeoplesavers.savetheoldpeople;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.HeartRateQuality;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    TextView dataView;
    TextView heartRateView;
    TextView curAdd;
    Button btnStart;
    Button updateLocation;
    Button btnStop;

    Firebase mikelocRef;
    Firebase mikerateRef;

    private BandClient client = null;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    long lastTimeStamp = 0;
    long currentTimeStamp = 0;

    private LocationRequest mLocationRequest;

    private static final int REQUEST_LOCATION = 1;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    TextView mLatitudeText;
    TextView mLongitudeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase myFirebaseRef = new Firebase("https://watchdog-app.firebaseio.com/");
        mikelocRef = new Firebase("https://watchdog-app.firebaseio.com/Bill/People/Mike/location");
        mikerateRef = new Firebase("https://watchdog-app.firebaseio.com/Bill/People/Mike/heartRate");

        dataView = (TextView) findViewById(R.id.data);
        heartRateView = (TextView) findViewById(R.id.sensorData);

        btnStart = (Button) findViewById(R.id.startButton);
        btnStop = (Button) findViewById(R.id.stopButton);

        updateLocation = (Button) findViewById(R.id.updateLocation);

        mLatitudeText = (TextView) findViewById(R.id.lat);
        mLongitudeText = (TextView) findViewById(R.id.lon);
        curAdd = (TextView) findViewById(R.id.address);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Log.d("Testing", (String) postSnapshot.getKey());
                    Log.d("Bill's Child", (String) postSnapshot.getChildren().iterator().next().getKey());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartRateView.setText("");
                new HeartRateSubscriptionTask().execute();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
                } catch (BandIOException e) {
                    e.printStackTrace();
                }
            }
        });

        updateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getLocation();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public long getCurrentTime(){
        //Divide by 1000 to get seconds
        return System.currentTimeMillis() / 1000;
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    private void appendToUI(final String string){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                heartRateView.setText(string);
            }
        });
    }

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
                appendToUI(String.format("Heart Rate = %d beats per minute\n"
                        + "Quality = %s\n", event.getHeartRate(), event.getQuality()));

                if (getCurrentTime() >= currentTimeStamp+5){
                    currentTimeStamp = getCurrentTime();
                    if(event.getQuality() == HeartRateQuality.LOCKED){
                        mikerateRef.child(Long.toString(currentTimeStamp)).setValue(event.getHeartRate());
                    }else{
                        mikerateRef.child(Long.toString(currentTimeStamp)).setValue(-1);
                    }
                }
            }
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        try {
            getLocation();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestLocationPermissions(){
        ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
    }

    public void getLocation() throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestLocationPermissions();
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("Log", "Location");
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));

            Map<String, Object> locationRef = new HashMap<String, Object>();
            locationRef.put("lat", mLastLocation.getLatitude());
            locationRef.put("lng", mLastLocation.getLongitude());

            mikelocRef.updateChildren(locationRef);
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            Address address = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(),1).get(0);

            curAdd.setText("Current Address: " + address.getAddressLine(0));

        } else {
            Log.d("Log", "Location last null");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, MainActivity.this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }


    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }
}