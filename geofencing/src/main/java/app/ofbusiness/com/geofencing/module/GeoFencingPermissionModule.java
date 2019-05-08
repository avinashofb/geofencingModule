package app.ofbusiness.com.geofencing.module;

import android.Manifest;
import android.app.Activity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import app.ofbusiness.com.geofencing.listener.MapPermissionListener;

public class GeoFencingPermissionModule {

    private MapPermissionListener mapPermissionListener;
    private Activity activity;

    public GeoFencingPermissionModule() {

    }

    public GeoFencingPermissionModule(MapPermissionListener mapPermissionListener, Activity activity) {
        this.mapPermissionListener = mapPermissionListener;
        this.activity = activity;
    }

    public void checkAllPermissions(Activity activity) {
        Dexter.withActivity(activity)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                            mapPermissionListener.onPermissionsChecked(report);
                        }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                        mapPermissionListener.onPermissionRationaleShouldBeShownNote(permissions, token);
                    }
                }).check();

    }

}
