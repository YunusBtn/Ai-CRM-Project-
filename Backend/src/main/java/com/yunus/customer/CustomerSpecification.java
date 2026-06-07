package com.yunus.customer;

import com.yunus.enums.CustomerStatus;
import com.yunus.tag.Tag;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CustomerSpecification {


    private CustomerSpecification() {
    }


    public static Specification<Customer> conjunction() {
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
            Join<Customer, Tag> tagJoin = root.join("tags");
            return cb.and(
                    cb.equal(tagJoin.get("id"), tagId),
                    cb.isFalse(tagJoin.get("isDeleted"))
            );

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
                    cb.like(safeLower(cb, root.get("firstName")), keyword),
                    cb.like(safeLower(cb, root.get("lastName")), keyword),
                    cb.like(safeLower(cb, root.get("email")), keyword),
                    cb.like(safeLower(cb, root.get("phone")), keyword));
        };
    }

    private static Expression<String> safeLower(
            CriteriaBuilder cb,
            Path<String> path
    ) {
        return cb.lower(cb.coalesce(path, ""));
    }
}
