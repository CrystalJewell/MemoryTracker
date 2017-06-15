package com.crystaljewell.memorytracker.ui.map;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.crystaljewell.memorytracker.R;
import com.crystaljewell.memorytracker.ui.memory.create.CreateMemory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.add_content_button)
    protected FloatingActionButton addContentButton;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLng mCurrentLatLng;
    private LatLng mMemoryLocation;
    private Marker mCurrLocationMarker;
    private LocationRequest mLocationRequest;
    private int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Required to use Butterknife in this class
        ButterKnife.bind(this);

        //Check to see if permission to access location has been granted
        verifyLocationPermissions();
    }

    private void verifyLocationPermissions() {
        /*
        Explicit permission has to be granted at time of using features Google deems potentially
        harmful to users in SDK 23+
        */
        if (Build.VERSION.SDK_INT >= 23) {
            //Check to see if permission has already been granted for the users location
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Permission had already been granted so we go on to building the location client
                buildGoogleApiClient();
            } else {
                //User had not already granted permission, need to ask for it
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            }
        }
        /*
        If the user is not on SDK 23+ just go ahead and get current location since we requested
        permission in the manifest
        */
        else {
            buildGoogleApiClient();
        }
    }

    /*
    This method is used only after the requestPermissions method is called
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Access has been granted, go ahead and build the GoogleApiClient
                    buildGoogleApiClient();
                } else {
                    /*
                    Permission denied, use AlertDialog to explain to user why we need this permission
                    Update PERMISSION_REQUEST_CODE to 2 when the user answers again it will go to
                    the next case in this switch statement
                    */
                    PERMISSION_REQUEST_CODE = 2;
                    explainPermissionNeededDialog();
                }
                break;
            }
            case 2:
                //This case will be used if we have had to explain why we need the permission to the user
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                } else {
                    /*
                    Permission denied, again.  Will have to give another alert dialog to user
                    letting them know this permission is required for the app to run.
                    */
                    permissionDeniedDialog();
                }
                break;

            default:
                //This is the case it will come to if the resultCode is not a case above
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    /*
    @TargetAPI is so we don't get red squiggles under the requestPermissions below even
    though we have already made sure this method wont be accessed unless they are using
    SDK 23 and above. Android doesn't realize that so we have to explicitly state this method
    statement requires at least SDK 23(Marshmallow or VERSION_CODES.M) for it to run
    */
    @TargetApi(Build.VERSION_CODES.M)
    private void explainPermissionNeededDialog() {
        /*
        Explain to user this app requires access to users location to be able to save memories to
        the map
        */
        AlertDialog.Builder explainPermission = new AlertDialog.Builder(this);
        explainPermission.setTitle("Location Permission Required");
        explainPermission.setMessage("This app requires access to your device location to be able " +
                "to save your memories on the map" + "\nIf this permission is not granted the app " +
                "will not run properly");
        explainPermission.setPositiveButton("OK, I got it",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_CODE);
                    }
                });
        //Creates the alert dialog
        explainPermission.create();
        //Shows the alert dialog on the screen
        explainPermission.show();
    }

    /*
    This method will only run once the user has denied permission to their location for the second
    time. If the user restarts the app, they will go through the same cycle of being prompted for
    permissions.
     */
    private void permissionDeniedDialog() {
        //Explain to user this app requires access to users location to be able to save memories to the map
        AlertDialog.Builder permissionDenied = new AlertDialog.Builder(this);
        permissionDenied.setTitle("Location Permission Required");
        permissionDenied.setMessage("This app requires access to your device location." +
                "\nThis permission has been denied and the app will not function without it." +
                "\nMemory Tracker will now close");
        permissionDenied.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //calling this method will close out our app and all of its processes
                finishAffinity();
            }
        });
        permissionDenied.create();
        permissionDenied.show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Adds compass to UI of page
        mMap.getUiSettings().setCompassEnabled(true);
        //Security check for user running SDK 23 and above
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                /*
                Return false so we do not consume the event, which means we can click on multiple
                markers without restarting the activity
                */
                return false;
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        //Builds our Google API Client to be able to track users location while in app
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        setupMapFragment();
    }

    private void setupMapFragment() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    /*
    This method is called when the users location changes to update the position marker on the screen
     */
    @Override
    public void onLocationChanged(Location location) {
        //Get users current location
        mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        //Move map camera to current location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }

    /*
    This method is used to set how often the users location is updated on the map
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Uses Google Location API to start location requests every 10 seconds
        //but no more often than ever 5 seconds
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        //We want the users location to be as accurate as possible when they are trying to save memories
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Once setting are added to the location request we can start asking for updates to the location
        allowLocationUpdates();
    }

    private void allowLocationUpdates() {
        /*
        Because of the security exception getting the location requires we have to make sure the
        use has given us permission again, any time we use anything that has required permission before
        we will have to wrap it in the appropriate check to make sure the user has explicitly
        granted us permission to do so.
        */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest,
                    this);
        }
    }

    @OnClick(R.id.add_content_button)
    protected void addContentDialog() {
        final AlertDialog.Builder addMemory = new AlertDialog.Builder(this);
        addMemory.setTitle("Add Memory?");
        addMemory.setMessage("Would you like to add a memory at your current location?");
        addMemory.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMemoryLocation = mCurrentLatLng;
                Intent createMemory = new Intent(MapsActivity.this, CreateMemory.class);
                createMemory.putExtra("MEMORY_LOCATION", mMemoryLocation);
                startActivityForResult(createMemory, 1);

            }
        });

        addMemory.create();
        addMemory.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //After we come back from adding a memory we need to mark that position on the map, add the
        //title they gave the memory as well as set the onMarkerClick to han
        String title = data.getStringExtra("MEMORY_TITLE");
        mMap.addMarker(new MarkerOptions()
                .position(mMemoryLocation).title(title));

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    protected void stopLocationUpdates() {
        // Removes location requests when the activity is in a paused or stopped state to help
        // with battery life.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /*
    LifeCycle methods to control background resources
     */
    @Override
    protected void onStart() {
        super.onStart();
        //When the map starts we check to see if there is already a GoogleApiClient started, if
        //there is not we build one, if there is we go ahead and connect to it for location updates
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        } else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Checks to see if when we hit this lifecycle method there is a GoogleApiClient connected.
        //If there is it stops location updates to save battery, but doesn't disconnect the
        //GoogleApiClient object if not we just go on about our business.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Restarts getting users location data after the app has come back from onPause
        if (mGoogleApiClient.isConnected()) {
            allowLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stops location updates and disconnects the GoogleApiClient object.
        mGoogleApiClient.disconnect();
    }
}
