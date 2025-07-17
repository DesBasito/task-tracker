package kg.manurov.tasktracker.strategy.impl;

import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.domain.models.Task;
import kg.manurov.tasktracker.strategy.TaskStatusStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class InProgressStatusStrategy implements TaskStatusStrategy {

    @Override
    public TaskStatus getStatus() {
        return TaskStatus.IN_PROGRESS;
    }

    @Override
    public Set<TaskStatus> getAllowedTransitions() {
        return Set.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED, TaskStatus.PENDING);
    }

    @Override
    public boolean canTransitionTo(TaskStatus newStatus) {
        return getAllowedTransitions().contains(newStatus);
    }

    @Override
    public void executeTransition(Task task, TaskStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Невозможно изменить статус с %s на %s",
                            getStatusDescription(), newStatus.getDescription())
            );
        }

        log.info("Переход задачи {} из статуса IN_PROGRESS в {}", task.getId(), newStatus);

        // Логика выхода из текущего статуса
        onExit(task);

        // Изменяем статус
        task.setStatus(newStatus.name());

        // Дополнительная логика в зависимости от целевого статуса
        switch (newStatus) {
            case COMPLETED -> {
                log.info("Задача {} успешно завершена", task.getId());
                // Логика завершения: фиксация результата, уведомления об успехе
            }
            case CANCELLED -> {
                log.info("Задача {} отменена во время выполнения", task.getId());
                // Логика отмены: откат изменений, сохранение промежуточного результата
            }
            case PENDING -> {
                log.info("Задача {} возвращена в очередь", task.getId());
                // Логика возврата: сохранение прогресса, освобождение ресурсов
            }
        }
    }

    @Override
    public String getStatusDescription() {
        return "В процессе";
    }

    @Override
    public String getTransitionDescription() {
        return "Из статуса 'В процессе' можно перейти в: 'Завершена', 'Отменена', 'В ожидании'";
    }

    @Override
    public void onEnter(Task task) {
        log.debug("Задача {} взята в работу", task.getId());
        // Логика при входе: блокировка ресурсов, уведомление исполнителя
    }

    @Override
    public void onExit(Task task) {
        log.debug("Задача {} завершает активную фазу", task.getId());
        // Логика при выходе: освобождение ресурсов, сохранение прогресса
    }
}