package app.ofbusiness.com.geofencing.listener;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;

import java.util.List;

public interface MapPermissionListener {

    void onPermissionsChecked(MultiplePermissionsReport report);

    void onPermissionRationaleShouldBeShownNote(List<PermissionRequest> permissions, PermissionToken token);
}
