package com.open_f.fu.account.persistence.entity;

import com.open_f.fu.shared.persistence.BaseEntity;
import com.open_f.fu.workspace.domain.model.TaskComment;
import com.open_f.fu.workspace.persistence.entity.TaskCommentEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

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


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<TaskCommentEntity> taskComment = new ArrayList<>();



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
    public List<TaskCommentEntity> getTaskComment() {
        return taskComment;
    }



    public void setName(String name) { this.name = name; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setTaskComment(List<TaskCommentEntity> taskComment) {
        this.taskComment = taskComment;
    }
}
