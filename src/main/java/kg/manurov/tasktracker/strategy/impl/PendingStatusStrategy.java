package kg.manurov.tasktracker.strategy.impl;

import kg.manurov.tasktracker.domain.enums.TaskStatus;
import kg.manurov.tasktracker.domain.models.Task;
import kg.manurov.tasktracker.strategy.TaskStatusStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class PendingStatusStrategy implements TaskStatusStrategy {

    @Override
    public TaskStatus getStatus() {
        return TaskStatus.PENDING;
    }

    @Override
    public Set<TaskStatus> getAllowedTransitions() {
        return Set.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED);
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

        log.info("Переход задачи {} из статуса PENDING в {}", task.getId(), newStatus);

        onExit(task);

        task.setStatus(newStatus.name());

        switch (newStatus) {
            case IN_PROGRESS -> {
                log.info("Задача {} начата в работу", task.getId());
            }
            case CANCELLED -> {
                log.info("Задача {} отменена из очереди", task.getId());
            }
        }
    }

    @Override
    public String getStatusDescription() {
        return "В ожидании";
    }

    @Override
    public String getTransitionDescription() {
        return "Из статуса 'В ожидании' можно перейти в: 'В процессе', 'Отменена'";
    }

    @Override
    public void onEnter(Task task) {
        log.debug("Задача {} помещена в очередь ожидания", task.getId());
    }

    @Override
    public void onExit(Task task) {
        log.debug("Задача {} покидает очередь ожидания", task.getId());
    }
}