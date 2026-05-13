package com.yunus.customer;

import com.yunus.enums.CustomerStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CustomerSpecification {


    private CustomerSpecification() {
    }


    public static Specification<Customer> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<Customer> hasStatus(CustomerStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Customer> hasTag(UUID tagId) {
        return (root, query, cb) -> {
            if (tagId == null) {
                return cb.conjunction();
            }
            query.distinct(true);
            return cb.equal(root.join("tags").get("id"), tagId);
        };
    }

    public static Specification<Customer> containsSearch(String search) {
        return (root, query, cb) ->
        {
            if (StringUtils.isBlank(search)) {
                return cb.conjunction();
            }
            String keyword = "%" + search.toLowerCase().trim() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), keyword),
                    cb.like(cb.lower(root.get("lastName")), keyword),
                    cb.like(cb.lower(root.get("email")), keyword),
                    cb.like(cb.lower(root.get("phone")), keyword));
        };
    }


}
