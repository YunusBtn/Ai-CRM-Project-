package com.yunus.tag;

import com.yunus.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "tags")
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_deleted = false")
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(nullable = false)
    private boolean isDeleted=false;


}
