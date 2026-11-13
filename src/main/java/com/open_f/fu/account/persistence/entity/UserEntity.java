package com.open_f.fu.account.persistence.entity;

import com.open_f.fu.shared.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Solo la tabla "users". Las reglas viven en accounts.domain.model.User.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

    protected UserEntity() {
    }

    public UserEntity(String email, String password, String name, String lastName) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.lastName = lastName;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getLastName() { return lastName; }

    public void setName(String name) { this.name = name; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
