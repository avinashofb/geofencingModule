package module.ofbusiness.com.geofencing;

import java.io.Serializable;
import java.math.BigDecimal;

public class GeoTaggedImageMeta implements Serializable {

    private BigDecimal distanceFromReference;

    private String referenceAddress;

    private Double refLat;

    private Double refLong;

    private Double geoTaggedLat;

    private Double geoTaggedLong;

    private boolean geoTagged;

    public GeoTaggedImageMeta() {

    }

    public GeoTaggedImageMeta(BigDecimal distanceFromReference, String referenceAddress, Double refLat, Double refLong, Double geoTaggedLat, Double geoTaggedLong, boolean geoTagged) {
        this.distanceFromReference = distanceFromReference;
        this.referenceAddress = referenceAddress;
        this.refLat = refLat;
        this.refLong = refLong;
        this.geoTaggedLat = geoTaggedLat;
        this.geoTaggedLong = geoTaggedLong;
        this.geoTagged = geoTagged;
    }

    public BigDecimal getDistanceFromReference() {
        return distanceFromReference;
    }

    public void setDistanceFromReference(BigDecimal distanceFromReference) {
        this.distanceFromReference = distanceFromReference;
    }

    public String getReferenceAddress() {
        return referenceAddress;
    }

    public void setReferenceAddress(String referenceAddress) {
        this.referenceAddress = referenceAddress;
    }

    public Double getRefLat() {
        return refLat;
    }

    public void setRefLat(Double refLat) {
        this.refLat = refLat;
    }

    public Double getRefLong() {
        return refLong;
    }

    public void setRefLong(Double refLong) {
        this.refLong = refLong;
    }

    public Double getGeoTaggedLat() {
        return geoTaggedLat;
    }

    public void setGeoTaggedLat(Double geoTaggedLat) {
        this.geoTaggedLat = geoTaggedLat;
    }

    public Double getGeoTaggedLong() {
        return geoTaggedLong;
    }

    public void setGeoTaggedLong(Double geoTaggedLong) {
        this.geoTaggedLong = geoTaggedLong;
    }

    public boolean isGeoTagged() {
        return geoTagged;
    }

    public void setGeoTagged(boolean geoTagged) {
        this.geoTagged = geoTagged;
    }
}
