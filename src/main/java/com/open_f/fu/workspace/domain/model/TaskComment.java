package com.open_f.fu.workspace.domain.model;

import com.open_f.fu.shared.domain.DomainException;

public class TaskComment {


    public static  final int MAX_CONTENT = 4000;

    private final Long id;
    private final Long taskId;
    private final Long authorId;
    private String content;


    private TaskComment(Long id, Long taskId, Long authorId, String content){
        this.id = id;
        this.taskId  = taskId;
        this.authorId  = authorId;
        this.content  = content;

        if(taskId == null){
            throw  new DomainException("El comentario necesita una tarjeta");
        }
        if(authorId == null){
            throw  new DomainException("El comentario necesita un autor");
        }
    }

    public static TaskComment create(Long taskId, Long authorId, String content){
        return  new TaskComment(null, taskId, authorId, content);
    }
    public static TaskComment restore(Long id, Long taskId, Long authorId, String content){
        if(id == null){
            throw  new IllegalArgumentException("El id es obligatorio al restaurar");
        }
        return  new TaskComment(id, taskId, authorId, content);
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
        if(!authorId.equals(actorId)){
            throw  new DomainException("Solo el autor puede modificar el comentario");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getContent() {
        return content;
    }
}
