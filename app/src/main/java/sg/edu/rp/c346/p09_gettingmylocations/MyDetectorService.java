package sg.edu.rp.c346.p09_gettingmylocations;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;

public class MyDetectorService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    boolean started;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    String folderLocation;

    public MyDetectorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "Created");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        folderLocation = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/P09";

        File folder = new File(folderLocation);
        if (folder.exists() == false){
            boolean result = folder.mkdir();
            if (result == true){
                Log.d("File Read/Write", "Folder created");
                Toast.makeText(this,"File Read/Write, Folder created",Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(this,"File Read/Write, Folder not created",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (started == false){
            started = true;
            Log.d("Service", "Started");
            mGoogleApiClient.connect();
        } else {
            Log.d("Service", "Still running");
            Toast.makeText(this,"Service, Still running",Toast.LENGTH_SHORT).show();

        }

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("Service", "Exited");
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION);


        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                ||  permissionCheck_Fine  == PermissionChecker.PERMISSION_GRANTED){
            mLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest
                    .PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setSmallestDisplacement(100);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);

        } else {
            mLocation = null;
            Toast.makeText(this,
                    "Permission not granted to retrieve location info",
                    Toast.LENGTH_SHORT).show();
//
        }

        if (mLocation != null) {
//            Toast.makeText(this, "Lat : " + mLocation.getLatitude() +
//                            " Lng : " + mLocation.getLongitude(),
//                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location not Detected",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        //the detected location is given by the variable location in the signature
//        Toast.makeText(this, "Lat : " + location.getLatitude() + " Lng : " +
//                location.getLongitude(), Toast.LENGTH_SHORT).show();
        File targetFile = new File(folderLocation, "data.txt");

        try {
            FileWriter writer = new FileWriter(targetFile, true);
            writer.write(location.getLatitude() + ", " + location.getLongitude() +"\n");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to write!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
