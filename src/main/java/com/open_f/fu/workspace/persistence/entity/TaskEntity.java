package com.open_f.fu.workspace.persistence.entity;

import com.open_f.fu.shared.persistence.BaseEntity;
import com.open_f.fu.workspace.domain.model.TaskComment;
import com.open_f.fu.workspace.domain.model.TaskPriority;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Solo la tabla "project_task". Las reglas viven en workspaces.domain.model.Task.
 * El autor se guarda por taskId: workspaces no depende de las entidades de accounts.
 */
@Entity
@Table(name = "project_task")
public class TaskEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @Column(name = "position_y")
    private Double positionY;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "column_id", nullable = false)
    private ProjectColumnEntity column;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "author_id", nullable = false)
    private Long authorId;


    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    private List<TaskComment> taskComment = new ArrayList<>();

    protected TaskEntity() {
    }

    public TaskEntity(String code, String title, String description, TaskPriority priority, Double positionY,
                      ProjectEntity project, ProjectColumnEntity column, Long authorId) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.positionY = positionY;
        this.project = project;
        this.column = column;
        this.authorId = authorId;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskPriority getPriority() { return priority; }
    public Double getPositionY() { return positionY; }
    public ProjectColumnEntity getColumn() { return column; }
    public ProjectEntity getProject() { return project; }
    public Long getAuthorId() { return authorId; }
    public List<TaskComment> getTaskComment() {
        return taskComment;
    }



    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }
    public void setPositionY(Double positionY) { this.positionY = positionY; }
    public void setTaskComment(List<TaskComment> taskComment) {
        this.taskComment = taskComment;
    }
}
