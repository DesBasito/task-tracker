package kg.manurov.tasktracker.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    Long id;
    @NotBlank(message = "Название задачи не может быть пустым")
    @Size(max = 255, message = "Название задачи не может превышать 255 символов")
    @NotNull
    @Schema(description = "Тема задачи")
    String title;
    @Schema(description = "Описание задачи")
    @NotBlank(message = "Описание задачи не может быть пустым")
    @Size(max = 1000, message = "Описание не может превышать 1000 символов")
    @NotNull
    String description;
    @Schema(description = "Статус задачи")
    String status;
    @Schema(description = "Дата создания задачи")
    LocalDateTime createdAt;
    @Schema(description = "Дата обновления задачи")
    LocalDateTime updatedAt;
}
