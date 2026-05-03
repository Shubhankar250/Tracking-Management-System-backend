package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "parent_child_map")
@Getter
@Setter
public class ParentChildMap {
    @Id
    private Long id;
    private Long parentId;
    private Long childPassengerId;
    private Long loginPassengerId;
    private Boolean active = true;
}
