package restopass.dto;

public class Dish {

    private String dishId;
    private String name;
    private String description;
    private String img;
    private Integer baseMembership;
    private String baseMembershipName;
    private Float stars = 0f;
    private Integer countStars = 0;


    public Dish(){

    }

    public Dish(String dishId, String name, String img, String description, MembershipType baseMembership) {
        this.dishId = dishId;
        this.name = name;
        this.img = img;
        this.description = description;
        this.baseMembership = baseMembership.ordinal();
        this.baseMembershipName = baseMembership.getName();
    }

    public Integer getCountStars() {
        return countStars;
    }

    public void setCountStars(Integer countStars) {
        this.countStars = countStars;
    }

    public String getDishId() {
        return dishId;
    }

    public void setDishId(String dishId) {
        this.dishId = dishId;
    }

    public Float getStars() {
        return stars;
    }

    public void setStars(Float stars) {
        this.stars = stars;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getBaseMembership() {
        return baseMembership;
    }

    public void setBaseMembership(Integer baseMembership) {
        this.baseMembership = baseMembership;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getBaseMembershipName() {
        return baseMembershipName;
    }

    public void setBaseMembershipName(String baseMembershipName) {
        this.baseMembershipName = baseMembershipName;
    }

    public void setAverageStars() {
        Integer countStars = getCountStars();
        if (countStars != 0) {
            this.setStars(getStars() / countStars);
        }
    }
}
