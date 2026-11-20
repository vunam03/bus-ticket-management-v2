package com.busbooking.module_2.model;

import com.busbooking.common.enums.RoleName;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private RoleName name;

    @Column(length = 120)
    private String description;

    public Role() {
    }

    public Role(RoleName name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    public boolean isAdminRole() {
        return name == RoleName.ROLE_ADMIN;
    }

    @Transient
    public String getBadgeCssClass() {
        return isAdminRole() ? "bg-warning text-dark" : "bg-primary-subtle text-primary";
    }
}
