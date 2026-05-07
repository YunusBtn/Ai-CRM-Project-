package com.yunus.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yunus.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity {
    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private String firstName;

    private String lastName;


    private boolean isDeleted = false;

    private boolean isActive = true;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;


}
