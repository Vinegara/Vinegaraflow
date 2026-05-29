package vinegaraflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull Long recipientId,
        @NotBlank String message,
        String type
) {}