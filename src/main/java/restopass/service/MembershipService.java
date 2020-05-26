package restopass.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import restopass.dto.Membership;
import restopass.dto.response.MembershipResponse;
import restopass.dto.response.MembershipsResponse;
import restopass.dto.User;
import restopass.mongo.MembershipRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MongoTemplate mongoTemplate;
    private final RestaurantService restaurantService;

    private UserService userService;

    @Autowired
    public MembershipService(MembershipRepository membershipRepository, MongoTemplate mongoTemplate,
                             UserService userService, RestaurantService restaurantService) {
        this.membershipRepository = membershipRepository;
        this.mongoTemplate = mongoTemplate;
        this.userService = userService;
        this.restaurantService = restaurantService;
    }

    public void createMembership(Membership membership) {
        this.membershipRepository.save(membership);
    }

    public MembershipsResponse getMemberships(String userId) {
        MembershipsResponse membershipsResponse = new MembershipsResponse();

        User user = this.userService.findById(userId);
        List<Membership> memberships = this.membershipRepository.findAll();

        if(user != null && user.getActualMembership() != null) {
            Membership actualMembership = memberships.stream().filter(m -> m.getMembershipId().equals(user.getActualMembership())).findAny().get();
            MembershipResponse mr = new MembershipResponse();
            mr.setMembershipInfo(actualMembership);
            mr.setRestaurants(this.restaurantService.getRestaurantInAMemberships(actualMembership.getMembershipId()));
            membershipsResponse.setActualMembership(mr);
            memberships.remove(actualMembership);
        }

        membershipsResponse.setMemberships(memberships.stream().map(m -> {
            MembershipResponse mr = new MembershipResponse();
            mr.setMembershipInfo(m);
            mr.setRestaurants(this.restaurantService.getRestaurantInAMemberships(m.getMembershipId()));
            return mr;
        }).collect(Collectors.toList()));

        return membershipsResponse;
    }
}
