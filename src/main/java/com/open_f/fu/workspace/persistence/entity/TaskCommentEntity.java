package com.open_f.fu.workspace.persistence.entity;


import com.fasterxml.jackson.databind.ser.Serializers;
import com.open_f.fu.account.persistence.entity.UserEntity;
import com.open_f.fu.shared.persistence.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "task_comments")
public class TaskCommentEntity extends BaseEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private  Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private TaskEntity task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(columnDefinition = "TEXT")
    private String content;
}
