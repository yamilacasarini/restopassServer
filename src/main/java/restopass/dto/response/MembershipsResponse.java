package restopass.dto.response;

import java.util.List;

public class MembershipsResponse {

    private MembershipResponse actualMembership;
    private List<MembershipResponse> memberships;

    public MembershipResponse getActualMembership() {
        return actualMembership;
    }

    public void setActualMembership(MembershipResponse actualMembership) {
        this.actualMembership = actualMembership;
    }

    public List<MembershipResponse> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<MembershipResponse> memberships) {
        this.memberships = memberships;
    }
}
