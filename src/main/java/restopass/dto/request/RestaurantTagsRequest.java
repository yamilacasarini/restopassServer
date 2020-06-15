package restopass.dto.request;

import restopass.dto.MembershipType;

import java.util.List;

public class RestaurantTagsRequest {

    private Double lat;
    private Double lng;
    private String freeText;
    private List<String> tags;
    private MembershipType topMembership;

    public String getFreeText() {
        return freeText;
    }

    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public MembershipType getTopMembership() {
        return topMembership;
    }

    public void setTopMembership(MembershipType topMembership) {
        this.topMembership = topMembership;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
