package com.yunus.entity;

import com.yunus.enums.CustomerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {


    private String firstName;

    private String lastName;

    @Column(unique = true, length = 100)
    @Email
    private String email;

    @Column(unique = true, length = 11)
    private String phone;

    private boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status;

    private Set<Tag> tags = new HashSet<>();

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }


}
