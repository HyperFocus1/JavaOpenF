package com.open_f.fu.workspace.domain.model;

import com.open_f.fu.shared.domain.DomainException;

public class TaskComment {


    public static  final int MAX_CONTENT = 4000;

    private final Long id;
    private final Long taskId;
    private final Long userId;
    private String content;


    private TaskComment(Long id, Long taskId, Long userId, String content){
        this.id = id;
        this.taskId  = taskId;
        this.userId  = userId;
        this.content  = content;

        if(taskId == null){
            throw  new DomainException("El comentario necesita una tarjeta");
        }
        if(userId == null){
            throw  new DomainException("El comentario necesita un autor");
        }
    }

    public static TaskComment create(Long taskId, Long userId, String content){
        return  new TaskComment(null, taskId, userId, content);
    }
    public static TaskComment restore(Long id, Long taskId, Long userId, String content){
        if(id == null){
            throw  new IllegalArgumentException("El id es obligatorio al restaurar");
        }
        return  new TaskComment(id, taskId, userId, content);
    }

    public  void edit(String newContent){
        if(newContent == null ||  newContent.isBlank()){
            throw  new DomainException("El comentario no puede estar vacio");
        }
        String clean =  newContent.trim();
        if(clean.length() > MAX_CONTENT){
            throw  new DomainException("El comentario no puede pasar de: " + MAX_CONTENT + " caracteres");
        }
        this.content = clean;
    }
    public  void assertAuthor(Long actorId){
        if(!userId.equals(actorId)){
            throw  new DomainException("Solo el autor puede modificar el comentario");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }
}
