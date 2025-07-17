package kg.manurov.tasktracker.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kg.manurov.tasktracker.domain.dto.TaskDto;
import kg.manurov.tasktracker.service.EmailService;
import kg.manurov.tasktracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Email Reports", description = "API для отправки отчетов по email")
public class EmailController {
    private final EmailService emailService;
    private final TaskService taskService;

    @PostMapping("/send-tasks")
    public ResponseEntity<String> sendTasksToEmail(@RequestParam @Email @NotNull @NotBlank String email) {
        try {
            log.info("Отправка всех задач на email: {}", email);

            List<TaskDto> allTasks = taskService.getAllTasks();
            emailService.sendTasksReport(email, allTasks);

            log.info("Успешно отправлено {} задач на email: {}", allTasks.size(), email);
            return ResponseEntity.ok("Задачи отправлены на email: " + email);

        } catch (Exception e) {
            log.error("Ошибка при отправке задач на email {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body("Ошибка отправки: " + e.getMessage());
        }
    }
}
