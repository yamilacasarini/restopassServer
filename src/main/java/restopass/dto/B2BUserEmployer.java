package restopass.dto;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "b2b_users")
public class B2BUserEmployer {

    private String companyId;
    @Indexed(unique = true)
    private String companyName;
    private List<Float> percentageDiscountPerMembership;
    private List<String> employeesEmails;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
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

    public List<String> getEmployeesEmails() {
        return employeesEmails;
    }

    public void setEmployeesEmails(List<String> employeesEmails) {
        this.employeesEmails = employeesEmails;
    }
}
