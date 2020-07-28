package restopass.dto;

import java.util.List;

public class B2BUserEmployee {

    private String companyName;
    private List<Float> percentageDiscountPerMembership;

    public B2BUserEmployee(List<Float> percentageDiscountPerMembership, String companyName) {
        this.percentageDiscountPerMembership = percentageDiscountPerMembership;
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public List<Float> getPercentageDiscountPerMembership() {
        return percentageDiscountPerMembership;
    }

    public void setPercentageDiscountPerMembership(List<Float> percentageDiscountPerMembership) {
        this.percentageDiscountPerMembership = percentageDiscountPerMembership;
    }
}
