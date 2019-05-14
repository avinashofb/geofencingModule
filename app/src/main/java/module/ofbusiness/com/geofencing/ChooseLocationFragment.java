package module.ofbusiness.com.geofencing;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

public class ChooseLocationFragment extends Fragment implements MapWrapperLayout.OnDragListener, OnMapReadyCallback, View.OnClickListener, ChooseLocationActivity.IOnFocusListenable {

    private View mMarkerParentView;
    private ImageView mMarkerImageView;
    private TextView mLocationTextView;
    private Button updateLocation;

    private GoogleMap googleMap;
    private GeofencingMapFragment geofencingMapFragment;
    private Location lastKnownLocation;
    private String applicationId;
    public static final String ARG_SELECTED_LAT = "correctLat";
    public static final String ARG_SELECTED_LONG = "correctLong";
    private double selectedLat;
    private double selectedLong;
    private Marker mapMarker;
    private boolean isCurrentLocationInsideCircle = true;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mapLocationRequest;
    boolean isMockEnabled = false;

    private static final String TAG = "PINING_LOCATION_ACTIVITY";
    private static final int MAP_CIRCULAR_RADIUS = 100;
    private static final long UPDATE_LOCATION_INTERVAL = 120 * 1000;
    private static final long UPDATE_LOCATION_FAST_INTERVAL = 120 * 1000;
    private static final String ARG_APPLICATION_ID = "ARG_APPLICATION_ID";
    private static final int FINISHING_CODE = 180;

    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChooseLocationActivity){
            activity =(Activity) context;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_location_fragment, container, false);

        mMarkerParentView = view.findViewById(R.id.marker_view_incl);
        mMarkerImageView = view.findViewById(R.id.marker_icon_view);
        mLocationTextView = view.findViewById(R.id.location_text_view);
        updateLocation = view.findViewById(R.id.update_location_bt);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateLocation.setOnClickListener(this);

        if(activity == null){
            return;
        }
//        List<String> mockApplicationApps = MapUtils.areThereMockPermissionApps(getActivity());
//        if (mockApplicationApps != null && mockApplicationApps.size() > 0) {
//            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
//            LayoutInflater inflater = this.getLayoutInflater();
//            View dialogView= inflater.inflate(R.layout.location_display_view, null);
//
//            TextView allAppsName = (TextView)dialogView.findViewById(R.id.apps_name);
//            Button okButton = (Button)dialogView.findViewById(R.id.ok_bt);
//
//            alertBuilder.setView(dialogView);
//            alertBuilder.setCancelable(false);
//            alertBuilder.setOnCancelListener(dialog -> {
//                dialog.dismiss();
//                getActivity().finish();
//            });
//            okButton.setOnClickListener(v -> getActivity().finish());
//
//            StringBuilder appNames = new StringBuilder();
//            appNames.append(" Uninstall the following apps: \n\n");
//            for(int i=0; i<= mockApplicationApps.size()-1; i++){
//                appNames.append((i+1) +". " +mockApplicationApps.get(i) + " \n");
//            }
//            allAppsName.setText(appNames.toString());
//            AlertDialog alert = alertBuilder.create();
//            alert.show();
//        }

        //updateLocationBt.setBackgroundColor(getResources().getColor(R.color.lead_converted));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        GeoFencingPermissionModule geoFencingPermissionModule = new GeoFencingPermissionModule(new MapPermissionListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if (!multiplePermissionsReport.areAllPermissionsGranted()) {
                    //onPermissionDenied();
                }else{
                    geofencingMapFragment = (GeofencingMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                    geofencingMapFragment.setOnDragListener(ChooseLocationFragment.this);
                    geofencingMapFragment.getMapAsync(ChooseLocationFragment.this);
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShownNote(List<PermissionRequest> list, PermissionToken permissionToken) {
            }
        }, activity);
        geoFencingPermissionModule.checkAllPermissions(activity);

    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapLocationRequest = new LocationRequest();
        mapLocationRequest.setInterval(UPDATE_LOCATION_INTERVAL);
        mapLocationRequest.setFastestInterval(UPDATE_LOCATION_FAST_INTERVAL);
        mapLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        initializeMap();

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }else {
            mFusedLocationClient.requestLocationUpdates(mapLocationRequest, mapLocationCallback, Looper.myLooper());
        }
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);

    }

    private void initializeMap() {
        if (googleMap == null) {
            Toast.makeText(activity, "Sorry! unable to create maps", Toast.LENGTH_LONG).show();
        }
    }

    GeoFencingHelperModule geoFencingHelperModule = new GeoFencingHelperModule(new LocationPickerListener() {
        @Override
        public void markerInSelectedRegion(LatLng latLng) {
            selectedLat = latLng.latitude;
            selectedLong = latLng.longitude;
            mLocationTextView.setVisibility(View.VISIBLE);
            updateLocation.setBackgroundColor(getResources().getColor(R.color.acceptBtColor));
            isCurrentLocationInsideCircle = true;
        }

        @Override
        public void markerNotInSelectedRegion() {
            updateLocation.setBackgroundColor(getResources().getColor(R.color.rejectBtColor));
            mLocationTextView.setVisibility(View.GONE);
            isCurrentLocationInsideCircle = false;
        }

        @Override
        public void getAddressByCoordinates(String completeAddress) {
            mLocationTextView.setVisibility(View.VISIBLE);
            mLocationTextView.setText(MapUtils.getCompleteAddress(selectedLat, selectedLong, activity));
        }
    }, googleMap, activity, MAP_CIRCULAR_RADIUS, lastKnownLocation);

    @Override
    public void onDrag(MotionEvent motionEvent) {
        geoFencingHelperModule.dragListener(motionEvent);
    }

    LocationCallback mapLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                lastKnownLocation = location;
                geoFencingHelperModule.lastKnownLocation = lastKnownLocation;
                geoFencingHelperModule.googleMap = googleMap;
                geoFencingHelperModule.activity = activity;
                googleMap.clear();
                MapUtils.moveCameraToLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), googleMap);
                MapUtils.showAreaBoundaryCircle(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), googleMap, 21);
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
            }
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        geoFencingHelperModule.onWindowFocusChanged(activity, mMarkerParentView, mMarkerImageView);
    }

    @Override
    public void onClick(View v) {

    }

//
//    private void onPermissionDenied(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(getResources().getString(R.string.access_permission));
//        builder.setMessage(getResources().getString(R.string.permission_description));
//        builder.setPositiveButton(getResources().getString(R.string.lets_do),
//                (dialogInterface, i) -> {
//                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                            Uri.fromParts("package", getActivity().getPackageName(), null));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                    getActivity().finish();
//                });
//        builder.setOnCancelListener(dialog -> {
//            getActivity().finish();
//        });
//        builder.setCancelable(false);
//        builder.show();
//    }
}
