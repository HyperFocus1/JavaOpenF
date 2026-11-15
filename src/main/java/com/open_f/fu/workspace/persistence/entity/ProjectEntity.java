package com.open_f.fu.workspace.persistence.entity;

import java.util.ArrayList;
import java.util.List;

import com.open_f.fu.account.persistence.entity.UserEntity;
import com.open_f.fu.shared.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Solo la tabla "projects". Las reglas viven en workspaces.domain.model.Project.
 * La propiedad user representa al usuario propietario mediante la columna owner_id.
 */
@Entity
@Table(name = "projects")
public class ProjectEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 15)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<ProjectColumnEntity> columns = new ArrayList<>();

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<TaskEntity> tasks = new ArrayList<>();

    protected ProjectEntity() {
    }

    public ProjectEntity(UserEntity user, String title, String description) {
        this.user = user;
        this.title = title;
        this.description = description;
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<ProjectColumnEntity> getColumns() { return columns; }
    public List<TaskEntity> getTasks() { return tasks; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
}
