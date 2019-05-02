package module.ofbusiness.com.geofencing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;

import java.util.List;

import app.ofbusiness.com.geofencing.GeofencingMapFragment;
import app.ofbusiness.com.geofencing.MapWrapperLayout;
import app.ofbusiness.com.geofencing.listener.LocationPickerListener;
import app.ofbusiness.com.geofencing.listener.MapPermissionListener;
import app.ofbusiness.com.geofencing.module.GeoFencingHelperModule;
import app.ofbusiness.com.geofencing.module.GeoFencingPermissionModule;
import app.ofbusiness.com.geofencing.utils.MapUtils;

import static app.ofbusiness.com.geofencing.utils.MapUtils.areThereMockPermissionApps;

public class ChooseLocationActivity extends AppCompatActivity implements MapWrapperLayout.OnDragListener, OnMapReadyCallback {

    private GoogleMap googleMap;
    private GeofencingMapFragment geofencingMapFragment;
    private Location lastKnownLocation;

    public static final String ARG_SELECTED_LAT = "correctLat";
    public static final String ARG_SELECTED_LONG = "correctLong";

    private View mMarkerParentView;
    private ImageView mMarkerImageView;

    private double correctedLat;
    private double correctedLong;

    private TextView mLocationTextView;
    private Button updateLocation;
    private Marker mapMarker;

    private boolean isCurrentLocationInsideCircle = true;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_locatio_new);

        mLocationTextView = findViewById(R.id.location_text_view);
        mMarkerParentView = findViewById(R.id.marker_view_incl);
        mMarkerImageView = findViewById(R.id.marker_icon_view);
        updateLocation = findViewById(R.id.update_location_bt);

        updateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCurrentLocationInsideCircle) {
                    Intent intent = new Intent(getBaseContext(), GeoTaggingActivity.class);
                    if (correctedLat == 0 || correctedLong == 0) {
                        newIntent(intent, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    } else {
                        newIntent(intent, correctedLat, correctedLong);
                    }
                    startActivity(intent);
                } else {
                    Toast.makeText(ChooseLocationActivity.this, "Please select inside the Circular Region", Toast.LENGTH_LONG).show();
                }
            }
        });

        String mockApplicationApps = MapUtils.areThereMockPermissionApps(this);
        if (mockApplicationApps != null) {
            android.app.AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(this);
            alertBuilder.setCancelable(false);
            alertBuilder.setTitle("Uninstall "+mockApplicationApps+" app !");
            alertBuilder.setMessage("Your device is using "+mockApplicationApps+" app for location spoofing. Please Uninstall and disable GPS spoofing permission.");
            android.app.AlertDialog alert = alertBuilder.create();
            alert.show();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        GeoFencingPermissionModule geoFencingPermissionModule = new GeoFencingPermissionModule(new MapPermissionListener() {

            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (ActivityCompat.checkSelfPermission(ChooseLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChooseLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }else {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShownNote(List<PermissionRequest> permissions, PermissionToken token) {
                Toast.makeText(ChooseLocationActivity.this, "onPermissionRationaleShouldBeShownNote", Toast.LENGTH_SHORT).show();
            }
        }, this);
        geoFencingPermissionModule.checkAllPermissions(this);

        geofencingMapFragment = (GeofencingMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        geofencingMapFragment.setOnDragListener(this);
        geofencingMapFragment.getMapAsync(this);

    }

    private Intent newIntent(Intent intent, double lat, double longt) {
        intent.putExtra(ARG_SELECTED_LAT, lat);
        intent.putExtra(ARG_SELECTED_LONG, longt);
        return intent;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(120000); // two minute interval
        mLocationRequest.setFastestInterval(120000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        initializeMap();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onDrag(MotionEvent motionEvent) {
        geoFencingHelperModule.dragListener(motionEvent);
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                lastKnownLocation = location;
                geoFencingHelperModule.lastKnownLocation = lastKnownLocation;
                geoFencingHelperModule.googleMap = googleMap;
                googleMap.clear();
                MapUtils.moveCameraToLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), googleMap);
                MapUtils.showAreaBoundaryCircle(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), googleMap, 21);
                if (ActivityCompat.checkSelfPermission(ChooseLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ChooseLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
            }
        }
    };

    private void initializeMap() {
        try {
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    GeoFencingHelperModule geoFencingHelperModule = new GeoFencingHelperModule(new LocationPickerListener() {
        @Override
        public void markerInSelectedRegion(LatLng latLng) {
            correctedLat = latLng.latitude;
            correctedLong = latLng.longitude;
            updateLocation.setBackgroundColor(getResources().getColor(R.color.acceptBtColor));
            isCurrentLocationInsideCircle = true;
        }

        @Override
        public void markerNotInSelectedRegion() {
            updateLocation.setBackgroundColor(getResources().getColor(R.color.rejectBtColor));
            mLocationTextView.setText("-");
            isCurrentLocationInsideCircle = false;
        }

        @Override
        public void getAddressByCoordinates() {

        }
    }, googleMap, ChooseLocationActivity.this, 100, lastKnownLocation);


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        geoFencingHelperModule.onWindowFocusChanged(this, mMarkerParentView, mMarkerImageView);
    }

    private void allPermissionAreMandatory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.storage_permission_title));
        builder.setMessage(getResources().getString(R.string.storage_permission_desc));
        builder.setPositiveButton(getResources().getString(R.string.lets_do),
                (dialogInterface, i) -> {
                    // permission is denied permenantly, navigate user to app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
        builder.setNegativeButton(getResources().getString(R.string.later),
                (dialogInterface, i) -> {
                });

        builder.setCancelable(true);
        builder.show();
    }
}
