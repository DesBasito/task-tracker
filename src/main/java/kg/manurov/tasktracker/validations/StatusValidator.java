package kg.manurov.tasktracker.validations;

import kg.manurov.tasktracker.domain.dto.TaskDto;
import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.domain.models.Task;
import kg.manurov.tasktracker.repositories.TaskRepository;
import kg.manurov.tasktracker.service.TaskStatusManager;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StatusValidator implements ConstraintValidator<ValidStatus, TaskDto> {

    private final TaskRepository taskRepository;
    private final TaskStatusManager statusManager;



    @Override
    public boolean isValid(TaskDto task, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        Optional<TaskStatus> newStatusOpt = TaskStatus.fromString(task.getStatus());
        if (newStatusOpt.isEmpty()) {
            context.buildConstraintViolationWithTemplate(
                            TaskStatus.getAvailableStatusesDescription())
                    .addConstraintViolation();
            return false;
        }

        TaskStatus newStatus = newStatusOpt.get();

        if (task.getId() != null) {
            Optional<Task> taskOpt = taskRepository.findById(task.getId());
            if (taskOpt.isPresent()) {
                Task existingTask = taskOpt.get();
                TaskStatus currentStatus = TaskStatus.valueOf(existingTask.getStatus());

                // Проверяем, можно ли выполнить переход
                if (!statusManager.canTransition(currentStatus, newStatus)) {
                    String errorMessage = String.format(
                            "Невозможно изменить статус с '%s' на '%s'. %s",
                            currentStatus.getDescription(),
                            newStatus.getDescription(),
                            statusManager.getTransitionDescription(currentStatus)
                    );

                    context.buildConstraintViolationWithTemplate(errorMessage)
                            .addConstraintViolation();
                    isValid = false;
                }
            }
        }

        return isValid;
    }
}