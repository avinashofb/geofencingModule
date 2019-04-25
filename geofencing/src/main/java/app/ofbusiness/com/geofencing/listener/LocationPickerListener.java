package app.ofbusiness.com.geofencing.listener;

import com.google.android.gms.maps.model.LatLng;

public interface LocationPickerListener {

    void markerInSelectedRegion(LatLng latLng);

    void markerNotInSelectedRegion();

    void getAddressByCoordinates();

}
