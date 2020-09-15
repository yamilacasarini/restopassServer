package restopass.dto.request;

import java.util.List;

public class RestaurantTagsRequest {

    private Double lat;
    private Double lng;
    private Double radius;
    private String freeText;
    private List<String> tags;
    private Integer topMembership;

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

    public Integer getTopMembership() {
        return topMembership;
    }

    public void setTopMembership(Integer topMembership) {
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

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }
}
