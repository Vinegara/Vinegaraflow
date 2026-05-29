package vinegaraflow.dto;

import vinegaraflow.entity.Priority;
import vinegaraflow.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank String title,
        String description,
        TaskStatus status,
        Priority priority,
        LocalDate deadline,
        Long projectId,  // убрали @NotNull
        Long assigneeId
) {}