package com.open_f.fu.workspace.api.response;

import com.open_f.fu.workspace.domain.model.Project;

public record ProjectResponse(Long id, String title, String description) {

    public static ProjectResponse from(Project p) {
        return new ProjectResponse(p.getId(), p.getTitle(), p.getDescription());
    }
}
