package vinegaraflow.dto;

import vinegaraflow.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record ProjectMemberRequest(
        @NotNull Long userId,
        @NotNull Long projectId,
        @NotNull ProjectRole role
) {}