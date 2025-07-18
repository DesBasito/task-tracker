package kg.manurov.tasktracker.controller;

import kg.manurov.tasktracker.domain.dto.TaskDto;
import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.service.TaskService;
import kg.manurov.tasktracker.service.TaskStatusManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "API для управления задачами")
@SecurityRequirement(name = "basicAuth")
public class TaskController {

    private final TaskService taskService;
    private final TaskStatusManager statusManager;

    @Operation(summary = "Создать новую задачу",
            description = "Создает новую задачу со статусом PENDING")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Задача успешно создана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания задачи",
                    required = true)
            @Valid @RequestBody TaskDto taskDTO) {
        log.info("Получен запрос на создание задачи: {}", taskDTO.getTitle());

        TaskDto createdTask = taskService.createTask(taskDTO);

        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Получить все задачи",
            description = "Возвращает список всех задач, отсортированных по дате создания (новые сначала)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список задач успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        log.info("Получен запрос на получение всех задач");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @Operation(
            summary = "Получить задачу по ID",
            description = "Возвращает задачу с указанным идентификатором"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Задача найдена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(
            @Parameter(description = "ID задачи", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Получен запрос на получение задачи с ID: {}", id);
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(
            summary = "Обновить задачу",
            description = "Обновляет существующую задачу. Можно обновить название, описание и статус"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Задача успешно обновлена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @Parameter(description = "ID задачи", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновленные данные задачи",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class)
                    )
            )
            @Valid @RequestBody TaskDto taskDTO) {
        log.info("Получен запрос на обновление задачи с ID: {}", id);
        TaskDto updatedTask = taskService.updateTask(id, taskDTO);
        return ResponseEntity.ok(updatedTask);
    }


    @Operation(
            summary = "Изменить статус задачи",
            description = "Изменяет статус задачи с использованием системы переходов состояний"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Статус успешно изменен",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TaskDto.class)
            )
    )
    @ApiResponse(responseCode = "401", description = "Не авторизован")
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDto> changeTaskStatus(
            @Parameter(description = "ID задачи", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Статус на которую хотите изменить", required = true, example = "PENDING | IN_PROGRESS | COMPLETED | CANCELLED")
            @RequestParam String statusRequest) {
        log.info("Получен запрос на изменение статуса задачи {}", id);

        TaskStatus newStatus = TaskStatus.fromString(statusRequest)
                .orElseThrow(() -> new IllegalArgumentException("Неверный статус: " + statusRequest));

        TaskDto updatedTask = taskService.changeTaskStatus(id, newStatus);

        return ResponseEntity.ok(updatedTask);
    }

    @Operation(
            summary = "Удалить задачу",
            description = "Удаляет задачу по указанному ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID задачи", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Получен запрос на удаление задачи с ID: {}", id);

        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Получить задачи по статусу",
            description = "Возвращает список задач с указанным статусом"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список задач успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskDto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Неверный статус"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDto>> getTasksByStatus(
            @Parameter(
                    description = "Статус задачи",
                    required = true,
                    example = "PENDING",
                    schema = @Schema(allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
            )
            @PathVariable String status) {
        log.info("Получен запрос на получение задач со статусом: {}", status);

        TaskStatus taskStatus = TaskStatus.fromString(status)
                .orElseThrow(() -> new IllegalArgumentException("Неверный статус: " + status));

        List<TaskDto> tasks = taskService.getTasksByStatus(taskStatus);

        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "Получить доступные переходы статусов",
            description = "Возвращает список статусов, на которые можно перейти из текущего статуса задачи"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список доступных переходов",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            ["IN_PROGRESS", "CANCELLED"]
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/{id}/transitions")
    public ResponseEntity<List<TaskStatus>> getAvailableTransitions(
            @Parameter(description = "ID задачи", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Получен запрос на получение доступных переходов для задачи {}", id);

        List<TaskStatus> transitions = taskService.getAvailableTransitions(id);

        return ResponseEntity.ok(transitions);
    }


    @Operation(
            summary = "Получить информацию о статусах",
            description = "Возвращает информацию о всех доступных статусах и возможных переходах"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о статусах",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "statuses": ["PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"],
                                                "descriptions": {
                                                    "PENDING": "Из статуса 'В ожидании' можно перейти в: 'В процессе', 'Отменена'",
                                                    "IN_PROGRESS": "Из статуса 'В процессе' можно перейти в: 'Завершена', 'Отменена', 'В ожидании'",
                                                    "COMPLETED": "Статус 'Завершена' является финальным. Переходы невозможны.",
                                                    "CANCELLED": "Статус 'Отменена' является финальным. Переходы невозможны."
                                                }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/status-info")
    public ResponseEntity<Map<String, Object>> getStatusInfo() {
        log.info("Получен запрос на получение информации о статусах");

        Map<String, Object> statusInfo = Map.of(
                "statuses", TaskStatus.values(),
                "descriptions", Map.of(
                        "PENDING", statusManager.getTransitionDescription(TaskStatus.PENDING),
                        "IN_PROGRESS", statusManager.getTransitionDescription(TaskStatus.IN_PROGRESS),
                        "COMPLETED", statusManager.getTransitionDescription(TaskStatus.COMPLETED),
                        "CANCELLED", statusManager.getTransitionDescription(TaskStatus.CANCELLED)
                )
        );

        return ResponseEntity.ok(statusInfo);
    }
}