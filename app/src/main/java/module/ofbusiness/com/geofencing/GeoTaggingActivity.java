package module.ofbusiness.com.geofencing;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import app.ofbusiness.com.geofencing.utils.MapUtils;


public class GeoTaggingActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {


    private static final int CAMERA_REQUEST_CODE = 123;
    private static final String TAG = "UPLOAD_GEOTAG_IMAGE_ACTIVITY";
    public static final String ARG_FILE_PATH = "filePath";
    public static final String ARG_FILE_NAME = "fileName";
    public static final String ARG_IMG_INFO_DTO = "imgInfo";

    public static final String DOCUMENT_GROUP = "COMPANY_PHOTOGRAPHS";
    private static final String ARG_APPLICATION_ID = "ARG_APPLICATION_ID";

    private static String DOCUMENT_TYPE_ID = "6523817688429500847";
    private static String DOCUMENT_TYPE = "Company Asset Images";

    public static final String ARG_SELECTED_LAT = "correctLat";
    public static final String ARG_SELECTED_LONG = "correctLong";
    private static final long UPDATE_LOCATION_INTERVAL = 120 * 1000;
    private static final long UPDATE_LOCATION_FAST_INTERVAL = 120 * 1000;

    View contentContainer;
    ImageView uploadImage;
    ViewPager imageViewPager;
    TextView empityVpImageView;
    ProgressBar progressBar;
    Button uploadDocumentBt;
    //@BindView(R.id.circle_page_indicator)
    //CirclePageIndicator circlePageIndicator;

    private String imagePath = "";
    private GoogleMap googleMap;
    private SupportMapFragment mapFrag;
    private LocationRequest locationRequest;
    private Marker mapMarker;
    private FusedLocationProviderClient fusedLocationClient;

    private double selectedLat;
    private double selectedLong;
    private String applicationId;
    private Location lastKnownLocation;
    //private List<GeotaggedImageDocumentDto> geotaggedImageDocumentDtos = new ArrayList<>();
    private ClickedImageVpAdapter clickedImageVpAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_image_activity);

        contentContainer = (View) findViewById(R.id.cl_root_container);
        uploadImage = (ImageView) findViewById(R.id.click_image_iv);
        imageViewPager = findViewById(R.id.clicked_image_viewpager);
        empityVpImageView = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.btn_loading);
        uploadDocumentBt = findViewById(R.id.upload_documents_bt);
       // circlePageIndicator = findViewById(R.id.circle_page_indicator);

        clickedImageVpAdapter = new ClickedImageVpAdapter(getSupportFragmentManager());
        imageViewPager.setAdapter(clickedImageVpAdapter);

//        circlePageIndicator.setViewPager(imageViewPager);
//        circlePageIndicator.setFillColor(ContextCompat.getColor(this, R.color.colorPrimary));
//        circlePageIndicator.setStrokeColor(ContextCompat.getColor(this, R.color.dark_grey));
//        circlePageIndicator.setSnap(true);

//        if (getIntent().getExtras() != null) {
//            if (getIntent().hasExtra(ARG_SELECTED_LONG)) {
//                selectedLong = getIntent().getDoubleExtra(ARG_SELECTED_LONG, 0);
//                selectedLat = getIntent().getDoubleExtra(ARG_SELECTED_LAT, 0);
//                applicationId = getIntent().getStringExtra(ARG_APPLICATION_ID);
//            }
//        }

        uploadImage.setOnClickListener(this);

        imageViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateMapView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

//        uploadDocumentBt.setOnClickListener(v -> {
//            if (geotaggedImageDocumentDtos.size() > 0) {
//
//                progressBar.setVisibility(View.VISIBLE);
//                Observable<ResponseWrapper<List<MinCreateDocumentDto>>> observable = Repository.getInstance()
//                        .uploadImageToS3(applicationId, geotaggedImageDocumentDtos)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread());
//
//                observables.add(observable);
//
//                observable.subscribe(response -> {
//                    progressBar.setVisibility(View.GONE);
//                    imageUploadedSuccessfully();
//                }, throwable -> {
//                    progressBar.setVisibility(View.GONE);
//                    displayInfo("Could not upload Document - " + throwable.getLocalizedMessage());
//                    AppLog.i(TAG, "Could not upload Document - ", throwable);
//                });
//            } else {
//                Utils.showCustomToast(UploadGeotaggedImageActivity.this, "Please click image to upload", Toast.LENGTH_SHORT, false);
//            }
//        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mapLocationCallback);
        }
    }


//    @Override
//    protected void bindViews() {
//        setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
//        toolbar.setNavigationOnClickListener(v -> onBackPressed());
//        toolbar.setTitle("Upload Image");
//    }

//    private void imageUploadedSuccessfully() {
//        android.app.AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(this);
//        alertBuilder.setCancelable(true);
//        alertBuilder.setTitle("Success !");
//        alertBuilder.setMessage("Image has been uploaded successfully.");
//        //android.app.AlertDialog alert = alertBuilder.create();
//        alertBuilder.setPositiveButton("OK", (dialog, which) -> {
//            Intent returnIntent = new Intent();
//            setResult(Activity.RESULT_OK, returnIntent);
//            finish();
//        });
//        alertBuilder.setOnCancelListener(dialog -> {
//            Intent returnIntent = new Intent();
//            setResult(Activity.RESULT_OK, returnIntent);
//            finish();
//        });
//        alertBuilder.show();
//    }


    private void addTab(ViewPager viewPager, String filePath, String fileName, GeoTaggedImageMeta geoTaggedImageMeta) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_FILE_PATH, filePath);
        bundle.putString(ARG_FILE_NAME, fileName);
        bundle.putSerializable(ARG_IMG_INFO_DTO, (Serializable) geoTaggedImageMeta);
        ClickedImageFragment clickedImageFragment = new ClickedImageFragment();
        clickedImageFragment.setArguments(bundle);
        clickedImageVpAdapter.addFrag(clickedImageFragment, fileName);
        viewPager.setAdapter(clickedImageVpAdapter);
        viewPager.setCurrentItem(0);
//        if(geotaggedImageDocumentDtos.size() == 1){
//            updateMapView(0);
//        }
    }

    private void updateMapView(int position) {
        ClickedImageFragment clickedImageFragment = (ClickedImageFragment) clickedImageVpAdapter.getItem(position);
        GeoTaggedImageMeta geoTaggedImageMeta = clickedImageFragment.getGeoTaggedImageMeta();
        googleMap.clear();
        initMap(googleMap);
        if (geoTaggedImageMeta.getGeoTaggedLat() != 0.0D && geoTaggedImageMeta.getGeoTaggedLong() != 0.0D) {
            MapUtils.addMarker(geoTaggedImageMeta.getGeoTaggedLat(), geoTaggedImageMeta.getGeoTaggedLong(), "Image Location", mapMarker, googleMap, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            MapUtils.moveCameraToLocation(selectedLat, selectedLong, googleMap);
            //Utils.showCustomToast(this, "Location not available for this image", Toast.LENGTH_SHORT, false);
        }
    }

//    @Override
//    protected View getRootView() {
//        return appBarLayout;
//    }

//    public static Intent newIntent(Context context, double lat, double longt, String applicationId) {
//        Intent intent = new Intent(context, UploadGeotaggedImageActivity.class);
//        intent.putExtra(ARG_SELECTED_LAT, lat);
//        intent.putExtra(ARG_SELECTED_LONG, longt);
//        intent.putExtra(ARG_APPLICATION_ID, applicationId);
//        return intent;
//    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        initMap(googleMap);
        MapUtils.addMarker(selectedLat, selectedLong, "Selected Location", mapMarker, googleMap, BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        MapUtils.moveCameraToLocation(selectedLat, selectedLong, googleMap);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, mapLocationCallback, Looper.myLooper());
        googleMap.setMyLocationEnabled(true);
    }

    LocationCallback mapLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);
                lastKnownLocation = location;
            }
        }
    };

    private void initMap(GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_LOCATION_INTERVAL);
        locationRequest.setFastestInterval(UPDATE_LOCATION_FAST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.click_image_iv:
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String path = null;
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    Uri tempUri = getImageUri(getApplicationContext(), photo);
                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    File finalFile = new File(getRealPathFromURI(tempUri));
//                    if(finalFile != null){
//                        path = finalFile.toString();
                    //}
                    if (finalFile != null) {
                        double geotagLat;
                        double geotagLong;
                        boolean isGeotagged;

                        LatLng geoTaggedLatLong = MapUtils.getGeoTaggedCoordinatesFromImage(finalFile);
                        geotagLat = geoTaggedLatLong.latitude;
                        geotagLong = geoTaggedLatLong.longitude;
                        if ((geotagLat == 0.0) || (geotagLong == 0.0)) {
                            //If not geoTagged - find current coordinates
                            if (lastKnownLocation != null) {
                                geotagLat = lastKnownLocation.getLatitude();
                                geotagLong = lastKnownLocation.getLongitude();
                                isGeotagged = true;
                            } else {
                                // If no current coordinates return boolean
                                isGeotagged = false;
                                geotagLat = 0.0D;
                                geotagLong = 0.0D;
                            }
                        } else {
                            isGeotagged = true;
                        }

                        imageViewPager.setVisibility(View.VISIBLE);
                        empityVpImageView.setVisibility(View.GONE);

                        GeoTaggedImageMeta geoTaggedImageMeta = new GeoTaggedImageMeta(
                                getDisplacement(geotagLat, geotagLong),
                                getAddress(geotagLat, geotagLong, this),
                                selectedLat,
                                selectedLong,
                                geotagLat,
                                geotagLong,
                                isGeotagged
                        );
                        addTab(imageViewPager, finalFile.toString(), finalFile.getName(), geoTaggedImageMeta);
//                        startUpload(path, DOCUMENT_GROUP, 1, filePath, geoTaggedImageMeta);
                    }
                }
                break;
        }


    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private BigDecimal getDisplacement(double geotagLat, double geotagLong) {
        if (geotagLat != 0.0D && geotagLong != 0.0D) {
            return BigDecimal.valueOf(MapUtils.getDisplacementBetweenCoordinates(selectedLat, selectedLong, geotagLat, geotagLong));
        } else {
            return null;
        }
    }

    private String getAddress(double geotagLat, double geotagLong, Context context) {
        if (geotagLat != 0.0D && geotagLong != 0.0D) {
            return MapUtils.getCompleteAddress(geotagLat, geotagLong, context);
        } else {
            return null;
        }
    }


}

