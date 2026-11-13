package com.open_f.fu.shared.persistence;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

// @MappedSuperclass: JPA hereda estas columnas en cada entidad hija.
@MappedSuperclass
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    protected Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
