package com.open_f.fu.workspace.persistence.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

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
 * Solo la tabla "project_column". Las reglas viven en workspaces.domain.model.ProjectColumn.
 */
@Entity
@Table(name = "project_column")
public class ProjectColumnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 10, nullable = false)
    @ColumnDefault("'#64748b'")
    private String color = "#64748b";

    @Column(nullable = false)
    @ColumnDefault("0")
    private int positionY = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @OneToMany(mappedBy = "column", fetch = FetchType.LAZY)
    private List<TaskEntity> tasks = new ArrayList<>();

    protected ProjectColumnEntity() {
    }

    public ProjectColumnEntity(String name, String color, int positionY, ProjectEntity project) {
        this.name = name;
        this.color = color;
        this.positionY = positionY;
        this.project = project;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public int getPositionY() { return positionY; }
    public ProjectEntity getProject() { return project; }
    public List<TaskEntity> getTasks() { return tasks; }

    public void setName(String name) { this.name = name; }
    public void setColor(String color) { this.color = color; }
    public void setPositionY(int positionY) { this.positionY = positionY; }
}
